package org.hadatac.metadata.loader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.io.FileUtils;
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
	
	public static String generateTTL(int mode, RDFContext rdf, String xlsName) {

		String message = "";
		message += Feedback.println(mode, "   Triples before [loadXLS]: " + rdf.totalTriples());
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Parsing spreadsheet " + xlsName);
		message += Feedback.println(mode, " ");
		
		String ttl = "";
		
		//Create Workbook instance holding reference to .xlsx file
		FileInputStream file;
		try {
			file = new FileInputStream(new File(xlsName));

			XSSFWorkbook workbook;
			try {
				// print the registered list of name spaces 
				NameSpaces ns = NameSpaces.getInstance();
				ttl = ttl + ns.printNameSpaceList();
				
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
				
				file.close();

			} catch (IOException e) {
				message += Feedback.println(mode, "[ERROR]: Could not open file  " + xlsName + " as an XLS spreadsheet");
				//e.printStackTrace();
			}
		
		} catch (FileNotFoundException e) {
			message += Feedback.println(mode, "[ERROR]: Could not open file " + xlsName);
			//e.printStackTrace();
		}
		
		String fileName = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			fileName = TTL_DIR + "HASNetO-" + timeStamp + ".ttl";
			FileUtils.writeStringToFile(new File(fileName), ttl);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + fileName + " and stored locally.");
		message += Feedback.print(mode, "   Uploading generated file.");
		rdf.loadLocalFile(mode, fileName, KB_FORMAT);
		message += Feedback.println(mode, "");
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Triples after [loadXLS]: " + rdf.totalTriples());
		return message;
	}
}
