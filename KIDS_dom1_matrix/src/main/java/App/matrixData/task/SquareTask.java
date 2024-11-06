package App.matrixData.task;

public class SquareTask implements Task {

    String matrixToSquareName;

    public SquareTask(String matrixToSquareName) {
        this.matrixToSquareName = matrixToSquareName;
    }

    public String getMatrixToSquareName() {
        return matrixToSquareName;
    }

    @Override
    public TaskType getTaskType() {
        return TaskType.SQUARE;
    }
}
