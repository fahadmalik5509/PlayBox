package com.fahadmalik5509.playbox.dotandboxes;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.playSoundAndVibrate;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

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
        dotHitRadius = dpToPx(16, context);
        gridInternalPadding = dpToPx(32, context);
        setLayerType(LAYER_TYPE_SOFTWARE, null);
        game = new DotAndBoxesGame(gridSize);

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
        boxPaintPlayer1.setColor(Color.parseColor("#BBDEFB"));
        boxPaintPlayer1.setStyle(Paint.Style.FILL);

        boxPaintPlayer2 = new Paint(Paint.ANTI_ALIAS_FLAG);
        boxPaintPlayer2.setColor(Color.parseColor("#FFCDD2"));
        boxPaintPlayer2.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldW, int oldH) {
        boardSide = Math.min(w, h) - 2 * gridInternalPadding;
        offsetX = (w - boardSide) / 2;
        offsetY = (h - boardSide) / 2;
        spacing = boardSide / gridSize; // gridSize segments (boxes), dots are gridSize+1
        super.onSizeChanged(w, h, oldW, oldH);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawBoxes(canvas);
        drawCommittedLines(canvas);
        drawDots(canvas);
        if (isDragging) drawPreviewLine(canvas);
    }

    private void drawBoxes(Canvas canvas) {
        int[][] boxes = game.getBoxes();
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] != 0) {
                    float left = offsetX + c * spacing;
                    float top = offsetY + r * spacing;
                    float right = offsetX + (c + 1) * spacing;
                    float bottom = offsetY + (r + 1) * spacing;
                    canvas.drawRect(left, top, right, bottom,
                            boxes[r][c] == 1 ? boxPaintPlayer1 : boxPaintPlayer2);
                }
            }
        }
    }

    private void drawCommittedLines(Canvas canvas) {
        int[][] hLines = game.getHorizontalLines();
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize; col++) {
                if (hLines[row][col] != 0) {
                    linePaint.setColor(hLines[row][col] == 1 ? Color.BLUE : Color.RED);
                    float startX = offsetX + col * spacing;
                    float startY = offsetY + row * spacing;
                    canvas.drawLine(startX, startY, offsetX + (col + 1) * spacing, startY, linePaint);
                }
            }
        }
        int[][] vLines = game.getVerticalLines();
        for (int row = 0; row < gridSize; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                if (vLines[row][col] != 0) {
                    linePaint.setColor(vLines[row][col] == 1 ? Color.BLUE : Color.RED);
                    float startX = offsetX + col * spacing;
                    float startY = offsetY + row * spacing;
                    canvas.drawLine(startX, startY, startX, offsetY + (row + 1) * spacing, linePaint);
                }
            }
        }
    }

    private void drawDots(Canvas canvas) {
        for (int row = 0; row < gridSize + 1; row++) {
            for (int col = 0; col < gridSize + 1; col++) {
                float cx = offsetX + col * spacing;
                float cy = offsetY + row * spacing;
                canvas.drawCircle(cx, cy, dotRadius, dotPaint);
            }
        }
    }

    private void drawPreviewLine(Canvas canvas) {
        previewPaint.setColor(game.isPlayerOneTurn() ? Color.BLUE : Color.RED);
        previewPaint.setAlpha(128);
        float dx = currentDragX - dragStartX, dy = currentDragY - dragStartY;
        float distance = (float) Math.sqrt(dx * dx + dy * dy);
        float clampedDistance = Math.min(distance, spacing);
        float endX = (distance == 0) ? dragStartX : dragStartX + (dx / distance) * clampedDistance;
        float endY = (distance == 0) ? dragStartY : dragStartY + (dy / distance) * clampedDistance;
        canvas.drawLine(dragStartX, dragStartY, endX, endY, previewPaint);
    }

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
                if (dot != null) {
                    playSoundAndVibrate(getContext(), R.raw.sound_dot_clicked, true, 100);
                    startDotRow = dot[0];
                    startDotCol = dot[1];
                    dragStartX = offsetX + startDotCol * spacing;
                    dragStartY = offsetY + startDotRow * spacing;
                    isDragging = true;
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
                    float dx = currentDragX - dragStartX, dy = currentDragY - dragStartY;
                    float distance = (float) Math.sqrt(dx * dx + dy * dy);
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
                            playSoundAndVibrate(getContext(), R.raw.sound_box_complete, true, 50);
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
        }
        return super.onTouchEvent(event);
    }

    private int countClaimedBoxes(int[][] boxes) {
        int count = 0;
        for (int[] row : boxes)
            for (int box : row)
                if (box != 0) count++;
        return count;
    }

    public void updateGridSize(int newGridSize) {
        this.gridSize = newGridSize;
        game = new DotAndBoxesGame(newGridSize);
        if (boardSide > 0) spacing = boardSide / gridSize;
        requestLayout();
        invalidate();
    }

    public void restartGame() {
        game.resetGame();
        invalidate();
    }

    public String getScoreText() {
        return game.getScoreText();
    }

    private float dpToPx(float dp, Context context) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public boolean isGameOver() {
        return game.isGameOver();
    }
}
