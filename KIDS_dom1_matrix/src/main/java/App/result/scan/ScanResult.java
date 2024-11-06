package App.result.scan;

import App.matrixData.task.TaskType;
import App.result.Result;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

public class ScanResult implements Result<int[][]> {


    private final String matrixName;
    private final List<Future<Map<String, Integer>>> futureResults;
    private final int rows;
    private final int cols;
    private final String filePath;

    public ScanResult(String matrixName, List<Future<Map<String, Integer>>> futureResults, int rows, int cols, String filePath) {
        this.matrixName = matrixName;
        this.futureResults = futureResults;
        this.rows = rows;
        this.cols = cols;
        this.filePath = filePath;
    }

    @Override
    public int[][] getResult() {
        //prodjemo kroz listu rezultata (lista jer podeljena matrica u segmente)
        //svaki element liste je mapa, prodjemo kroz njih i stavimo sve vrednosti u jednu totalnu matricu
        try {
            int[][] matrix = new int[rows][cols];

            for (Future<Map<String, Integer>> future : futureResults) {
                Map<String, Integer> resultMap = future.get();
                for (Map.Entry<String, Integer> entry : resultMap.entrySet()) {
                    String[] parts = entry.getKey().split(",");
                    int row = Integer.parseInt(parts[0]);
                    int col = Integer.parseInt(parts[1]);
                    matrix[row][col] = entry.getValue();
                }
            }
            return matrix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean futureIsDone() {
        for (Future<Map<String, Integer>> future : futureResults) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    public String getMatrixName() {
        return matrixName;
    }

    @Override
    public TaskType getScanType() {
        return TaskType.CREATE;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }

    public String getFilePath() {
        return filePath;
    }
}
