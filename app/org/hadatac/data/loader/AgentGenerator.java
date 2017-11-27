package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.hadatac.utils.ConfigProp;

public class AgentGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	private int counter = 1; //starting index number
	
	public AgentGenerator(File file) {
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
        mapCol.put("CPI1FName", "Co-PI 1 First Name");
        mapCol.put("CPI1LName", "Co-PI 1 Last Name");
        mapCol.put("CPI1Email", "Co-PI 1 Email");
        mapCol.put("CPI2FName", "Co-PI 2 First Name");
        mapCol.put("CPI2LName", "Co-PI 2 Last Name");
        mapCol.put("CPI2Email", "Co-PI 2 Email");
        mapCol.put("contactFName", "Contact First Name");
        mapCol.put("contactLName", "Contact Last Name");
        mapCol.put("contactEmail", "Contact Email");
        mapCol.put("createdDate","Project Created Date");
        mapCol.put("updatedDate","Project Last Updated Date");
        mapCol.put("DCAccessBool", "DC Access?");
	}
	
	/*private String getUri(CSVRecord rec) { 
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
	}*/
	
	private String getInstitutionUri(CSVRecord rec) {
		return kbPrefix + "ORG-" + rec.get(mapCol.get("institution")).replaceAll(" ", "-").replaceAll(" ", "-").replaceAll(",", "").replaceAll("'", ""); 
	}
	
	private String getInstitutionName(CSVRecord rec) {
		return rec.get(mapCol.get("institution")); 
	}
	
	private String getPIUri(CSVRecord rec) {
		return kbPrefix + "PER-" + rec.get(mapCol.get("PI")).replaceAll(" ", "-"); 
	}
	
	private String getPIFullName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")); 
	}
	
	private String getPIGivenName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")).substring(0, getPIFullName(rec).indexOf(' ')); 
	}
	
	private String getPIFamilyName(CSVRecord rec) {
		return rec.get(mapCol.get("PI")).substring(getPIFullName(rec).indexOf(' ')+1); 
	}
	
	private String getPIMBox(CSVRecord rec) {
		return rec.get(mapCol.get("PIEmail")); 
	}
	
	private String getCPI1Uri(CSVRecord rec) {
		return kbPrefix + "PER-" + getCPI1FullName(rec).replaceAll(" ", "-"); 
	}
	
	private String getCPI1FullName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI1FName")) + " " + rec.get(mapCol.get("CPI1LName"));
	}
	
	private String getCPI1GivenName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI1FName")); 
	}
	
	private String getCPI1FamilyName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI1LName")); 
	}
	
	private String getCPI1MBox(CSVRecord rec) {
		return rec.get(mapCol.get("CPI1Email")); 
	}
    
	private String getCPI2Uri(CSVRecord rec) {
		return kbPrefix + "PER-" + getCPI2FullName(rec).replaceAll(" ", "-"); 
	}
	
	private String getCPI2FullName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI2FName")) + " " + rec.get(mapCol.get("CPI2LName"));
	}
	
	private String getCPI2GivenName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI2FName")); 
	}
	
	private String getCPI2FamilyName(CSVRecord rec) {
		return rec.get(mapCol.get("CPI2LName")); 
	}
	
	private String getCPI2MBox(CSVRecord rec) {
		return rec.get(mapCol.get("CPI2Email")); 
	}
	
	private String getContactUri(CSVRecord rec) {
		return kbPrefix + "PER-" + getContactFullName(rec).replaceAll(" ", "-"); 
	}
	
	private String getContactFullName(CSVRecord rec) {
		return rec.get(mapCol.get("contactFName")) + " " + rec.get(mapCol.get("contactLName"));
	}
	
	private String getContactGivenName(CSVRecord rec) {
		return rec.get(mapCol.get("contactFName")); 
	}
	
	private String getContactFamilyName(CSVRecord rec) {
		return rec.get(mapCol.get("contactLName")); 
	}
	
	private String getContactMBox(CSVRecord rec) {
		return rec.get(mapCol.get("contactEmail")); 
	}
	
    public Map<String, Object> createPIRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getPIUri(rec));
    	row.put("a", "prov:Person");
    	row.put("foaf:name", getPIFullName(rec));
    	row.put("rdfs:comment", "PI from " + getInstitutionName(rec));
    	row.put("foaf:familyName", getPIFamilyName(rec));
    	row.put("foaf:givenName", getPIGivenName(rec));
    	row.put("foaf:mbox", getPIMBox(rec));
    	row.put("foaf:member", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
    }
    
    public Map<String, Object> createCPI1Row(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCPI1Uri(rec));
    	row.put("a", "prov:Person");
    	row.put("foaf:name", getCPI1FullName(rec));
    	row.put("rdfs:comment", "Co-PI from " + getInstitutionName(rec));
    	row.put("foaf:familyName", getCPI1FamilyName(rec));
    	row.put("foaf:givenName", getCPI1GivenName(rec));
    	row.put("foaf:mbox", getCPI1MBox(rec));
    	row.put("foaf:member", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
    }

    
    public Map<String, Object> createCPI2Row(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getCPI2Uri(rec));
    	row.put("a", "prov:Person");
    	row.put("foaf:name", getCPI2FullName(rec));
    	row.put("rdfs:comment", "Co-PI from " + getInstitutionName(rec));
    	row.put("foaf:familyName", getCPI2FamilyName(rec));
    	row.put("foaf:givenName", getCPI2GivenName(rec));
    	row.put("foaf:mbox", getCPI2MBox(rec));
    	row.put("foaf:member", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
    }

    public Map<String, Object> createContactRow(CSVRecord rec) {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getContactUri(rec));
    	row.put("a", "prov:Person");
    	row.put("foaf:name", getContactFullName(rec));
    	row.put("rdfs:comment", "Co-PI from " + getInstitutionName(rec));
    	row.put("foaf:familyName", getContactFamilyName(rec));
    	row.put("foaf:givenName", getContactGivenName(rec));
    	row.put("foaf:mbox", getContactMBox(rec));
    	row.put("foaf:member", getInstitutionUri(rec));
    	counter++;
    	
    	return row;
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
    
	@Override    
    public List< Map<String, Object> > createRows() {
		boolean duplicate=false;
    	rows.clear();
    	// Currently using an inefficient way to check if row already exists in the list of rows; This should be addressed in the future
    	for (CSVRecord record : records) {
    		if(record.get(mapCol.get("PI")).length()>0){
    			System.out.println("Creating PI Row:" + record.get(mapCol.get("PI")) + ":");
    			duplicate=false;
    			for (Map<String, Object> row : rows){
        			//System.out.println("Comparing: " + getPIUri(record));
        			//System.out.println("With: " + row.get("hasURI"));
    				if(row.get("hasURI").equals(getPIUri(record))){
    					System.out.println("Found Duplicate: " + getPIUri(record));
    					duplicate=true;
    					break;
    				}
    			}
    			if(!duplicate){
        			System.out.println("Didn't Find Duplicate, adding PI " + getPIUri(record) + " row to list of rows.");
        			rows.add(createPIRow(record));
    			}
    		}
    		if(record.get(mapCol.get("CPI1FName")).length()>0){
    			System.out.println("Creating CPI1 Row:" + record.get(mapCol.get("CPI1FName")) + ":");
    			duplicate=false;
    			for (Map<String, Object> row : rows){
        			//System.out.println("Comparing: " + getCPI1Uri(record));
        			//System.out.println("With: " + row.get("hasURI"));
    				if(row.get("hasURI").equals(getCPI1Uri(record))){
    					System.out.println("Found Duplicate: " + getCPI1Uri(record));
    					duplicate=true;
    					break;
    				}
    			}
    			if(!duplicate){
        			System.out.println("Didn't Find Duplicate, adding CPI1 " + getCPI1Uri(record) + " row to list of rows.");
        			rows.add(createCPI1Row(record));
    			}
    			
    		}
    		if(record.get(mapCol.get("CPI2FName")).length()>0){
    			System.out.println("Creating CPI2 Row:" + record.get(mapCol.get("CPI2FName")) + ":");
    			duplicate=false;
    			for (Map<String, Object> row : rows){
        			//System.out.println("Comparing: " + getCPI2Uri(record));
        			//System.out.println("With: " + row.get("hasURI"));
    				if(row.get("hasURI").equals(getCPI2Uri(record))){
    					System.out.println("Found Duplicate: " + getCPI2Uri(record));
    					duplicate=true;
    					break;
    				}
    			}
    			if(!duplicate){
    				System.out.println("Didn't Find Duplicate, adding CPI2 " + getCPI2Uri(record) + " row to list of rows.");
    				rows.add(createCPI2Row(record));
    			}
    		}
    		if(record.get(mapCol.get("contactFName")).length()>0){
    			System.out.println("Creating Contact Row:" + record.get(mapCol.get("contactFName")) + ":");
    			duplicate=false;
    			for (Map<String, Object> row : rows){
        			//System.out.println("Comparing: " + getContactUri(record));
        			//System.out.println("With: " + row.get("hasURI"));
    				if(row.get("hasURI").equals(getContactUri(record))){
    					System.out.println("Found Duplicate: " + getContactUri(record));
    					duplicate=true;
    					break;
    				}
    			}
    			if(!duplicate){
    				System.out.println("Didn't Find Duplicate, adding Contact " + getContactUri(record) + " row to list of rows.");
    				rows.add(createContactRow(record));
    			}
    		}
    		if(record.get(mapCol.get("institution")).length()>0){
    			System.out.println("Creating Institution Row:" + record.get(mapCol.get("institution")) + ":");
    			duplicate=false;
    			for (Map<String, Object> row : rows){
        			//System.out.println("Comparing: " + getInstitutionUri(record));
        			//System.out.println("With: " + row.get("hasURI"));
    				if(row.get("hasURI").equals(getInstitutionUri(record))){
    					System.out.println("Found Duplicate: " + getInstitutionUri(record));
    					duplicate=true;
    					break;
    				}
    			}
    			if(!duplicate){
    				System.out.println("Didn't Find Duplicate, adding Contact " + getInstitutionUri(record) + " row to list of rows.");
    				rows.add(createInstitutionRow(record));
    			}
    		}
    	}
    	//System.out.println(rows.toString());
    	return rows;
    }

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}