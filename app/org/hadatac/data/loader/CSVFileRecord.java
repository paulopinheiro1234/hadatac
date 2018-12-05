package org.hadatac.data.loader;

import org.apache.commons.csv.CSVRecord;

public class CSVFileRecord implements Record {

    private CSVRecord rec;

    public CSVFileRecord(CSVRecord rec) {
        this.rec = rec;
    }

    @Override
    public String getValueByColumnName(String colomnName) {
        String value = "";
        try {
            value = rec.get(colomnName).trim();
        } catch (Exception e) {
            // System.out.println("row " + rec.getRecordNumber() + ", column name " + colomnName + " not found!");
        }

        return value;
    }

    @Override
    public String getValueByColumnIndex(int index) {
        String value = "";
        try {
            value = rec.get(index).trim();
        } catch (Exception e) {
            // System.out.println("row " + rec.getRecordNumber() + ", column index " + index + " not valid!");
        }

        return value;
    }

    @Override
    public int size() {
        return rec.size();
    }
}
