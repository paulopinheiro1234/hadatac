package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;


public class SSDSampleMapper extends BasicGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    private int counter = 1;
    private Map<String, String> mapIdUriCache = new HashMap<String, String>();
    String study_id;
    String file_name;
    MotherGenerator motherGenerator = null;

    public SSDSampleMapper(RecordFile file) {
        super(file);
        mapIdUriCache = getMapIdUri();
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "").replaceAll(".csv", "");
    }

    public SSDSampleMapper(RecordFile file, MotherGenerator motherGenerator) {
        super(file);
        file_name = file.getFile().getName();
        study_id = file.getFile().getName().replaceAll("SSD-", "").replaceAll(".xlsx", "").replaceAll(".csv", "");
        this.motherGenerator = motherGenerator;
        mapIdUriCache = getMapIdUri(motherGenerator);
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
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

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

    private String getUri(Record rec) {
        return kbPrefix + "SPL-" + getOriginalSID(rec);
    }

    private String getType(Record rec) {
    	if (file_name.startsWith("SSD-")){
    		return rec.getValueByColumnName(mapCol.get("type"));
    	}
    	return "sio:Sample";
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

    private String getStudyId(Record rec) {
        if (file_name.startsWith("SSD-")){
            return study_id;
        }
        return null;
    }

    private String getCollectionUri(Record rec) {
        String pid = getOriginalPID(rec);
        if (file_name.startsWith("SSD-")){
	        if (mapIdUriCache.containsKey(pid)) {
	            return kbPrefix + "SOC-" + getStudyId(rec) + "-MSAMPLES";
	        } else {
	            return kbPrefix + "SOC-" + getStudyId(rec) + "-SSAMPLES";
	        }
        } else {
        	return kbPrefix + "SOC-" + getStudyId(rec) + "-SSAMPLES";
        }
    }

    private String getCollectionLabel(Record rec) {
        return "Sample Collection of Study " + getStudyId(rec);
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
        	scopeUris.add(kbPrefix + "TIME-" + getTimeScopeUri(record) + "-" + study_id);
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
                kbPrefix + "STD-" + getStudyId(record));
        
        setStudyUri(URIUtils.replacePrefixEx(kbPrefix + "STD-" + getStudyId(record)));

        return oc;
    }

    @Override
    public void preprocess() throws Exception {
        if (motherGenerator != null) {
            mapIdUriCache = getMapIdUri(motherGenerator);
        } else {
        	mapIdUriCache = getMapIdUri();
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
