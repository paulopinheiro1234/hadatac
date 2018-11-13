package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;


public class SampleCollectionGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();

	public SampleCollectionGenerator(RecordFile file) {
		super(file);
	}

	@Override
	public void initMapping() {
		mapCol.clear();
		if (this.fileName.startsWith("STD")){
			mapCol.put("studyID", Templates.STUDYID);
		} else if (this.fileName.startsWith("SID")){
			mapCol.put("studyID", Templates.SAMPLESTUDYID);
		} else {
			mapCol.put("studyID", "Study ID");
		}
	}

	private String getStudyUri(Record rec) {
		return kbPrefix + "STD-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getUri(Record rec) {
		return kbPrefix + "SC-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getLabel(Record rec) {
		return "Sample Collection of Study " + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	@Override
	public Map<String, Object> createRow(Record rec, int rownumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", "hasco:SampleCollection");
		row.put("rdfs:label", getLabel(rec));
		row.put("hasco:isMemberOf", getStudyUri(rec));
		
		setStudyUri(URIUtils.replacePrefixEx(getStudyUri(rec)));
		
		return row;
	}

	@Override
	public String getTableName() {
		return "SampleCollection";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in SampleCollectionGenerator: " + e.getMessage();
	}
}
