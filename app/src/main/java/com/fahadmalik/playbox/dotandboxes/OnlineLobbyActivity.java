package com.fahadmalik.playbox.dotandboxes;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.fahadmalik.playbox.R;
import com.fahadmalik.playbox.databinding.OnlineLobbyLayoutBinding;
import com.fahadmalik.playbox.miscellaneous.BaseActivity;
import com.fahadmalik.playbox.miscellaneous.GamesActivity;

import static com.fahadmalik.playbox.miscellaneous.ActivityUtils.*;

/**
 * Lobby screen for online multiplayer.
 *
 * ── Flow ──────────────────────────────────────────────────────────────────────
 *  HOST  → enters name → taps "Create Room"
 *          → room created in Firebase → waiting screen shows the room code
 *          → when opponent joins, game starts automatically
 *
 *  GUEST → enters name + room code → taps "Join Room"
 *          → room joined in Firebase → game starts immediately
 *
 * The room code, player number, grid size and timer are forwarded to
 * DotAndBoxesActivity via Intent extras (keys defined as constants below).
 */
public class OnlineLobbyActivity extends BaseActivity {

    // ── Intent extras (used by DotAndBoxesActivity too) ─────────────────────────
    public static final String EXTRA_ROOM_ID      = "dnb_room_id";
    public static final String EXTRA_PLAYER_NUM   = "dnb_player_num";
    public static final String EXTRA_PLAYER_NAME  = "dnb_player_name";
    public static final String EXTRA_GRID_SIZE    = "dnb_grid_size";
    public static final String EXTRA_TIMER        = "dnb_timer";
    public static final String EXTRA_MODE_ONLINE  = "dnb_mode_online";

    // ── Fields ───────────────────────────────────────────────────────────────────

    private OnlineLobbyLayoutBinding vb;
    private OnlineGameManager manager;

    private int selectedGridSize = 6;   // boxes per side
    private int timerSeconds     = 15;  // 0 = no timer

    // ── Lifecycle ─────────────────────────────────────────────────────────────────

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        vb = OnlineLobbyLayoutBinding.inflate(getLayoutInflater());
        setContentView(vb.getRoot());

        manager = new OnlineGameManager();

        setupGridSizeSelector();
        setupTimerControls();

        vb.createRoomBtn.setOnClickListener(v -> handleCreateRoom());
        vb.joinRoomBtn.setOnClickListener(v -> handleJoinRoom());
        vb.cancelWaitingBtn.setOnClickListener(v -> cancelWaiting());
    }

    // ── UI helpers ────────────────────────────────────────────────────────────────

    private void setupGridSizeSelector() {
        int[] sizes = {4, 5, 6, 7, 8};
        View[] buttons = {
                vb.grid4Btn, vb.grid5Btn, vb.grid6Btn, vb.grid7Btn, vb.grid8Btn
        };
        for (int i = 0; i < buttons.length; i++) {
            final int size = sizes[i];
            buttons[i].setOnClickListener(v -> {
                selectedGridSize = size;
                updateGridSelectionUI(sizes, buttons, size);
                playSoundAndVibrate(R.raw.sound_ui, true, 50);
            });
        }
        // Default selection
        updateGridSelectionUI(sizes, buttons, selectedGridSize);
    }

    private void updateGridSelectionUI(int[] sizes, View[] buttons, int selected) {
        for (int i = 0; i < buttons.length; i++) {
            buttons[i].setSelected(sizes[i] == selected);
        }
    }

    private void setupTimerControls() {
        vb.timerValueTV.setText(timerSeconds == 0 ? "OFF" : timerSeconds + "s");

        vb.timerPlusBtn.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            if (timerSeconds < 60) {
                timerSeconds = (timerSeconds == 0) ? 10 : timerSeconds + 5;
                vb.timerValueTV.setText(timerSeconds + "s");
            } else {
                playSoundAndVibrate(R.raw.sound_error, true, 50);
            }
        });

        vb.timerMinusBtn.setOnClickListener(v -> {
            playSoundAndVibrate(R.raw.sound_ui, true, 50);
            if (timerSeconds > 0) {
                timerSeconds -= 5;
                if (timerSeconds < 0) timerSeconds = 0;
                vb.timerValueTV.setText(timerSeconds == 0 ? "OFF" : timerSeconds + "s");
            } else {
                playSoundAndVibrate(R.raw.sound_error, true, 50);
            }
        });
    }

    private void showWaiting(String roomCode) {
        vb.lobbySetupLayout.setVisibility(View.GONE);
        vb.waitingLayout.setVisibility(View.VISIBLE);
        vb.roomCodeTV.setText(roomCode);
    }

    private void showLoading(boolean show) {
        vb.loadingProgress.setVisibility(show ? View.VISIBLE : View.GONE);
        vb.createRoomBtn.setEnabled(!show);
        vb.joinRoomBtn.setEnabled(!show);
    }

    private void hideKeyboard(View view) {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    // ── Actions ───────────────────────────────────────────────────────────────────

    private void handleCreateRoom() {
        String name = resolvedName(vb.playerNameET.getText().toString(), "Player 1");
        hideKeyboard(vb.playerNameET);
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        showLoading(true);

        manager.createRoom(name, selectedGridSize, timerSeconds, new OnlineGameManager.RoomCallback() {
            @Override
            public void onRoomReady(String roomId, int assignedPlayer) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showWaiting(roomId);
                    // Start listening; game begins when player2Name is set
                    manager.listenForStateChanges((game, currentPlayer, status, p1Name, p2Name) -> {
                        if (p2Name != null && !p2Name.isEmpty()
                                && OnlineGameManager.STATUS_PLAYING.equals(status)) {
                            manager.detachListener();
                            launchGame(roomId, 1, name, selectedGridSize, timerSeconds);
                        }
                    });
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showToast(message);
                });
            }
        });
    }

    private void handleJoinRoom() {
        String name = resolvedName(vb.playerNameET.getText().toString(), "Player 2");
        String code = vb.roomCodeET.getText().toString().trim().toUpperCase();
        hideKeyboard(vb.roomCodeET);

        if (code.length() != 6) {
            showToast("Please enter a valid 6-character room code.");
            return;
        }

        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        showLoading(true);

        manager.joinRoom(code, name, new OnlineGameManager.RoomCallback() {
            @Override
            public void onRoomReady(String roomId, int assignedPlayer) {
                runOnUiThread(() -> {
                    showLoading(false);
                    launchGame(roomId, 2, name,
                            manager.getGridSize(), timerSeconds);
                });
            }

            @Override
            public void onError(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showToast(message);
                });
            }
        });
    }

    private void cancelWaiting() {
        playSoundAndVibrate(R.raw.sound_ui, true, 50);
        manager.detachListener();
        manager.leaveRoom(1);
        vb.waitingLayout.setVisibility(View.GONE);
        vb.lobbySetupLayout.setVisibility(View.VISIBLE);
    }

    private void launchGame(String roomId, int playerNum, String playerName,
                            int gridSize, int timer) {
        Intent intent = new Intent(this, DotAndBoxesActivity.class);
        intent.putExtra(EXTRA_MODE_ONLINE,  true);
        intent.putExtra(EXTRA_ROOM_ID,      roomId);
        intent.putExtra(EXTRA_PLAYER_NUM,   playerNum);
        intent.putExtra(EXTRA_PLAYER_NAME,  playerName);
        intent.putExtra(EXTRA_GRID_SIZE,    gridSize);
        intent.putExtra(EXTRA_TIMER,        timer);
        startActivity(intent);
        finish();
    }

    // ── Utilities ─────────────────────────────────────────────────────────────────

    private static String resolvedName(String input, String fallback) {
        String trimmed = input.trim().replaceAll("\\s+", "");
        return trimmed.isEmpty() ? fallback : trimmed;
    }

    /** Shows a short toast — replace with your app's snackbar/toast util if preferred. */
    private void showToast(String msg) {
        android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
    }

    // ── Navigation ────────────────────────────────────────────────────────────────

    @Override
    public void backLogic() {
        if (vb.waitingLayout.getVisibility() == View.VISIBLE) {
            cancelWaiting();
        } else {
            changeActivity(this, GamesActivity.class);
        }
    }

    @Override
    protected Class<?> getBackDestination() {
        return GamesActivity.class;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        manager.detachListener();
    }
}
