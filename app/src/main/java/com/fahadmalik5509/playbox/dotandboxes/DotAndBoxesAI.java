package com.fahadmalik5509.playbox.dotandboxes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    // Evaluates the damage (chain reaction) caused by a move.
    public static int evaluateDamage(DotAndBoxesGame game, Move move) {
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

    // Main AI method to choose a move.
    public static Move chooseAIMove(DotAndBoxesGame game) {
        List<Move> validMoves = getValidMoves(game);
        if (validMoves.isEmpty()) return null;

        Collections.shuffle(validMoves);

        // 1. Look for moves that immediately complete a box.
        List<Move> completingMoves = validMoves.stream()
                .filter(m -> isCompletingMove(game, m))
                .collect(Collectors.toList());
        if (!completingMoves.isEmpty()) {
            Collections.shuffle(completingMoves);
            return completingMoves.get(0);
        }

        // 2. Look for safe moves.
        List<Move> safeMoves = validMoves.stream()
                .filter(m -> isSafeMove(game, m))
                .collect(Collectors.toList());
        if (!safeMoves.isEmpty()) {
            Collections.shuffle(safeMoves);
            return safeMoves.get(0);
        }

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
        return candidateMoves.get(0);
    }
}