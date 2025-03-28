package com.fahadmalik5509.playbox.dotandboxes;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class DotAndBoxesTacticalAI {
    private static final int MAX_DEPTH = 5;
    private static final double CHAIN_POTENTIAL_WEIGHT = 1.8;
    private static final double VULNERABILITY_PENALTY = -2.5;
    private static final double CENTER_CONTROL_WEIGHT = 0.7;

    public static class MoveResult {
        public DotAndBoxesCasualAI.Move move;
        public double evaluation;

        public MoveResult(DotAndBoxesCasualAI.Move move, double evaluation) {
            this.move = move;
            this.evaluation = evaluation;
        }
    }

    public static DotAndBoxesCasualAI.Move chooseAIMove(DotAndBoxesGame game) {
        List<DotAndBoxesCasualAI.Move> moves = getStrategicMoves(game.deepCopy());
        return !moves.isEmpty() ? moves.get(0) : DotAndBoxesCasualAI.chooseAIMove(game);
    }

    private static List<DotAndBoxesCasualAI.Move> getStrategicMoves(DotAndBoxesGame game) {
        List<MoveResult> results = new ArrayList<>();
        ExecutorService executor = Executors.newWorkStealingPool();

        try {
            for (DotAndBoxesCasualAI.Move move : DotAndBoxesCasualAI.getValidMoves(game)) {
                executor.submit(() -> {
                    DotAndBoxesGame sim = game.deepCopy();
                    boolean completed = sim.markLine(move.isHorizontal, move.row, move.col);
                    double eval = evaluatePosition(sim, completed, MAX_DEPTH);
                    synchronized (results) {
                        results.add(new MoveResult(move, eval));
                    }
                });
            }

            executor.shutdown();
            executor.awaitTermination(1500, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            return DotAndBoxesCasualAI.getValidMoves(game);
        }

        Collections.sort(results, (a, b) -> Double.compare(b.evaluation, a.evaluation));
        return results.stream()
                .map(mr -> mr.move)
                .collect(Collectors.toList());
    }

    private static double evaluatePosition(DotAndBoxesGame game, boolean lastMoveCompleted, int depth) {
        if (depth == 0 || game.isGameOver()) {
            return calculatePositionValue(game);
        }

        double evaluation = lastMoveCompleted ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY;
        final int analysisWidth = Math.max(3, 7 - depth); // Properly scoped final variable
        final DotAndBoxesGame finalGame = game.deepCopy(); // Final copy for lambda

        List<DotAndBoxesCasualAI.Move> responses = DotAndBoxesCasualAI.getValidMoves(game).stream()
                .sorted(Comparator.comparingDouble(m ->
                        -movePriority(finalGame, m) // Use final copy in priority calculation
                ))
                .limit(analysisWidth)
                .collect(Collectors.toList());

        for (DotAndBoxesCasualAI.Move response : responses) {
            DotAndBoxesGame sim = finalGame.deepCopy();
            boolean completed = sim.markLine(response.isHorizontal, response.row, response.col);
            double responseEval = evaluatePosition(sim, completed, depth - 1);

            if (lastMoveCompleted) {
                // Maximizing player (AI continues turn)
                evaluation = Math.max(evaluation, responseEval);
            } else {
                // Minimizing player (opponent's turn)
                evaluation = Math.min(evaluation, responseEval);
            }
        }

        return evaluation + calculatePositionValue(game);
    }

    // Supporting method with full implementation
    private static double movePriority(DotAndBoxesGame game, DotAndBoxesCasualAI.Move move) {
        double priority = 0;

        // 1. Immediate box completion check
        if (DotAndBoxesCasualAI.isCompletingMove(game, move)) {
            priority += 150;
        }

        // 2. Safety check
        if (DotAndBoxesCasualAI.isSafeMove(game, move)) {
            priority += 80;
        }

        // 3. Center position preference
        int size = game.getBoxes().length;
        double centerRow = size / 2.0;
        double centerCol = size / 2.0;
        priority += 50 * (1 - (Math.abs(move.row - centerRow) / centerRow));
        priority += 50 * (1 - (Math.abs(move.col - centerCol) / centerCol));

        // 4. Vertical move bias
        if (!move.isHorizontal) {
            priority += 40;
        }

        // 5. Chain potential
        priority += calculateChainPotential(game, move) * 2.5;

        return priority;
    }

    private static double calculateChainPotential(DotAndBoxesGame game, DotAndBoxesCasualAI.Move move) {
        DotAndBoxesGame sim = game.deepCopy();
        sim.markLine(move.isHorizontal, move.row, move.col);
        int chainCount = 0;

        while (true) {
            List<DotAndBoxesCasualAI.Move> completingMoves = DotAndBoxesCasualAI.getValidMoves(sim).stream()
                    .filter(m -> DotAndBoxesCasualAI.isCompletingMove(sim, m))
                    .collect(Collectors.toList());

            if (completingMoves.isEmpty()) break;
            chainCount += completingMoves.size();

            for (DotAndBoxesCasualAI.Move m : completingMoves) {
                sim.markLine(m.isHorizontal, m.row, m.col);
            }
        }

        return chainCount;
    }

    private static double calculatePositionValue(DotAndBoxesGame game) {
        double value = 0;

        // 1. Chain potential analysis
        value += calculateChainPotential(game) * CHAIN_POTENTIAL_WEIGHT;

        // 2. Vulnerability assessment
        value += countVulnerabilities(game) * VULNERABILITY_PENALTY;

        // 3. Center control calculation
        value += calculateCenterControl(game) * CENTER_CONTROL_WEIGHT;

        // 4. Line parity analysis
        value += calculateParityAdvantage(game);

        return value;
    }

    private static double movePriority(DotAndBoxesCasualAI.Move move) {
        // Prioritize vertical moves and center positions
        double priority = 0;
        if (!move.isHorizontal) priority += 2.5;
        priority += (1 - Math.abs(move.row - 3.0)/3.0); // Center row preference
        priority += (1 - Math.abs(move.col - 3.0)/3.0); // Center column preference
        return priority;
    }

    private static int calculateChainPotential(DotAndBoxesGame game) {
        int potential = 0;
        for (int r = 0; r < game.getBoxes().length; r++) {
            for (int c = 0; c < game.getBoxes()[0].length; c++) {
                int lines = game.getBoxLineCount(r, c);
                if (lines == 3) potential += 4; // Immediate completion
                if (lines == 2) potential += 2; // Potential chain starter
            }
        }
        return potential;
    }

    private static int countVulnerabilities(DotAndBoxesGame game) {
        int vulnerabilities = 0;
        for (int r = 0; r < game.getBoxes().length; r++) {
            for (int c = 0; c < game.getBoxes()[0].length; c++) {
                if (game.getBoxLineCount(r, c) == 3) vulnerabilities += 3;
                if (game.getBoxLineCount(r, c) == 2) vulnerabilities += 1;
            }
        }
        return vulnerabilities;
    }

    private static double calculateCenterControl(DotAndBoxesGame game) {
        double control = 0;
        int size = game.getBoxes().length;
        for (int r = size/3; r < 2*size/3; r++) {
            for (int c = size/3; c < 2*size/3; c++) {
                control += 3 - game.getBoxLineCount(r, c); // More control for fewer lines
            }
        }
        return control;
    }

    private static double calculateParityAdvantage(DotAndBoxesGame game) {
        int totalLines = countTotalLines(game);
        int openLines = countOpenLines(game);
        return (openLines % 2 == 0) ? 1.5 : -1.5;
    }

    private static int countOpenLines(DotAndBoxesGame game) {
        int count = 0;
        for (int[] row : game.getHorizontalLines()) {
            for (int line : row) if (line == 0) count++;
        }
        for (int[] row : game.getVerticalLines()) {
            for (int line : row) if (line == 0) count++;
        }
        return count;
    }

    private static int countTotalLines(DotAndBoxesGame game) {
        return game.getHorizontalLines().length * game.getHorizontalLines()[0].length +
                game.getVerticalLines().length * game.getVerticalLines()[0].length;
    }
}