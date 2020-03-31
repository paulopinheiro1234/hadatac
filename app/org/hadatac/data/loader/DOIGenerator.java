package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.loader.URIUtils;

public class DOIGenerator extends BaseGenerator {

    String fileName;

    public DOIGenerator(DataFile dataFile) {
        super(dataFile);
        this.fileName = dataFile.getFileName();
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("filename", "Filename");
        mapCol.put("wasDerivedFrom", "prov:wasDerivedFrom");
    }

    private String getFilename(Record rec) {
        return rec.getValueByColumnName(mapCol.get("filename"));
    }

    private String getWasDerivedFrom(Record rec) {
        return rec.getValueByColumnName(mapCol.get("wasDerivedFrom"));
    }

    @Override
    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {
    	String da_filename = getFilename(rec);
    	String was_derived_from = getWasDerivedFrom(rec);
    	List<String> derivedList = new ArrayList<String>();
    	StringTokenizer st = new StringTokenizer(was_derived_from, ","); 
        while (st.hasMoreTokens()) {
        	derivedList.add(st.nextToken().trim()); 
        }
    	DataFile da = DataFile.findByNameAndStatus(da_filename, DataFile.PROCESSED);
    	if (da == null) {
    		dataFile.getLogger().printWarningByIdWithArgs("DOI_00007",da_filename);
    	} else {
    		da.setWasDerivedFrom(derivedList);
    		da.save();
    		dataFile.getLogger().println("DOI ingestion: updating DOI for [" + da_filename + "]");
    	}
        return null;
    }

    @Override
    public String getTableName() {
        return "DOI";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in DOIGenerator: " + e.getMessage();
    }
}

