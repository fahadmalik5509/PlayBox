package com.fahadmalik5509.playbox.dotandboxes;

public class DotAndBoxesGame {
    private final int gridSize;  // boxes per row (dots count = gridSize + 1)
    private int[][] horizontalLines; // (gridSize+1) x gridSize
    private int[][] verticalLines;   // gridSize x (gridSize+1)
    private int[][] boxes;           // gridSize x gridSize
    private boolean playerOneTurn;

    public DotAndBoxesGame(int gridSize) {
        this.gridSize = gridSize;
        resetGame();
    }

    public void resetGame() {
        horizontalLines = new int[gridSize + 1][gridSize];
        verticalLines = new int[gridSize][gridSize + 1];
        boxes = new int[gridSize][gridSize];
        playerOneTurn = true;
    }

    /**
     * Marks a line (horizontal/vertical) using the current player's number.
     * Returns false if the line was already drawn.
     * Switches turn if no box is completed.
     */
    public boolean markLine(boolean horizontal, int row, int col) {
        int currentPlayer = playerOneTurn ? 1 : 2;
        if (horizontal) {
            if (horizontalLines[row][col] != 0) return false;
            horizontalLines[row][col] = currentPlayer;
        } else {
            if (verticalLines[row][col] != 0) return false;
            verticalLines[row][col] = currentPlayer;
        }
        if (!checkForCompletedBoxes()) {
            playerOneTurn = !playerOneTurn;
        }
        return true;
    }

    /**
     * Claims any box whose four sides are complete.
     * Returns true if at least one new box was claimed.
     */
    public boolean checkForCompletedBoxes() {
        boolean anyCompleted = false;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 0 && getBoxLineCount(r, c) == 4) {
                    boxes[r][c] = playerOneTurn ? 1 : 2;
                    anyCompleted = true;
                }
            }
        }
        return anyCompleted;
    }

    /**
     * Helper method: returns the count of marked sides for the box at (r, c).
     */
    public int getBoxLineCount(int r, int c) {
        int count = 0;
        if (horizontalLines[r][c] != 0) count++;
        if (horizontalLines[r+1][c] != 0) count++;
        if (verticalLines[r][c] != 0) count++;
        if (verticalLines[r][c+1] != 0) count++;
        return count;
    }

    public DotAndBoxesGame deepCopy() {
        DotAndBoxesGame copy = new DotAndBoxesGame(this.gridSize);
        copy.horizontalLines = deepCopy2DArray(this.horizontalLines);
        copy.verticalLines = deepCopy2DArray(this.verticalLines);
        copy.boxes = deepCopy2DArray(this.boxes);
        copy.playerOneTurn = this.playerOneTurn;
        return copy;
    }

    private int[][] deepCopy2DArray(int[][] array) {
        if (array == null) return null;
        int[][] copy = new int[array.length][];
        for (int i = 0; i < array.length; i++) {
            copy[i] = array[i].clone();
        }
        return copy;
    }

    public int[] getScores() {
        int score1 = 0, score2 = 0;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 1) score1++;
                else if (boxes[r][c] == 2) score2++;
            }
        }
        return new int[]{score1, score2};
    }

    public boolean isGameOver() {
        for (int[] row : boxes) {
            for (int box : row) {
                if (box == 0) return false;
            }
        }
        return true;
    }

    /**
     * Returns the total count of claimed boxes.
     */
    public int getClaimedBoxesCount() {
        int[] scores = getScores();
        return scores[0] + scores[1];
    }

    public int[][] getHorizontalLines() { return horizontalLines; }
    public int[][] getVerticalLines() { return verticalLines; }
    public int[][] getBoxes() { return boxes; }
    public boolean isPlayerOneTurn() { return playerOneTurn; }
}