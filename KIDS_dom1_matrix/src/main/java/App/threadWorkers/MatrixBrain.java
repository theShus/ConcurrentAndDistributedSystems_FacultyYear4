package App.threadWorkers;

import App.App;
import App.matrixData.MatrixData;
import App.matrixData.task.SquareTask;
import App.matrixData.task.TaskType;
import App.result.Result;
import App.result.multiply.MultiplyResult;
import App.result.scan.ScanResult;
import App.result.scan.SquareResult;
import App.threadWorkers.pools.workers.MatrixFileWriter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MatrixBrain extends Thread {

    private boolean running = true;
    ExecutorService fileWriterThreadPool = Executors.newWorkStealingPool();

    @Override
    public void run() {
        while (running) {
            try {
                Result result = App.resultQueue.take();

                if (result.getScanType() == TaskType.CREATE) {
                    ScanResult scanResult = (ScanResult) result;
                    if (scanResult.futureIsDone()) {
                        App.logger.resultRetrieverSorter("Matrix " + scanResult.getMatrixName() + " is finished scanning, adding to cache");
                        MatrixData matrixData = new MatrixData(scanResult.getMatrixName(), scanResult.getResult(), scanResult.getRows(), scanResult.getCols(), scanResult.getFilePath());
                        App.cashedMatrices.put(scanResult.getMatrixName(), matrixData);

                        App.taskQueue.add(new SquareTask(scanResult.getMatrixName()));//stavimo novi task da se napravi kvadratna matrica
                    }
                    else App.resultQueue.add(result);
                }
                else if (result.getScanType() == TaskType.SQUARE) {
                    MatrixData squaredMatrixData = ((SquareResult) result).getResult();
                    App.logger.resultRetrieverSorter("Matrix " + squaredMatrixData.getName() + " has been squared, adding to cache");
                    App.cashedMatrices.put(squaredMatrixData.getName(), squaredMatrixData);
                }
                else if (result.getScanType() == TaskType.MULTIPLY) {
                    MultiplyResult multiplyResult = (MultiplyResult) result;
                    if (multiplyResult.futureIsDone()) {
                        App.logger.resultRetrieverSorter("Matrix " + multiplyResult.getMatrixName() + " is finished multiplying, adding to cache");
                        App.cashedMatrices.put(multiplyResult.getMatrixName(),
                                new MatrixData(multiplyResult.getMatrixName(), multiplyResult.getResult(), multiplyResult.getRows(), multiplyResult.getCols(), "-"));

//                        printMatrix(multiplyResult.getResult());
                    } else App.resultQueue.add(result);
                }

            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void clearMatrixData(String matrixName) {
        if (!App.cashedMatrices.containsKey(matrixName)) {
            System.err.println("Requested matrix is nonexistent");
            return;
        }
        String filePath = App.cashedMatrices.get(matrixName).getFilePath();
        App.cashedMatrices.remove(matrixName);
        App.logger.clearingMatrix("Cleared matrix " + matrixName + " from cache");
        App.cashedMatrices.remove(matrixName + "_square");
        App.logger.clearingMatrix("Cleared matrix " + matrixName + "_square from cache");

        if (!Objects.equals(filePath, "-")) {
            Path path = Paths.get(filePath);
            String fileName = path.getFileName().toString();
            App.systemExplorer.removeMatrixFromLastModified(fileName);
        }
    }

    public void clearAllMatrixData(String matrixFileName) {
        Path filePath = null;
        String matrixName = "";

        //nadjemo file path iz kesiranih matrica, lakse nego da pretrazujemo filove opet
        for (Map.Entry<String, MatrixData> cashedMapRow : App.cashedMatrices.entrySet()) {
            Path path = Paths.get(cashedMapRow.getValue().getFilePath());
            if (path.getFileName().toString().equals(matrixFileName)) {
                filePath = path;
                matrixName = cashedMapRow.getValue().getName();
                break;
            }
        }
        if (filePath == null) {
            System.err.println("Requested matrix file could not be found");
            return;
        }

        try {
            Files.deleteIfExists(filePath);
            App.logger.clearingMatrix("Deleted matrix " + matrixFileName + " from disc - ALT+TAB TO REFRESH INTELLIJ");
        } catch (IOException e) {
            System.err.println("Error deleting file: " + filePath);
        }
        App.cashedMatrices.remove(matrixName);
        App.logger.clearingMatrix("Cleared matrix " + matrixName + " from cache");
        String fileName = filePath.getFileName().toString();
        App.systemExplorer.removeMatrixFromLastModified(fileName);
    }


    public void saveMatrixToFile(MatrixData matrixData, String matName, String fileName) {
        fileWriterThreadPool.submit(new MatrixFileWriter(matrixData, matName, fileName));
    }

    public void terminate() {
        System.err.println("Terminating MatrixBrain thread");
        running = false;
        fileWriterThreadPool.shutdown();
    }
}
