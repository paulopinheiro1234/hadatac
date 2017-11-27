package org.hadatac.data.loader;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import org.apache.commons.csv.CSVRecord;
import org.hadatac.utils.ConfigProp;


public class DASchemaGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	String startTime = "";
	String SDDName = "";

	public DASchemaGenerator(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		SDDName = fileName.replace("SDD-", "").replace(".csv", "");
		rows.clear();
		int row_number = 0;
		for (CSVRecord record : records) {
			rows.add(createRow(record, ++row_number));
			break;
		}
		return rows;
	}

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", kbPrefix + "DAS-" + SDDName);
		row.put("a", "hasco:DASchema");
		row.put("rdfs:label", "Schema for EPI Data Acquisition");
		row.put("rdfs:comment", "");
		return row;
	}
}