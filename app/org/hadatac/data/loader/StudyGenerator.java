package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

import org.apache.commons.csv.CSVRecord;

public class StudyGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	private int counter = 1; //starting index number

	public StudyGenerator(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("studyID", Templates.STUDYID);
		mapCol.put("studyTitle", Templates.STUDYTITLE);
		mapCol.put("studyAims", Templates.STUDYAIMS);
		mapCol.put("studySignificance", Templates.STUDYSIGNIFICANCE);
		mapCol.put("numSubjects", Templates.NUMSUBJECTS);
		mapCol.put("numSamples", Templates.NUMSAMPLES);
		mapCol.put("institution", Templates.INSTITUTION);
		mapCol.put("PI", Templates.PI);
		mapCol.put("PIAddress", Templates.PIADDRESS);
		mapCol.put("PICity", Templates.PICITY);
		mapCol.put("PIState", Templates.PISTATE);
		mapCol.put("PIZipCode", Templates.PIZIPCODE);
		mapCol.put("PIEmail", Templates.PIEMAIL);
		mapCol.put("PIPhone", Templates.PIPHONE);
		mapCol.put("CPI1FName", Templates.CPI1FNAME);
		mapCol.put("CPI1LName", Templates.CPI1LNAME);
		mapCol.put("CPI1Email", Templates.CPI1EMAIL);
		mapCol.put("CPI2FName", Templates.CPI2FNAME);
		mapCol.put("CPI2LName", Templates.CPI2LNAME);
		mapCol.put("CPI2Email", Templates.CPI2EMAIL);
		mapCol.put("contactFName", Templates.CONTACTFNAME);
		mapCol.put("contactLName", Templates.CONTACTLNAME);
		mapCol.put("contactEmail", Templates.CONTACTEMAIL);
		mapCol.put("createdDate", Templates.CREATEDDATE);
		mapCol.put("updatedDate", Templates.UPDATEDDATE);
		mapCol.put("DCAccessBool", Templates.DCACCESSBOOL);
		mapCol.put("externalSource", Templates.EXTSRC);
	}

	private String getUri(CSVRecord rec) { 
		return kbPrefix + "STD-" + getValueByColumnName(rec, mapCol.get("studyID"));
	}

	private String getType() {
		return "hasco:Study";
	}

	private String getTitle(CSVRecord rec) { 
		return getValueByColumnName(rec, mapCol.get("studyTitle"));
	}

	private String getAims(CSVRecord rec) { 
		return getValueByColumnName(rec, mapCol.get("studyAims"));
	}

	private String getSignificance(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("studySignificance"));
	}

	private String getInstitutionUri(CSVRecord rec) {
		return kbPrefix + "ORG-" + getValueByColumnName(rec, mapCol.get("institution")).replaceAll(" ", "-").replaceAll(",", "").replaceAll("'", ""); 
	}

	private String getAgentUri(CSVRecord rec) {
		return kbPrefix + "PER-" + getValueByColumnName(rec, mapCol.get("PI")).replaceAll(" ", "-"); 
	}

	private String getExtSource(CSVRecord rec) {
		return getValueByColumnName(rec, mapCol.get("externalSource")); 
	}

	@Override
	public Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", getType());
		row.put("rdfs:label", getTitle(rec));
		row.put("skos:definition", getAims(rec));
		row.put("rdfs:comment", getSignificance(rec));
		if(getValueByColumnName(rec, mapCol.get("PI")).length() > 0) {
			row.put("hasco:hasAgent", getAgentUri(rec));
		}
		if(getValueByColumnName(rec, mapCol.get("institution")).length() > 0) {
			row.put("hasco:hasInstitution", getInstitutionUri(rec));
		}
		if(getValueByColumnName(rec, mapCol.get("externalSource")).length() > 0) {
			row.put("hasco:hasExternalSource", getExtSource(rec));
		}
		counter++;

		return row;
	}
}

