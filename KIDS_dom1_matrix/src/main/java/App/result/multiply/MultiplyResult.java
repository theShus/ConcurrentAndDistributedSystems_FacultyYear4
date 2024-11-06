package App.result.multiply;

import App.matrixData.task.TaskType;
import App.result.Result;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class MultiplyResult implements Result<int[][]> {


    private final String matrixName;
    private final List<Future<SubMultiplyResult>> futureResults;
    private final int rows;
    private final int cols;

    public MultiplyResult(String matrixName, List<Future<SubMultiplyResult>> futureResults, int rows, int cols) {
        this.matrixName = matrixName;
        this.futureResults = futureResults;
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public TaskType getScanType() {
        return TaskType.MULTIPLY;
    }

    @Override
    public int[][] getResult() {
        int[][] fullMatrix = new int[rows][cols];

        for (Future<SubMultiplyResult> future : futureResults) {
            try {
                SubMultiplyResult subResult = future.get();
                int[][] subMatrix = subResult.getSubMatrix();
                int startX = subResult.getCordX();
                int startY = subResult.getCordY();

                // Place the submatrix into the final matrix at the specified coordinates.
                for (int i = 0; i < subMatrix.length; i++) {
                    for (int j = 0; j < subMatrix[i].length; j++) {
                        fullMatrix[startX + i][startY + j] = subMatrix[i][j];
                    }
                }
            } catch (InterruptedException | ExecutionException e) {
                System.err.println("Failed to retrieve submatrix result: " + e.getMessage());
                Thread.currentThread().interrupt(); // handle the interrupt appropriately
            }
        }

        return fullMatrix;
    }

    @Override
    public boolean futureIsDone() {
        for (Future<SubMultiplyResult> future : futureResults) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }


    public String getMatrixName() {
        return matrixName;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
