package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;


public class SampleGenerator extends BasicGenerator {
	final String kbPrefix = ConfigProp.getKbPrefix();
	private int counter = 1; //starting index number

	StudyObject obj = null;

	private String hasScopeUri = "";    
	private List<String> scopeUris = new ArrayList<String>();
	private List<String> spaceScopeUris = new ArrayList<String>();
	private List<String> timeScopeUris = new ArrayList<String>();
	private List<String> objectUris = new ArrayList<String>();

	public SampleGenerator(RecordFile file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("sampleID", "specimen_id");
		mapCol.put("studyID", "study_id");
		mapCol.put("sampleSuffix", "suffix");
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
		row.put("rdfs:comment", getComment(rec));
		row.put("hasco:hasSamplingMethod", getSamplingMethod(rec));
		row.put("hasco:hasSamplingVolume", getSamplingVolume(rec));
		row.put("hasco:hasSamplingVolumeUnit", getSamplingVolumeUnit(rec));
		row.put("hasco:hasStorageTemperature", getStorageTemperature(rec));
		row.put("hasco:hasStorageTemperatureUnit", getStorageTemperatureUnit());
		row.put("hasco:hasNumFreezeThaw", getNumFreezeThaw(rec));
		counter++;

		return row;
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
		+ "-" + rec.getValueByColumnName(mapCol.get("studyID")); //  + "-" + getSampleSuffix()
	}

	private String getType(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("sampleType")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("sampleType"));
		} else {
			return "sio:Sample";
		}
	}

	private String getLabel(Record rec) {
		return "SID " + String.format("%04d", counter + getSampleCount(rec.getValueByColumnName(mapCol.get("studyID")))) + " - " 
				+ rec.getValueByColumnName(mapCol.get("studyID")) + " " + getSampleSuffix(rec);
	}

	private String getOriginalID(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("sampleID")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("sampleID"));
		} else {
			return "";
		}
	}

	private String getSubjectUri(Record rec) {
		if (rec.getValueByColumnName(mapCol.get("subjectID")).equalsIgnoreCase("NULL")) {
			return "";
		}

		String subject = "";
		String subjectQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT ?subjectURI WHERE { "
				+ " ?subjectURI hasco:originalID \"" + rec.getValueByColumnName(mapCol.get("subjectID")) + "\" . }";

		QueryExecution qexecSubject = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectQuery);
		ResultSet subjectResults = qexecSubject.execSelect();
		ResultSetRewindable resultsrwSubject = ResultSetFactory.copyResults(subjectResults);
		qexecSubject.close();
		if (resultsrwSubject.hasNext()) {
			QuerySolution soln = resultsrwSubject.next();
			subject = soln.get("subjectURI").toString();
		}

		return subject;
	}

	private String getComment(Record rec) {
		return "Sample " + String.format("%04d", counter + getSampleCount(rec.getValueByColumnName(mapCol.get("studyID")))) 
		+ " for " + rec.getValueByColumnName(mapCol.get("studyID")) + " " + getSampleSuffix(rec);
	}

	private String getSamplingMethod(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("samplingMethod")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("samplingMethod"));
		} else {
			return "";
		}
	}

	private String getSamplingVolume(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("samplingVol")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("samplingVol"));
		} else {
			return "";
		}
	}

	private String getSamplingVolumeUnit(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("samplingVolUnit")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("samplingVolUnit"));
		} else {
			return "obo:UO_0000095"; // default volume unit
		}
	}

	private String getStorageTemperature(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("storageTemp")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("storageTemp"));
		} else {
			return "";
		}
	}

	private String getStorageTemperatureUnit() {
		// defaulting to Celsius since SID file does not contain temp unit
		return "obo:UO_0000027";
	}

	private String getNumFreezeThaw(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("FTcount")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName("FTcount");
		} else {
			return "";
		}
	}

	private String getSampleSuffix(Record rec) {
		if(!rec.getValueByColumnName(mapCol.get("sampleSuffix")).equalsIgnoreCase("NULL")){
			return rec.getValueByColumnName(mapCol.get("sampleSuffix"));
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

	public void createObj(Record record) throws Exception {
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

	public boolean createOc(Record record) throws Exception {
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
		for (Record record : records) {
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

	@Override
	public String getTableName() {
		return "";
	}

	@Override
	public String getErrorMsg(Exception e) {
		return "";
	}
}
