package org.hadatac.data.loader;

import java.io.File;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.csv.CSVRecord;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.ObjectCollectionType;
import org.hadatac.entity.pojo.StudyObjectType;

import com.google.common.collect.Iterables;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.mvc.Controller;
import play.mvc.Result;
import play.Play;


public class SubjectGenerator extends BasicGenerator {
	
    static final long MAX_OBJECTS = 1000;
    static final long LENGTH_CODE = 6;

    private static String path_unproc = ConfigProp.getPropertyValue("autoccsv.config", "path_unproc");

	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	
	StudyObject obj = null;
	
	private int counter = 1; //starting index number
    private String studyUri = "";
    private String hasScopeUri = "";    
    private List<String> scopeUris = new ArrayList<String>();
    private List<String> spaceScopeUris = new ArrayList<String>();
    private List<String> timeScopeUris = new ArrayList<String>();
    private List<String> objectUris = new ArrayList<String>();

//    public ObjectCollection oc = new ObjectCollection("", "", "", "", studyUri, hasScopeUri, spaceScopeUris, timeScopeUris);
	
	public SubjectGenerator(File file) {
		super(file);
	}
	
	@Override
	void initMapping() {
//		final SysUser sysUser = AuthApplication.getLocalUser(session());
		mapCol.clear();
        mapCol.put("subjectID", "patient_id");
        mapCol.put("pilotNum", "project_id");
	}
	
//	private int getSubjectCount(String pilotNum){
//		int count = 0;
//		String subjectCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
//				+ " SELECT (count(DISTINCT ?subjectURI) as ?subjectCount) WHERE { "
//				+ " ?subjectURI hasco:isSubjectOf chear-kb:CH-" + pilotNum + " . "
//				+ " }";
//		QueryExecution qexecSubject = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), subjectCountQuery);
//		ResultSet subjectResults = qexecSubject.execSelect();
//		ResultSetRewindable resultsrwSubject = ResultSetFactory.copyResults(subjectResults);
//		qexecSubject.close();
//		
//		if (resultsrwSubject.hasNext()) {
//			QuerySolution soln = resultsrwSubject.next();
//			Literal countLiteral = (Literal) soln.get("subjectCount");
//			if(countLiteral != null){
//				count += countLiteral.getInt();
//			}
//		}
//		
//		return count;
//	}
	
	private String getUri(CSVRecord rec) {
/*		return kbPrefix + "SBJ-" + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum"))))
			+ "-" + rec.get(mapCol.get("pilotNum")); */
		return kbPrefix + "SBJ-" + getOriginalID(rec) + "-" + rec.get(mapCol.get("pilotNum"));
	}
	private String getType() {
		return "http://semanticscience.org/resource/Human";
	}
	private String getLabel(CSVRecord rec) {
/*		return "ID " + String.format("%04d", counter + getSubjectCount(rec.get(mapCol.get("pilotNum")))) + " - " 
			+ rec.get(mapCol.get("pilotNum")); */
		return "Subject ID " + getOriginalID(rec) + " - " 
		+ rec.get(mapCol.get("pilotNum"));
	}
    private String getOriginalID(CSVRecord rec) {
    	return rec.get(mapCol.get("subjectID"));
    }
    
    private String getStudyUri(CSVRecord rec) {
    	return kbPrefix + "STD-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortUri(CSVRecord rec) {
    	return kbPrefix + "CH-" + rec.get(mapCol.get("pilotNum"));
    }
    
    private String getCohortLabel(CSVRecord rec) {
    	return "Study Population of " + rec.get(mapCol.get("pilotNum"));
    }
    
    public void createObj(CSVRecord record) throws Exception {
	    	
	    	// insert current state of the OBJ
	    	obj = new StudyObject(getUri(record), "sio:Human", getOriginalID(record), getLabel(record), getCohortUri(record), getLabel(record), scopeUris);
	        
	    	// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
	    	obj.save();
	    	System.out.println("obj " + getUri(record).toString() + " saved!");
	    	// update/create new OBJ in LabKey
	    	int nRowsAffected = obj.saveToLabKey("gychant", "labkey");
	    	if (nRowsAffected <= 0) {
	    	    System.out.println("[ERROR] Failed to insert new OBJ to LabKey!");
	    	}
	    	
    		objectUris.add(getUri(record));
    		System.out.println(objectUris.size());

    }
    
    public boolean createOc() throws Exception {
    	
	    // insert current state of the OC
		ObjectCollection oc = new ObjectCollection(getCohortUri(records.iterator().next()),
																"http://hadatac.org/ont/hasco/SubjectGroup",
																getCohortLabel(records.iterator().next()),
																getCohortLabel(records.iterator().next()),
																getStudyUri(records.iterator().next()),
																hasScopeUri,
																spaceScopeUris,
																timeScopeUris);

    	for (CSVRecord record : records) {
    		
//    		System.out.println(getUri(record));
    		createObj(record);
	    }
    	
    	oc.setObjectUris(objectUris);
	
		// insert the new OC content inside of the triplestore regardless of any change -- the previous content has already been deleted
		oc.save();
		System.out.println("oc saved!");
		// update/create new OC in LabKey
		int nRowsAffected = oc.saveToLabKey("gychant", "labkey");
//		System.out.println("nRowsAffected : " + nRowsAffected);
		if (nRowsAffected <= 0) {
			System.out.println("Failed to insert new OC to LabKey!\n");
			return false;
		}
		return true;

    }
    
    @Override
    Map<String, Object> createRow(CSVRecord rec, int row_number) throws Exception {
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", getUri(rec));
    	row.put("a", getType());
    	row.put("rdfs:label", getLabel(rec));
    	row.put("hasco:originalID", getOriginalID(rec));
    	row.put("hasco:isSubjectOf", getCohortUri(rec));
    	counter++;
    	
    	return row;
    }
    
    @Override
	public List< Map<String, Object> > createRows() throws Exception {
    	rows.clear();
    	int row_number = 0;
    	for (CSVRecord record : records) {
    		
    		objectUris.add(getUri(record));
    		rows.add(createRow(record, ++row_number));
	    }
    	return rows;
    }

}