package App.threadWorkers.pools;

import App.App;
import App.PropertyStorage;
import App.matrixData.MatrixData;
import App.matrixData.SplitMatrix;
import App.result.scan.ScanResult;
import App.result.scan.SquareResult;
import App.threadWorkers.pools.workers.MatrixScanWorker;
import App.threadWorkers.pools.workers.MatrixSquareWorker;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class MatrixExtractor {

    private final ExecutorService threadPool;
    private final ExecutorCompletionService<Map<String, Integer>> extractCompletionService;

    private final ExecutorService sThreadPool;
    private final ExecutorCompletionService<MatrixData> squareCompletionService;


    public MatrixExtractor() {
        this.threadPool = Executors.newCachedThreadPool();
        this.extractCompletionService = new ExecutorCompletionService<>(threadPool);
        this.sThreadPool = Executors.newCachedThreadPool();
        this.squareCompletionService = new ExecutorCompletionService<>(threadPool);
    }

    public void sendMatrixForScanning(String filePath) throws IOException {
        //split matrix
        List<Future<Map<String, Integer>>> matrixScanResults = new ArrayList<>();
        SplitMatrix splitMatrix = calculateSegments(filePath); //stavljamo u split matrix jer je lakse da se izvadi ime matrice tokom splitovanja nego posle

        //submit segments for scanning
        for (long[] segment : splitMatrix.getSegments()) {
            long start = segment[0];
            long end = segment[1];
            matrixScanResults.add(this.extractCompletionService.submit(new MatrixScanWorker(filePath, start, end)));
        }

        //add future scan results to result queue
        App.logger.jobDispatcher("Sent future results to queue");
        App.resultQueue.add(new ScanResult(splitMatrix.getMatrixName(), matrixScanResults, splitMatrix.getRows(), splitMatrix.getCols(), filePath));
    }


    /*
     delimo segmente po max chung size time sto postavimo granice prvo
     ako se granica ne nalazi na \n (kraju linije) idemo unazad (da ne bi bilo vise od max size) dok ne dodjemo do kraja reda
     zatim to napravimo u segment
     */
    private SplitMatrix calculateSegments(String filePath) throws IOException {
        RandomAccessFile file = new RandomAccessFile(filePath, "r");

        String firstLine = file.readLine(); // Uzmemo podatke iz prve linije
        String[] metadata = firstLine.split(", ");
        String matrixName = metadata[0].split("=")[1];
        int rows = Integer.parseInt(metadata[1].split("=")[1]);
        int cols = Integer.parseInt(metadata[2].split("=")[1]);

        List<long[]> segments = new ArrayList<>();
        long fileSize = file.length();

        long currentOffset = 0;
        while (file.read() != '\n') {//nadjemo prvu liniju matrice (pointer se pomerio kad smo gledali metadata, prvu liniju)
            file.seek(++currentOffset);
        }

        while (currentOffset < fileSize) {//idi dok ne dodjes do kraja
            long segmentStart = currentOffset;
            long segmentEnd = segmentStart + PropertyStorage.getInstance().getMaximum_file_chunk_size();//povecavaj za chunk size

            if (segmentEnd > fileSize) {//ako je chunk veci od file, uzemi file size
                segmentEnd = fileSize;
            } else {
                file.seek(segmentEnd);
                while (segmentEnd > segmentStart && file.read() != '\n') {//nadji kraj linije tako da ne uzmes u sredini
                    segmentEnd--;
                    file.seek(segmentEnd);
                }
            }
            segments.add(new long[]{segmentStart, segmentEnd});//dodaj granice segmenta
            currentOffset = segmentEnd;
        }

        file.close();
        return new SplitMatrix(matrixName, segments, rows, cols);//vracamo u klasi kako bi lakse vradili metadata
    }

    public void squareMatrix(String matrixName) {
        App.resultQueue.add(new SquareResult(this.squareCompletionService.submit(new MatrixSquareWorker(matrixName))));
    }

    public void terminatePool() {
        System.err.println("Terminating Extractor thread pool");
        threadPool.shutdown();
        System.err.println("Terminating Square Extractor thread pool");
        sThreadPool.shutdown();
    }

}
