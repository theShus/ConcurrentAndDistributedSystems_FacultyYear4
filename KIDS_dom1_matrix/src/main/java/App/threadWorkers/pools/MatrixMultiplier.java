package App.threadWorkers.pools;


import App.App;
import App.PropertyStorage;
import App.matrixData.MatrixData;
import App.matrixData.task.MultiplyTask;
import App.result.multiply.MultiplyResult;
import App.result.multiply.SubMultiplyResult;
import App.threadWorkers.pools.workers.MatrixMultiplicationWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixMultiplier {

    private final ExecutorService threadPool;
    private final ExecutorCompletionService<SubMultiplyResult> completionService;
    private static int MAXIMUM_ROWS_SIZE;

    public MatrixMultiplier() {
        threadPool = Executors.newCachedThreadPool();
        this.completionService = new ExecutorCompletionService<>(threadPool);
    }

    public void multiplyMatricesAsync(MultiplyTask multiplyTask) {
        //ako nema custom ime samo cemo da spojimo imena matrica
        String newName;
        if (Objects.equals(multiplyTask.getNewName(), "")) {
            newName = multiplyTask.getMatrixData1().getName() + multiplyTask.getMatrixData2().getName();
        } else newName = multiplyTask.getNewName();

        if (multiplyTask.getMatrixData1().getCols() != multiplyTask.getMatrixData2().getRows() || multiplyTask.getMatrixData2().getCols() != multiplyTask.getMatrixData1().getRows()) {
            System.err.println("Matrices can not be multiplied (row/col dont match)");
            return;
        }

//        podeliti matrice na redove i kolone za workere
        List<int[]> subMatricesARow = extractRowsAsArrays(multiplyTask.getMatrixData1().getMatrix());
        List<int[]> subMatricesBColumns = extractColumnsAsArrays(multiplyTask.getMatrixData2().getMatrix());

        if (subMatricesARow.size() != subMatricesBColumns.size()) {
            System.err.println("Matrices have malformed while splitting, exiting");
            return;
        }

        //posalje se podeljeno u workere
        List<Future<SubMultiplyResult>> matrixMultiplyResults = new ArrayList<>();
        List<int[]> rowsForWorker;
        List<int[]> colsForWorker;

        for (int rowCounter = 0; rowCounter < subMatricesARow.size(); rowCounter += MAXIMUM_ROWS_SIZE) {
            for (int colCounter = 0; colCounter < subMatricesBColumns.size(); colCounter += MAXIMUM_ROWS_SIZE) {
                rowsForWorker = new ArrayList<>();
                colsForWorker = new ArrayList<>();


                for (int i = 0; i < MAXIMUM_ROWS_SIZE; i++) {
                    if (rowCounter + i == subMatricesARow.size()) break;
                    rowsForWorker.add(subMatricesARow.get(rowCounter + i));
                }
                for (int i = 0; i < MAXIMUM_ROWS_SIZE; i++) {
                    if (colCounter + i == subMatricesBColumns.size()) break;
                    colsForWorker.add(subMatricesBColumns.get(colCounter + i));
                }
                matrixMultiplyResults.add(this.completionService.submit(new MatrixMultiplicationWorker(rowCounter, colCounter, rowsForWorker, colsForWorker)));
            }
        }

//        stavi se future na queue
        int finalRows = multiplyTask.getMatrixData1().getRows();
        int finalCols = multiplyTask.getMatrixData2().getCols();
        App.resultQueue.add(new MultiplyResult(newName, matrixMultiplyResults, finalRows, finalCols));
    }


    public List<int[]> extractRowsAsArrays(int[][] matrix) {
        List<int[]> rowList = new ArrayList<>();
        for (int[] row : matrix) {
            // Clone the row to ensure that changes to the original matrix do not affect the extracted rows
            int[] clonedRow = row.clone();
            rowList.add(clonedRow);
        }
        return rowList;
    }

    public static List<int[]> extractColumnsAsArrays(int[][] matrix) {
        List<int[]> columnList = new ArrayList<>();
        int rows = matrix.length;

        int cols = matrix[0].length;

        // Iterate over each column in the matrix
        for (int j = 0; j < cols; j++) {
            int[] column = new int[rows];  // Create a new array for the column
            for (int i = 0; i < rows; i++) {
                column[i] = matrix[i][j];  // Copy each row's j-th element into the column array
            }
            columnList.add(column);  // Add the column array to the list
        }
        return columnList;
    }


    //BLOCKING
    public void multiplyMatricesBlocking(MultiplyTask multiplyTask) {
        String newName;
        if (Objects.equals(multiplyTask.getNewName(), "")) { //ako nema custom ime samo cemo da spojimo imena matrica
            newName = multiplyTask.getMatrixData1().getName() + multiplyTask.getMatrixData2().getName();
        } else newName = multiplyTask.getNewName();

        if (multiplyTask.getMatrixData1().getCols() != multiplyTask.getMatrixData2().getRows() || multiplyTask.getMatrixData2().getCols() != multiplyTask.getMatrixData1().getRows()) {
            System.err.println("Matrices can not be multiplied (row/col dont match)");
            return;
        }

        int[][] result = multiplyMatrices(multiplyTask.getMatrixData1(), multiplyTask.getMatrixData2());
        App.logger.logMultiplying("Finished multiplying matrices " + multiplyTask.getMatrixData1().getName() + " * " + multiplyTask.getMatrixData2().getName() +
                " saving result to cache");
        App.cashedMatrices.put(newName, new MatrixData(
                newName,
                result,
                result.length,
                result[0].length,
                "-"
        ));
    }

    private int[][] multiplyMatrices(MatrixData matrixData1, MatrixData matrixData2) {
        //Orjentisemo matrice tako da se poklope za mnozenje
        if (matrixData1.getCols() != matrixData2.getRows() || matrixData2.getCols() != matrixData1.getRows()) {
            System.err.println("Matrices can not be multiplied (row/col dont match)");
            return null;
        }

        int[][] mat1 = matrixData1.getMatrix();//da ne bi get-ovali konstantno, malo efikasnije
        int[][] mat2 = matrixData2.getMatrix();

        //Mnozimo matrice
        int[][] result = new int[matrixData1.getRows()][matrixData2.getCols()];
        for (int i = 0; i < matrixData1.getRows(); i++) {
            for (int j = 0; j < matrixData2.getCols(); j++) {
                for (int k = 0; k < matrixData1.getCols(); k++) {
                    result[i][j] += mat1[i][k] * mat2[k][j];
                }
            }
        }
        return result;
    }

    public void setRowSize() {
        MAXIMUM_ROWS_SIZE = PropertyStorage.getInstance().getMaximum_rows_size();
        System.out.println(MAXIMUM_ROWS_SIZE);
    }

    public void terminatePool() {
        System.err.println("Terminating Multiplier thread pool");
        threadPool.shutdown();
    }

}
