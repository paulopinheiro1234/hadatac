package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.api.MetadataFactory;
import org.hadatac.utils.CollectionUtil;


public class DPLGenerator extends BaseGenerator {
    
	public DPLGenerator(DataFile dataFile) {
		super(dataFile);
	}

	@Override
	public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
		//System.out.println("inside DPLGenerator.createRow ");
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
	public void createRows() throws Exception {
		//System.out.println("inside DPLGenerator.createRows() ");
		if (records == null) {
			return;
		}

		int rowNumber = 0;
		int skippedRows = 0;
		Record lastRecord = null;
		for (Record record : records) {
			if (lastRecord != null && record.equals(lastRecord)) {
				skippedRows++;
			} else {
				Map<String, Object> tempRow = createRow(record, ++rowNumber);
				if (tempRow != null) {
					rows.add(tempRow);
					lastRecord = record;
				}
			}
		}
		if (skippedRows > 0) {
			System.out.println("Skipped rows: " + skippedRows);
		}
	}

	@Override
	public boolean commitRowsToTripleStore(List<Map<String, Object>> rows) {
		for (Map<String, Object> row : rows) {
			for (Map.Entry<String, Object> entry : row.entrySet()) {
			}
		}
		Model model = MetadataFactory.createModel(rows, getNamedGraphUri());
		int numCommitted = MetadataFactory.commitModelToTripleStore(
				model, CollectionUtil.getCollectionPath(
						CollectionUtil.Collection.METADATA_GRAPH));

		if (numCommitted > 0) {
			logger.println(String.format("%d triple(s) have been committed to triple store", model.size()));
		}

		return true;
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
