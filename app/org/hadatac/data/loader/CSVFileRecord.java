package org.hadatac.data.loader;

import org.apache.commons.csv.CSVRecord;

public class CSVFileRecord implements Record {

    private CSVRecord rec;

    public CSVFileRecord(CSVRecord rec) {
        this.rec = rec;
    }

    @Override
    public String getValueByColumnName(String columnName) {
        String value = "";
        try {
            value = rec.get(columnName);
	    //if (columnName.equals("originalID")) {
	    //System.out.println("CSVFileRecord: getValuedByColumnName = [" + value + "]");
	    //}
        } catch (Exception e) {
            System.out.println("row " + rec.getRecordNumber() + ", column name " + columnName + " not found!");
        }

        return value;
    }

    @Override
    public String getValueByColumnIndex(int index) {
        String value = "";
        try {
            value = rec.get(index);
        } catch (Exception e) {
            System.out.println("row " + rec.getRecordNumber() + ", column index " + index + " not valid!");
        }

        return value;
    }

    @Override
    public int size() {
        return rec.size();
    }
}
