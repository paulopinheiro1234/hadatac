package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.api.MetadataFactory;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.Templates;

public class DASchemaAttrGenerator extends BaseGenerator {

    final String kbPrefix = ConfigProp.getKbPrefix();
    String startTime = "";
    String SDDName = "";
    String filename = "";
    Map<String, String> codeMap;
    Map<String, List<String>> hasEntityMap = new HashMap<String, List<String>>();
    Map<String, List<String>> mergedEA = new HashMap<String, List<String>>();
    Map<String, List<String>> mergedAA = new HashMap<String, List<String>>();
    List<String> AttrList = new ArrayList<String>();
    Map<String, String> currentHasEntity = new HashMap<String, String>();

    public DASchemaAttrGenerator(DataFile dataFile, String SDDName, Map<String, String> codeMap, List<Map<String, List<String>>> merging) {
        super(dataFile);
        this.codeMap = codeMap;
        this.SDDName = SDDName;
        this.mergedEA = merging.get(0);
        this.mergedAA = merging.get(1);
        this.fileName = dataFile.getFileName();
        logger.println("[Merged Attributes] : " + mergedEA.keySet());
        logger.println("[Derived Attributes] : " + mergedAA.keySet());

        initMapping();

        for (Record rec : file.getRecords()) {
            List<String> tmp = new ArrayList<String>();
            tmp.add(rec.getValueByColumnName(mapCol.get("AttributeOf")));
            tmp.add(rec.getValueByColumnName(mapCol.get("Entity")));
            tmp.add(rec.getValueByColumnName(mapCol.get("InRelationTo")));
            hasEntityMap.put(rec.getValueByColumnName(mapCol.get("Label")), tmp);
            if (rec.getValueByColumnName(mapCol.get("AttributeType")) != null && rec.getValueByColumnName(mapCol.get("AttributeType")).length() > 0) {
                AttrList.add(rec.getValueByColumnName(mapCol.get("Label")));
            }
        }
    }

    //Column	Attribute	attributeOf	Unit	Time	Entity	Role	Relation	inRelationTo	wasDerivedFrom	wasGeneratedBy	hasPosition	
    @Override
    public void initMapping() {
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

    private String getAttributeOf(String str) {
        if (str == null || str.equals("")) {
            return "";
        }
        String attr = str.trim();
        return kbPrefix + "DASO-" + SDDName + "-" + attr.replace(" ", "").replace("_","-").replace("??", "");
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

        return kbPrefix + "DASO-" + SDDName + "-" + rec.getValueByColumnName(mapCol.get("Time")).trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-");
    }

    private String getEntity(Record rec) {
        String daso = rec.getValueByColumnName(mapCol.get("AttributeOf"));
        if (daso.equals("")) {
            currentHasEntity.put(getLabel(rec), "hasco:unknownEntity");
            return "hasco:unknownEntity";
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
                currentHasEntity.put(getLabel(rec), "hasco:unknownEntity");
                return "hasco:unknownEntity";
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
            return items.get(0);
        }
    }

    private List<String> getWasDerivedFrom(Record rec) {
        String derivedFrom = rec.getValueByColumnName(mapCol.get("WasDerivedFrom"));
        List<String> tbd = new ArrayList<String>();
        if (derivedFrom.length() == 0) {
            return tbd;
        } else {
            List<String> items = Arrays.asList(derivedFrom.split("\\s*,\\s*"));
            for (String item : items) {
                if (AttrList.contains(item)) {
                    tbd.add(kbPrefix + "DASA-" + SDDName + "-" + item.replace(" ", "").replace("_","-").replace("??", ""));
                }
            }
            return tbd;
        }
    }

    public boolean checkCellUriRegistered(String str) {
        String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList();
        //        System.out.println(prefixString);
        if (str.contains(":")){
            String[] split = str.split(":");
            String prefixname = split[0];
            if (!prefixString.contains(prefixname)){
                return false;
            }
            return true;
        } else {
            return true;
        }
    }

    public boolean checkCellUriResolvable(String str) {

        if (str.contains(":")){
            if (URIUtils.isValidURI(str)){
                try {
                    URIUtils.convertToWholeURI(str);
                } catch (Exception e) {
                    return false;
                }
            } else {
                return false;
            }
        }
        return true;
    }

    private String getWasGeneratedBy(Record rec) {

        String str = rec.getValueByColumnName(mapCol.get("WasGeneratedBy"));
        if (str.length() == 0) {
            return "";
        } else if (checkCellUriRegistered(str)) {
            if (checkCellUriResolvable(str)) {
                return str;
            }
        } else if (AttrList.contains(str)) {
            return kbPrefix + "DASA-" + SDDName + "-" + str.replace(" ", "").replace("_","-").replace("??", "");
        } else {
            return "";
        }

        return "";
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

        Map<String, List<String>> mergedEA = this.mergedEA;
        rows.clear();
        List<String> column_name = new ArrayList<String>();
        int rowNumber = 0;

        for (Record record : records) {

            String attr = getAttribute(record);
            if ( attr  == null || attr.equals("")){
                if (column_name.contains(getLabel(record))){
                    rows.add(createRelationRow(record, ++rowNumber));
                }
                continue;
            } else {
                rows.add(createRow(record, ++rowNumber));
                for (String item : getWasDerivedFrom(record)) {
                    rows.add(createDerivedFromRow(item, record));
                }
                column_name.add(getLabel(record));
            }
        }

        /*if (mergedEA != null && mergedEA.keySet().size() > 0) {
            for (String attr : mergedEA.keySet()) {
                if (attr.length() > 0) {
                    rows.add(createMergeEAAttrRow(attr, mergedEA));
                }
            }
        }
        if (mergedAA != null && mergedAA.keySet().size() > 0) {
            for (String attr : mergedAA.keySet()) {
                if (attr.length() > 0) {
                    rows.add(createMergeAAAttrRow(attr, mergedAA));
                }
            }
        }*/
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
    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception {

        Map<String, Object> row = new HashMap<String, Object>();
        List<String> tmp = new ArrayList<String>();

        /* this makes sure columns that are related to mergedEA will not be repeatedly processed.
           for example:
           MSLvrTscore	Visual Reception: T score	ncit:C120401	??visual_reception1
           ??visual_reception1		                cogat:03171 	??child
           MSLvrTscore is processed since ??visual_reception1 is in mergedEA, i.e., processing
           ??visual_reception1 is the same as processing MSLvrTscore. So when we scan the original file
           and reach MSLvrTscore, we should skip it. This tmp map is to make sure we can skip things like
           these correctly
         */
        for (String i : mergedEA.keySet()) {
            tmp.add(mergedEA.get(i).get(0));
        }

        String lineLabel = getLabel(rec);
        if (mergedEA.containsKey(getLabel(rec))) {
            logger.println("[Merged Attribute] : " + getLabel(rec) + " ---> " + mergedEA.get(getLabel(rec)));
            row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + mergedEA.get(getLabel(rec)).get(0).trim().replace(" ", "").replace("_","-").replace("??", ""));
            row.put("a", "hasco:DASchemaAttribute");
            row.put("hasco:hascoType", "hasco:DASchemaAttribute");
            row.put("rdfs:label", mergedEA.get(getLabel(rec)).get(0));
            row.put("rdfs:comment", mergedEA.get(getLabel(rec)).get(1));
            row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
            if (!currentHasEntity.containsKey(getLabel(rec))){
                row.put("hasco:hasEntity", getEntity(rec));
            }
            if (getRelation(rec).length() > 0) {
                row.put(getRelation(rec), getInRelationTo(rec));
            } else {
                row.put("sio:SIO_000668", getInRelationTo(rec));
            }
            if (getInRelationTo(rec).length() > 0) {
                if (getRelation(rec).length() > 0) {
                    row.put("hasco:Relation", getRelation(rec));
                } else {
                    row.put("hasco:Relation", "sio:SIO_000668");
                }
            }
            row.put("hasco:hasAttribute", retrieveAttributes(rec, lineLabel));
            row.put("hasco:hasUnit", mergedEA.get(getLabel(rec)).get(2));
            if (mergedEA.get(getLabel(rec)).get(3).length()>0){
                //row.put("hasco:hasEvent", kbPrefix + "DASE-" + SDDName + "-" + mergedEA.get(getLabel(rec)).get(3).trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-"));
                row.put("hasco:hasEvent", kbPrefix + "DASO-" + SDDName + "-" + mergedEA.get(getLabel(rec)).get(3).trim().replace(" ","").replace("_","-").replace("??", "").replace(":", "-"));
            }
            row.put("hasco:hasSource", "");
            row.put("hasco:isAttributeOf", getAttributeOf(rec));
            row.put("hasco:isVirtual", checkVirtual(rec).toString());
            row.put("hasco:isPIConfirmed", "false");
            if (getWasGeneratedBy(rec).length() > 0) {
                row.put("prov:wasGeneratedBy", getWasGeneratedBy(rec));	
            }
        } else if (!tmp.contains(getLabel(rec))) {
            if (mergedAA.containsKey(getLabel(rec))) {
                logger.println("[Derived Attribute] : " + getLabel(rec) + " ---> " + mergedAA.get(getLabel(rec)));
                List<String> attributes = mergedAA.get(getLabel((rec)));
                row.put("hasco:isAttributeOf", getAttributeOf(attributes.get(attributes.size()-1)));
                row.put("hasco:hasAttribute", attributes);
                //row.put("hasco:hasAttribute", URIUtils.replacePrefixEx(mergedAA.get(getLabel(rec)).get(1)));
            } else {
                row.put("hasco:isAttributeOf", getAttributeOf(rec));
                row.put("hasco:hasAttribute", getAttribute(rec));
            }
            row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
            row.put("a", "hasco:DASchemaAttribute");
            row.put("hasco:hascoType", "hasco:DASchemaAttribute");
            row.put("rdfs:label", getLabel(rec));
            row.put("rdfs:comment", getLabel(rec));
            row.put("hasco:partOfSchema", kbPrefix + "DAS-" + SDDName);
            if (!currentHasEntity.containsKey(getLabel(rec))){
                row.put("hasco:hasEntity", getEntity(rec));
            }
            if (getRelation(rec).length() > 0) {
                row.put(getRelation(rec), getInRelationTo(rec));
            } else {
                row.put("sio:SIO_000668", getInRelationTo(rec));
            }
            if (getInRelationTo(rec).length() > 0) {
                if (getRelation(rec).length() > 0) {
                    row.put("hasco:Relation", getRelation(rec));
                } else {
                    row.put("hasco:Relation", "sio:SIO_000668");
                }
            }
            row.put("hasco:hasUnit", getUnit(rec));
            row.put("hasco:hasEvent", getTime(rec));
            row.put("hasco:hasSource", "");
            row.put("hasco:isVirtual", checkVirtual(rec).toString());
            row.put("hasco:isPIConfirmed", "false");
            if (getWasGeneratedBy(rec).length() > 0) {
                row.put("prov:wasGeneratedBy", getWasGeneratedBy(rec));	
            }
        } else {
            row.put("hasURI", kbPrefix + "DASA-merged-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
        }

        return row;
    }

    private List<String> retrieveAttributes(Record record, String columnLabel) {

        List<String> attributes = new ArrayList<>();
        if ( !mergedEA.containsKey(columnLabel) ) return attributes;

        // note starting from [4] is the list of attributes
        // attributes.add(getAttribute(record));
        List<String> items = mergedEA.get(columnLabel);
        for ( int i = items.size()-1; i >= 4; i-- ) {
            attributes.add(items.get(i));
        }
        attributes.add("end"); // a dummy one to be consistent with mergeAA

        return attributes;
    }

    Map<String, Object> createRelationRow(Record rec, int rowNumber) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
        if (getRelation(rec).length() > 0) {
            row.put(getRelation(rec), getInRelationTo(rec));
        } else {
            row.put("sio:SIO_000668", getInRelationTo(rec));
        }
        if (getInRelationTo(rec).length() > 0) {
            if (getRelation(rec).length() > 0) {
                row.put("hasco:Relation", getRelation(rec));
            } else {
                row.put("hasco:Relation", "sio:SIO_000668");
            }
        }

        return row;
    }

    Map<String, Object> createDerivedFromRow(String item, Record rec) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + getLabel(rec).trim().replace(" ", "").replace("_","-").replace("??", ""));
        row.put("prov:wasDerivedFrom", item);

        return row;
    }

    Map<String, Object> createMergeEAAttrRow(String attr, Map<String, List<String>> mergedEA) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + mergedEA.get(attr).get(0).trim().replace(" ", "").replace("_","-").replace("??", ""));
        row.put("hasco:hasAttribute", mergedEA.get(attr).get(4));
        return row;
    }

    Map<String, Object> createMergeAAAttrRow(String attr, Map<String, List<String>> mergedAA) throws Exception {
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", kbPrefix + "DASA-" + SDDName + "-" + attr.trim().replace(" ", "").replace("_","-").replace("??", ""));
        row.put("hasco:hasAttribute", mergedAA.get(attr).get(1));
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

    @Override
    public void deleteRowsFromTripleStore(List<Map<String, Object>> rows) {
        // doing nothing here because the SDD itself has been deleted already
        return;
    }

}
