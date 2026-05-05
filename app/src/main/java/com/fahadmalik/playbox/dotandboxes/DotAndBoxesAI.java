package com.fahadmalik.playbox.dotandboxes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI logic for Dots and Boxes.
 *
 * Replaces the old DotAndBoxesCasualAI + DotAndBoxesTacticalAI duo — those two
 * classes were 90 % identical. The difference is now captured by a single
 * {@code tactical} flag passed to {@link #chooseMove}.
 *
 * Casual  – takes any completing move immediately; otherwise picks a random safe move.
 * Tactical – avoids handing the opponent a two-box chain when a safe alternative exists.
 */
public class DotAndBoxesAI {

    // ── Public data types ────────────────────────────────────────────────────────

    public static class Move {
        public final boolean isHorizontal;
        public final int row, col;

        public Move(boolean isHorizontal, int row, int col) {
            this.isHorizontal = isHorizontal;
            this.row  = row;
            this.col  = col;
        }
    }

    // ── Entry point ──────────────────────────────────────────────────────────────

    /** Selects the best AI move for the current game state. */
    public static Move chooseMove(DotAndBoxesGame game, boolean tactical) {
        List<Move> valid = getValidMoves(game);
        if (valid.isEmpty()) return null;
        Collections.shuffle(valid);

        List<Move> completing = valid.stream()
                .filter(m -> isCompletingMove(game, m))
                .collect(Collectors.toList());

        // ── Completing moves available ───────────────────────────────────────────
        if (!completing.isEmpty()) {
            if (tactical) {
                // Prefer chains of 3+; settle for shorter chains only if no safe moves exist
                List<Move> longChain = completing.stream()
                        .filter(m -> evaluateDamage(game, m) >= 3)
                        .collect(Collectors.toList());
                if (!longChain.isEmpty()) {
                    Collections.shuffle(longChain);
                    return longChain.get(0);
                }
                // Two-box chains: prefer a safe non-completing move if one exists
                List<Move> safe = valid.stream()
                        .filter(m -> isSafeMove(game, m))
                        .collect(Collectors.toList());
                if (!safe.isEmpty()) {
                    Collections.shuffle(safe);
                    return safe.get(0);
                }
            }
            // Casual (or tactical with no better option): take any completing move
            Collections.shuffle(completing);
            return completing.get(0);
        }

        // ── No completing moves ──────────────────────────────────────────────────
        List<Move> safe = valid.stream()
                .filter(m -> isSafeMove(game, m))
                .collect(Collectors.toList());
        if (!safe.isEmpty()) {
            Collections.shuffle(safe);
            return safe.get(0);
        }

        // Forced: pick the move that gives the opponent the shortest chain
        return leastDamageMove(valid, game);
    }

    // ── Move evaluation helpers ──────────────────────────────────────────────────

    public static List<Move> getValidMoves(DotAndBoxesGame game) {
        List<Move> moves = new ArrayList<>();
        int[][] hLines = game.getHorizontalLines();
        int[][] vLines = game.getVerticalLines();

        for (int r = 0; r < hLines.length; r++)
            for (int c = 0; c < hLines[r].length; c++)
                if (hLines[r][c] == 0) moves.add(new Move(true, r, c));

        for (int r = 0; r < vLines.length; r++)
            for (int c = 0; c < vLines[r].length; c++)
                if (vLines[r][c] == 0) moves.add(new Move(false, r, c));

        return moves;
    }

    /** Returns true if playing this move would immediately complete at least one box. */
    public static boolean isCompletingMove(DotAndBoxesGame game, Move move) {
        if (move.isHorizontal) {
            if (move.row > 0 && game.getBoxLineCount(move.row - 1, move.col) == 3) return true;
            return move.row < game.getBoxes().length && game.getBoxLineCount(move.row, move.col) == 3;
        } else {
            if (move.col > 0 && game.getBoxLineCount(move.row, move.col - 1) == 3) return true;
            return move.col < game.getVerticalLines()[0].length - 1
                    && game.getBoxLineCount(move.row, move.col) == 3;
        }
    }

    /**
     * Returns true if playing this move does NOT leave any adjacent box with exactly
     * three sides drawn (i.e. does not hand an easy box to the opponent).
     */
    public static boolean isSafeMove(DotAndBoxesGame game, Move move) {
        if (move.isHorizontal) {
            if (move.row > 0 && game.getBoxLineCount(move.row - 1, move.col) == 2) return false;
            return move.row >= game.getBoxes().length
                    || game.getBoxLineCount(move.row, move.col) != 2;
        } else {
            if (move.col > 0 && game.getBoxLineCount(move.row, move.col - 1) == 2) return false;
            return move.col >= game.getVerticalLines()[0].length - 1
                    || game.getBoxLineCount(move.row, move.col) != 2;
        }
    }

    /**
     * Simulates the move and returns the number of boxes an opponent could collect
     * in the resulting chain reaction (higher = worse for us).
     */
    public static int evaluateDamage(DotAndBoxesGame game, Move move) {
        DotAndBoxesGame sim = game.deepCopy();
        sim.markLine(move.isHorizontal, move.row, move.col);
        return simulateChain(sim);
    }

    // ── Private helpers ──────────────────────────────────────────────────────────

    private static int simulateChain(DotAndBoxesGame sim) {
        int count = 0;
        boolean progress = true;
        while (progress) {
            progress = false;
            List<Move> completing = getValidMoves(sim).stream()
                    .filter(m -> isCompletingMove(sim, m))
                    .collect(Collectors.toList());
            for (Move m : completing) {
                sim.markLine(m.isHorizontal, m.row, m.col);
                count++;
                progress = true;
            }
        }
        return count;
    }

    private static Move leastDamageMove(List<Move> moves, DotAndBoxesGame game) {
        int minDamage = Integer.MAX_VALUE;
        List<Move> best = new ArrayList<>();
        for (Move m : moves) {
            int d = evaluateDamage(game, m);
            if (d < minDamage) { best.clear(); minDamage = d; }
            if (d == minDamage) best.add(m);
        }
        Collections.shuffle(best);
        return best.get(0);
    }
}
