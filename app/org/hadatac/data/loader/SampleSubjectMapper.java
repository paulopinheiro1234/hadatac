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
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.StudyObject;

public class SampleSubjectMapper extends BasicGenerator {

	public SampleSubjectMapper(File file) {
		super(file);
	}

	@Override
	void initMapping() {
		mapCol.clear();
		mapCol.put("originalPID", "patient_id");
		mapCol.put("originalSID", "specimen_id");
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

	@Override
	Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
		Map<String, Object> row = new HashMap<String, Object>();
		String sampleUri = getSampleUri(rec);
		String subjectUri = getSubjectUri(rec);
		if (sampleUri.equals("") || subjectUri.equals("")) {
			System.out.println("Mapping Sample(" + sampleUri + ") <-> Subject(" + subjectUri + ") rejected");
			return null;
		}
		row.put("hasURI", sampleUri);
		row.put("hasco:isSampleOf", subjectUri);
		return row;
	}
}
