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
import org.hadatac.metadata.loader.NameSpaces;
import org.hadatac.metadata.loader.SheetProcessing;

public class SpreadsheetProcessing {
	
	public static final String KB_FORMAT = "text/turtle";
	
	public static String generateTTL(String xlsName) {
		
		System.out.println("   Triples before [loadXLS]: " + Loader.getMetadataContext().totalTriples());
		System.out.println("   Parsing spreadsheet " + xlsName);
		
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
					System.out.print("   Processing sheet " + sheet.getSheetName() + "     ");
					for (int i = sheet.getSheetName().length(); i < 20; i++) {
						System.out.print(" ");
					}
					ttl = ttl + "\n# concept: " + sheet.getSheetName() + SheetProcessing.generateTTL(sheet) + "\n";
				}
				
				file.close();

			} catch (IOException e) {
				System.out.println("[ERROR]: Could not open file  " + xlsName + " as an XLS spreadsheet");
				//e.printStackTrace();
			}
		
		} catch (FileNotFoundException e) {
			System.out.println("[ERROR]: Could not open file " + xlsName);
			//e.printStackTrace();
		}
		
		String fileName = "";
		try {
			String timeStamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
			fileName = "HASNetO-" + timeStamp + ".ttl";
			FileUtils.writeStringToFile(new File(fileName), ttl);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
		
		System.out.println("   Generated " + fileName + " and stored locally.");
		System.out.print("   Uploading generated file.");
		Loader.getMetadataContext().loadLocalFile(fileName, KB_FORMAT);
		System.out.println("");
		System.out.println("   Triples after [loadXLS]: " + Loader.getMetadataContext().totalTriples());
		return ttl;
	}
}
