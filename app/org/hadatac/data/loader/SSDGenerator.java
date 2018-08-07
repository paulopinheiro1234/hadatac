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
    String SDDName = ""; //used for reference column uri
    
    public SSDGenerator(RecordFile file) {
        super(file);
        String str = file.getFile().getName().replaceAll("SSD-", "");
        this.SDDName = str.substring(0, str.lastIndexOf('.'));
		if (records.get(0) != null) {
		    studyUri = URIUtils.convertToWholeURI(getUri(records.get(0)));
		} else {
			studyUri = "";
		}
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
    	ObjectCollection oc = 
	    new ObjectCollection(URIUtils.replacePrefixEx(getUri(record)),
				 URIUtils.replacePrefixEx(getTypeUri(record)),
				 getLabel(record),
				 getLabel(record),
				 this.studyUri,
				 URIUtils.replacePrefixEx(gethasScopeUri(record)),
				 getGroundingLabel(record),
				 getSOCReference(record),
				 getSpaceScopeUris(record),
				 getTimeScopeUris(record));
    	
        return oc;
    }

    @Override
    public void preprocess() throws Exception {}
    
    @Override
    HADatAcThing createObject(Record rec, int row_number) throws Exception {
        if (!URIUtils.replacePrefixEx(getUri(rec)).equals(studyUri)) {
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
