package org.hadatac.data.loader;

import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.ss.usermodel.Row;

public class SpreadsheetFileRecord implements Record {
	Row row;

	public SpreadsheetFileRecord(Row row) {
		this.row = row;
	}
	
	@Override
	public String getValueByColumnName(String colomnName) {
		String value = "";
		try {
			value = row.getCell(getColomnIndexByName(colomnName)).toString();
		} catch (Exception e) {
			System.out.println("column " + colomnName + " not found!");
		}
		
		return value;
	}
	
	@Override
	public String getValueByColumnIndex(int index) {
		String value = "";
		try {
			value = row.getCell(index).toString();
		} catch (Exception e) {
			System.out.println("column index " + index + " not valid!");
		}
		
		return value;
	}
	
	@Override
	public int size() {
		return row.getLastCellNum() + 1;
	}
	
	private int getColomnIndexByName(String colomnName) {
		Sheet sheet = row.getSheet();
		Row firstRow = sheet.getRow(sheet.getFirstRowNum());
		
		for(int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
			if (firstRow.getCell(i).toString().equals(colomnName)) {
				return i;
			}	
		}
		
		return -1;
	}
}
