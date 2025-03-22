package com.fahadmalik5509.playbox.dotandboxes;

public class DotAndBoxesGame {
    private final int gridSize;  // number of boxes per row (dots count = gridSize + 1)
    private int[][] horizontalLines; // dimensions: (gridSize+1) x gridSize; 0 = not drawn, 1 = Player 1, 2 = Player 2
    private int[][] verticalLines;   // dimensions: gridSize x (gridSize+1)
    private int[][] boxes;           // dimensions: gridSize x gridSize; 0 = unclaimed, 1 = Player 1, 2 = Player 2
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
     * Marks a line (horizontal or vertical) at the given row and col using the current player's number.
     * If the line is already drawn, returns false.
     * After marking, checks for any completed boxes.
     * If no box was completed, toggles the turn.
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
        boolean boxCompleted = checkForCompletedBoxes();
        // If no box was completed, switch turn.
        if (!boxCompleted) {

            playerOneTurn = !playerOneTurn;
        }
        return true;
    }

    /**
     * Checks every box on the board. If all four surrounding lines of an unclaimed box
     * are drawn, the box is claimed by the current player.
     * Returns true if at least one box was completed by this move.
     */
    public boolean checkForCompletedBoxes() {
        boolean anyCompleted = false;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 0 &&
                        horizontalLines[r][c] != 0 &&
                        horizontalLines[r + 1][c] != 0 &&
                        verticalLines[r][c] != 0 &&
                        verticalLines[r][c + 1] != 0) {
                    boxes[r][c] = playerOneTurn ? 1 : 2;
                    anyCompleted = true;
                }
            }
        }
        return anyCompleted;
    }

    public int[][] getHorizontalLines() {
        return horizontalLines;
    }

    public int[][] getVerticalLines() {
        return verticalLines;
    }

    public int[][] getBoxes() {
        return boxes;
    }

    public boolean isPlayerOneTurn() {
        return playerOneTurn;
    }

    /**
     * Builds a score string by counting the claimed boxes for each player.
     */
    public String getScoreText() {
        int score1 = 0, score2 = 0;
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 1) score1++;
                else if (boxes[r][c] == 2) score2++;
            }
        }
        return "Player 1: " + score1 + "  |  Player 2: " + score2;
    }

    public boolean isGameOver() {
        for (int r = 0; r < gridSize; r++) {
            for (int c = 0; c < gridSize; c++) {
                if (boxes[r][c] == 0) return false;
            }
        }
        return true;
    }
}
