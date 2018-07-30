package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.metadata.loader.URIUtils;

public class SSDGenerator extends BasicGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String studyUri = "";
    String SDDName = ""; //used for reference column uri
    
    public SSDGenerator(RecordFile file) {
        super(file);
        String str = file.getFile().getName().replaceAll("SSD-", "");
        this.SDDName = str.substring(0, str.lastIndexOf('.'));
    }

    @Override
    public void initMapping() {
        mapCol.clear();
        mapCol.put("sheet", "sheet");
        mapCol.put("uri", "hasURI");
        mapCol.put("typeUri", "type");
        mapCol.put("hasSOCReference", "hasSOCReference");
        mapCol.put("label", "label");
        mapCol.put("studyUri", "isMemberOf");
        mapCol.put("hasScopeUri", "hasScope");
        mapCol.put("groundingLabel", "groundingLabel");
        mapCol.put("spaceScopeUris", "hasSpaceScope");
        mapCol.put("timeScopeUris", "hasTimeScope");
    }

    private String getUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("uri"));
    }

    private String getTypeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("typeUri"));
    }
    
    private String getLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("label"));
    }

    private String getStudyUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("studyUri"));
    }
    
    private String getSOCReference(Record rec) {
        String ref = rec.getValueByColumnName(mapCol.get("hasSOCReference"));
//        return kbPrefix + "DASO-" + SDDName + "-" + ref.trim().replace(" ","").replace("_","-").replace("??", "");
        return ref.trim().replace(" ","").replace("_","-");
    }

    private String gethasScopeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("hasScopeUri"));
    }
    
    private String getGroundingLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("groundingLabel"));
    }

    private List<String> getSpaceScopeUris(Record rec) {
    	List<String> ans = Arrays.asList(rec.getValueByColumnName(mapCol.get("spaceScopeUris")).split(","))
    	        .stream()
                .map(s -> URIUtils.replacePrefixEx(s))
                .collect(Collectors.toList());
    	return ans;
    }
    
    private List<String> getTimeScopeUris(Record rec) {
    	List<String> ans = Arrays.asList(rec.getValueByColumnName(mapCol.get("timeScopeUris")).split(","))
    	        .stream()
                .map(s -> URIUtils.replacePrefixEx(s))
                .collect(Collectors.toList());
    	return ans;
    }

    public ObjectCollection createObjectCollection(Record record) throws Exception {
    	ObjectCollection oc = new ObjectCollection(
    	        URIUtils.replacePrefixEx(getUri(record)),
    	        URIUtils.replacePrefixEx(getTypeUri(record)),
    			getLabel(record),
    			getLabel(record),
    			URIUtils.replacePrefixEx(getStudyUri(record)),
    			URIUtils.replacePrefixEx(gethasScopeUri(record)),
    			getGroundingLabel(record),
    			getSOCReference(record),
                getSpaceScopeUris(record),
                getTimeScopeUris(record));
    	
    	setStudyUri(URIUtils.replacePrefixEx(getStudyUri(record)));
    	
        return oc;
    }

    @Override
    public void preprocess() throws Exception {
    	
        List<String> lstr = new ArrayList<String>();
        studyUri = getUri(records.get(0));
        String studyUriFull = URIUtils.convertToWholeURI(getUri(records.get(0)));
	Study study = Study.find(studyUriFull);
        //String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	//    "SELECT ?s WHERE { " + 
	//    "?s a <http://hadatac.org/ont/hasco/Study> . " + 
	//    "}";
        
        //ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);
	
        //if (!resultsrw.hasNext()) {
        //    AnnotationLog.printException("SSD ingestion: Could not find the study uri in the TS, check the study uri in the SSD sheet.", file.getFile().getName());
        //    return;
        //}
        
        //while (resultsrw.hasNext()) {
        //    QuerySolution soln = resultsrw.next();
        //    lstr.add(soln.getResource("s").toString());
        //}
        
        //if (lstr.contains(studyUriFull)) {
        if (study != null) {
            AnnotationLog.println("SSD ingestion: The study uri :" + studyUriFull + " is in the TS.", file.getFile().getName());
        } else {
            AnnotationLog.printException("SSD ingestion: Could not find the study uri : " + studyUriFull + " in the TS, check the study uri in the SSD sheet.", file.getFile().getName());
        }
    }
    
    @Override
	HADatAcThing createObject(Record rec, int row_number) throws Exception {
        if (!getUri(rec).equals(studyUri)) {
            return createObjectCollection(rec);
        }
        
        return null;
    }
    
    @Override
    public String getErrorMsg(Exception e) {
        return "Error in SSDGenerator: " + e.getMessage();
    }

	@Override
	public String getTableName() {
		// TODO Auto-generated method stub
		return null;
	}
}
