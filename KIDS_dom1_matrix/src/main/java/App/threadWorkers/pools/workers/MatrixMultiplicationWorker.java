package App.threadWorkers.pools.workers;

import App.result.multiply.SubMultiplyResult;

import java.util.List;
import java.util.concurrent.Callable;


public class MatrixMultiplicationWorker implements Callable<SubMultiplyResult> {

    private final int cordX;
    private final int cordY;
    private final List<int[]> rows;
    private final List<int[]> cols;

    public MatrixMultiplicationWorker(int cordX, int cordY, List<int[]> rows, List<int[]> cols) {
        this.cordX = cordX;
        this.cordY = cordY;
        this.rows = rows;
        this.cols = cols;
    }

    @Override
    public SubMultiplyResult call() {
        int[][] resultMatrix = new int[rows.size()][cols.size()];

        // Iterate through each row
        for (int i = 0; i < rows.size(); i++) {
            int[] currentRow = rows.get(i);

            // Iterate through each column
            for (int j = 0; j < cols.size(); j++) {
                int[] currentCol = cols.get(j);
                int sum = 0;

                // Multiply corresponding elements and add them
                for (int k = 0; k < currentRow.length; k++) {
                    sum += currentRow[k] * currentCol[k];
                }

                // Store the result in the matrix
                resultMatrix[i][j] = sum;
            }
        }
        return new SubMultiplyResult(cordX, cordY, resultMatrix);
    }
}
