package App;

import App.logger.Logger;
import App.matrixData.MatrixData;
import App.matrixData.task.MultiplyTask;
import App.matrixData.task.Task;
import App.result.Result;
import App.threadWorkers.MatrixBrain;
import App.threadWorkers.SystemExplorer;
import App.threadWorkers.TaskCoordinator;
import App.threadWorkers.pools.MatrixExtractor;
import App.threadWorkers.pools.MatrixMultiplier;

import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.stream.Collectors;

public class App {


    //Queue
    public static final BlockingQueue<Task> taskQueue = new LinkedBlockingQueue<>(20);
    public static final BlockingQueue<Result> resultQueue = new LinkedBlockingQueue<>(100);

    //Results
    public static final Map<String, MatrixData> cashedMatrices = new ConcurrentHashMap<>();

    //Else
    private static final CopyOnWriteArrayList<String> dirsToExplore = new CopyOnWriteArrayList<>();
    public static final Logger logger = new Logger();

    //Threads
    public static final SystemExplorer systemExplorer = new SystemExplorer(dirsToExplore);
    private static final TaskCoordinator taskCoordinator = new TaskCoordinator();
    private static final MatrixBrain matrixBrain = new MatrixBrain();

    //Pools
    public static final MatrixExtractor matrixExtractor = new MatrixExtractor();
    public static final MatrixMultiplier matrixMultiplier = new MatrixMultiplier();

    public void start() {
        PropertyStorage.getInstance().loadProperties();
        dirsToExplore.add(PropertyStorage.getInstance().getStart_dir());
        matrixMultiplier.setRowSize();

        systemExplorer.start();
        taskCoordinator.start();
        matrixBrain.start();

        startCommandParser();
    }

    private void startCommandParser() {
        Scanner cli = new Scanner(System.in);
        String line;
        String[] tokens;

        while (true) {
            line = cli.nextLine().trim();
            tokens = line.split(" ");

            if (line.isEmpty()) continue;

            switch (tokens[0]) {
                case "dir" -> {
                    if (badCommandLength(tokens.length, 2, 2)) continue;
                    dirsToExplore.add(tokens[1]);
                    logger.cli("Added directory to scan list");
                }
                case "info" -> {
                    if (badCommandLength(tokens.length, 2, 4)) continue;

                    if (tokens.length == 2) {
                        switch (tokens[1]) {
                            case "-all" -> printCashedMatrices();
                            case "-asc" -> printAscDescMatrices(true);
                            case "-desc" -> printAscDescMatrices(false);
                            default -> {
                                MatrixData matrixData;
                                if (cashedMatrices.containsKey(tokens[1])) matrixData = cashedMatrices.get(tokens[1]);
                                else {
                                    System.err.println("Nonexistent matrix or matrix hasn't finished work");
                                    continue;
                                }
                                logger.cli("Matrix " + matrixData.getName() + ": | rows: " + matrixData.getRows() + " | columns: " + matrixData.getCols() + " | file path: " + matrixData.getFilePath());
                            }
                        }
                    } else if (tokens.length == 3) {
                        if (!isEntirelyInteger(tokens[2])) {
                            System.err.println("Bad number input");
                            continue;
                        }

                        if (tokens[1].equals("-s")) printFirstLastMatrices(true, Integer.parseInt(tokens[2]));
                        else if (tokens[1].equals("-e")) printFirstLastMatrices(false, Integer.parseInt(tokens[2]));
                    }
                }

                case "multiply" -> {
                    if (badCommandLength(tokens.length, 3, 6)) continue;

                    boolean asyncFlag = false;
                    String newName = "";

                    if (!cashedMatrices.containsKey(tokens[1]) || !cashedMatrices.containsKey(tokens[2])) {
                        System.err.println("One of the matrices is nonexistent");
                        continue;
                    }

                    for (int i = 3; i < tokens.length; i++) {
                        if (Objects.equals(tokens[i], "-async")) asyncFlag = true;
                        if (Objects.equals(tokens[i], "-name")) newName = tokens[i + 1];
                    }

                    MultiplyTask multiplyTask = new MultiplyTask(
                            cashedMatrices.get(tokens[1]),
                            cashedMatrices.get(tokens[2]),
                            newName
                    );

                    if (asyncFlag) taskQueue.add(multiplyTask);
                    else matrixMultiplier.multiplyMatricesBlocking(multiplyTask);
                }
                case "save" -> {
                    if (badCommandLength(tokens.length, 3, 5)) continue;

                    String matName = "";
                    String fileName = "";

                    for (int i = 1; i < tokens.length; i++) {
                        if (Objects.equals(tokens[i], "-name")) matName = tokens[i + 1];
                        if (Objects.equals(tokens[i], "-file")) fileName = tokens[i + 1];
                    }

                    if (!cashedMatrices.containsKey(matName)) {
                        System.err.println("Requested matrix is nonexistent or matrix hasn't finished work");
                        continue;
                    }

                    matrixBrain.saveMatrixToFile(cashedMatrices.get(matName), matName, fileName);
                }
                case "print" -> {
                    if (!cashedMatrices.containsKey(tokens[1])) {
                        System.err.println("Nonexistent matrix");
                        continue;
                    }
                    printMatrix(cashedMatrices.get(tokens[1]).getMatrix());
                }
                case "clear" -> {
                    if (badCommandLength(tokens.length, 2, 2)) continue;
                    if (tokens[1].contains(".rix")) matrixBrain.clearAllMatrixData(tokens[1]);
                    else matrixBrain.clearMatrixData(tokens[1]);
                }
                case "help" -> logger.cli
                        (
                                """
                                        --> dir <dir_name>:  Dodaje novi direktorijum za skeniranje
                                        --> print <dir_name>:  Ispisivanje matrice
                                        --> info <matrix_name>:  Dohvata osnovne informacije o specifičnoj matrici ili skupu matrica
                                          ->  -all: Prikazuje sve matrice (koristi se bez arguments za naziv matrice).
                                          ->  -asc: Sortira matrice rastuće po broju redova, a zatim po broju kolona.
                                          ->  -desc: Sortira matrice opadajuće po broju redova, a zatim po broju kolona.
                                          ->  -s <n>: Prikazuje prvih N matrica. Na primer, -s 10 prikazuje prvih 10 matrica.
                                          ->  -e <n>: Prikazuje poslednjih N matrica. Na primer, -e 5 prikazuje poslednjih 5 matrica.
                                        --> mult <mat1 mat2>:  Korisnik  zatraži množenje dve matrice
                                          ->  -async: Omogućava asinhrono izvršavanje množenja, bez blokiranja Matrix Brain niti.
                                          ->  -name <matrix_name>: Omogućava imenovanje matrice, ukoliko se ne navede kao parametar ime koje matrica koristi je konkatenacija naziva prve i druge matrice, primer: mat1mat2
                                        --> save -name <mat_name> -file <file_name>:  Omogućava korisniku da sačuva matricu na disk
                                        --> clear <mat_name>: Brisu se rezultati zeljene matrice
                                        --> clear <file_name>: Brisu se sve matrice sa upisanog fajla i kupe se opet (automatski)
                                        --> stop: Gasi aplikaciju
                                        """
                        );
                case "stop" -> {
                    logger.cli("Stopping the app");
                    stopThreads();
                    cli.close();
                    return;
                }
                default -> System.err.println("Unknown command");
            }
        }
    }

    public static void printMatrix(int[][] matrix) {
        for (int[] ints : matrix) {
            for (int anInt : ints) {
                System.out.print(anInt + " ");
            }
            System.out.println(); // Move to the next line after printing each row
        }
    }

    private boolean badCommandLength(int tokenLength, int minSize, int maxSize) {
        if (tokenLength > maxSize || tokenLength < minSize) {
            System.err.println("Wrong number of arguments");
            return true;
        }
        return false;
    }

    private void printCashedMatrices() {
        for (Map.Entry<String, MatrixData> entry : cashedMatrices.entrySet()) {
            logger.cli("Matrix " + entry.getValue().getName() + ": | rows: " +
                    entry.getValue().getRows() + " | columns: " + entry.getValue().getCols() + " | file path: " + entry.getValue().getFilePath());
        }
    }

    private void printAscDescMatrices(boolean asc) {
        List<MatrixData> sortedMatrices = sortMatrices(cashedMatrices);
        if (asc) {
            for (MatrixData md : sortedMatrices) {
                logger.cli("Matrix " + md.getName() + ": | rows: " + md.getRows() + " | columns: " + md.getCols() + " | file path: " + md.getFilePath());
            }
        } else {
            for (int i = sortedMatrices.size() - 1; i >= 0; i--) {
                MatrixData md = sortedMatrices.get(i);
                logger.cli("Matrix " + md.getName() + ": | rows: " + md.getRows() + " | columns: " + md.getCols() + " | file path: " + md.getFilePath());
            }
        }
    }

    public static List<MatrixData> sortMatrices(Map<String, MatrixData> matrixMap) {
        return matrixMap.values().stream()
                .sorted(Comparator.comparingInt(MatrixData::getRows)
                        .thenComparingInt(MatrixData::getCols))
                .collect(Collectors.toList());
    }

    private void printFirstLastMatrices(boolean first, int n) {
        int count = 0;
        if (first) {//Pisi prvih n matrica
            for (Map.Entry<String, MatrixData> entry : cashedMatrices.entrySet()) {
                if (count++ == n || count == cashedMatrices.size()) break;
                logger.cli("Matrix " + entry.getValue().getName() + ": | rows: " +
                        entry.getValue().getRows() + " | columns: " + entry.getValue().getCols() + " | file path: " + entry.getValue().getFilePath());
            }
        } else {//Pisi poslednjih n matrica
            int start = cashedMatrices.size() - n;
            if (start < 0) start = 0;  // In case n is larger than the map size
            for (Map.Entry<String, MatrixData> entry : cashedMatrices.entrySet()) {
                if (count++ < start) continue;
                logger.cli("Matrix " + entry.getValue().getName() + ": | rows: " +
                        entry.getValue().getRows() + " | columns: " + entry.getValue().getCols() + " | file path: " + entry.getValue().getFilePath());
            }
        }
    }

    private boolean isEntirelyInteger(String input) {
        return input.matches("\\d+");
    }

    private void stopThreads() {
        System.err.println("STOPPING THREADS");

        systemExplorer.terminate();
        taskCoordinator.terminate();
        matrixBrain.terminate();
        matrixExtractor.terminatePool();
        matrixMultiplier.terminatePool();
    }

}
