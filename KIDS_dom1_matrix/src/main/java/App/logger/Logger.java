package App.logger;

public class Logger {

    private static final boolean cli = true;
    private static final boolean propertyStorage = true;
    private static final boolean logExplorer = false;
    private static final boolean jobDispatcher = true;
    private static final boolean clearMatrix = true;
    private static final boolean multiplying = true;
    private static final boolean resultRetrieverSorter = true;
    private static final boolean fileWriter = true;


    public void cli(String message) {
        if (!cli) return;
        System.out.println(Color.WHITE_BOLD + message + Color.RESET);
    }

    public void clearingMatrix(String message) {
        if (!clearMatrix) return;
        System.out.println(Color.YELLOW_BRIGHT + message + Color.RESET);
    }

    public void logMultiplying(String message) {
        if (!multiplying) return;
        System.out.println(Color.BLUE_BRIGHT + message + Color.RESET);
    }

    public void logExplorer(String message) {
        if (!logExplorer) return;
        System.out.println(Color.YELLOW + message + Color.RESET);
    }

    public void jobDispatcher(String message) {
        if (!jobDispatcher) return;
        System.out.println(Color.CYAN + message + Color.RESET);
    }

    public void resultRetrieverSorter(String message) {
        if (!resultRetrieverSorter) return;
        System.out.println(Color.GREEN + message + Color.RESET);
    }

    public void fileWriter(String message) {
        if (!fileWriter) return;
        System.out.println(Color.MAGENTA + message + Color.RESET);
    }

    public void propertyStorage(String message) {
        if (!propertyStorage) return;
        System.out.println(Color.WHITE_BOLD + message + Color.RESET);
    }

}
