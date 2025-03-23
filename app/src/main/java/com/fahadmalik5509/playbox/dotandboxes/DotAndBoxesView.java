package com.fahadmalik5509.playbox.dotandboxes;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.DARK_GREEN_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.LIGHT_RED_COLOR;
import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;

import com.fahadmalik5509.playbox.R;
import com.fahadmalik5509.playbox.miscellaneous.ActivityUtils;

public class DotAndBoxesView extends View {
    private int gridSize;
    private DotAndBoxesGame game;

    private float offsetX, offsetY, spacing, boardSide;
    private float gridInternalPadding;
    private float dotRadius = 10f, dotHitRadius;
    private final float touchThreshold = 30f;

    private Paint dotPaint, linePaint, previewPaint, boxPaintPlayer1, boxPaintPlayer2;
    private boolean isDragging = false;
    private float dragStartX, dragStartY, currentDragX, currentDragY;
    private int startDotRow = -1, startDotCol = -1;
    public boolean gameInProgress = false;

    // For dot highlighting.
    private int highlightedDotRow = -1, highlightedDotCol = -1;
    private final float HIGHLIGHT_MULTIPLIER = 1.5f;

    // Animation scales for box fills.
    private float[][] boxFillScales;

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

    private void init(Context context) {
        // Initial gridSize could be set by external call to updateGridSize.
        gridInternalPadding = dpToPx(32, context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        // Initialize with a default grid size if not already set.
        gridSize = 6;
        game = new DotAndBoxesGame(gridSize);
        dotHitRadius = dpToPx(32 - gridSize, context);

        dotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        dotPaint.setColor(ActivityUtils.BEIGE_COLOR);
        dotPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setStrokeWidth(8);
        linePaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        previewPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        previewPaint.setStrokeWidth(8);
        previewPaint.setShadowLayer(4f, 0f, 0f, Color.BLACK);

        boxPaintPlayer1 = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaintPlayer1.setColor(DARK_GREEN_COLOR);
        boxPaintPlayer1.setStyle(Paint.Style.FILL);

        boxPaintPlayer2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaintPlayer2.setColor(LIGHT_RED_COLOR);
        boxPaintPlayer2.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        boardSide = Math.min(w, h) - 2 * gridInternalPadding;
        offsetX = (w - boardSide) / 2;
        offsetY = (h - boardSide) / 2;
        spacing = boardSide / gridSize;
        super.onSizeChanged(w, h, oldW, oldH);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        drawBoxes(canvas);
        drawCommittedLines(canvas);
        if (isDragging) {
            drawPreviewLine(canvas);
        }
        drawDots(canvas);
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
                    Paint fillPaint = boxes[r][c] == 1 ? boxPaintPlayer1 : boxPaintPlayer2;
                    canvas.drawRect(left, top, right, bottom, fillPaint);
                    canvas.restore();
                }
            }
        }
    }

    private void animateBoxFill(final int r, final int c) {
        ValueAnimator animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(300);
        animator.addUpdateListener(animation -> {
            boxFillScales[r][c] = (float) animation.getAnimatedValue();
            invalidate();
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
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                float cx = offsetX + col * spacing;
                float cy = offsetY + row * spacing;
                float radius = (row == highlightedDotRow && col == highlightedDotCol)
                        ? dotRadius * HIGHLIGHT_MULTIPLIER : dotRadius;
                canvas.drawCircle(cx, cy, radius, dotPaint);
            }
        }
    }

    private void drawPreviewLine(Canvas canvas) {
        previewPaint.setColor(game.isPlayerOneTurn() ? LIGHT_GREEN_COLOR : Color.RED);
        previewPaint.setAlpha(128);
        float dx = currentDragX - dragStartX;
        float dy = currentDragY - dragStartY;
        float distance = (float) Math.hypot(dx, dy);
        float clampedDistance = Math.min(distance, spacing);
        float endX = (distance == 0) ? dragStartX : dragStartX + (dx / distance) * clampedDistance;
        float endY = (distance == 0) ? dragStartY : dragStartY + (dy / distance) * clampedDistance;
        canvas.drawLine(dragStartX, dragStartY, endX, endY, previewPaint);
    }

    // Returns the nearest dot if within hit radius; otherwise null.
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

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                int[] dot = getNearestDot(event.getX(), event.getY());
                if (dot != null && !isDotClosed(dot[0], dot[1])) {
                    playSoundAndVibrate(getContext(), R.raw.sound_dot_clicked, true, 100);
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
                        if (distSq < bestDistSq && distSq <= touchThreshold * touchThreshold) {
                            bestDistSq = distSq;
                            chosen = candidate;
                        }
                    }
                    int boxesBefore = countClaimedBoxes(game.getBoxes());
                    boolean moveCommitted = false;
                    if (chosen != null) {
                        int dr = Math.abs(chosen[0] - startDotRow);
                        int dc = Math.abs(chosen[1] - startDotCol);
                        if ((dr == 1 && dc == 0) || (dr == 0 && dc == 1)) {
                            if (dr == 0) {
                                moveCommitted = game.markLine(true, startDotRow, Math.min(startDotCol, chosen[1]));
                            } else {
                                moveCommitted = game.markLine(false, Math.min(startDotRow, chosen[0]), startDotCol);
                            }
                        }
                    }
                    int boxesAfter = countClaimedBoxes(game.getBoxes());
                    if (moveCommitted) {
                        if (boxesAfter > boxesBefore) {
                            playSoundAndVibrate(getContext(), R.raw.sound_box_complete, true, 200);
                            int[][] boxes = game.getBoxes();
                            for (int r = 0; r < gridSize; r++) {
                                for (int c = 0; c < gridSize; c++) {
                                    if (boxes[r][c] != 0 && boxFillScales[r][c] == 0f) {
                                        animateBoxFill(r, c);
                                    }
                                }
                            }
                        } else {
                            playSoundAndVibrate(getContext(), R.raw.sound_dot_clicked, true, 50);
                            gameInProgress = true;
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

    // Determines if a dot is "closed" (all adjacent lines are drawn)
    private boolean isDotClosed(int row, int col) {
        int[][] hLines = game.getHorizontalLines();
        int[][] vLines = game.getVerticalLines();
        boolean left = (col == 0) || (hLines[row][col - 1] != 0);
        boolean right = (col == gridSize) || (hLines[row][col] != 0);
        boolean up = (row == 0) || (vLines[row - 1][col] != 0);
        boolean down = (row == gridSize) || (vLines[row][col] != 0);
        return left && right && up && down;
    }

    private int countClaimedBoxes(int[][] boxes) {
        int count = 0;
        for (int[] row : boxes) {
            for (int box : row) {
                if (box != 0) count++;
            }
        }
        return count;
    }

    public void updateGridSize(int newGridSize) {
        this.gridSize = newGridSize;
        game = new DotAndBoxesGame(newGridSize);
        boxFillScales = new float[newGridSize][newGridSize];
        // Initialize fill scales to 0.
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

    public String getScoreText() {
        return game.getScoreText();
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }

    private float dpToPx(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }
}
