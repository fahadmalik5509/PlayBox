package com.fahadmalik5509.playbox.dotandboxes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DotAndBoxesAI {

    // Simple representation of a move.
    public static class Move {
        public boolean isHorizontal;
        public int row, col;

        public Move(boolean isHorizontal, int row, int col) {
            this.isHorizontal = isHorizontal;
            this.row = row;
            this.col = col;
        }
    }

    // Get a list of all valid moves for the given game state.
    public static List<Move> getValidMoves(DotAndBoxesGame game) {
        List<Move> moves = new ArrayList<>();
        int[][] hLines = game.getHorizontalLines();
        int[][] vLines = game.getVerticalLines();

        // Horizontal moves.
        for (int r = 0; r < hLines.length; r++) {
            for (int c = 0; c < hLines[r].length; c++) {
                if (hLines[r][c] == 0) {
                    moves.add(new Move(true, r, c));
                }
            }
        }
        // Vertical moves.
        for (int r = 0; r < vLines.length; r++) {
            for (int c = 0; c < vLines[r].length; c++) {
                if (vLines[r][c] == 0) {
                    moves.add(new Move(false, r, c));
                }
            }
        }
        return moves;
    }

    // Check if a move immediately completes a box.
    public static boolean isCompletingMove(DotAndBoxesGame game, Move move) {
        if (move.isHorizontal) {
            // Check the box above.
            if (move.row > 0) {
                int count = 0;
                if (game.getHorizontalLines()[move.row - 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row - 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row - 1][move.col + 1] != 0) count++;
                if (count == 3) return true;
            }
            // Check the box below.
            if (move.row < game.getBoxes().length) {
                int count = 0;
                if (game.getHorizontalLines()[move.row + 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row][move.col + 1] != 0) count++;
                return count == 3;
            }
        } else { // Vertical move.
            // Check the box to the left.
            if (move.col > 0) {
                int count = 0;
                if (game.getVerticalLines()[move.row][move.col - 1] != 0) count++;
                if (game.getHorizontalLines()[move.row][move.col - 1] != 0) count++;
                if (game.getHorizontalLines()[move.row + 1][move.col - 1] != 0) count++;
                if (count == 3) return true;
            }
            // Check the box to the right.
            if (move.col < game.getVerticalLines()[0].length - 1) {
                int count = 0;
                if (game.getVerticalLines()[move.row][move.col + 1] != 0) count++;
                if (game.getHorizontalLines()[move.row][move.col] != 0) count++;
                if (game.getHorizontalLines()[move.row + 1][move.col] != 0) count++;
                return count == 3;
            }
        }
        return false;
    }

    // Returns true if a move is considered safe (it does not create a dangerous 3-sided box).
    public static boolean isSafeMove(DotAndBoxesGame game, Move move) {
        if (move.isHorizontal) {
            // Check box above.
            if (move.row > 0) {
                int count = 0;
                if (game.getHorizontalLines()[move.row - 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row - 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row - 1][move.col + 1] != 0) count++;
                if (count == 2) return false;
            }
            // Check box below.
            if (move.row < game.getBoxes().length) {
                int count = 0;
                if (game.getHorizontalLines()[move.row + 1][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row][move.col] != 0) count++;
                if (game.getVerticalLines()[move.row][move.col + 1] != 0) count++;
                return count != 2;
            }
        } else { // Vertical
            // Check box to the left.
            if (move.col > 0) {
                int count = 0;
                if (game.getVerticalLines()[move.row][move.col - 1] != 0) count++;
                if (game.getHorizontalLines()[move.row][move.col - 1] != 0) count++;
                if (game.getHorizontalLines()[move.row + 1][move.col - 1] != 0) count++;
                if (count == 2) return false;
            }
            // Check box to the right.
            if (move.col < game.getVerticalLines()[0].length - 1) {
                int count = 0;
                if (game.getVerticalLines()[move.row][move.col + 1] != 0) count++;
                if (game.getHorizontalLines()[move.row][move.col] != 0) count++;
                if (game.getHorizontalLines()[move.row + 1][move.col] != 0) count++;
                return count != 2;
            }
        }
        return true;
    }

    // Evaluates the damage (chain reaction) caused by a move.
    public static int evaluateDamage(DotAndBoxesGame game, Move move) {
        // Assume game.deepCopy() returns a deep copy of the game state.
        DotAndBoxesGame simulatedGame = game.deepCopy();
        simulatedGame.markLine(move.isHorizontal, move.row, move.col);
        return simulateChainReaction(simulatedGame);
    }

    // Recursively simulates completing moves and returns the chain length.
    private static int simulateChainReaction(DotAndBoxesGame simulatedGame) {
        int chainCount = 0;
        boolean moveMade = true;
        while (moveMade) {
            moveMade = false;
            List<Move> completingMoves = new ArrayList<>();
            for (Move m : getValidMoves(simulatedGame)) {
                if (isCompletingMove(simulatedGame, m)) {
                    completingMoves.add(m);
                }
            }
            if (!completingMoves.isEmpty()) {
                for (Move m : completingMoves) {
                    simulatedGame.markLine(m.isHorizontal, m.row, m.col);
                    chainCount++;
                }
                moveMade = true;
            }
        }
        return chainCount;
    }

    // Main AI method to choose a move based on valid moves, completing moves, safe moves, or minimal damage.
    public static Move chooseAIMove(DotAndBoxesGame game) {
        List<Move> validMoves = getValidMoves(game);
        if (validMoves.isEmpty()) return null;

        Collections.shuffle(validMoves);
        Move chosenMove;

        // 1. Look for moves that immediately complete a box.
        List<Move> completingMoves = new ArrayList<>();
        for (Move move : validMoves) {
            if (isCompletingMove(game, move)) {
                completingMoves.add(move);
            }
        }
        if (!completingMoves.isEmpty()) {
            Collections.shuffle(completingMoves);
            chosenMove = completingMoves.get(0);
        } else {
            // 2. Look for safe moves.
            List<Move> safeMoves = new ArrayList<>();
            for (Move move : validMoves) {
                if (isSafeMove(game, move)) {
                    safeMoves.add(move);
                }
            }
            if (!safeMoves.isEmpty()) {
                Collections.shuffle(safeMoves);
                chosenMove = safeMoves.get(0);
            } else {
                // 3. Forced scenario: choose the move that minimizes damage.
                int minDamage = Integer.MAX_VALUE;
                List<Move> candidateMoves = new ArrayList<>();
                for (Move move : validMoves) {
                    int damage = evaluateDamage(game, move);
                    if (damage < minDamage) {
                        candidateMoves.clear();
                        candidateMoves.add(move);
                        minDamage = damage;
                    } else if (damage == minDamage) {
                        candidateMoves.add(move);
                    }
                }
                Collections.shuffle(candidateMoves);
                chosenMove = candidateMoves.get(0);
            }
        }
        return chosenMove;
    }
}
