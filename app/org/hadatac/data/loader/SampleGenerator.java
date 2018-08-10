package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Literal;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;


public class SampleGenerator extends BasicGenerator {
    final String kbPrefix = ConfigProp.getKbPrefix();
    
    private int counter = 1;
    private String hasScopeUri = "";    
    private String hasGroundingLabel = "";
    private String hasSOCReference = "";
    private List<String> scopeUris = new ArrayList<String>();
    private List<String> spaceScopeUris = new ArrayList<String>();
    private List<String> timeScopeUris = new ArrayList<String>();
    private List<String> objectUris = new ArrayList<String>();

    public SampleGenerator(RecordFile file) {
        super(file);
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("sampleID", "specimen_id");
        mapCol.put("studyID", "study_id");
        mapCol.put("sampleSuffix", "suffix");
    }

    private int getSampleCount(String studyID){
        int count = 0;
        String sampleCountQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
                + " SELECT (count(DISTINCT ?sampleURI) as ?sampleCount) WHERE { \n"
                + " ?sampleURI hasco:isMemberOf* chear-kb:STD-" + studyID + " . \n"
                + "}";
        
        ResultSetRewindable resultsrwSample = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), sampleCountQuery);
        
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
        
        ResultSetRewindable resultsrwSubject = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), subjectQuery);
        
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

    public StudyObject createStudyObject(Record record) throws Exception {
        StudyObject obj = new StudyObject(getUri(record), "sio:Sample", getOriginalID(record), 
                getLabel(record), getCollectionUri(record), getLabel(record), scopeUris, timeScopeUris, spaceScopeUris);

        objectUris.add(getUri(record));
        
        return obj;
    }

    public ObjectCollection createObjectCollection(Record record) throws Exception {
        ObjectCollection oc = new ObjectCollection(
                getCollectionUri(record),
                "http://hadatac.org/ont/hasco/SampleCollection",
                getCollectionLabel(record),
                getCollectionLabel(record),
                getStudyUri(record),
                hasScopeUri,
                hasGroundingLabel,
                hasSOCReference,
                spaceScopeUris,
                timeScopeUris);

        oc.setObjectUris(objectUris);
        
        setStudyUri(URIUtils.replacePrefixEx(getStudyUri(record)));

        return oc;
    }
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        return createStudyObject(rec);
    }
    
    @Override
    public void preprocess() throws Exception {
        if (!records.isEmpty()) {
            objects.add(createObjectCollection(records.get(0)));
        }
    }

    @Override
    public String getTableName() {
        return "StudyObject";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SampleGenerator: " + e.getMessage();
    }
}
