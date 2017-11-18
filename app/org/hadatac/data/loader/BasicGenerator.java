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

import play.mvc.Controller;

public abstract class BasicGenerator extends Controller {

	protected Iterable<CSVRecord> records = null;
	protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
	protected HashMap<String, String> mapCol = new HashMap<String, String>();
	protected String fileName = "";

	public BasicGenerator() {}

	public BasicGenerator(File file) {
		try {
			records = CSVFormat.DEFAULT.withHeader().parse(new FileReader(file));
			fileName = file.getName();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		initMapping();
	}

	abstract void initMapping();

	abstract Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception;

	public List<Map<String, Object>> getRows() {
		return rows;
	}
	
	public String getValueByColumnName(CSVRecord rec, String colomnName) {
		String value = "";
		try {
			value = rec.get(colomnName);
		} catch (Exception e) {
			System.out.println("column " + colomnName + " not found!");
		}
		
		return value;
	}

	public List<Map<String, Object>> createRows() throws Exception {
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
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