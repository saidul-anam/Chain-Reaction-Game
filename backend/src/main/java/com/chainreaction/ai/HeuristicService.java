package com.chainreaction.ai;
import com.chainreaction.model.Board;
import com.chainreaction.model.Cell;
class DefensiveHeuristic extends BaseHeuristicService {
    @Override
    public int calculate(Board board, int aiPlayer) {
        int humanPlayer = (aiPlayer == 1) ? 2 : 1;
        int stabilityScore = 0;
        int safeCells = 0;
        int threatMitigation = 0;
        int controlledCells = 0;

        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.columns; j++) {
                Cell cell = board.getCell(i, j);
                int owner = cell.getOwner();
                int value = cell.getValue();
                int criticalMass = board.getCriticalMass(i, j);

                if (owner == aiPlayer) {
                    // Stabilityscore: AI cells that are not close to exploding.
                    //safecell: how many of AI's cells are safe
                    if (value < criticalMass - 1) {
                        stabilityScore += 3;
                        safeCells++;
                    } else if (value == criticalMass - 1) {
                        stabilityScore -=1;
                    }
                    // Reward cells with friendly neighbors
                    int neighbors = countNeighbors(board, i, j, aiPlayer);
                    stabilityScore += neighbors * 4;
                    controlledCells++;
                } else if (owner == humanPlayer) {
                    // threadMitigation : Rewards if AI cells are next to opponent's unstable cells
                    if (value >= criticalMass - 1) {
                        threatMitigation += (value - criticalMass + 1) * 2;
                    }
                    int aiNeighbors = countNeighbors(board, i, j, aiPlayer);
                    threatMitigation += aiNeighbors * 3;
                }
            }
        }
        return (5 * stabilityScore) + (2 * safeCells) + (4 * threatMitigation) + (3* controlledCells);
    }
}


class AggressiveHeuristic extends BaseHeuristicService {
    @Override
    public int calculate(Board board, int aiPlayer) {
        int humanPlayer = (aiPlayer == 1) ? 2 : 1;
        int orbScore = 0;
        int triggerScore = 0;
        int edgeControl = 0;
        int chainPotential = 0;

        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.columns; j++) {
                Cell cell = board.getCell(i, j);
                int owner = cell.getOwner();
                int value = cell.getValue();
                int criticalMass = board.getCriticalMass(i, j);

                if (owner == aiPlayer) {
                    orbScore += value * 2;
                    // rewarding cells that are to explode
                    if (value >= criticalMass - 1) {
                        triggerScore += (value - criticalMass + 2) * 10;
                    }
                    // rewarding the edge corner cells
                    if (isEdgeOrCorner(i, j, Board.rows, Board.columns)) {
                        edgeControl += 7;
                    }
                    // rewarding cells that can be part of a chain if explode happen
                    int neighbors = countNeighbors(board, i, j, humanPlayer);
                    chainPotential += neighbors * 4;
                } else if (owner == humanPlayer) {
                    orbScore -= value;
                    // benalising if the triggerscore is from another owner
                    if (value < criticalMass - 1) {
                        triggerScore -= 2;
                    }
                }
            }
        }
        return (5 * orbScore) + (10 * triggerScore) + (8 * edgeControl) + (5 * chainPotential);
    }
}

class BalancedHeuristic extends BaseHeuristicService {
    @Override
    public int calculate(Board board, int aiPlayer) {
        int humanPlayer = (aiPlayer == 1) ? 2 : 1;
        int orbScore = 0;
        int mobility = 0;
        int stabilityScore = 0;
        int controlScore = 0;

        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.columns; j++) {
                Cell cell = board.getCell(i, j);
                int owner = cell.getOwner();
                int value = cell.getValue();
                int criticalMass = board.getCriticalMass(i, j);

                if (owner == aiPlayer) {
                    orbScore += value;
                    if (board.isValidMove(i, j, aiPlayer)) {
                        mobility += 3;
                    }
                    if (value == criticalMass - 1) {
                        stabilityScore += 4;
                    } else if (value < criticalMass - 1) {
                        stabilityScore += 2;
                    } else {
                        stabilityScore -= (value - criticalMass) * 2;
                    }
                    if (isEdgeOrCorner(i, j, Board.rows, Board.columns)) {
                        controlScore += 3;
                    }
                } else if (owner == humanPlayer) {
                    orbScore -= value;
                    if (value >= criticalMass - 1) {
                        stabilityScore += (value - criticalMass + 1) * 2;
                    }
                }
            }
        }
        return (2 * orbScore) + (3 * mobility) + (3 * stabilityScore) + (2 * controlScore);
    }
}

class ControlHeuristic extends BaseHeuristicService {
    @Override
    public int calculate(Board board, int aiPlayer) {
        int humanPlayer = (aiPlayer == 1) ? 2 : 1;
        int centerControl = 0;
        int neighborControl = 0;
        int orbScore = 0;
        int mobility = 0;
        int centerRows = Board.rows / 2;
        int centerCols = Board.columns / 2;
        for (int i = 0; i < Board.rows; i++) {
            for (int j = 0; j < Board.columns; j++) {
                Cell cell = board.getCell(i, j);
                int owner = cell.getOwner();
                int value = cell.getValue();
                if (owner == aiPlayer) {
                    orbScore += value;
                    if (board.isValidMove(i, j, aiPlayer)) {
                        mobility += 2;
                    }
                    if (Math.abs(i - centerRows) <= 1 && Math.abs(j - centerCols) <= 1) {
                        centerControl += 20;
                    }
                    int opponentNeighbors = countNeighbors(board, i, j, humanPlayer);
                    neighborControl += opponentNeighbors * 3;
                } else if (owner == humanPlayer) {
                    orbScore -= value;
                    if (Math.abs(i - centerRows) <= 1 && Math.abs(j - centerCols) <= 1) {
                        centerControl -= 3;
                    }
                }
            }
        }
        return (3 * orbScore) + (4 * centerControl) + (10 * neighborControl) + (5 * mobility);
    }
}

public class HeuristicService {
    private final BaseHeuristicService[] heuristics = {
        new DefensiveHeuristic(),
        new AggressiveHeuristic(),
        new BalancedHeuristic(),
        new ControlHeuristic()
    };

    public int calculate(Board board, int aiPlayer, String strategy) {
        switch (strategy.toLowerCase()) {
            case "defensive":
                return heuristics[0].calculate(board, aiPlayer);
            case "aggressive":
                return heuristics[1].calculate(board, aiPlayer);
            case "balanced":
                return heuristics[2].calculate(board, aiPlayer);
            case "control":
                return heuristics[3].calculate(board, aiPlayer);
            default:
                return heuristics[2].calculate(board, aiPlayer);
        }
    }
}