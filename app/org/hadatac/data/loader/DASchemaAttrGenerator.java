package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Templates;

public class DASchemaAttrGenerator extends BasicGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String startTime = "";
    String SDDName = "";
    Map<String, String> codeMap;
    Map<String, List<String>> hasEntityMap = new HashMap<String, List<String>>();
    Map<String, String> currentHasEntity = new HashMap<String, String>();

    public DASchemaAttrGenerator(RecordFile file, String SDDName, Map<String, String> codeMap) {
        super(file);
        this.codeMap = codeMap;
        this.SDDName = SDDName;

        initMapping();

        for (Record rec : file.getRecords()) {
            List<String> tmp = new ArrayList<String>();
            tmp.add(rec.getValueByColumnName(mapCol.get("AttributeOf")));
            tmp.add(rec.getValueByColumnName(mapCol.get("Entity")));
            hasEntityMap.put(rec.getValueByColumnName(mapCol.get("Label")), tmp);
            System.out.println(rec.getValueByColumnName(mapCol.get("Label")) + " *** " + tmp);
        }
    }

    //Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition	
    @Override
    void initMapping() {
        mapCol.clear();
        mapCol.put("Label", Templates.LABEL);
        mapCol.put("AttributeType", Templates.ATTRIBUTETYPE);
        mapCol.put("AttributeOf", Templates.ATTTRIBUTEOF);
        mapCol.put("Unit", Templates.UNIT);
        mapCol.put("Time", Templates.TIME);
        mapCol.put("Entity", Templates.ENTITY);
        mapCol.put("Role", Templates.ROLE);
        mapCol.put("Relation", Templates.RELATION);
        mapCol.put("InRelationTo", Templates.INRELATIONTO);
        mapCol.put("WasDerivedFrom", Templates.WASDERIVEDFROM);       
        mapCol.put("WasGeneratedBy", Templates.WASGENERATEDBY);
    }

    private String getLabel(Record rec) {
        return rec.getValueByColumnName(mapCol.get("Label"));
    }

    private String getAttribute(Record rec) {
        return rec.getValueByColumnName(mapCol.get("AttributeType"));
    }

    private String getAttributeOf(Record rec) {
        if (rec.getValueByColumnName(mapCol.get("AttributeOf").trim()).equals("")) {
            return "";
        }
        
        return kbPrefix + "DASO-" + SDDName + "-" + rec.getValueByColumnName(mapCol.get("AttributeOf")).replace(" ", "").replace("_","-").replace("??", "");
    }

    private String getUnit(Record rec) {
        String original = rec.getValueByColumnName(mapCol.get("Unit"));
        if (URIUtils.isValidURI(original)) {
            return original;
        } else if (codeMap.containsKey(original)) {
            return codeMap.get(original);
        }

        return "";
    }

    private String getTime(Record rec) {
        if (rec.getValueByColumnName(mapCol.get("Time").trim()).equals("")) {
            return "";
        }
        
        return kbPrefix + "DASE-" + SDDName + "-" + rec.getValueByColumnName(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-");
    }

    private String getEntity(Record rec) {
        String daso = rec.getValueByColumnName(mapCol.get("AttributeOf"));
        if (daso.equals("")) {
            currentHasEntity.put(getLabel(rec), "chear:unknownEntity");
            return "chear:unknownEntity";
        } else {
            if (codeMap.containsKey(hasEntityMap.get(daso))) {
                currentHasEntity.put(getLabel(rec), codeMap.get(hasEntityMap.get(daso)));
                return codeMap.get(hasEntityMap.get(daso));
            } else {
                if (hasEntityMap.containsKey(daso)) {
                    if(codeMap.containsKey(hasEntityMap.get(daso).get(1))) {
                        currentHasEntity.put(getLabel(rec), codeMap.get(hasEntityMap.get(daso).get(1)));
                        return codeMap.get(hasEntityMap.get(daso).get(1));
                    }
                    currentHasEntity.put(getLabel(rec), hasEntityMap.get(daso).get(1));
                    return hasEntityMap.get(daso).get(1);
                }
                currentHasEntity.put(getLabel(rec), "chear:unknownEntity");
                return "chear:unknownEntity";
            }
        }
    }

    private String getRelation(Record rec) {
        return rec.getValueByColumnName(mapCol.get("Relation"));
    }

    private String getInRelationTo(Record rec) {
        String inRelationTo = rec.getValueByColumnName(mapCol.get("InRelationTo"));
        if (inRelationTo.length() == 0) {
            return "";
        } else {
            List<String> items = new ArrayList<String>();
            for (String item : Arrays.asList(inRelationTo.split("\\s*,\\s*"))) {
                items.add(kbPrefix + "DASO-" + SDDName + "-" + item.replace(" ", "").replace("_","-").replace("??", ""));
            }
            return String.join(" & ", items);
        }
    }

    private String getWasDerivedFrom(Record rec) {
        return rec.getValueByColumnName(mapCol.get("WasDerivedFrom"));
    }

    private String getWasGeneratedBy(Record rec) {
        return rec.getValueByColumnName(mapCol.get("WasGeneratedBy"));
    }

    private Boolean checkVirtual(Record rec) {
        if (getLabel(rec).contains("??")){
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void createRows() throws Exception {
        rows.clear();
        List<String> column_name = new ArrayList<String>();
        int row_number = 0;
        for (Record record : records) {
            if (getAttribute(record)  == null || getAttribute(record).equals("")){
                if (column_name.contains(getLabel(record))){
                    rows.add(createRelationRow(record, ++row_number));
                }
                continue;
            } else {
                rows.add(createRow(record, ++row_number));
                column_name.add(getLabel(record));
            }
        }
    }

    public List<String> createUris() throws Exception {
        List<String> result = new ArrayList<String>();
        for (Record record : records) {
            if (getAttribute(record)  == null || getAttribute(record).equals("")){
                continue;
            } else {
                result.add(kbPrefix + "DASA-" + SDDName + "-" + getLabel(record).trim().replace(" ", "").replace("_","-").replace("??", ""));
            }
        }
        return result;
    }

    //Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition   
    @Override
    Map<String, Object> createRow(Record rec, int row_number) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
        row.put("a", "hasco:DASchemaAttribute");
        row.put("rdfs:label", getLabel(rec));
        row.put("rdfs:comment", getLabel(rec));
        row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
        if (!currentHasEntity.containsKey(getLabel(rec))){
            row.put("hasco:hasEntity", getEntity(rec));
        }
        if (getRelation(rec).length() > 0) {
            row.put(getRelation(rec), getInRelationTo(rec));
        } else {
            row.put("sio:inRelationTo", getInRelationTo(rec));
        }
        if (getInRelationTo(rec).length() > 0) {
            if (getRelation(rec).length() > 0) {
                row.put("sio:Relation", getRelation(rec));
            } else {
                row.put("sio:Relation", "sio:inRelationTo");
            }
        }
        row.put("hasco:hasAttribute", getAttribute(rec));
        row.put("hasco:hasUnit", getUnit(rec));
        row.put("hasco:hasEvent", getTime(rec));
        row.put("hasco:hasSource", "");
        row.put("hasco:isAttributeOf", getAttributeOf(rec));
        row.put("hasco:isVirtual", checkVirtual(rec).toString());
        row.put("hasco:isPIConfirmed", "false");

        return row;
    }

    Map<String, Object> createRelationRow(Record rec, int row_number) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
        if (getRelation(rec).length() > 0) {
            row.put(getRelation(rec), getInRelationTo(rec));
        } else {
            row.put("sio:inRelationTo", getInRelationTo(rec));
        }
        if (getInRelationTo(rec).length() > 0) {
            if (getRelation(rec).length() > 0) {
                row.put("sio:Relation", getRelation(rec));
            } else {
                row.put("sio:Relation", "sio:inRelationTo");
            }
        }

        return row;
    }

    @Override
    public String getTableName() {
        return "DASchemaAttribute";
    }

    @Override
    public String getErrorMsg(Exception e) {
        return "Error in DASchemaAttrGenerator: " + e.getMessage();
    }
}
