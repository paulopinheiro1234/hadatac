package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class GeneralGenerator extends BasicGenerator {	
	public GeneralGenerator() {}
	
	@Override
	void initMapping() {}
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	return new HashMap<String, Object>();
    }
    
    public void addRow(Map<String, Object> row) {
    	rows.add(row);
    }
}