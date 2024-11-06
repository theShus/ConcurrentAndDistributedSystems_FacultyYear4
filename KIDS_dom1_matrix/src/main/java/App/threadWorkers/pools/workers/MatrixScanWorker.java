package App.threadWorkers.pools.workers;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;

public class MatrixScanWorker implements Callable<Map<String, Integer>> {

    private final String filePath;
    private final long start;
    private final long end;

    public MatrixScanWorker(String filePath, long start, long end) {
        this.filePath = filePath;
        this.start = start;
        this.end = end;
    }

    @Override
    public Map<String, Integer> call() {
        try (RandomAccessFile file = new RandomAccessFile(filePath, "r")) {
            file.seek(start); // pomeri pointer na pocetak segmenta

            if (start != 0) {
                file.readLine(); // baci liniju ako je prazna//todo proveri da li je ovde sve uredu nesto se cudno desilo sa dodavanjem novog dira
            }

            String line;
            Map<String, Integer> values = new HashMap<>();

            while (file.getFilePointer() < end && (line = file.readLine()) != null) {//prodji kroz linije i stavi ih u mapu
                String[] parts = line.split(" = ");

                if (parts.length != 2) {
                    System.err.println("skipped malformed line in matrix " + filePath);

                    continue;
                }

                int value = Integer.parseInt(parts[1].trim());
                String[] indices = parts[0].split(",");
                int row = Integer.parseInt(indices[0].trim());
                int col = Integer.parseInt(indices[1].trim());
                values.put(row + "," + col, value);
            }

            return values;
        } catch (IOException e) {
            System.err.println("Error reading the file segment: " + e.getMessage());
        }
        return null;
    }

}
