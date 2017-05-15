package org.hadatac.metadata.loader;

import java.util.Iterator;
import java.util.Vector;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.metadata.model.SpreadsheetParsingResult;
import org.hadatac.utils.Feedback;

public class SheetProcessing {

	public static SpreadsheetParsingResult generateTTL(int mode, XSSFSheet sheet) {

		String shttl = "";
		String message = "";
		
		boolean firstRow = true;
		Vector<String> predicates = new Vector<String>();

		// Iterate through each row in the sheet
		Iterator<Row> rowIterator = sheet.iterator();
		//System.out.println("#of rows: " + sheet.getLastRowNum());
		boolean blankRow = false;
		int processedRows = 0;

		while (rowIterator.hasNext() & !blankRow) {

			Row row = rowIterator.next();
			//System.out.println("Row number: " + row.getRowNum());
			processedRows++;

			//Cell firstCell = row.getCell(0);
			//System.out.println("First cell: " + firstCell.getStringCellValue() + " " + firstCell.getCellType());

			//For each row, iterate through all the columns
			Iterator<Cell> cellIterator = row.cellIterator();

			boolean hasOutput = false;
			blankRow = true;
			
			// Iterate through each cell in a row, one by one
			while (cellIterator.hasNext()) {
				Cell cell = cellIterator.next();

				//Check the cell type and format accordingly
				//System.out.println(currentCell + " " + cell.getCellType());
				switch (cell.getCellType()) {

		   		    case Cell.CELL_TYPE_NUMERIC:
		   		    	//System.out.println("NUMERIC");
		   		    	if (predicates.elementAt(cell.getColumnIndex()).equals("vstoi:hasSerialNumber")) {	                        		
		   		    		shttl = shttl + "   " + predicates.elementAt(cell.getColumnIndex()) + " \"";
		   		    		shttl = shttl + String.format("%.0f", cell.getNumericCellValue());
		   		    		shttl = shttl + "\";\n";
		   		    	} else {
		   		    		shttl = shttl + "   " + predicates.elementAt(cell.getColumnIndex()) + " " + cell.getNumericCellValue() + ";\n";
		   		    	}
		   		    	blankRow = false;
		   		    	hasOutput = true;
		   		    	break;
		   		    case Cell.CELL_TYPE_STRING:
		   		    case Cell.CELL_TYPE_FORMULA:
		   		    	//System.out.println("STRING");
		   		    	if (firstRow) {
		   		    		String newPredicate = cell.getStringCellValue();
		   		    		ValueCellProcessing.validateNameSpace(newPredicate);
		   		    		predicates.add(cell.getColumnIndex(), newPredicate);
		   		    	} else {
		   		    		shttl = shttl + ValueCellProcessing.exec(cell, predicates);
		   		    		hasOutput = true;
		   		    	}
		   		    	blankRow = false;
		   		    	break;
		   		    	/* default: 
	                    	   System.out.println("NEITHER NUMERIC NOR STRING");
	                    	   break; */
				}
			}
			if (hasOutput) {
				shttl = shttl + ".\n";
			}

			if (firstRow) {
				// after processing all the cells in the first row, prints all identified predicates as a turtle comment 
				shttl = shttl + "# properties: ";
				for (String pred : predicates) {
					shttl = shttl + "[" + pred + "] ";
				}
				shttl = shttl + "\n";
			}
			firstRow = false;
		}
		
		message += Feedback.println(mode, "processed " + processedRows + " row(s).");
		SpreadsheetParsingResult result = new SpreadsheetParsingResult(message, shttl);
		return result;
	}

}
