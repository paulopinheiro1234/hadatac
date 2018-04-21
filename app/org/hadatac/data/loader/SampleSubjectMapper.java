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
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;


public class SampleSubjectMapper extends BasicGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    private int counter = 1;
    private Map<String, String> mapIdUriCache = new HashMap<String, String>();
    String study_id;
    String file_name;
    MotherGenerator motherGenerator = null;
    
    public SampleSubjectMapper(RecordFile file) {
        super(file);
        mapIdUriCache = getMapIdUri();
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("MAP-", "").replaceAll("SSD-", "").replaceAll(".xlsx", "").replaceAll(".csv", "");
    }

    public SampleSubjectMapper(RecordFile file, MotherGenerator motherGenerator) {
        super(file);
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("MAP-", "").replaceAll("SSD-", "").replaceAll(".xlsx", "").replaceAll(".csv", "");
        this.motherGenerator = motherGenerator;
    }

    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("type", "rdf:type");
        mapCol.put("originalPID", "CHEAR PID");
        mapCol.put("originalSID", "originalID");
        try{
        	mapCol.put("pilotNum", "CHEAR_Project_ID");
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
            System.out.println("This sheet or MAP file contains no CHEAR_Project_ID column");
        }
        try{
        	mapCol.put("timeScopeID", "timeScopeID");
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
            System.out.println("This sheet or MAP file contains no timeScopeID column");
        }
    }
    
    private Map<String, String> getMapIdUri(MotherGenerator generator) {
        Map<String, String> mapIdUri = new HashMap<String, String>();
        for (HADatAcThing obj : generator.getObjects()) {
            StudyObject studyObj = (StudyObject)obj;
            mapIdUri.put(studyObj.getOriginalId(), studyObj.getUri());
        }
        
        return mapIdUri;
    }

    private Map<String, String> getMapIdUri() {
        Map<String, String> mapIdUri = new HashMap<String, String>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?uri ?id WHERE { \n" +
                " ?uri hasco:originalID ?id . \n" +
                "}";

        try {
            Query sampleQuery = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(
                    CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sampleQuery);
            ResultSet results = qexec.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
            qexec.close();
            
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if(soln.get("id") != null && soln.get("uri") != null) {
                    mapIdUri.put(soln.get("id").toString(), soln.get("uri").toString());
                }
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return mapIdUri;
    }
    
    private String getSubjectType(String sbj) {
        String answer = "";

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                "SELECT ?o WHERE { " +
                "<" + sbj + ">" + " <http://hadatac.org/ont/hasco/hasRole>	?o ." +
                "}";

        try {
            Query sampleQuery = QueryFactory.create(queryString);
            QueryExecution qexec = QueryExecutionFactory.sparqlService(
                    CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sampleQuery);
            ResultSet results = qexec.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
            qexec.close();
            
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if(soln.get("o") != null) {
                	answer = soln.get("o").toString();
                }
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return answer;
    }

    private int getSampleCount(String studyID){
        int count = 0;
        String sampleCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT (count(DISTINCT ?sampleURI) as ?sampleCount) WHERE { \n"
                + " ?sampleURI hasco:isMemberOf* chear-kb:STD-" + studyID + " . \n"
                + "}";
        QueryExecution qexecSample = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sampleCountQuery);
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
        return kbPrefix + "SPL-" + getOriginalSID(rec);
    }
    
    private String getType(Record rec) {
        return rec.getValueByColumnName(mapCol.get("type"));
    }

    private String getLabel(Record rec) {
        return "Sample " + rec.getValueByColumnName(mapCol.get("originalSID"));
    }

    private String getOriginalSID(Record rec) {
        if(!rec.getValueByColumnName(mapCol.get("originalSID")).equalsIgnoreCase("NULL")){
            return rec.getValueByColumnName(mapCol.get("originalSID"));
        } else {
            return "";
        }
    }
    
    private String getOriginalPID(Record rec) {
        if(!rec.getValueByColumnName(mapCol.get("originalPID")).equalsIgnoreCase("NULL")){
            return rec.getValueByColumnName(mapCol.get("originalPID")).replaceAll("(?<=^\\d+)\\.0*$", "");
        } else {
            return "";
        }
    }

    private String getPilotNum(Record rec) {
        return rec.getValueByColumnName(mapCol.get("pilotNum"));
    }

    private String getStudyUri(Record rec) {
    	if (file_name.startsWith("MAP-")){
    		return getPilotNum(rec);
    	} else if (file_name.startsWith("SSD-")){
            return study_id;
    	}
		return null;
    }

    private String getCollectionUri(Record rec) {
        String pid = getOriginalPID(rec);
        if (mapIdUriCache.containsKey(pid)) {
        	return kbPrefix + "SOC-" + getStudyUri(rec) + "-MSAMPLES";
        } else {
        	return kbPrefix + "SOC-" + getStudyUri(rec) + "-SSAMPLES";
        }
    }

    private String getCollectionLabel(Record rec) {
        return "Sample Collection of Study " + getStudyUri(rec);
    }
    
    private String getTimeScopeUri(Record rec) {
    	String ans = rec.getValueByColumnName(mapCol.get("timeScopeID"));
    	return ans;
    }


    public StudyObject createStudyObject(Record record) throws Exception {
    	List<String> scopeUris = new ArrayList<String>();
    	String pid = getOriginalPID(record);
        if (!pid.isEmpty()) {
        	scopeUris.add(kbPrefix + "SBJ-" + pid + "-" + study_id);
        }
        if (!getTimeScopeUri(record).isEmpty()){
        	scopeUris.add("http://hadatac.org/kb/chear#"+getTimeScopeUri(record));
        }
    	
        System.out.println("scopeUris :" + scopeUris);
        
        StudyObject obj = new StudyObject(getUri(record), getType(record), getOriginalSID(record), 
                getLabel(record), getCollectionUri(record), getLabel(record), scopeUris);
        
        return obj;
    }

    public ObjectCollection createObjectCollection(Record record) throws Exception {
        ObjectCollection oc = new ObjectCollection(
                getCollectionUri(record),
                "http://hadatac.org/ont/hasco/SampleCollection",
                getCollectionLabel(record),
                getCollectionLabel(record),
                kbPrefix + "STD-" + getStudyUri(record));

        return oc;
    }

    @Override
    public void preprocess() throws Exception {
        if (motherGenerator != null) {
            mapIdUriCache = getMapIdUri(motherGenerator);
        }
        
        if (!records.isEmpty()) {
            objects.add(createObjectCollection(records.get(0)));
        }
    }

    @Override
    public HADatAcThing createObject(Record rec, int row_number) throws Exception {
        System.out.println("counter: " + counter);
        
        counter++;
        return createStudyObject(rec);
    }

    @Override
    public String getTableName() {
        return "StudyObject";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SampleSubjectMapper: " + e.getMessage();
    }
}
