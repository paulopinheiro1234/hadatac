package org.hadatac.data.loader;

import java.lang.String;
import java.util.Map;

public class GeneralGenerator extends BasicGenerator {
	
	private String tableName;
	
	public GeneralGenerator(String tableName) {
		this.tableName = tableName;
	}
	
	@Override
	void initMapping() {}
    
    public void addRow(Map<String, Object> row) {
    	rows.add(row);
    }

	@Override
	public String getTableName() {
		return tableName;
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in GeneralGenerator: " + e.getMessage();
	}
}