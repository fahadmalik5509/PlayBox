package com.fahadmalik5509.playbox.dotandboxes;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.CHARCOAL_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DARK_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

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

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.miscellaneous.ActivityUtils;

public class DotAndBoxesView extends View {

    // Game state
    private int gridSize;
    private DotAndBoxesGame game;
    private OnMoveListener moveListener;
    private boolean inputEnabled = true;
    public boolean gameInProgress = false;

    // Layout properties
    private float offsetX, offsetY, spacing, boardSide;
    private float gridInternalPadding;
    private float dotHitRadius;

    // Paints for drawing various elements
    private Paint dotPaint, linePaint, previewPaint, boxPaintPlayer1, boxPaintPlayer2;

    // Touch-handling
    private boolean isDragging = false;
    private float dragStartX, dragStartY, currentDragX, currentDragY;
    private int startDotRow = -1, startDotCol = -1;
    private int highlightedDotRow = -1, highlightedDotCol = -1;

    // Animation scales for box fills (to animate completed boxes)
    private float[][] boxFillScales;

    // Constructors
    public DotAndBoxesView(Context context) {
        super(context);
        init(context);
    }

    public DotAndBoxesView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public DotAndBoxesView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    // Initialization: set up game, paints, and defaults.
    private void init(Context context) {
        gridInternalPadding = dpToPx(32, context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);

        // Default grid size and game initialization.
        gridSize = 6;
        game = new DotAndBoxesGame(gridSize);

        // Initialize animation scales for box fills.
        boxFillScales = new float[gridSize][gridSize];
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                boxFillScales[r][c] = 0f;
            }
        }
        dotHitRadius = dpToPx(32 - gridSize, context);

        // Dot paint.
        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(ActivityUtils.BEIGE_COLOR);
        dotPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        // Line paint for committed moves.
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(8);
        linePaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        // Preview paint (for drag preview).
        previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStrokeWidth(8);
        previewPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        // Box paints for player fills.
        boxPaintPlayer1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaintPlayer1.setColor(DARK_GREEN_COLOR);
        boxPaintPlayer1.setStyle(Paint.Style.FILL);

        boxPaintPlayer2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaintPlayer2.setColor(LIGHT_RED_COLOR);
        boxPaintPlayer2.setStyle(Paint.Style.FILL);
    }

    // Handle view size changes.
    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        boardSide = Math.min(w, h) - 2 * gridInternalPadding;
        offsetX = (w - boardSide) / 2;
        offsetY = (h - boardSide) / 2;
        spacing = boardSide / gridSize;
        super.onSizeChanged(w, h, oldW, oldH);
    }

    // Drawing method.
    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        drawCheckerboard(canvas);
        drawDashedGridLines(canvas);
        drawBoxes(canvas);
        drawCommittedLines(canvas);
        if (isDragging) {
            drawPreviewLine(canvas);
        }
        drawDots(canvas);
    }

    // ========================
    // Drawing helper methods
    // ========================

    private void drawCheckerboard(Canvas canvas) {
        Paint checkerPaint = new Paint();
        checkerPaint.setColor(Color.parseColor("#1D1D1D"));
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if ((r + c) % 2 == 1) {
                    float left = offsetX + c * spacing;
                    float top = offsetY + r * spacing;
                    canvas.drawRect(left, top, left + spacing, top + spacing, checkerPaint);
                }
            }
        }
    }

    private void drawDashedGridLines(Canvas canvas) {
        Paint dashPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dashPaint.setColor(CHARCOAL_COLOR);
        dashPaint.setStrokeWidth(2);
        dashPaint.setStyle(Paint.Style.STROKE);
        dashPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));
        for (int row = 0; row <= gridSize; row++) {
            float y = offsetY + row * spacing;
            canvas.drawLine(offsetX, y, offsetX + boardSide, y, dashPaint);
        }
        for (int col = 0; col <= gridSize; col++) {
            float x = offsetX + col * spacing;
            canvas.drawLine(x, offsetY, x, offsetY + boardSide, dashPaint);
        }
    }

    private void drawBoxes(Canvas canvas) {
        int[][] boxes = game.getBoxes();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] != 0) {
                    float left = offsetX + c * spacing;
                    float top = offsetY + r * spacing;
                    float right = left + spacing;
                    float bottom = top + spacing;
                    float cx = (left + right) / 2;
                    float cy = (top + bottom) / 2;
                    canvas.save();
                    canvas.scale(boxFillScales[r][c], boxFillScales[r][c], cx, cy);
                    Paint fillPaint = (boxes[r][c] == 1) ? boxPaintPlayer1 : boxPaintPlayer2;
                    canvas.drawRect(left, top, right, bottom, fillPaint);
                    canvas.restore();
                }
            }
        }
    }

    // animateBoxFill – animate box filling over 300ms.
    private void animateBoxFill(final int r, final int c) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            boxFillScales[r][c] = (float) animation.getAnimatedValue();
            invalidate(); // Trigger redraw.
        });
        animator.start();
    }

    private void drawCommittedLines(Canvas canvas) {
        int[][] hLines = game.getHorizontalLines();
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (hLines[row][col] != 0) {
                    linePaint.setColor(hLines[row][col] == 1 ? LIGHT_GREEN_COLOR : Color.RED);
                    float startX = offsetX + col * spacing;
                    float startY = offsetY + row * spacing;
                    canvas.drawLine(startX, startY, startX + spacing, startY, linePaint);
                }
            }
        }
        int[][] vLines = game.getVerticalLines();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                if (vLines[row][col] != 0) {
                    linePaint.setColor(vLines[row][col] == 1 ? LIGHT_GREEN_COLOR : Color.RED);
                    float startX = offsetX + col * spacing;
                    float startY = offsetY + row * spacing;
                    canvas.drawLine(startX, startY, startX, startY + spacing, linePaint);
                }
            }
        }
    }

    private void drawDots(Canvas canvas) {
        float dotRadius = 10f;
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                float cx = offsetX + col * spacing;
                float cy = offsetY + row * spacing;
                float radius = (row == highlightedDotRow && col == highlightedDotCol) ? dotRadius * 1.5f : dotRadius;
                canvas.drawCircle(cx, cy, radius, dotPaint);
            }
        }
    }

    private void drawPreviewLine(Canvas canvas) {
        previewPaint.setColor(game.isPlayerOneTurn() ? LIGHT_GREEN_COLOR : Color.RED);
        previewPaint.setAlpha(128);
        float dx = currentDragX - dragStartX, dy = currentDragY - dragStartY;
        float distance = (float) Math.hypot(dx, dy);
        float clampedDistance = Math.min(distance, spacing);
        float endX = (distance == 0) ? dragStartX : dragStartX + (dx / distance) * clampedDistance;
        float endY = (distance == 0) ? dragStartY : dragStartY + (dy / distance) * clampedDistance;
        canvas.drawLine(dragStartX, dragStartY, endX, endY, previewPaint);
    }

    // ========================
    // Touch Handling
    // ========================
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (!inputEnabled) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int[] dot = getNearestDot(event.getX(), event.getY());
                if (dot != null && !isDotClosed(dot[0], dot[1])) {
                    playSoundAndVibrate(R.raw.sound_dot_clicked, true, 100);
                    startDotRow = dot[0];
                    startDotCol = dot[1];
                    highlightedDotRow = dot[0];
                    highlightedDotCol = dot[1];
                    dragStartX = offsetX + startDotCol * spacing;
                    dragStartY = offsetY + startDotRow * spacing;
                    isDragging = true;
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_MOVE:
                if (isDragging) {
                    currentDragX = event.getX();
                    currentDragY = event.getY();
                    invalidate();
                }
                return true;
            case MotionEvent.ACTION_UP:
                if (isDragging) {
                    highlightedDotRow = -1;
                    highlightedDotCol = -1;
                    float dx = currentDragX - dragStartX;
                    float dy = currentDragY - dragStartY;
                    float distance = (float) Math.hypot(dx, dy);
                    float clampedDistance = Math.min(distance, spacing);
                    float clampedX = (distance == 0) ? dragStartX : dragStartX + (dx / distance) * clampedDistance;
                    float clampedY = (distance == 0) ? dragStartY : dragStartY + (dy / distance) * clampedDistance;

                    int[][] candidates = new int[4][2];
                    int candidateCount = 0;
                    if (startDotRow > 0) candidates[candidateCount++] = new int[]{startDotRow - 1, startDotCol};
                    if (startDotRow < gridSize) candidates[candidateCount++] = new int[]{startDotRow + 1, startDotCol};
                    if (startDotCol > 0) candidates[candidateCount++] = new int[]{startDotRow, startDotCol - 1};
                    if (startDotCol < gridSize) candidates[candidateCount++] = new int[]{startDotRow, startDotCol + 1};

                    int[] chosen = null;
                    float bestDistSq = Float.MAX_VALUE;
                    for (int i = 0; i < candidateCount; i++) {
                        int[] candidate = candidates[i];
                        float cx = offsetX + candidate[1] * spacing;
                        float cy = offsetY + candidate[0] * spacing;
                        float distSq = (clampedX - cx) * (clampedX - cx) + (clampedY - cy) * (clampedY - cy);
                        if (distSq < bestDistSq && distSq <= 900f) {
                            bestDistSq = distSq;
                            chosen = candidate;
                        }
                    }
                    int boxesBefore = game.getClaimedBoxesCount();
                    boolean moveCommitted = false;
                    if (chosen != null) {
                        int dr = Math.abs(chosen[0] - startDotRow);
                        int dc = Math.abs(chosen[1] - startDotCol);
                        if ((dr == 1 && dc == 0) || (dr == 0 && dc == 1)) {
                            moveCommitted = (dr == 0)
                                    ? game.markLine(true, startDotRow, Math.min(startDotCol, chosen[1]))
                                    : game.markLine(false, Math.min(startDotRow, chosen[0]), startDotCol);
                        }
                    }
                    int boxesAfter = game.getClaimedBoxesCount();
                    if (moveCommitted) {
                        if (boxesAfter > boxesBefore) {
                            playSoundAndVibrate(R.raw.sound_box_complete, true, 200);
                            for (int r = 0; r < gridSize; r++) {
                                for (int c = 0; c < gridSize; c++) {
                                    if (game.getBoxes()[r][c] != 0 && boxFillScales[r][c] == 0f) {
                                        animateBoxFill(r, c);
                                    }
                                }
                            }
                        } else {
                            playSoundAndVibrate(R.raw.sound_line_placed, true, 50);
                            gameInProgress = true;
                        }

                        if (moveListener != null) {
                            moveListener.onMoveCommitted();
                        }
                    }
                    isDragging = false;
                    startDotRow = -1;
                    startDotCol = -1;
                    invalidate();
                }
                return true;
            default:
                return super.onTouchEvent(event);
        }
    }

    // ========================
    // Helper Methods
    // ========================

    // Finds the nearest dot to the given x, y.
    private int[] getNearestDot(float x, float y) {
        int nearestRow = -1, nearestCol = -1;
        float minDistSq = Float.MAX_VALUE;
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                float cx = offsetX + col * spacing;
                float cy = offsetY + row * spacing;
                float distSq = (x - cx) * (x - cx) + (y - cy) * (y - cy);
                if (distSq < minDistSq) {
                    minDistSq = distSq;
                    nearestRow = row;
                    nearestCol = col;
                }
            }
        }
        return (minDistSq <= dotHitRadius * dotHitRadius) ? new int[]{nearestRow, nearestCol} : null;
    }

    // Checks if a dot is closed (i.e., all adjacent lines have been drawn).
    private boolean isDotClosed(int row, int col) {
        int[][] hLines = game.getHorizontalLines();
        int[][] vLines = game.getVerticalLines();
        boolean left = (col == 0) || (hLines[row][col - 1] != 0);
        boolean right = (col == gridSize) || (hLines[row][col] != 0);
        boolean up = (row == 0) || (vLines[row - 1][col] != 0);
        boolean down = (row == gridSize) || (vLines[row][col] != 0);
        return left && right && up && down;
    }

    // Converts dp to pixels.
    private float dpToPx(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    // ========================
    // Public API Methods for Activity Interaction
    // ========================

    public void updateGridSize(int newGridSize) {
        this.gridSize = newGridSize;
        game = new DotAndBoxesGame(newGridSize);
        boxFillScales = new float[newGridSize][newGridSize];
        for (int r = 0; r < newGridSize; r++) {
            for (int c = 0; c < newGridSize; c++) {
                boxFillScales[r][c] = 0f;
            }
        }
        if (boardSide > 0) {
            spacing = boardSide / gridSize;
        }
        requestLayout();
        invalidate();
    }

    public void restartGame() {
        game.resetGame();
        boxFillScales = new float[gridSize][gridSize];
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                boxFillScales[r][c] = 0f;
            }
        }
        invalidate();
    }

    public void animateGameReset() {
        this.animate().alpha(0f).setDuration(300).withEndAction(() -> this.animate().alpha(1f).setDuration(300).start()).start();
    }

    public void animateCompletedBoxes() {
        int[][] boxes = game.getBoxes();
        for (int r = 0; r < boxes.length; r++) {
            for (int c = 0; c < boxes[r].length; c++) {
                if (boxes[r][c] != 0 && boxFillScales[r][c] == 0f) {
                    animateBoxFill(r, c);
                }
            }
        }
    }

    public int[] getScore() { return game.getScores(); }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    public DotAndBoxesGame getGame() {
        return game;
    }

    public void setInputEnabled(boolean enabled) {
        inputEnabled = enabled;
    }

    public interface OnMoveListener { void onMoveCommitted(); }

    public void setOnMoveListener(OnMoveListener listener) { this.moveListener = listener; }
}