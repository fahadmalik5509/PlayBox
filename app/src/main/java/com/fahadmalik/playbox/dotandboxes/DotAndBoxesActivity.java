package com.fahadmalik.playbox.dotandboxes;

import static android.view.View.VISIBLE;
import static com.fahadmalik.playbox.miscellaneous.ActivityUtils.*;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.fahadmalik.playbox.R;
import com.fahadmalik.playbox.databinding.DotandboxesLayoutBinding;
import com.fahadmalik.playbox.databinding.NavigationLayoutBinding;
import com.fahadmalik.playbox.databinding.ShadowLayoutBinding;
import com.fahadmalik.playbox.databinding.ShopButtonLayoutBinding;
import com.fahadmalik.playbox.databinding.ShopLayoutBinding;
import com.fahadmalik.playbox.miscellaneous.BaseActivity;
import com.fahadmalik.playbox.miscellaneous.GamesActivity;

import java.util.List;
import java.util.Random;

/**
 * Main activity for Dots and Boxes.
 *
 * Supports three game modes:
 *   • PvAI   – player vs. CPU (casual or tactical difficulty)
 *   • PvP    – local two-player with optional per-turn countdown timer
 *   • Online – two players over Firebase Realtime Database
 *
 * When launching in Online mode, the host/guest flow is handled by
 * {@link OnlineLobbyActivity}, which forwards room details via Intent extras.
 */
public class DotAndBoxesActivity extends BaseActivity {

    // ── Game mode enum ────────────────────────────────────────────────────────────

    private enum Mode { PVP, PVAI, ONLINE }

    // ── Fields ────────────────────────────────────────────────────────────────────

    private DotandboxesLayoutBinding vb;

    private Mode    mode          = Mode.PVAI;
    private boolean tacticalAI   = false;

    // Online multiplayer
    private OnlineGameManager onlineManager;
    private int  myPlayerNumber  = 1;   // 1 or 2 (only meaningful in ONLINE mode)
    private boolean myTurn       = true; // blocks input when it's the opponent's turn

    // Timer
    private CountDownTimer countDownTimer;
    private int timerSeconds = 15;      // configurable; 0 = no timer

    // Grid-size picker
    private TextView previousGridSizeTV = null;

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = DotandboxesLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        initViewBindings();
        handleIncomingIntent();   // must run before setupGameUI so names/sizes are right
        setupGameUI();

        // Move listener – fires when DotAndBoxesView commits a human touch move
        vb.dotAndBoxesView.setOnMoveListener(() -> {
            onMoveCommitted(vb.dotAndBoxesView.getGame());
        });

        vb.dotAndBoxesView.setOnTouchListener((v, event) -> {
            v.onTouchEvent(event);
            return true;
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopTimer();
        if (onlineManager != null) onlineManager.detachListener();
    }

    // ── Intent / mode setup ───────────────────────────────────────────────────────

    private void handleIncomingIntent() {
        boolean isOnline = getIntent().getBooleanExtra(OnlineLobbyActivity.EXTRA_MODE_ONLINE, false);
        if (!isOnline) return;

        mode           = Mode.ONLINE;
        myPlayerNumber = getIntent().getIntExtra(OnlineLobbyActivity.EXTRA_PLAYER_NUM, 1);
        myTurn         = (myPlayerNumber == 1);
        timerSeconds   = getIntent().getIntExtra(OnlineLobbyActivity.EXTRA_TIMER, 15);

        int    gs         = getIntent().getIntExtra(OnlineLobbyActivity.EXTRA_GRID_SIZE, 6);
        String roomId     = getIntent().getStringExtra(OnlineLobbyActivity.EXTRA_ROOM_ID);
        String myName     = getIntent().getStringExtra(OnlineLobbyActivity.EXTRA_PLAYER_NAME);

        vb.dotAndBoxesView.updateGridSize(gs);

        // Restore manager so we can push/receive moves
        onlineManager = new OnlineGameManager();
        onlineManager.rejoinRoom(roomId, myPlayerNumber, gs);

        onlineManager.listenForStateChanges(new OnlineGameManager.GameStateCallback() {
            @Override
            public void onStateChanged(DotAndBoxesGame game, int currentPlayer,
                                       String status, String p1Name, String p2Name) {
                runOnUiThread(() -> {
                    // Sync names
                    vb.playerOneNameTV.setText(p1Name != null ? p1Name : "Player 1");
                    vb.playerTwoNameTV.setText(p2Name != null ? p2Name : "Player 2");

                    // Adopt the authoritative Firebase state
                    vb.dotAndBoxesView.applyRemoteState(game);
                    updateScoreUI();
                    updateTurnIndicator(currentPlayer);

                    myTurn = (currentPlayer == myPlayerNumber);
                    vb.dotAndBoxesView.setInputEnabled(myTurn && !game.isGameOver());

                    if (OnlineGameManager.STATUS_FINISHED.equals(status)) {
                        vb.dotAndBoxesView.gameInProgress = false;
                        stopTimer();
                        if (game.isGameOver()) showWinnerSplash();
                        else showAbandonedDialog();
                    } else {
                        vb.dotAndBoxesView.gameInProgress = true;
                        if (timerSeconds > 0 && myTurn) resetTimer(); else stopTimer();
                    }
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> showAbandonedDialog());
            }
        });
    }

    // ── UI initialisation ─────────────────────────────────────────────────────────

    private void initViewBindings() {
        ShopButtonLayoutBinding shopBtn  = ShopButtonLayoutBinding.bind(vb.ShopButton.getRoot());
        ShopLayoutBinding        shop    = ShopLayoutBinding.bind(vb.Shop.getRoot());
        NavigationLayoutBinding  nav     = NavigationLayoutBinding.bind(vb.Navigation.getRoot());
        ShadowLayoutBinding      shadow  = ShadowLayoutBinding.bind(vb.Shadow.getRoot());
        setBindings(shopBtn, shop, nav, shadow);
    }

    private void setupGameUI() {
        if (mode != Mode.ONLINE) {
            // Restore saved player names
            vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
            vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));

            final int    defaultGrid  = 6;
            final String defaultMode  = "pvai";
            final String defaultDiff  = "casual";
            vb.dotAndBoxesView.updateGridSize(defaultGrid);
            updateGridSizeHighlight(defaultGrid);
            applyMode(defaultMode);
            applyDifficulty(defaultDiff);
        } else {
            // Online mode: hide mode/difficulty selectors; show room indicator
            hideOnlineIrrelevantUI();
        }

        toggleVisibility(true, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        updateTurnIndicator(1);
        updateMenuTimerLabel();
    }

    private void hideOnlineIrrelevantUI() {
        toggleVisibility(false,
                vb.pvaiTV, vb.pvpTV,
                vb.difficultyTV, vb.difficultyLL,
                vb.profileIV);
        // Online always has a timer if configured; show/hide accordingly
        toggleVisibility(timerSeconds > 0, vb.timerTV);
        toggleVisibility(false, vb.setTimerTV, vb.timerLL);
    }

    // ── Core move handler (shared by all modes) ────────────────────────────────────

    private void onMoveCommitted(DotAndBoxesGame game) {
        updateScoreUI();
        updateTurnIndicator(game.isPlayerOneTurn() ? 1 : 2);

        if (mode == Mode.ONLINE) {
            onlineManager.pushGameState(game);
            // Disable input until Firebase echoes back the updated state
            vb.dotAndBoxesView.setInputEnabled(false);
            return;
        }

        // Local modes: manage timer and AI
        if (mode == Mode.PVP) resetTimer();

        if (game.isGameOver()) {
            endGame();
        } else if (mode == Mode.PVAI && !game.isPlayerOneTurn()) {
            scheduleAIMove();
        }
    }

    // ── Score & turn UI ───────────────────────────────────────────────────────────

    private void updateScoreUI() {
        int[] score = vb.dotAndBoxesView.getScore();
        vb.playerOneScoreTV.setText(String.valueOf(score[0]));
        vb.playerTwoScoreTV.setText(String.valueOf(score[1]));

        if (vb.dotAndBoxesView.isGameOver()) {
            endGame();
        }
    }

    private void updateTurnIndicator(int currentPlayer) {
        boolean p1 = (currentPlayer == 1);
        changeBackgroundColor(vb.playerOneRL, p1 ? LIGHT_GREEN_COLOR : CHARCOAL_COLOR);
        changeBackgroundColor(vb.playerTwoRL, p1 ? CHARCOAL_COLOR    : LIGHT_RED_COLOR);
        animateViewScale(vb.playerOneRL, p1 ? 1f : 1.1f, p1 ? 1.1f : 1f, 200);
        animateViewScale(vb.playerTwoRL, p1 ? 1.1f : 1f, p1 ? 1f : 1.1f, 200);
    }

    // ── Game-end logic ────────────────────────────────────────────────────────────

    private void endGame() {
        vb.dotAndBoxesView.gameInProgress = false;
        stopTimer();
        showWinnerSplash();
    }

    private void showWinnerSplash() {
        int[] score = vb.dotAndBoxesView.getScore();
        if (score[0] != score[1]) {
            CharSequence winner = score[0] > score[1]
                    ? vb.playerOneNameTV.getText()
                    : vb.playerTwoNameTV.getText();
            vb.winnerNameTV.setText(winner);
            animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
            animateViewScale(vb.winnerWonTV,  0f, 1f, 500);
            toggleVisibility(true, vb.winnerNameTV, vb.winnerWonTV);
        } else {
            vb.winnerNameTV.setText(getString(R.string.draw));
            animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
            toggleVisibility(true, vb.winnerNameTV);
        }
        playSoundAndVibrate(R.raw.sound_victory, true, 200);
        vb.celebrationLAV.setVisibility(VISIBLE);
        vb.celebrationLAV.playAnimation();
    }

    private void showAbandonedDialog() {
        // Reuse winnerNameTV to show a "Opponent left" message
        vb.winnerNameTV.setText(getString(R.string.opponent_left));
        animateViewScale(vb.winnerNameTV, 0f, 1f, 500);
        toggleVisibility(true, vb.winnerNameTV);
    }

    // ── Grid size picker ──────────────────────────────────────────────────────────

    private void updateGridSizeHighlight(int boxesCount) {
        int dotCount = boxesCount + 1;
        if (dotCount < 5 || dotCount > 10) return;

        TextView[] tvs = {
                vb.gridSize5TV, vb.gridSize6TV, vb.gridSize7TV,
                vb.gridSize8TV, vb.gridSize9TV, vb.gridSize10TV
        };
        int idx = dotCount - 5;
        for (int i = 0; i < tvs.length; i++) {
            tvs[i].setSelected(i == idx);
            if (i == idx && previousGridSizeTV != tvs[i]) {
                animateViewScale(tvs[i], 1.0f, 1.1f, 200);
                if (previousGridSizeTV != null) {
                    animateViewScale(previousGridSizeTV, 1.1f, 1.0f, 0);
                    previousGridSizeTV.setSelected(false);
                }
                previousGridSizeTV = tvs[i];
            }
        }
    }

    // ── Event handlers (wired from XML via android:onClick) ─────────────────────

    public void handleDotAndBoxesMenuClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        boolean open = "open".equals(view.getTag());
        toggleVisibility(open, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        if (open) animateViewScale(vb.DotAndBoxesMenuLayout, 0f, 1.0f, 200);
    }

    public void handleGridSizeButtons(View view) {
        if (mode == Mode.ONLINE) return; // grid locked in online mode
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        int dots  = Integer.parseInt(view.getTag().toString());
        int boxes = dots - 1;
        vb.dotAndBoxesView.updateGridSize(boxes);
        resetGame();
        updateGridSizeHighlight(boxes);
    }

    public void handleGameModeButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        applyMode(view.getTag().toString());
    }

    private void applyMode(String modeTag) {
        switch (modeTag) {
            case "pvai":
                mode = Mode.PVAI;
                vb.pvaiTV.setSelected(true);
                vb.pvpTV.setSelected(false);
                vb.onlineTV.setSelected(false);
                animateViewScale(vb.pvaiTV, 1f, 1.05f, 200);
                animateViewScale(vb.pvpTV,  1.05f, 1f, 0);
                toggleVisibility(true,  vb.difficultyTV, vb.difficultyLL);
                toggleVisibility(false, vb.profileIV, vb.timerTV, vb.setTimerTV, vb.timerLL);
                vb.playerOneNameTV.setText(R.string.you);
                vb.playerTwoNameTV.setText(R.string.ai);
                stopTimer();
                break;

            case "pvp":
                mode = Mode.PVP;
                vb.pvaiTV.setSelected(false);
                vb.pvpTV.setSelected(true);
                vb.onlineTV.setSelected(false);
                animateViewScale(vb.pvaiTV, 1.05f, 1f, 0);
                animateViewScale(vb.pvpTV,  1f, 1.05f, 200);
                toggleVisibility(false, vb.difficultyTV, vb.difficultyLL);
                toggleVisibility(true,  vb.profileIV, vb.timerTV, vb.setTimerTV, vb.timerLL);
                vb.playerOneNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
                vb.playerTwoNameTV.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
                startTimer();
                break;
        }
        resetGame();
    }

    public void handleDifficultyButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        applyDifficulty(view.getTag().toString());
    }

    public void handleOnlineClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        toggleVisibility(false, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        changeActivity(this, OnlineLobbyActivity.class);
    }

    private void applyDifficulty(String tag) {
        tacticalAI = "tactical".equals(tag);
        vb.casualTV.setSelected(!tacticalAI);
        vb.tacticalTV.setSelected(tacticalAI);
        animateViewScale(tacticalAI ? vb.tacticalTV : vb.casualTV,  1f,    1.05f, 200);
        animateViewScale(tacticalAI ? vb.casualTV  : vb.tacticalTV, 1.05f, 1f,    0);
    }

    public void handleResetClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if (mode == Mode.ONLINE) return; // no reset in online games

        if (vb.resetRL.getVisibility() == VISIBLE) {
            if ("yes".equals(view.getTag())) resetGame();
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
            return;
        }
        if (vb.dotAndBoxesView.isGameOver()) {
            resetGame();
        } else if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.resetRL, vb.Shadow.ShadowLayout);
        }
    }

    private void resetGame() {
        vb.dotAndBoxesView.gameInProgress = false;
        vb.dotAndBoxesView.animateGameReset();
        vb.dotAndBoxesView.restartGame();
        vb.dotAndBoxesView.setInputEnabled(true);
        updateScoreUI();
        updateTurnIndicator(1);
        toggleVisibility(false, vb.celebrationLAV, vb.winnerWonTV, vb.winnerNameTV, vb.Shadow.ShadowLayout);
        vb.celebrationLAV.cancelAnimation();
        stopTimer();
        updateMenuTimerLabel();
        if (mode == Mode.PVP) startTimer();
    }

    public void handleExitButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if ("no".equals(view.getTag())) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            if (mode == Mode.ONLINE && onlineManager != null) {
                onlineManager.leaveRoom(myPlayerNumber);
                onlineManager.detachListener();
            }
            changeActivity(this, GamesActivity.class);
        }
    }

    // ── AI moves ──────────────────────────────────────────────────────────────────

    private void scheduleAIMove() {
        vb.dotAndBoxesView.setInputEnabled(false);
        new Handler().postDelayed(() -> {
            DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
            if (game.isPlayerOneTurn() || game.isGameOver()) {
                vb.dotAndBoxesView.setInputEnabled(true);
                return;
            }
            DotAndBoxesAI.Move move = DotAndBoxesAI.chooseMove(game, tacticalAI);
            if (move == null) { vb.dotAndBoxesView.setInputEnabled(true); return; }

            int before = game.getClaimedBoxesCount();
            boolean ok = game.markLine(move.isHorizontal, move.row, move.col);
            if (!ok)   { vb.dotAndBoxesView.setInputEnabled(true); return; }

            vb.dotAndBoxesView.invalidate();
            updateScoreUI();
            updateTurnIndicator(game.isPlayerOneTurn() ? 1 : 2);

            if (game.getClaimedBoxesCount() > before) {
                vb.dotAndBoxesView.animateCompletedBoxes();
                playSoundAndVibrate(R.raw.sound_box_complete, true, 200);
                if (!game.isGameOver()) scheduleAIMove(); // chain-complete
            } else {
                playSoundAndVibrate(R.raw.sound_line_placed, true, 50);
                vb.dotAndBoxesView.setInputEnabled(true);
            }
        }, 250);
    }

    // ── Timer ─────────────────────────────────────────────────────────────────────

    public void handleTimeButtons(View view) {
        if (mode == Mode.ONLINE) return; // timer is set in lobby for online games
        String tag = view.getTag().toString();
        if ("+".equals(tag)) {
            if (timerSeconds >= 60) { playSoundAndVibrate(R.raw.sound_error, true, 50); return; }
            timerSeconds += 5;
        } else {
            if (timerSeconds <= 0)  { playSoundAndVibrate(R.raw.sound_error, true, 50); return; }
            timerSeconds -= 5;
        }
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        updateMenuTimerLabel();
        toggleVisibility(timerSeconds > 0, vb.timerTV);
        resetGame();
    }

    private void updateMenuTimerLabel() {
        vb.menuTimerTV.setText(timerSeconds == 0 ? "OFF" : String.valueOf(timerSeconds));
        vb.timerTV.setText(String.valueOf(timerSeconds));
    }

    private void startTimer() {
        if (timerSeconds == 0 || mode == Mode.PVAI || !vb.dotAndBoxesView.gameInProgress) return;
        stopTimer();
        vb.timerTV.setText(String.valueOf(timerSeconds));
        countDownTimer = new CountDownTimer(timerSeconds * 1000L, 1000) {
            @Override public void onTick(long ms) {
                vb.timerTV.setText(String.valueOf((int)(ms / 1000) + 1));
            }
            @Override public void onFinish() { handleTimeout(); }
        }.start();
    }

    private void stopTimer() {
        if (countDownTimer != null) { countDownTimer.cancel(); countDownTimer = null; }
    }

    private void resetTimer() {
        stopTimer();
        startTimer();
    }

    private void handleTimeout() {
        if (vb.dotAndBoxesView.isGameOver()) return;
        DotAndBoxesGame game = vb.dotAndBoxesView.getGame();
        List<DotAndBoxesAI.Move> moves = DotAndBoxesAI.getValidMoves(game);
        if (moves.isEmpty()) return;

        DotAndBoxesAI.Move random = moves.get(new Random().nextInt(moves.size()));

        if (mode == Mode.ONLINE) {
            // In online mode, only auto-move if it's actually my turn
            if (!myTurn) return;
            game.markLine(random.isHorizontal, random.row, random.col);
            vb.dotAndBoxesView.invalidate();
            onlineManager.pushGameState(game);
        } else {
            game.markLine(random.isHorizontal, random.row, random.col);
            vb.dotAndBoxesView.invalidate();
            updateScoreUI();
            updateTurnIndicator(game.isPlayerOneTurn() ? 1 : 2);
            startTimer();
        }
    }

    // ── Profile editing (PvP only) ────────────────────────────────────────────────

    public void handleProfileClick(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        animateViewScale(vb.profileRL, 0f, 1.0f, 200);
        toggleVisibility(true, vb.profileRL, vb.Shadow.ShadowLayout);
        vb.playerOneET.setText(sharedPreferences.getString(DNBS_PLAYER_ONE_NAME_KEY, "Player 1"));
        vb.playerTwoET.setText(sharedPreferences.getString(DNBS_PLAYER_TWO_NAME_KEY, "Player 2"));
    }

    public void handleProfileButtons(View view) {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if ("save".equals(view.getTag())) saveProfiles();
        toggleVisibility(false, vb.profileRL, vb.Shadow.ShadowLayout);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void saveProfiles() {
        String p1 = resolvedName(vb.playerOneET.getText().toString(), getString(R.string.player_one));
        String p2 = resolvedName(vb.playerTwoET.getText().toString(), getString(R.string.player_two));
        saveToSharedPreferences(DNBS_PLAYER_ONE_NAME_KEY, p1);
        saveToSharedPreferences(DNBS_PLAYER_TWO_NAME_KEY, p2);
        vb.playerOneNameTV.setText(p1);
        vb.playerTwoNameTV.setText(p2);
    }

    private static String resolvedName(String input, String fallback) {
        String t = input.trim().replaceAll("\\s+", "");
        return t.isEmpty() ? fallback : t;
    }

    // ── Back / lifecycle ──────────────────────────────────────────────────────────

    @Override
    public void backLogic() {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        if (vb.Shop.ShopLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.Shop.ShopLayout, vb.Shadow.ShadowLayout);
        } else if (vb.DotAndBoxesMenuLayout.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.DotAndBoxesMenuLayout, vb.Shadow.ShadowLayout);
        } else if (vb.resetRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.resetRL, vb.Shadow.ShadowLayout);
        } else if (vb.leaveRL.getVisibility() == VISIBLE) {
            toggleVisibility(false, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else if (vb.dotAndBoxesView.gameInProgress) {
            toggleVisibility(true, vb.leaveRL, vb.Shadow.ShadowLayout);
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    @Override
    protected Class<?> getBackDestination() { return GamesActivity.class; }
}