package App.matrixData;

public class MatrixData {
    private String name;
    private int[][] matrix;
    private int rows;
    private int cols;
    private String filePath;

    public MatrixData(String name, int[][] matrix, int rows, int cols, String filePath) {
        this.name = name;
        this.matrix = matrix;
        this.rows = rows;
        this.cols = cols;
        this.filePath = filePath;
    }

    public MatrixData(MatrixData matrixData) {
        this.name = matrixData.getName();
        this.matrix = matrixData.getMatrix();
        this.rows = matrixData.getRows();
        this.cols = matrixData.getCols();
        this.filePath = matrixData.getFilePath();
    }

    public String getName() {
        return name;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int[][] getMatrix() {
        return matrix;
    }

    public void setMatrix(int[][] matrix) {
        this.matrix = matrix;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public int getCols() {
        return cols;
    }

    public void setCols(int cols) {
        this.cols = cols;
    }

    public String getFilePath() {
        return filePath;
    }

    @Override
    public String toString() {
        return "MatrixData{" +
                "name='" + name + '\'' +
                ", rows=" + rows +
                ", cols=" + cols +
                ", filePath='" + filePath + '\'' +
                '}';
    }
}
