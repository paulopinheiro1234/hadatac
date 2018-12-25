package org.hadatac.data.loader;

import java.lang.String;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.hadatac.utils.ConfigProp;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.metadata.loader.URIUtils;

public class SSDGenerator extends BaseGenerator {

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

    private String getHasScopeUri(Record rec) {
        return rec.getValueByColumnName(mapCol.get("hasScopeUri"));
    }

    private String getGroundingLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("groundingLabel"));
    }

    private List<String> getSpaceScopeUris(Record rec) {
        if (mapCol.get("spaceScopeUris") == null || rec.getValueByColumnName(mapCol.get("spaceScopeUris")) == null) {
	    return new ArrayList<String>();
	}
        List<String> ans = Arrays.asList(rec.getValueByColumnName(mapCol.get("spaceScopeUris")).split(","))
                .stream()
                .map(s -> URIUtils.replacePrefixEx(s))
                .collect(Collectors.toList());
        return ans;
    }

    private List<String> getTimeScopeUris(Record rec) {
        if (mapCol.get("timeScopeUris") == null || rec.getValueByColumnName(mapCol.get("timeScopeUris")) == null) {
	    return new ArrayList<String>();
	}
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
                this.studyUri,
                URIUtils.replacePrefixEx(getHasScopeUri(record)),
                getGroundingLabel(record),
                getSOCReference(record),
                getSpaceScopeUris(record),
                getTimeScopeUris(record));

        return oc;
    }

    @Override
    public void preprocess() throws Exception {}

    @Override
    public HADatAcThing createObject(Record rec, int rowNumber) throws Exception {
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
