package App;

import java.io.IOException;
import java.util.Properties;

public class PropertyStorage {

    private static PropertyStorage instance = null;
    private final Properties properties;
    private long sys_explorer_sleep_time;
    private long maximum_file_chunk_size;
    private int maximum_rows_size;
    private String start_dir;
    private String save_dir;

    private PropertyStorage() {
        properties = new Properties();
        try {
            properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));
        } catch (IOException e) {
            System.err.println("Error while loading app.properties " + e.getMessage());
        }
    }

    public static PropertyStorage getInstance() {
        if (instance == null) instance = new PropertyStorage();
        return instance;
    }

    //ucitamo iz app.properties
    public void loadProperties() {
        sys_explorer_sleep_time = Long.parseLong(readProperty("sys_explorer_sleep_time"));
        maximum_file_chunk_size = Long.parseLong(readProperty("maximum_file_chunk_size"));
        maximum_rows_size = Integer.parseInt(readProperty("maximum_rows_size"));
        start_dir = readProperty("start_dir");
        save_dir = readProperty("save_dir");
    }

    private String readProperty(String keyName) {
        App.logger.propertyStorage("Loading property " + keyName);
        return properties.getProperty(keyName, "Missing data");
    }

    public String getStart_dir() {
        return start_dir;
    }

    public long getSys_explorer_sleep_time() {
        return sys_explorer_sleep_time;
    }

    public long getMaximum_file_chunk_size() {
        return maximum_file_chunk_size;
    }

    public int getMaximum_rows_size() {
        return maximum_rows_size;
    }

    public String getSave_dir() {
        return save_dir;
    }
}
