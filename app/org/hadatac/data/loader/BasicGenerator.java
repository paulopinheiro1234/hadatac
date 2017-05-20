package org.hadatac.data.loader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

public abstract class BasicGenerator {
	protected Iterable<CSVRecord> records = null;
	protected List< Map<String, Object> > rows = new ArrayList<Map<String, Object>>();
	protected HashMap<String, String> mapCol = new HashMap<String, String>();
	
	public BasicGenerator(File file) {
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initMapping();
	}
	
	abstract void initMapping();
    abstract Map<String, Object> createRow(CSVRecord rec, int rownumber);
    
    public List< Map<String, Object> > createRows() {
    	rows.clear();
    	int rownumber = 0;
    	for (CSVRecord record : records) {
    		rows.add(createRow(record, ++rownumber));
    	}
    	return rows;
    }
    
    public String toString() {
    	if(rows.isEmpty()) { 
    		return "";
    	}
    	
    	String result = "";
    	result = String.join(",", rows.get(0).keySet());
    	for (Map<String, Object> row : rows) {
    		List<String> values = new ArrayList<String>();
    		for (String colName : rows.get(0).keySet()) {
    			if (row.containsKey(colName)) {
    				values.add((String)row.get(colName));
    			}
    			else {
    				values.add("");
    			}
    		}
    		result += "\n";
    		result += String.join(",", values);
    	}
    	
    	return result;
    }
}