package org.hadatac.data.loader;

import java.lang.String;
import java.util.HashMap;
import java.util.Map;

import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

public class StudyGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();

	public StudyGenerator(RecordFile file) {
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

	private String getUri(Record rec) { 
		return kbPrefix + "STD-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getType() {
		return "hasco:Study";
	}

	private String getTitle(Record rec) {
		return rec.getValueByColumnName(mapCol.get("studyTitle"));
	}

	private String getAims(Record rec) {
		return rec.getValueByColumnName(mapCol.get("studyAims"));
	}

	private String getSignificance(Record rec) {
		return rec.getValueByColumnName(mapCol.get("studySignificance"));
	}

	private String getInstitutionUri(Record rec) {
		return kbPrefix + "ORG-" + rec.getValueByColumnName(mapCol.get("institution")).replaceAll(" ", "-").replaceAll(",", "").replaceAll("'", ""); 
	}

	private String getAgentUri(Record rec) {
		return kbPrefix + "PER-" + rec.getValueByColumnName(mapCol.get("PI")).replaceAll(" ", "-"); 
	}

	private String getExtSource(Record rec) {
		return rec.getValueByColumnName(mapCol.get("externalSource")); 
	}

	@Override
	public Map<String, Object> createRow(Record rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", getType());
		row.put("rdfs:label", getTitle(rec));
		row.put("skos:definition", getAims(rec));
		row.put("rdfs:comment", getSignificance(rec));
		if(rec.getValueByColumnName(mapCol.get("PI")).length() > 0) {
			row.put("hasco:hasAgent", getAgentUri(rec));
		}
		if(rec.getValueByColumnName(mapCol.get("institution")).length() > 0) {
			row.put("hasco:hasInstitution", getInstitutionUri(rec));
		}
		if(rec.getValueByColumnName(mapCol.get("externalSource")).length() > 0) {
			row.put("hasco:hasExternalSource", getExtSource(rec));
		}
		
		setStudyUri(URIUtils.replacePrefixEx(getUri(rec)));

		return row;
	}

	@Override
	public String getTableName() {
		return "Study";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "Error in StudyGenerator: " + e.getMessage();
	}
}

