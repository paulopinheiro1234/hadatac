package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.http.ConfigUtils;
import org.hadatac.utils.Templates;

import org.apache.commons.csv.CSVRecord;

import play.Play;

public class StudyGenerator extends BasicGenerator {
	final String kbPrefix = ConfigUtils.getKbPrefix();
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
		return kbPrefix + "STD-" + rec.get(mapCol.get("studyID")); 
	}
	
	private String getType() {
		return "hasco:Study";
	}
	
	private String getTitle(CSVRecord rec) { 
		return rec.get(mapCol.get("studyTitle")); 
	}
	
	private String getAims(CSVRecord rec) { 
		return rec.get(mapCol.get("studyAims")) ; 
	}
	
	private String getSignificance(CSVRecord rec) { 
		return rec.get(mapCol.get("studySignificance")) ; 
	}
	
	private String getInstitutionUri(CSVRecord rec) {
		return kbPrefix + "ORG-" + rec.get(mapCol.get("institution")).replaceAll(" ", "-").replaceAll(",", "").replaceAll("'", ""); 
	}
	
/*	private String getInstitutionName(CSVRecord rec) {
		return rec.get(mapCol.get("institution")); 
	}
	*/
	private String getAgentUri(CSVRecord rec) {
		return kbPrefix + "PER-" + rec.get(mapCol.get("PI")).replaceAll(" ", "-"); 
	}
	
	private String getExtSource(CSVRecord rec) {
		System.out.println("[debug] getExtSource...");
		return rec.get(mapCol.get("externalSource")); 
	}

/*	private String getAgentFullName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")); 
	}
	
	private String getAgentGivenName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")).substring(0, getAgentFullName(rec).indexOf(' ')); 
	}
	
	private String getAgentFamilyName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")).substring(getAgentFullName(rec).indexOf(' ')+1); 
	}
	
	private String getAgentMBox(CSVRecord rec) {
		return rec.get(mapCol.get("PIEmail")); 
	}
*/    
	@Override
    public Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri(rec));
    	row.put("a", getType());
    	row.put("rdfs:label", getTitle(rec));
    	row.put("skos:definition", getAims(rec));
    	row.put("rdfs:comment", getSignificance(rec));
    	if(rec.get(mapCol.get("PI")).length()>0){
    		row.put("hasco:hasAgent", getAgentUri(rec));
    	}
    	if(rec.get(mapCol.get("institution")).length()>0){
    		row.put("hasco:hasInstitution", getInstitutionUri(rec));
    	}
	if(rec.get(mapCol.get("externalSource")).length()>0){
		row.put("hasco:hasExternalSource", getExtSource(rec));
	}
    	counter++;
    	
    	return row;
    }
    
	/*public Map<String, Object> createAgentRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getAgentUri(rec));
    	row.put("a", "prov:Person");
    	row.put("foaf:name", getAgentFullName(rec));
    	row.put("rdfs:comment", "PI from " + getInstitutionName(rec));
    	row.put("foaf:familyName", getAgentFamilyName(rec));
    	row.put("foaf:givenName", getAgentGivenName(rec));
    	row.put("foaf:mbox", getAgentMBox(rec));
    	row.put("foaf:member", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createAgentRows() {
    	rows.clear();
    	for (CSVRecord record : records) {
    		rows.add(createAgentRow(record));
    	}
    	return rows;
    }
    
    public Map<String, Object> createInstitutionRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getInstitutionUri(rec));
    	row.put("a", "prov:Organization");
    	row.put("foaf:name", getInstitutionName(rec));
    	row.put("rdfs:comment", getInstitutionName(rec) + " Institution");
    	counter++;
    	
    	return row;
    }
    
    public List< Map<String, Object> > createInstitutionRows() {
    	rows.clear();
    	for (CSVRecord record : records) {
    		rows.add(createInstitutionRow(record));
    	}
    	return rows;
    }*/
}

