package App.threadWorkers.pools.workers;

import App.App;
import App.matrixData.MatrixData;

import java.util.concurrent.Callable;

public class MatrixSquareWorker implements Callable<MatrixData> {

    String matrixToSquareName;

    public MatrixSquareWorker(String matrixToSquareName) {
        this.matrixToSquareName = matrixToSquareName;
    }

    @Override
    public MatrixData call() throws Exception {
        MatrixData matrixToSquare = new MatrixData(App.cashedMatrices.get(matrixToSquareName));

        int rows = matrixToSquare.getRows();
        int cols = matrixToSquare.getCols();

        int n = Math.max(rows, cols); //nova velicina matrice
        int[][] squareMatrix = new int[n][n];

        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                squareMatrix[i][j] = matrixToSquare.getMatrix()[i][j];//nova polja su automatski 0 u javi
            }
        }

        matrixToSquare.setMatrix(squareMatrix);
        return matrixToSquare;
    }

}
