package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;

public class SampleSubjectMapper extends BasicGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	private String dataAcquisition = "";
	private int counter = 1; //starting index number

	StudyObject obj = null;

	private String studyUri = "";
	private String hasScopeUri = "";    
	private List<String> scopeUris = new ArrayList<String>();
	private List<String> spaceScopeUris = new ArrayList<String>();
	private List<String> timeScopeUris = new ArrayList<String>();
	private List<String> objectUris = new ArrayList<String>();

	public SampleSubjectMapper(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("studyID", "CHEAR_Project_ID");
		mapCol.put("originalPID", "CHEAR PID");
		mapCol.put("originalSID", "Full SID");
	}
	
	@Override
	Map<String, Object> createRow(CSVRecord rec, int rownumber) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", getUri(rec));
		row.put("a", getType(rec));
		row.put("rdfs:label", getLabel(rec));
		row.put("hasco:originalID", getOriginalID(rec));
		row.put("hasco:isSampleOf", getSubjectUri(rec));
		row.put("hasco:isObjectOf", getCollectionUri(rec));
		row.put("rdfs:comment", getLabel(rec));
		row.put("hasco:hasSamplingMethod", "");
		row.put("hasco:hasSamplingVolume", "");
		row.put("hasco:hasSamplingVolumeUnit", "");
		row.put("hasco:hasStorageTemperature", "");
		row.put("hasco:hasStorageTemperatureUnit", "");
		row.put("hasco:hasNumFreezeThaw", "");
		counter++;

		return row;
	}

	private String getSampleUri(CSVRecord rec) {
		String sampleUri = "";
		String sampleQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE {" +
				"?s hasco:originalID \"" + rec.get(mapCol.get("originalSID")) + "\"." +
				"}";
		
		try {
			Query sampleQuery = QueryFactory.create(sampleQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			if(resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				if(soln.contains("s")) {
					sampleUri = DynamicFunctions.replaceURLWithPrefix(soln.get("s").toString());
					System.out.println("Sample URI: " + sampleUri);
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		System.out.println("getSampleUri Sample:" + sampleUri);
		return sampleUri;
	}

	private String getSubjectUri(CSVRecord rec) {
		String subjectUri = "";
		String subjectQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE {" +
				"?s hasco:originalID \"" + rec.get(mapCol.get("originalPID")) + "\"." +
				"}";
		try {
			Query subjectQuery = QueryFactory.create(subjectQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			if(resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				if(soln.contains("s")){
					subjectUri = DynamicFunctions.replaceURLWithPrefix(soln.get("s").toString());
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		
		return subjectUri;
	}
	
	private int getSampleCount(String studyID){
		int count = 0;
		String sampleCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT (count(DISTINCT ?sampleURI) as ?sampleCount) WHERE { "
				+ " ?sampleURI hasco:isObjectOf ?SC . "
				+ " ?SC hasco:isSampleCollectionOf chear-kb:STD-" + studyID + " . "
				+ "}";
		QueryExecution qexecSample = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), sampleCountQuery);
		ResultSet sampleResults = qexecSample.execSelect();
		ResultSetRewindable resultsrwSample = ResultSetFactory.copyResults(sampleResults);
		qexecSample.close();
		if (resultsrwSample.hasNext()) {
			QuerySolution soln = resultsrwSample.next();
			Literal countLiteral = (Literal) soln.get("sampleCount");
			if(countLiteral != null){ 
				count += countLiteral.getInt();
			}
		}

		return count;
	}
	
	private String getUri(CSVRecord rec) {
		return kbPrefix + "SPL-" + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("studyID")))) 
		+ "-" + rec.get(mapCol.get("studyID"));
	}

	private String getType(CSVRecord rec) {
			return "sio:Sample";
	}

	private String getLabel(CSVRecord rec) {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.get(mapCol.get("studyID")))) + " - " 
				+ rec.get(mapCol.get("studyID"));
	}

	private String getOriginalID(CSVRecord rec) {
		if(!rec.get(mapCol.get("originalSID")).equalsIgnoreCase("NULL")){
			return rec.get(mapCol.get("originalSID"));
		} else {
			return "";
		}
	}
	
	private String getStudyUri(CSVRecord rec) {
		return kbPrefix + "STD-" + rec.get(mapCol.get("studyID"));
	}

	private String getCollectionUri(CSVRecord rec) {
		return kbPrefix + "SC-" + rec.get(mapCol.get("studyID"));
	}

	private String getCollectionLabel(CSVRecord rec) {
		return "Sample Collection of Study " + rec.get(mapCol.get("studyID"));
	}
		
	
	public void createObj(CSVRecord record) throws Exception {
		// insert current state of the OBJ
		obj = new StudyObject(getUri(record), "sio:Sample", getOriginalID(record), 
				getLabel(record), getCollectionUri(record), getLabel(record), scopeUris);

		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		obj.save();
		counter++;
		System.out.println("obj " + getUri(record).toString() + " saved!");

		// update/create new OBJ in LabKey
		Credential cred = Credential.find();
		if (null == cred) {
			System.out.println(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			return;
		}
		int nRowsAffected = obj.saveToLabKey(cred.getUserName(), cred.getPassword());
		if (nRowsAffected <= 0) {
			System.out.println("[ERROR] Failed to insert new OBJ to LabKey!");
		}

		objectUris.add(getUri(record));
		System.out.println(objectUris.size());
	}
	
	public boolean createOc(CSVRecord record) throws Exception {
		// insert current state of the OC
		ObjectCollection oc = new ObjectCollection(
				getCollectionUri(record),
				"http://hadatac.org/ont/hasco/SampleCollection",
				getCollectionLabel(record),
				getCollectionLabel(record),
				getStudyUri(record),
				hasScopeUri,
				spaceScopeUris,
				timeScopeUris);

		oc.setObjectUris(objectUris);

		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		oc.save();
		System.out.println("oc saved!");

		// update/create new OC in LabKey
		Credential cred = Credential.find();
		if (null == cred) {
			System.out.println(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			return false;
		}
		int nRowsAffected = oc.saveToLabKey(cred.getUserName(), cred.getPassword());
		System.out.println("nRowsAffected : " + nRowsAffected);
		if (nRowsAffected <= 0) {
			System.out.println("Failed to insert new OC to LabKey!\n");
			return false;
		}

		return true;
	}

	@Override
	public List< Map<String, Object> > createRows() throws Exception {
		rows.clear();
		boolean firstRow = true;
		for (CSVRecord record : records) {
			if (firstRow) {
				if (!createOc(record)) {
					System.out.println("[ERROR] Failed to create sample collection!");
					break;
				}
				firstRow = false;
			}
			createObj(record);
		}
		
		return rows;
	}
	
	public boolean updateMappings() throws Exception {
		for (CSVRecord record : records) {
			if (getSampleUri(record) == ""){
				continue;
			} else {
				StudyObject obj = StudyObject.find(getSampleUri(record));
				System.out.println("obj: " + obj);

				List<String> scope_l = new ArrayList<String>();
				scope_l.add(getSubjectUri(record));
				if (obj.getScopeUris() == null || obj.getScopeUris().isEmpty()){
					obj.setScopeUris(scope_l);
				} else {
					obj.getScopeUris().add(getSubjectUri(record));
				}
				System.out.println("Added to scopeuris.");
				obj.save();
				// update/create new OBJ in LabKey
				Credential cred = Credential.find();
				if (null == cred) {
					System.out.println(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
					return false;
				}
				int nRowsAffected = obj.saveToLabKey(cred.getUserName(), cred.getPassword());
				System.out.println("nRowsAffected : " + nRowsAffected);
				if (nRowsAffected <= 0) {
					System.out.println("Failed to insert new OC to LabKey!\n");
					return false;
				}
			}
		}
		
		return true;
	}
}