package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;


public class DPLGenerator extends BaseGenerator {
    
	public DPLGenerator(RecordFile file) {
		super(file);
	}

	@Override
	public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		
		for (String header : file.getHeaders()) {
		    if (!header.trim().isEmpty()) {
		        String value = rec.getValueByColumnName(header);
		        if (value != null && !value.isEmpty()) {
		            row.put(header, value);
		        }
		    }
		}
		
		if (row.containsKey("hasURI") && !row.get("hasURI").toString().trim().isEmpty()) {
		    return row;
		}
		
		return null;
	}

	@Override
	public String getTableName() {
		return "DPL";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in DPLGenerator: " + e.getMessage();
	}
}
