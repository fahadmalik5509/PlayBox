package com.fahadmalik5509.playbox.dotandboxes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class DotAndBoxesTacticalAI {

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
            if (move.row > 0 && game.getBoxLineCount(move.row - 1, move.col) == 3) return true;
            // Check the box below.
            return move.row < game.getBoxes().length && game.getBoxLineCount(move.row, move.col) == 3;
        } else { // Vertical move.
            // Check the box to the left.
            if (move.col > 0 && game.getBoxLineCount(move.row, move.col - 1) == 3) return true;
            // Check the box to the right.
            return move.col < game.getVerticalLines()[0].length - 1 && game.getBoxLineCount(move.row, move.col) == 3;
        }
    }

    // Returns true if a move is considered safe (it does not create a dangerous 3-sided box).
    public static boolean isSafeMove(DotAndBoxesGame game, Move move) {
        if (move.isHorizontal) {
            if (move.row > 0 && game.getBoxLineCount(move.row - 1, move.col) == 2) return false;
            return move.row >= game.getBoxes().length || game.getBoxLineCount(move.row, move.col) != 2;
        } else { // Vertical move.
            if (move.col > 0 && game.getBoxLineCount(move.row, move.col - 1) == 2) return false;
            return move.col >= game.getVerticalLines()[0].length - 1 || game.getBoxLineCount(move.row, move.col) != 2;
        }
    }

    // Evaluates the damage (chain reaction length) caused by a move.
    // A higher chain count means that more boxes will be auto-completed
    // (which might be bad if it gives the opponent an opportunity).
    public static int evaluateDamage(DotAndBoxesGame game, Move move) {
        DotAndBoxesGame simulatedGame = game.deepCopy();
        simulatedGame.markLine(move.isHorizontal, move.row, move.col);
        return simulateChainReaction(simulatedGame);
    }

    // Recursively simulates the chain reaction after a move.
    // Returns the number of boxes that would be auto-completed.
    private static int simulateChainReaction(DotAndBoxesGame simulatedGame) {
        int chainCount = 0;
        boolean moveMade = true;
        while (moveMade) {
            moveMade = false;
            // Find all moves that would complete a box.
            List<Move> completingMoves = getValidMoves(simulatedGame).stream()
                    .filter(m -> isCompletingMove(simulatedGame, m))
                    .collect(Collectors.toList());
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

    // Main tactical AI method to choose a move.
    // It avoids taking a two-box chain move if there are safe alternatives.
    public static Move chooseAIMove(DotAndBoxesGame game) {
        List<Move> validMoves = getValidMoves(game);
        if (validMoves.isEmpty()) return null;

        // Shuffle valid moves to avoid deterministic behavior.
        Collections.shuffle(validMoves);

        // First, gather all moves that immediately complete a box.
        List<Move> completingMoves = validMoves.stream()
                .filter(m -> isCompletingMove(game, m))
                .collect(Collectors.toList());

        // Separate completing moves by chain length outcome.
        List<Move> chainMoreThanTwo = new ArrayList<>();
        List<Move> chainExactlyTwo = new ArrayList<>();

        for (Move m : completingMoves) {
            int chainLength = evaluateDamage(game, m);
            if (chainLength >= 3) {
                chainMoreThanTwo.add(m);
            } else if (chainLength == 2) {
                chainExactlyTwo.add(m);
            }
        }

        // If there is at least one move that gives a larger chain (>=3), take it.
        if (!chainMoreThanTwo.isEmpty()) {
            Collections.shuffle(chainMoreThanTwo);
            return chainMoreThanTwo.get(0);
        }

        // If only two-box chain moves are available, check if a safe, non-completing move exists.
        List<Move> safeMoves = validMoves.stream()
                .filter(m -> isSafeMove(game, m))
                .collect(Collectors.toList());
        if (!safeMoves.isEmpty()) {
            // Tactical decision: avoid taking the two-box chain,
            // so choose a safe move instead.
            Collections.shuffle(safeMoves);
            return safeMoves.get(0);
        }

        // If no safe moves exist, but there are completing moves (even if two-box),
        // choose the one that minimizes damage (chain length).
        if (!completingMoves.isEmpty()) {
            int minDamage = Integer.MAX_VALUE;
            List<Move> candidateMoves = new ArrayList<>();
            for (Move move : completingMoves) {
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
            return candidateMoves.get(0);
        }

        // If no completing moves exist, simply return a safe move (or any valid move).
        return safeMoves.isEmpty() ? validMoves.get(0) : safeMoves.get(0);
    }
}
