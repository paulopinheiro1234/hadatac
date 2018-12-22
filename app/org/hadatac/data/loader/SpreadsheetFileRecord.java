package org.hadatac.data.loader;

import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import java.text.SimpleDateFormat;

public class SpreadsheetFileRecord implements Record {
    Row row;

    public SpreadsheetFileRecord(Row row) {
        this.row = row;
    }

    @Override
    public String getValueByColumnName(String columnName) {
        String value = "";
        try {
            Cell c = row.getCell(getColumnIndexByName(columnName));
	    value = getCellValueAsString(c);
	    //if (columnName.equals("originalID")) {
	    //System.out.println("SpreadsheetFileRecord: getCell = [" + c.getStringCellValue() + "]");
	    //System.out.println("SpreadsheetFileRecord: getCellType = [" + c.getCellType() + "]");
	    //System.out.println("SpreadsheetFileRecord: getValuedByColumnName = [" + value + "]");
	    //}
        } catch (Exception e) {
            System.out.println("row " + row.getRowNum() + ", column name " + columnName + " not found!");
        }

        return value;
    }

    @Override
    public String getValueByColumnIndex(int index) {
        String value = "";
        try {
            value = row.getCell(index).toString();
        } catch (Exception e) {
            System.out.println("row " + row.getRowNum() + ", column index " + index + " not valid!");
        }

        return value;
    }

    @Override
    public int size() {
        return row.getLastCellNum() + 1;
    }

    private int getColumnIndexByName(String columnName) {
        Sheet sheet = row.getSheet();
        Row firstRow = sheet.getRow(sheet.getFirstRowNum());

        for(int i = firstRow.getFirstCellNum(); i <= firstRow.getLastCellNum(); i++) {
            if (firstRow.getCell(i).toString().equals(columnName)) {
                return i;
            }	
        }

        return -1;
    }

    public static String getCellValueAsString(Cell cell) {
        String strCellValue = null;
        if (cell != null) {
            switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                strCellValue = cell.toString();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    strCellValue = dateFormat.format(cell.getDateCellValue());
                } else {
                    Double value = cell.getNumericCellValue();
                    Long longValue = value.longValue();
                    strCellValue = new String(longValue.toString());
                }
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                strCellValue = new String(new Boolean(cell.getBooleanCellValue()).toString());
                break;
            case Cell.CELL_TYPE_BLANK:
                strCellValue = "";
                break;
            }
        }
        return strCellValue;
    }

}




