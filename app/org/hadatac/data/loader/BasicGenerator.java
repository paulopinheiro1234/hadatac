package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class BasicGenerator {

	protected Iterable<Record> records = null;
	protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
	protected HashMap<String, String> mapCol = new HashMap<String, String>();
	protected String fileName = "";

	public BasicGenerator() {}

	public BasicGenerator(RecordFile file) {
		records = file.getRecords();
		fileName = file.getFile().getName();
		initMapping();
	}

	abstract void initMapping();

	abstract Map<String, Object> createRow(Record rec, int row_number) throws Exception;

	public List<Map<String, Object>> getRows() {
		return rows;
	}

	public List<Map<String, Object>> createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (Record record : records) {
			Map<String, Object> tempRow = createRow(record, ++row_number);
			if (tempRow != null) {
				rows.add(tempRow);
			}
		}
		return rows;
	}

	public String toString() {
		if (rows.isEmpty()) {
			return "";
		}
		String result = "";
		result = String.join(",", rows.get(0).keySet());
		for (Map<String, Object> row : rows) {
			List<String> values = new ArrayList<String>();
			for (String colName : rows.get(0).keySet()) {
				if (row.containsKey(colName)) {
					values.add((String) row.get(colName));
				} else {
					values.add("");
				}
			}
			result += "\n";
			result += String.join(",", values);
		}
		return result;
	}
}