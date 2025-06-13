package com.chainreaction.ai;

import com.chainreaction.model.Board;
import com.chainreaction.model.Cell;

public abstract class BaseHeuristicService {
    protected boolean isEdgeOrCorner(int i, int j, int rows, int columns) {
        return i == 0 || i == rows - 1 || j == 0 || j == columns - 1;
    }

    protected int countNeighbors(Board board, int row, int col, int player) {
        int count = 0;
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
        for (int[] dir : directions) {
            int newRow = row + dir[0];
            int newCol = col + dir[1];
            if (newRow >= 0 && newRow < Board.rows && newCol >= 0 && newCol < Board.columns) {
                Cell cell = board.getCell(newRow, newCol);
                if (cell.getOwner() == player) count++;
            }
        }
        return count;
    }

    public abstract int calculate(Board board, int aiPlayer);
}