package com.fahadmalik.playbox.dotandboxes;

import java.util.HashMap;
import java.util.Map;

/**
 * Core game logic for Dots and Boxes.
 * Tracks horizontal/vertical lines, box ownership, and turn state.
 * Also supports Firebase serialization for online multiplayer.
 */
public class DotAndBoxesGame {

    private final int gridSize;
    private int[][] horizontalLines; // (gridSize+1) rows × gridSize cols
    private int[][] verticalLines;   // gridSize rows × (gridSize+1) cols
    private int[][] boxes;           // gridSize × gridSize, value = 0 (empty), 1, or 2
    private boolean playerOneTurn;

    public DotAndBoxesGame(int gridSize) {
        this.gridSize = gridSize;
        resetGame();
    }

    // ── Game Actions ────────────────────────────────────────────────────────────

    public void resetGame() {
        horizontalLines = new int[gridSize + 1][gridSize];
        verticalLines   = new int[gridSize][gridSize + 1];
        boxes           = new int[gridSize][gridSize];
        playerOneTurn   = true;
    }

    /**
     * Attempts to mark the given line for the current player.
     * Returns false if the line is already drawn.
     * Switches turn only when no new box is completed.
     */
    public boolean markLine(boolean horizontal, int row, int col) {
        int player = playerOneTurn ? 1 : 2;
        if (horizontal) {
            if (horizontalLines[row][col] != 0) return false;
            horizontalLines[row][col] = player;
        } else {
            if (verticalLines[row][col] != 0) return false;
            verticalLines[row][col] = player;
        }
        if (!claimCompletedBoxes()) {
            playerOneTurn = !playerOneTurn;
        }
        return true;
    }

    /** Claims any newly completed boxes. Returns true if at least one was claimed. */
    public boolean claimCompletedBoxes() {
        boolean anyCompleted = false;
        int player = playerOneTurn ? 1 : 2;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 0 && getBoxLineCount(r, c) == 4) {
                    boxes[r][c] = player;
                    anyCompleted = true;
                }
            }
        }
        return anyCompleted;
    }

    /** Returns how many of the four sides of box (r, c) are already drawn. */
    public int getBoxLineCount(int r, int c) {
        int count = 0;
        if (horizontalLines[r][c]     != 0) count++;
        if (horizontalLines[r + 1][c] != 0) count++;
        if (verticalLines[r][c]       != 0) count++;
        if (verticalLines[r][c + 1]   != 0) count++;
        return count;
    }

    // ── Queries ─────────────────────────────────────────────────────────────────

    public int[] getScores() {
        int s1 = 0, s2 = 0;
        for (int[] row : boxes)
            for (int box : row)
                if (box == 1) s1++; else if (box == 2) s2++;
        return new int[]{s1, s2};
    }

    public int getClaimedBoxesCount() {
        int[] s = getScores();
        return s[0] + s[1];
    }

    public boolean isGameOver() {
        for (int[] row : boxes)
            for (int box : row)
                if (box == 0) return false;
        return true;
    }

    public DotAndBoxesGame deepCopy() {
        DotAndBoxesGame copy = new DotAndBoxesGame(gridSize);
        copy.horizontalLines = copy2D(horizontalLines);
        copy.verticalLines   = copy2D(verticalLines);
        copy.boxes           = copy2D(boxes);
        copy.playerOneTurn   = playerOneTurn;
        return copy;
    }

    private static int[][] copy2D(int[][] src) {
        int[][] dst = new int[src.length][];
        for (int i = 0; i < src.length; i++) dst[i] = src[i].clone();
        return dst;
    }

    // ── Firebase Serialization ───────────────────────────────────────────────────

    /**
     * Serializes the mutable game state into a flat Map suitable for Firebase.
     * gridSize is not included here; it is stored separately in the room record.
     */
    public Map<String, Object> toFirebaseMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("currentPlayer", playerOneTurn ? 1 : 2);
        map.put("hLines", flatten(horizontalLines));
        map.put("vLines", flatten(verticalLines));
        map.put("boxes",  flatten(boxes));
        return map;
    }

    /**
     * Overwrites this game's mutable state with values received from Firebase.
     * Call this only when the Firebase snapshot is for the same gridSize.
     */
    public void applyFirebaseState(int currentPlayer, String hLines, String vLines, String boxes) {
        this.playerOneTurn   = (currentPlayer == 1);
        this.horizontalLines = unflatten(hLines, gridSize + 1, gridSize);
        this.verticalLines   = unflatten(vLines, gridSize,     gridSize + 1);
        this.boxes           = unflatten(boxes,  gridSize,     gridSize);
    }

    private static String flatten(int[][] arr) {
        StringBuilder sb = new StringBuilder();
        for (int[] row : arr)
            for (int v : row)
                sb.append(v).append(',');
        if (sb.length() > 0) sb.setLength(sb.length() - 1); // trim trailing comma
        return sb.toString();
    }

    private static int[][] unflatten(String s, int rows, int cols) {
        int[][] arr = new int[rows][cols];
        if (s == null || s.isEmpty()) return arr;
        String[] parts = s.split(",");
        int idx = 0;
        outer:
        for (int r = 0; r < rows; r++)
            for (int c = 0; c < cols; c++) {
                if (idx >= parts.length) break outer;
                arr[r][c] = Integer.parseInt(parts[idx++]);
            }
        return arr;
    }

    // ── Getters ──────────────────────────────────────────────────────────────────

    public int[][]  getHorizontalLines() { return horizontalLines; }
    public int[][]  getVerticalLines()   { return verticalLines; }
    public int[][]  getBoxes()           { return boxes; }
    public boolean  isPlayerOneTurn()    { return playerOneTurn; }
    public int      getGridSize()        { return gridSize; }
}
