package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;

public class StudyGenerator extends BasicGenerator {
	final String kbPrefix = "chear-kb:";
	private int counter = 1; //starting index number
	
	public StudyGenerator(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
		mapCol.clear();
        mapCol.put("studyID", "CHEAR Project ID");
        mapCol.put("studyTitle", "Title");
        mapCol.put("studyAims", "Specific Aims");
        mapCol.put("studySignificance", "Significance");
        mapCol.put("numSubjects", "Number of Participants");
        mapCol.put("numSamples", "Number of Sample IDs");
        mapCol.put("institution", "Institution");
        mapCol.put("PI", "Principal Investigator");
        mapCol.put("PIAddress", "PI Address");
        mapCol.put("PICity", "PI City");
        mapCol.put("PIState", "PI State");
        mapCol.put("PIZipCode", "PI Zip Code");
        mapCol.put("PIEmail", "Email");
        mapCol.put("PIPhone", "PI Phone");
        mapCol.put("DCAccessBool", "DC Access?");
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
		return kbPrefix + "ORG-" + rec.get(mapCol.get("institution")).replaceAll(" ", "-"); 
	}
	
	private String getInstitutionName(CSVRecord rec) {
		return rec.get(mapCol.get("institution")); 
	}
	
	private String getAgentUri(CSVRecord rec) {
		return kbPrefix + "PER-" + rec.get(mapCol.get("PI")).replaceAll(" ", "-"); 
	}
	
	private String getAgentFullName(CSVRecord rec) {
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
    
	@Override
    public Map<String, Object> createRow(CSVRecord rec, int rownumber) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri(rec));
    	row.put("a", getType());
    	row.put("rdfs:label", getTitle(rec));
    	row.put("skos:definition", getAims(rec));
    	row.put("rdfs:comment", getSignificance(rec));
    	row.put("hasco:hasAgent", getAgentUri(rec));
    	row.put("hasco:hasInstitution", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public Map<String, Object> createAgentRow(CSVRecord rec) {
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
    }
}