package App.result.scan;

import App.matrixData.MatrixData;
import App.matrixData.task.TaskType;
import App.result.Result;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class SquareResult implements Result<MatrixData> {

    private final Future<MatrixData> squaredMatrixData;

    public SquareResult(Future<MatrixData> squaredMatrixData) {
        this.squaredMatrixData = squaredMatrixData;
    }

    @Override
    public TaskType getScanType() {
        return TaskType.SQUARE;
    }

    @Override
    public MatrixData getResult() {
        try {
            MatrixData matrixData = squaredMatrixData.get();
            matrixData.setName(matrixData.getName() + "_square");
            matrixData.setFilePath("-");
            int newSize = Math.max(matrixData.getCols(), matrixData.getRows());
            matrixData.setRows(newSize);
            matrixData.setCols(newSize);
            return squaredMatrixData.get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean futureIsDone() {
        return squaredMatrixData.isDone();
    }
}
