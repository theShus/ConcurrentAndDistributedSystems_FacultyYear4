package App.result.multiply;

public class SubMultiplyResult {

    private final int cordX;
    private final int cordY;
    private final int[][] subMatrix;


    public SubMultiplyResult(int cordX, int cordY, int[][] subMatrix) {
        this.cordX = cordX;
        this.cordY = cordY;
        this.subMatrix = subMatrix;
    }

    public int getCordX() {
        return cordX;
    }

    public int getCordY() {
        return cordY;
    }

    public int[][] getSubMatrix() {
        return subMatrix;
    }
}
