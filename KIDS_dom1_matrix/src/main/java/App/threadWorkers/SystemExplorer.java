package App.threadWorkers;

import App.App;
import App.PropertyStorage;
import App.matrixData.task.ScanTask;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class SystemExplorer extends Thread {

    private final HashMap<String, Long> lastModifiedMap;
    private final CopyOnWriteArrayList<String> dirsToExplore;
    private boolean running = true;


    public SystemExplorer(CopyOnWriteArrayList<String> dirsToExplore) {
        lastModifiedMap = new HashMap<>();
        this.dirsToExplore = dirsToExplore;
    }

    @Override
    public void run() {
        while (running) {
            try {
                for (String path : dirsToExplore) {
                    exploreDirectory(new File(path), path);
                }
                Thread.sleep(PropertyStorage.getInstance().getSys_explorer_sleep_time());
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    //prodji kroz direktorijume koji su u queue
    //proveri filove kroz lastModified check, ako ga nema / promenjen stavi ga u JobQueue
    private void exploreDirectory(File inputDirectory, String path) throws InterruptedException {
        File[] fileList = inputDirectory.listFiles();

        if (fileList == null) {
            System.err.println("Given directory can not be found/opened");
            dirsToExplore.remove(path);
            return;
        }

        for (File file : fileList) {
            if (file.isFile()) {
                if (file.getName().endsWith(".rix")) {
                    App.logger.logExplorer("Found rix file: " + file.getName());
                    addFileToQueue(file);
                }
            } else {
                if (!file.canRead()) {
                    System.err.println("Directory can not be opened");
                    continue;
                }
                exploreDirectory(file, path);
            }
        }
    }

    //kada nadjemo file koji nema isti lastModified kao u mapi
    //pravimo ga u job i stavljamo na queue
    private void addFileToQueue(File fileToExplore) throws InterruptedException {
        String filePath = fileToExplore.getAbsolutePath();
        long lastModified = fileToExplore.lastModified();

        if (lastModifiedMap.getOrDefault(fileToExplore.getName(), 0L) != lastModified) {
            lastModifiedMap.put(fileToExplore.getName(), lastModified);
            App.taskQueue.put(new ScanTask(filePath));
        }
    }

    public void removeMatrixFromLastModified(String matrixFileName) {
        if (!lastModifiedMap.containsKey(matrixFileName)) {
            System.err.println("Couldn't find matrix file in last modified - skipping");
            return;
        }
        lastModifiedMap.remove(matrixFileName);
        App.logger.clearingMatrix("Cleared matrix " + matrixFileName + " from last modified list");
    }

    public void terminate() {
        System.err.println("Terminating DirectoryCrawler thread");
        running = false;
    }

}
