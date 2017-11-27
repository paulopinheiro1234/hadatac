package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;


public class SampleCollectionGenerator extends BasicGenerator {

	final String kbPrefix = ConfigProp.getKbPrefix();

	public SampleCollectionGenerator(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		if (this.fileName.startsWith("STD")){
			mapCol.put("studyID", Templates.STUDYID);
		} else if (this.fileName.startsWith("SID")){
			mapCol.put("studyID", Templates.SAMPLESTUDYID);
		} else {
			mapCol.put("studyID", "Study ID");
		}
	}

	private String getStudyUri(CSVRecord rec) {
		return kbPrefix + "STD-" + rec.get(mapCol.get("studyID"));
	}

	private String getUri(CSVRecord rec) {
		return kbPrefix + "SC-" + rec.get(mapCol.get("studyID"));
	}

	private String getLabel(CSVRecord rec) {
		return "Sample Collection of Study " + rec.get(mapCol.get("studyID"));
	}

	@Override
	Map<String, Object> createRow(CSVRecord rec, int rownumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", "hasco:SampleCollection");
		row.put("rdfs:label", getLabel(rec));
		//row.put("hasco:hasSize", 0);
		row.put("hasco:isSampleCollectionOf", getStudyUri(rec));

		return row;
	}

}
