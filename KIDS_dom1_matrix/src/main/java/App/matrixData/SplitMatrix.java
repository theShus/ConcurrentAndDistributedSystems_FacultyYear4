package App.matrixData;

import java.util.List;

public class SplitMatrix {

    private final String matrixName;
    private final List<long[]> segments;
    private final int rows;
    private final int cols;

    public SplitMatrix(String matrixName, List<long[]> segments, int rows, int cols) {
        this.matrixName = matrixName;
        this.segments = segments;
        this.rows = rows;
        this.cols = cols;
    }

    public String getMatrixName() {
        return matrixName;
    }

    public List<long[]> getSegments() {
        return segments;
    }

    public int getRows() {
        return rows;
    }

    public int getCols() {
        return cols;
    }
}
