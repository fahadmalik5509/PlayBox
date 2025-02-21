package com.fahadmalik5509.playbox.tictactoe;

import static com.fahadmalik5509.playbox.miscellaneous.ActivityUtils.getRandomNumber;

public class TicTacToeLogic {

    private final char[] gameBoard = { '0', '1', '2', '3', '4', '5', '6', '7', '8' };
    private int playerOneScore = 0, playerTwoScore = 0;
    private boolean gameWon = false, gameDraw = false;
    private char playerSymbol, aiSymbol;
    protected int winA, winB, winC;

    private final int[][] WINNING_COMBINATIONS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    protected void updateGameBoardAndState(int index, char symbol) {
        gameBoard[index] = symbol;
        checkGameState();
    }

    private void checkGameState() {
        checkAllLines();
        if(isBoardFull()) gameDraw = true;
    }

    private void checkAllLines() {
        for (int[] combo : WINNING_COMBINATIONS)
            checkLine(combo[0], combo[1], combo[2]);
    }

    private void checkLine(int a, int b, int c) {
        if (gameBoard[a] == gameBoard[b] && gameBoard[b] == gameBoard[c]) {
            winA = a;
            winB = b;
            winC = c;
            hasWon();
        }
    }

    private boolean isBoardFull() {
        for (char cell : gameBoard) {
            if (cell != 'X' && cell != 'O') {
                return false;
            }
        }
        return true;
    }

    private void hasWon() {
        if (gameBoard[winA] == 'X') playerOneScore++;
        else playerTwoScore++;
        gameWon = true;
    }

    protected void resetGameLogic() {
        gameWon = false;
        gameDraw = false;
        resetBoard();
    }

    private void resetBoard() {
        for (int i = 0; i < gameBoard.length; i++) {
            gameBoard[i] = (char) ('0' + i);
        }
    }

    protected int getAIMove(int difficulty,char playerSymbol,char aiSymbol) {
        this.playerSymbol = playerSymbol;
        this.aiSymbol = aiSymbol;
        return switch (difficulty) {
            case 2 -> getMediumMove();
            case 3 -> getHardMove();
            default -> getEasyMove();
        };
    }

    private int getEasyMove() {
        int randomNum;
        do {
            randomNum = getRandomNumber(0, 8);
        } while (gameBoard[randomNum] == 'X' || gameBoard[randomNum] == 'O');
        return randomNum;
    }

    private int getMediumMove() {
        int move;
        // First try to win
        for (int[] pattern : WINNING_COMBINATIONS) {
            move = checkLineForMove(pattern[0], pattern[1], pattern[2], 'O');
            if (move != -1) return move;
        }
        // Then try to block the opponent
        for (int[] pattern : WINNING_COMBINATIONS) {
            move = checkLineForMove(pattern[0], pattern[1], pattern[2], 'X');
            if (move != -1) return move;
        }
        return getEasyMove();
    }

    private int checkLineForMove(int a, int b, int c, char player) {
        if (gameBoard[a] == player && gameBoard[b] == player && gameBoard[c] != 'X' && gameBoard[c] != 'O')
            return c;
        if (gameBoard[a] == player && gameBoard[c] == player && gameBoard[b] != 'X' && gameBoard[b] != 'O')
            return b;
        if (gameBoard[b] == player && gameBoard[c] == player && gameBoard[a] != 'X' && gameBoard[a] != 'O')
            return a;
        return -1;
    }

    private int getHardMove() {

        int bestVal = Integer.MIN_VALUE, bestMove = -1;

        for (int i = 0; i < 9; i++) {
            if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                gameBoard[i] = aiSymbol;
                int moveVal = minimax(false, Integer.MIN_VALUE, Integer.MAX_VALUE);
                gameBoard[i] = (char) ('0' + i);
                if (moveVal > bestVal) {
                    bestMove = i;
                    bestVal = moveVal;
                }
            }
        }
        return bestMove;
    }

    private int minimax(boolean isMax, int alpha, int beta) {
        int score = evaluate();
        if (score == 10 || score == -10) return score;
        if (isBoardFull()) return 0;

        int best;
        if (isMax) {
            best = Integer.MIN_VALUE;
            for (int i = 0; i < 9; i++) {
                if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                    gameBoard[i] = aiSymbol;
                    best = Math.max(best, minimax(false, alpha, beta));
                    gameBoard[i] = (char) ('0' + i);
                    alpha = Math.max(alpha, best);
                    if (beta <= alpha) break;
                }
            }
        } else {
            best = Integer.MAX_VALUE;
            for (int i = 0; i < 9; i++) {
                if (gameBoard[i] != 'X' && gameBoard[i] != 'O') {
                    gameBoard[i] = playerSymbol;
                    best = Math.min(best, minimax(true, alpha, beta));
                    gameBoard[i] = (char) ('0' + i);
                    beta = Math.min(beta, best);
                    if (beta <= alpha) break;
                }
            }
        }
        return best;
    }

    private int evaluate() {
        for (int[] combo : WINNING_COMBINATIONS) {
            if (checkLine(combo[0], combo[1], combo[2], aiSymbol)) return 10;
            if (checkLine(combo[0], combo[1], combo[2], playerSymbol)) return -10;
        }
        return 0;
    }

    private boolean checkLine(int a, int b, int c, char symbol) {
        return gameBoard[a] == symbol && gameBoard[b] == symbol && gameBoard[c] == symbol;
    }

    public int getPlayerOneScore() {
        return playerOneScore;
    }
    public int getPlayerTwoScore() {
        return playerTwoScore;
    }

    public boolean getHasWon() {
        return gameWon;
    }

    public boolean getHasDraw() {
        return gameDraw;
    }


}
