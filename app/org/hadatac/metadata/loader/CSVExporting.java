package org.hadatac.metadata.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import org.hadatac.utils.Feedback;

public class CSVExporting {
	
	public static final String KB_FORMAT = "text/turtle";
	
	public static final String TTL_DIR = "tmp/ttl/";

    public static String generateCSV(
    		int mode, String oper, CSVExporterContext csv_context, String csvName) {

		String message = "";
		//Output a result set in CSV format using Jena
		FileOutputStream file;
		try {
			file = new FileOutputStream(new File(csvName));
			try {
				csv_context.getDetectorModelTriples(file);
				file.flush();
				file.close();
			} catch (IOException e) {
				message += Feedback.println(mode, "[ERROR]: Could not open file " + csvName);
				return message;
			}
		} catch (FileNotFoundException e) {
			message += Feedback.println(mode, "[ERROR]: Could not open file " + csvName);
			return message;
		}
		
		System.out.println("Output Successfully!");
		message += Feedback.println(mode, "Generated " + csvName + " successfully");
	    return message;
	}
}