package ga;
import java.io.FileNotFoundException;
import java.util.ArrayList;

public class Logger<T> {
    private String filename;
    private CSVLogger logger;
    private ArrayList<String> headers;
    private LogFunction<T> logFunction;


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

    public String filename() {
        return this.filename;
    }

    public ArrayList<String> headers() {
        return this.headers;
    }

    public void log(ArrayList<T> population, int generation) {
        this.logFunction.log(this.logger, this.filename, population, generation);
    }

    @FunctionalInterface
    public interface LogFunction<T> {
        void log(CSVLogger logger, String filename, ArrayList<T> population, int generation);
    } 
}
