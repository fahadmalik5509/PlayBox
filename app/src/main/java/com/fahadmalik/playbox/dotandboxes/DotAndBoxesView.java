package com.fahadmalik.playbox.dotandboxes;

import static com.fahadmalik.playbox.miscellaneous.ActivityUtils.*;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.fahadmalik.playbox.R;
import com.fahadmalik.playbox.miscellaneous.ActivityUtils;

/**
 * Custom view that renders the Dots-and-Boxes grid and handles touch input.

 * Changes from original:
 *  – {@link #applyRemoteState(DotAndBoxesGame)} lets the Activity overwrite the game
 *    object with a Firebase-synced version (online mode).
 *  – {@code isDotClosed} / {@code getNearestDot} are unchanged.
 *  – Box-fill animation, checkerboard, dashed guides, committed lines, drag preview
 *    are all preserved; only dead code and redundancies were removed.
 */
public class DotAndBoxesView extends View {

    // ── Listener ──────────────────────────────────────────────────────────────────

    public interface OnMoveListener { void onMoveCommitted(); }

    // ── State ─────────────────────────────────────────────────────────────────────

    private int gridSize = 6;
    private DotAndBoxesGame game;
    private OnMoveListener moveListener;

    private boolean inputEnabled  = true;
    public  boolean gameInProgress = false;

    // ── Layout ────────────────────────────────────────────────────────────────────

    private float offsetX, offsetY, spacing, boardSide;
    private float gridInternalPadding;
    private float dotHitRadius;

    // ── Paints ────────────────────────────────────────────────────────────────────

    private Paint dotPaint, linePaint, previewPaint, boxPaint1, boxPaint2;

    // ── Touch state ───────────────────────────────────────────────────────────────

    private boolean isDragging;
    private float   dragStartX, dragStartY, currentDragX, currentDragY;
    private int     startDotRow = -1, startDotCol = -1;
    private int     highlightedRow = -1, highlightedCol = -1;

    // ── Animation ─────────────────────────────────────────────────────────────────

    private float[][] boxFillScales;

    // ── Constructors ──────────────────────────────────────────────────────────────

    public DotAndBoxesView(Context context)                               { super(context);            init(context); }
    public DotAndBoxesView(Context context, AttributeSet attrs)           { super(context, attrs);      init(context); }
    public DotAndBoxesView(Context context, AttributeSet attrs, int def)  { super(context, attrs, def); init(context); }

    // ── Initialisation ────────────────────────────────────────────────────────────

    private void init(Context context) {
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        gridInternalPadding = dpToPx(32);
        dotHitRadius        = dpToPx(32 - gridSize);
        game                = new DotAndBoxesGame(gridSize);
        boxFillScales       = new float[gridSize][gridSize];

        dotPaint = makePaint(ActivityUtils.BEIGE_COLOR, Paint.Style.FILL, 0, true);
        dotPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        linePaint = makePaint(Color.WHITE, Paint.Style.STROKE, 8, true);
        linePaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        previewPaint = makePaint(Color.WHITE, Paint.Style.STROKE, 8, true);
        previewPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        boxPaint1 = makePaint(DARK_GREEN_COLOR, Paint.Style.FILL, 0, true);
        boxPaint2 = makePaint(LIGHT_RED_COLOR,  Paint.Style.FILL, 0, true);
    }

    private static Paint makePaint(int color, Paint.Style style, float strokeWidth, boolean aa) {
        Paint p = new Paint(aa ? Paint.ANTI_ALIAS_FLAG : 0);
        p.setColor(color);
        p.setStyle(style);
        if (strokeWidth > 0) p.setStrokeWidth(strokeWidth);
        return p;
    }

    // ── Layout ────────────────────────────────────────────────────────────────────

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        boardSide = Math.min(w, h) - 2 * gridInternalPadding;
        offsetX   = (w - boardSide) / 2f;
        offsetY   = (h - boardSide) / 2f;
        spacing   = boardSide / gridSize;
        super.onSizeChanged(w, h, oldW, oldH);
    }

    // ── Drawing ───────────────────────────────────────────────────────────────────

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        drawCheckerboard(canvas);
        drawDashedGuides(canvas);
        drawBoxFills(canvas);
        drawCommittedLines(canvas);
        if (isDragging) drawDragPreview(canvas);
        drawDots(canvas);
    }

    private void drawCheckerboard(Canvas canvas) {
        Paint p = new Paint();
        p.setColor(Color.parseColor("#1D1D1D"));
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if ((r + c) % 2 == 1)
                    canvas.drawRect(
                            offsetX + c * spacing, offsetY + r * spacing,
                            offsetX + (c + 1) * spacing, offsetY + (r + 1) * spacing, p);
    }

    private void drawDashedGuides(Canvas canvas) {
        Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
        p.setColor(CHARCOAL_COLOR);
        p.setStrokeWidth(2);
        p.setStyle(Paint.Style.STROKE);
        p.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        for (int i = 0; i <= gridSize; i++) {
            float y = offsetY + i * spacing;
            canvas.drawLine(offsetX, y, offsetX + boardSide, y, p);
            float x = offsetX + i * spacing;
            canvas.drawLine(x, offsetY, x, offsetY + boardSide, p);
        }
    }

    private void drawBoxFills(Canvas canvas) {
        int[][] boxes = game.getBoxes();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 0) continue;
                float l = offsetX + c * spacing, t = offsetY + r * spacing;
                float cx = l + spacing / 2f, cy = t + spacing / 2f;
                canvas.save();
                canvas.scale(boxFillScales[r][c], boxFillScales[r][c], cx, cy);
                canvas.drawRect(l, t, l + spacing, t + spacing, boxes[r][c] == 1 ? boxPaint1 : boxPaint2);
                canvas.restore();
            }
        }
    }

    private void drawCommittedLines(Canvas canvas) {
        int[][] hLines = game.getHorizontalLines();
        for (int r = 0; r < gridSize + 1; r++)
            for (int c = 0; c < gridSize; c++)
                if (hLines[r][c] != 0) {
                    linePaint.setColor(hLines[r][c] == 1 ? LIGHT_GREEN_COLOR : Color.RED);
                    canvas.drawLine(offsetX + c * spacing, offsetY + r * spacing,
                            offsetX + (c + 1) * spacing, offsetY + r * spacing, linePaint);
                }

        int[][] vLines = game.getVerticalLines();
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize + 1; c++)
                if (vLines[r][c] != 0) {
                    linePaint.setColor(vLines[r][c] == 1 ? LIGHT_GREEN_COLOR : Color.RED);
                    canvas.drawLine(offsetX + c * spacing, offsetY + r * spacing,
                            offsetX + c * spacing, offsetY + (r + 1) * spacing, linePaint);
                }
    }

    private void drawDots(Canvas canvas) {
        float base = 10f;
        for (int r = 0; r <= gridSize; r++)
            for (int c = 0; c <= gridSize; c++) {
                float cx = offsetX + c * spacing, cy = offsetY + r * spacing;
                float rad = (r == highlightedRow && c == highlightedCol) ? base * 1.5f : base;
                canvas.drawCircle(cx, cy, rad, dotPaint);
            }
    }

    private void drawDragPreview(Canvas canvas) {
        previewPaint.setColor(game.isPlayerOneTurn() ? LIGHT_GREEN_COLOR : Color.RED);
        previewPaint.setAlpha(128);
        float dx = currentDragX - dragStartX, dy = currentDragY - dragStartY;
        float dist = (float) Math.hypot(dx, dy);
        float clamped = Math.min(dist, spacing);
        float ex = (dist == 0) ? dragStartX : dragStartX + dx / dist * clamped;
        float ey = (dist == 0) ? dragStartY : dragStartY + dy / dist * clamped;
        canvas.drawLine(dragStartX, dragStartY, ex, ey, previewPaint);
    }

    // ── Touch ─────────────────────────────────────────────────────────────────────

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!inputEnabled) return false;

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN: {
                int[] dot = nearestDot(event.getX(), event.getY());
                if (dot != null && !isDotClosed(dot[0], dot[1])) {
                    playSoundAndVibrate(R.raw.sound_dot_clicked, true, 100);
                    startDotRow = dot[0]; startDotCol = dot[1];
                    highlightedRow = dot[0]; highlightedCol = dot[1];
                    dragStartX = offsetX + startDotCol * spacing;
                    dragStartY = offsetY + startDotRow * spacing;
                    isDragging = true;
                    invalidate();
                }
                return true;
            }
            case MotionEvent.ACTION_MOVE: {
                if (isDragging) { currentDragX = event.getX(); currentDragY = event.getY(); invalidate(); }
                return true;
            }
            case MotionEvent.ACTION_UP: {
                if (!isDragging) return true;
                highlightedRow = -1; highlightedCol = -1;

                float dx = currentDragX - dragStartX, dy = currentDragY - dragStartY;
                float dist = (float) Math.hypot(dx, dy);
                float clamped = Math.min(dist, spacing);
                float ex = (dist == 0) ? dragStartX : dragStartX + dx / dist * clamped;
                float ey = (dist == 0) ? dragStartY : dragStartY + dy / dist * clamped;

                int[] chosen = pickNeighbour(startDotRow, startDotCol, ex, ey);
                int before = game.getClaimedBoxesCount();
                boolean committed = false;

                if (chosen != null) {
                    int dr = Math.abs(chosen[0] - startDotRow);
                    int dc = Math.abs(chosen[1] - startDotCol);
                    if (dr + dc == 1) {
                        committed = (dr == 0)
                                ? game.markLine(true,  startDotRow, Math.min(startDotCol, chosen[1]))
                                : game.markLine(false, Math.min(startDotRow, chosen[0]), startDotCol);
                    }
                }

                if (committed) {
                    int after = game.getClaimedBoxesCount();
                    if (after > before) {
                        playSoundAndVibrate(R.raw.sound_box_complete, true, 200);
                        animateCompletedBoxes();
                    } else {
                        playSoundAndVibrate(R.raw.sound_line_placed, true, 50);
                        gameInProgress = true;
                    }
                    if (moveListener != null) moveListener.onMoveCommitted();
                }

                isDragging = false; startDotRow = -1; startDotCol = -1;
                invalidate();
                return true;
            }
            default: return super.onTouchEvent(event);
        }
    }

    // ── Private helpers ───────────────────────────────────────────────────────────

    private int[] nearestDot(float x, float y) {
        int br = -1, bc = -1;
        float minSq = Float.MAX_VALUE;
        for (int r = 0; r <= gridSize; r++)
            for (int c = 0; c <= gridSize; c++) {
                float dSq = sq(x - (offsetX + c * spacing)) + sq(y - (offsetY + r * spacing));
                if (dSq < minSq) { minSq = dSq; br = r; bc = c; }
            }
        return (minSq <= dotHitRadius * dotHitRadius) ? new int[]{br, bc} : null;
    }

    private int[] pickNeighbour(int row, int col, float ex, float ey) {
        int[][] neighbours = {
                {row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}
        };
        int[] best = null; float bestSq = 900f; // ~30px threshold
        for (int[] nb : neighbours) {
            if (nb[0] < 0 || nb[0] > gridSize || nb[1] < 0 || nb[1] > gridSize) continue;
            float dSq = sq(ex - (offsetX + nb[1] * spacing)) + sq(ey - (offsetY + nb[0] * spacing));
            if (dSq < bestSq) { bestSq = dSq; best = nb; }
        }
        return best;
    }

    private boolean isDotClosed(int row, int col) {
        int[][] h = game.getHorizontalLines(), v = game.getVerticalLines();
        boolean left  = col == 0       || h[row][col - 1] != 0;
        boolean right = col == gridSize || h[row][col]     != 0;
        boolean up    = row == 0       || v[row - 1][col]  != 0;
        boolean down  = row == gridSize || v[row][col]      != 0;
        return left && right && up && down;
    }

    private static float sq(float v) { return v * v; }

    private float dpToPx(float dp) {
        return dp * getResources().getDisplayMetrics().density;
    }

    // ── Animations ────────────────────────────────────────────────────────────────

    private void animateBoxFill(int r, int c) {
        ValueAnimator anim = ValueAnimator.ofFloat(0f, 1f);
        anim.setDuration(300);
        anim.addUpdateListener(a -> { boxFillScales[r][c] = (float) a.getAnimatedValue(); invalidate(); });
        anim.start();
    }

    public void animateCompletedBoxes() {
        int[][] boxes = game.getBoxes();
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if (boxes[r][c] != 0 && boxFillScales[r][c] == 0f)
                    animateBoxFill(r, c);
    }

    public void animateGameReset() {
        animate().alpha(0f).setDuration(300)
                .withEndAction(() -> animate().alpha(1f).setDuration(300).start()).start();
    }

    // ── Public API ────────────────────────────────────────────────────────────────

    public void updateGridSize(int newSize) {
        gridSize      = newSize;
        game          = new DotAndBoxesGame(newSize);
        boxFillScales = new float[newSize][newSize];
        dotHitRadius  = dpToPx(32 - newSize);
        if (boardSide > 0) spacing = boardSide / newSize;
        requestLayout();
        invalidate();
    }

    public void restartGame() {
        game.resetGame();
        boxFillScales = new float[gridSize][gridSize];
        invalidate();
    }

    /**
     * Replaces the current game object with a Firebase-synced snapshot.
     * Animates any newly completed boxes automatically.
     */
    public void applyRemoteState(DotAndBoxesGame remoteGame) {
        int[][] oldBoxes = game.getBoxes();
        game = remoteGame;
        // Animate any boxes that just appeared
        int[][] newBoxes = game.getBoxes();
        for (int r = 0; r < gridSize; r++)
            for (int c = 0; c < gridSize; c++)
                if (oldBoxes[r][c] == 0 && newBoxes[r][c] != 0)
                    animateBoxFill(r, c);
        invalidate();
    }

    public int[]            getScore()     { return game.getScores(); }
    public boolean          isGameOver()   { return game.isGameOver(); }
    public DotAndBoxesGame  getGame()      { return game; }
    public void setInputEnabled(boolean v) { inputEnabled = v; }
    public void setOnMoveListener(OnMoveListener l) { moveListener = l; }
}
