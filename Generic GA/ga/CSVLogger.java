package ga;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This class allows for an easy means to create CSV files and append data to them.
 * 
 * @author Jason Yoder
 *
 */
public class CSVLogger {

	private HashMap<String, PrintWriter> logFiles = new HashMap<String, PrintWriter>();
	private HashMap<String, Integer> dataColumnMap = new HashMap<String, Integer>();
	private String path;
	
	public CSVLogger(String path) {
		this.path = path;
	}

	/**
	 * Create a new file with the provided set of header column names
	 * 
	 * @param filename
	 * @param columnHeaders
	 * @throws FileNotFoundException
	 */
	public void createNewFile(String filename, ArrayList<String> columnHeaders) throws FileNotFoundException {
		if (logFiles.containsKey(filename)) {
			throw new RuntimeException("File already exists! Exitting...");
		} else {
			//Create folder if it does not already exist
			File pathAsFile = new File(path);
			if (!pathAsFile.exists()) {
				pathAsFile.mkdir();
			}
			
			logFiles.put(filename, new PrintWriter( new File(path+"/"+filename)));
			dataColumnMap.put(filename, columnHeaders.size() );
			PrintWriter pw = logFiles.get(filename);
			for (String header: columnHeaders) {
				pw.print(header+",");
			}
			pw.println("");
		}
	}

	public void appendToFile(String filename, ArrayList<String> dataRows) {
		for (String row: dataRows) {
			appendRowToFile(filename, row);
		}
	}
	
	/**
	 * Verify that there is the correct number of data entries
	 * 
	 * @param filename
	 * @param dataRow
	 * @return
	 */
	public boolean isValidDataRow(String filename, String dataRow) {
		//make sure that the row has the correct number of fields
		int columns = dataColumnMap.get(filename);
		//intentionally avoid the final character so that the count is consistent
		//regardless of whether there is a trailing "," or not
		int commas = 0;
		for (int i=0; i<dataRow.length()-1; i++) {
			if (dataRow.charAt(i) == ',') {
				commas++;
			}
		}
		if (commas + 1 != columns) {
			System.err.println( "There are " +commas +" commas, and " + columns + " columns! There should be 1 more column than comma!"   );  
			return false;
		} else {
			return true;
		}
	}

	/**
	 * Adds the provided row of data to the specified File
	 * 
	 * @param filename
	 * @param dataRow
	 */
	public void appendRowToFile(String filename, String dataRow) {
		if (isValidDataRow(filename, dataRow) ) {
			PrintWriter pw = logFiles.get(filename);
			pw.println(dataRow);
		} else {
			System.err.println("Invalid Data Row! Exitting...");
			System.exit(0);
		}
	}

	public void closeAll() {
		for (PrintWriter pw: logFiles.values()) {
			pw.close();
		}
	}

}
