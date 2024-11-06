package App.threadWorkers;

import App.App;
import App.matrixData.task.*;

import java.io.IOException;

public class TaskCoordinator extends Thread {

    private boolean running = true;


    @Override
    public void run() {
        while (running) {//uzima poslove sa TaskQueue i rasporedjuje ih u Scan ili Multiply queue
            try {
                Task task = App.taskQueue.take();

                if (task instanceof ScanTask) {
                    String filePath = ((ScanTask) task).getFilePath();
                    App.logger.jobDispatcher("Submitted matrix for scanning: " + filePath);
                    App.matrixExtractor.sendMatrixForScanning(filePath);
                }
                else if (task.getTaskType() == TaskType.MULTIPLY) {
                    App.matrixMultiplier.multiplyMatricesAsync((MultiplyTask) task);
                    App.logger.jobDispatcher("Submitted matrices for multiplication: " + ((MultiplyTask) task).getMatrixData1().getName() + " * " + ((MultiplyTask) task).getMatrixData2().getName());
                }
                else if (task.getTaskType() == TaskType.SQUARE) {
                    App.logger.jobDispatcher("Submitted matrix for squaring: " + ((SquareTask) task).getMatrixToSquareName());
                    App.matrixExtractor.squareMatrix(((SquareTask) task).getMatrixToSquareName());
                }
            } catch (InterruptedException | IOException e) {
                e.printStackTrace();
            }
        }
    }


    public void terminate() {
        System.err.println("Terminating TaskCoordinator thread");
        running = false;
    }

}
