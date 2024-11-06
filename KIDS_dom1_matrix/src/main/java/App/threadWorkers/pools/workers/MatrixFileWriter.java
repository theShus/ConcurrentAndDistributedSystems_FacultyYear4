package App.threadWorkers.pools.workers;

import App.App;
import App.PropertyStorage;
import App.matrixData.MatrixData;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class MatrixFileWriter implements Runnable {

    private final MatrixData matrixData;
    private final String matName;
    private final String filePath;

    public MatrixFileWriter(MatrixData matrixData, String matName, String fileName) {
        this.matrixData = matrixData;
        this.matName = matName;
        filePath = PropertyStorage.getInstance().getSave_dir() + "/" + fileName + ".rix";
    }

    @Override
    public void run() {
        try {
            writeMatrixToFile();
        } catch (IOException e) {
            System.err.println("Error writing matrix to file: " + e.getMessage());
        }
    }

    private void writeMatrixToFile() throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            // Write the matrix header
            writer.write("matrix_name=" + matName + ", rows=" + matrixData.getRows() + ", cols=" + matrixData.getCols());
            writer.newLine();

            // Write the matrix data
            for (int i = 0; i < matrixData.getRows(); i++) {
                for (int j = 0; j < matrixData.getCols(); j++) {
                    writer.write(i + "," + j + " = " + matrixData.getMatrix()[i][j]);
                    writer.newLine();
                }
            }
            writer.flush();
            App.logger.fileWriter("Matrix " + matName + " has finished writing to file (ALT+TAB TO REFRESH INTELLIJ)");

        }
    }
}
