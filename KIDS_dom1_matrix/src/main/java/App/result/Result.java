package App.result;

import App.matrixData.task.TaskType;

public interface Result<T> {

    TaskType getScanType();

    T getResult();

    boolean futureIsDone();
}
