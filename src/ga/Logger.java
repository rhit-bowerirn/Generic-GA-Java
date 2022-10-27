package ga;

import java.io.FileNotFoundException;
import java.util.ArrayList;

/**
 * a class to manage logging data in one csv file
 */
public class Logger<T> {
    private String filename;
    private CSVLogger logger;
    private ArrayList<String> headers;
    private LogFunction<T> logFunction;

    /**
     * ensures: instantiates an instance of logger
     * 
     * @param filename    the file to log to
     * @param logger      the csv logger used for logging
     * @param headers     the headers of the csv
     * @param logFunction the function generating each new row of data
     */
    public Logger(String filename, CSVLogger logger, ArrayList<String> headers, LogFunction<T> logFunction) {
        this.filename = filename;
        this.headers = headers;
        this.logger = logger;
        this.logFunction = logFunction;

        try {
            this.logger.createNewFile(filename, headers);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * ensures: logs a row of data to the csv file
     * 
     * @param population the current population in the GA
     * @param generation the current generation of the GA
     */
    public void log(ArrayList<T> population, int generation) {
        this.logFunction.log(this.logger, this.filename, population, generation);
    }

    @FunctionalInterface
    public interface LogFunction<T> {
        void log(CSVLogger logger, String filename, ArrayList<T> population, int generation);
    }
}
