package org.hadatac.metadata.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hadatac.metadata.loader.Loader;
import org.hadatac.metadata.loader.SheetProcessing;
import org.hadatac.metadata.model.SpreadsheetParsingResult;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

public class SpreadsheetProcessing {
	
	public static final String KB_FORMAT = "text/turtle";
	
	public static final String TTL_DIR = "tmp/ttl/";
	
	public static String printFileWithLineNumber(int mode, String filename) {
		String str = "";
		int lineNumber = 1;

        LineNumberReader reader = null;
        String line = null;

        try {
            reader = new LineNumberReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                str += Feedback.println(mode, lineNumber++ + " " + line);
            }
            reader.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return str;
	}

    public static String generateTTL(int mode, String oper, RDFContext rdf, String xlsName) {

		String message = "";
		if (oper.equals("load")) {
		   message += Feedback.println(mode, "   Triples before [loadXLS]: " + rdf.totalTriples());
		   message += Feedback.println(mode, " ");
		}
		message += Feedback.println(mode, "   Parsing spreadsheet " + xlsName);
		message += Feedback.println(mode, " ");
		
		String ttl = "";
		
		//Create Workbook instance holding reference to .xlsx file
		FileInputStream file;
		try {
			file = new FileInputStream(new File(xlsName));

			XSSFWorkbook workbook;
			try {
				ttl += NameSpaces.getInstance().printTurtleNameSpaceList();
				
				workbook = new XSSFWorkbook(file);

				//Iterate through workbook's sheets
				for (int currentSheet=0; currentSheet < workbook.getNumberOfSheets(); currentSheet++) {
					XSSFSheet sheet = workbook.getSheetAt(currentSheet);	            
					message += Feedback.print(mode, "   Processing sheet " + sheet.getSheetName() + "     ");
					for (int i = sheet.getSheetName().length(); i < 20; i++) {
						message += Feedback.print(mode, ".");
					}
					SpreadsheetParsingResult result = SheetProcessing.generateTTL(mode, sheet);
					ttl = ttl + "\n# concept: " + sheet.getSheetName() + result.getTurtle() + "\n";
					message += result.getMessage();
				}

				workbook.close();
				file.close();

			} catch (IOException e) {
				message += Feedback.println(mode, "[ERROR]: Could not open file  " + xlsName + " as an XLS spreadsheet");
				return message;
				//e.printStackTrace();
			}
		
		} catch (FileNotFoundException e) {
			message += Feedback.println(mode, "[ERROR]: Could not open file " + xlsName);
			return message;
			//e.printStackTrace();
		}
		
		String fileName = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			fileName = TTL_DIR + "HASNetO-" + timeStamp + ".ttl";
			FileUtils.writeStringToFile(new File(fileName), ttl);
		} catch (IOException e) {
			message += e.getMessage();
			return message;
		}
		
		String listing = "";
		try {
			listing = URLEncoder.encode(SpreadsheetProcessing.printFileWithLineNumber(mode, fileName), "UTF-8");
			//System.out.println(SpreadsheetProcessing.printFileWithLineNumber(mode, fileName));
		} catch (UnsupportedEncodingException e1) {
			e1.printStackTrace();
		};

		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + fileName + " and stored locally.");
		try {
			Model model = RDFDataMgr.loadModel(fileName);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM THE SPREADSHEET ====");
			message += listing;
			return message;
		}

		if (oper.equals("load")) {
		    message += Feedback.print(mode, "   Uploading generated file.");
		    rdf.loadLocalFile(mode, fileName, KB_FORMAT);
		    message += Feedback.println(mode, "");
		    message += Feedback.println(mode, " ");
		    message += Feedback.println(mode, "   Triples after [loadXLS]: " + rdf.totalTriples());
		}

	    return message;
	}
}
