package App.matrixData.task;

import App.matrixData.MatrixData;

public class MultiplyTask implements Task {

    private final MatrixData matrixData1;
    private final MatrixData matrixData2;
    private final String newName;

    public MultiplyTask(MatrixData matrixData1, MatrixData matrixData2, String newName) {
        this.matrixData1 = matrixData1;
        this.matrixData2 = matrixData2;
        this.newName = newName;
    }

    @Override
    public String toString() {
        return "MultiplyTask{" +
                "matrixData1=" + matrixData1 +
                ", matrixData2=" + matrixData2 +
                ", newName='" + newName + '\'' +
                '}';
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.MULTIPLY;
    }

    public MatrixData getMatrixData1() {
        return matrixData1;
    }

    public MatrixData getMatrixData2() {
        return matrixData2;
    }

    public String getNewName() {
        return newName;
    }
}
