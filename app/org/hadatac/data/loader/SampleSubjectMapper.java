package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;

public class SampleSubjectMapper extends BasicGenerator {
	
	final String kbPrefix = ConfigProp.getKbPrefix();
	private int counter = 1; //starting index number

	StudyObject obj = null;

	private String hasScopeUri = "";    
	private List<String> scopeUris = new ArrayList<String>();
	private List<String> spaceScopeUris = new ArrayList<String>();
	private List<String> timeScopeUris = new ArrayList<String>();
	private List<String> objectUris = new ArrayList<String>();

	public SampleSubjectMapper(RecordFile file) {
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
	Map<String, Object> createRow(Record rec, int rownumber) throws Exception {
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

	private String getSampleUri(Record rec) {
		String sampleUri = "";
		String sampleQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE {" +
				"?s hasco:originalID \"" + rec.getValueByColumnName(mapCol.get("originalSID")) + "\"." +
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

	private String getSubjectUri(Record rec) {
		String subjectUri = "";
		String subjectQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?s WHERE { \n" +
				"?s hasco:originalID \"" + rec.getValueByColumnName(mapCol.get("originalPID")) + "\". \n" +
				"} \n";
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
	
	private String getUri(Record rec) {
		return kbPrefix + "SPL-" + String.format("%04d", counter + getSampleCount(rec.getValueByColumnName(mapCol.get("studyID")))) 
		+ "-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getType(Record rec) {
			return "sio:Sample";
	}

	private String getLabel(Record rec) {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.getValueByColumnName(mapCol.get("studyID")))) + " - " 
				+ rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getOriginalID(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("originalSID")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("originalSID"));
		} else {
			return "";
		}
	}
	
	private String getStudyUri(Record rec) {
		return kbPrefix + "STD-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getCollectionUri(Record rec) {
		return kbPrefix + "SC-" + rec.getValueByColumnName(mapCol.get("studyID"));
	}

	private String getCollectionLabel(Record rec) {
		return "Sample Collection of Study " + rec.getValueByColumnName(mapCol.get("studyID"));
	}
		
	
	public HADatAcThing createStudyObject(Record record) throws Exception {
		StudyObject obj = new StudyObject(getUri(record), "sio:Sample", getOriginalID(record), 
				getLabel(record), getCollectionUri(record), getLabel(record), scopeUris);

		objectUris.add(getUri(record));
		System.out.println(objectUris.size());
		
		return obj;
	}
	
	public ObjectCollection createObjectCollection(Record record) throws Exception {
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
		
		return oc;
	}
	
	public void updateMappings() throws Exception {
		for (Record record : records) {
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
				objects.add(obj);
			}
		}
	}
	
	@Override
	public void preprocess() throws Exception {
		if (!records.isEmpty()) {
			objects.add(createObjectCollection(records.get(0)));
		}
	}
	
	@Override
	public void postprocess() throws Exception {
		updateMappings();
	}

	@Override
	public String getTableName() {
		return "StudyObject";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "";
	}

	@Override
	HADatAcThing createObject(Record rec, int row_number) throws Exception {
		return createStudyObject(rec);
	}
}
