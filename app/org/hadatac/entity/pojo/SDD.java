package org.hadatac.entity.pojo;

import java.io.File;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.QuerySolution;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.utils.Templates;

import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;

public class SDD {

    private Map<String, String> mapCatalog = new HashMap<String, String>();
    private Map<String, String> codeMappings = new HashMap<String, String>();
    private Map<String, String> mapAttrObj = new HashMap<String, String>();
    private Map<String, Map<String, String>> codebook = new HashMap<String, Map<String, String>>();
    private Map<String, Map<String, String>> timeline = new HashMap<String, Map<String, String>>();
    private RecordFile sddfile = null;
    
    private AnnotationLogger logger = null;

    private static List<String> metaAttributes;

    private static List<String> getMetaAttributes() {
        if (metaAttributes == null) {
            metaAttributes = new ArrayList<String>() {{
                add("sio:TimeStamp");
                add("sio:TimeInstant");
                add("hasco:namedTime");
                add("hasco:originalID");
                add("hasco:uriId");
                add("hasco:hasMetaEntity");
                add("hasco:hasMetaEntityURI");
                add("hasco:hasMetaAttribute");
                add("hasco:hasMetaAttributeURI");
                add("hasco:hasMetaUnit");
                add("hasco:hasMetaUnitURI");
                add("sio:InRelationTo");
                add("hasco:hasLOD");
                add("hasco:hasCalibration");
                add("hasco:hasElevation");
                add("hasco:hasLocation");
                add("chear:AnalysisMode");
                add("chear:LabHubAccession");
                add("chear:LevelOfDetection");
                add("chear:ReplicateNumber");
            }};
        }
        return metaAttributes;
    }

    public SDD(DataFile dataFile) {
        this.sddfile = dataFile.getRecordFile();
        getMetaAttributes();
        readCatalog(dataFile.getRecordFile());
        logger = dataFile.getLogger();
    }

    public String getName() {
        String sddName = mapCatalog.get("SDD_ID");
        //System.out.println("sddName: " + sddName);
        if (sddName == null) {
            return "";
        }
        return sddName;
    }

    public String getNameFromFileName() {
        return (sddfile.getFileName().split("\\.")[0]).replace("_", "-").replace("SDD-", "");
    }

    public String getFileName() {
        return sddfile.getFileName();
    }

    public String getVersion() {
        String sddVersion = mapCatalog.get("Version");
        if (sddVersion == null) {
            return "";
        }
        return sddVersion;
    }

    public Map<String, String> getCatalog() {
        return mapCatalog;
    }

    public Map<String, String> getCodeMapping() {
        return codeMappings;
    }

    public Map<String, String> getMapAttrObj() {
        return mapAttrObj;
    }

    public Map<String, Map<String, String>> getCodebook() {
        return codebook;
    }

    public Map<String, Map<String, String>> getTimeLine() {
        return timeline;
    }

    private void readCatalog(RecordFile file) {
        if (!file.isValid()) {
            return;
        }
        // This is on the infosheet
        for (Record record : file.getRecords()) {
            mapCatalog.put(record.getValueByColumnIndex(0), record.getValueByColumnIndex(1));
        }
    }

    public File downloadFile(String fileURL, String fileName) {
        System.out.println("fileURL: " + fileURL);
        
        if (fileURL == null || fileURL.length() == 0) {
            return null;
        } else {
            try {
                URL url = new URL(fileURL);
                File file = new File(fileName);
                FileUtils.copyURLToFile(url, file);
                return file;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        }
    }

    public boolean checkCellValue(String str) {
        if (str == null) {
            return false;
        }
        if (str.contains(",")) {
            return false;
        }
        if (str.trim().contains(" ")) {
            return false;
        }
        return true;
    }

    public boolean checkCellNamespace(String str) {
        if (str == null) {
            return true;
        }
        String prefixString = NameSpaces.getInstance().printSparqlNameSpaceList();
        // System.out.println(prefixString);
        if (str.contains(":")) {
            String[] split = str.split(":");
            String prefixname = split[0];
            if (!prefixString.contains(prefixname)) {
                return false;
            }
        } 
        return true;
    }

    public boolean checkCellLabel(String str) {
        if (str == null) {
            return true;
        }
        if (!str.contains(":")) {
            return true;
        }
        if (metaAttributes.contains(str)) {
            return true;
        }

        String foundLabel = FirstLabel.getLabel(URIUtils.replacePrefixEx(str));
        return (foundLabel != null && !foundLabel.equals(""));
    }

    /*
    public boolean checkCellUriResolvable(String str) {
        if (str == null) {
            return true;
        }
        if (str.contains(":")) {
            if (URIUtils.isValidURI(str)) {
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
	}*/

    public boolean checkIndicatorPath(String str) {

        if (metaAttributes.contains(str)) {
            return true;
        }

        String expanded = URIUtils.replacePrefixEx(str);
        String indvIndicatorQuery = "";
        String STUDY_INDICATOR = URIUtils.replacePrefixEx("hasco:StudyIndicator");
        String SAMPLE_INDICATOR = URIUtils.replacePrefixEx("hasco:SampleIndicator");

        indvIndicatorQuery += NameSpaces.getInstance().printSparqlNameSpaceList();
        indvIndicatorQuery += " SELECT * WHERE {  <" + expanded + "> rdfs:subClassOf*  ?super . }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), indvIndicatorQuery);

        if (!resultsrw.hasNext()) {
            return false;
        }

        String superStr = "";

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("super") != null) {
                superStr = soln.get("super").toString();
                // System.out.println("SDD:  Response for [" + expanded + "] is [" + superStr + "]");
                if (superStr.equals(STUDY_INDICATOR) || superStr.equals(SAMPLE_INDICATOR)) {
                    return true;
                }
            }

        }

        // System.out.println("SDD: [WARNING] " + expanded + " is not an indicator");
        return false;
    }

    public List<Map<String, List<String>>> readDDforEAmerge(RecordFile file) {

        //System.out.println("SDD: initiating readDDforEAmerge()");
        Map<String, List<String>> mapEAmerge = new HashMap<String, List<String>>();
        Map<String, List<String>> mapAAmerge = new HashMap<String, List<String>>();
        List<Map<String, List<String>>> response = new ArrayList<Map<String, List<String>>>(); 
        response.add(mapEAmerge);
        response.add(mapAAmerge);
        Map<String, List<String>> dasaContent = new HashMap<String, List<String>>();

        if (!file.isValid()) {
            return response;
        }

        for (Record record : file.getRecords()) {
            if (checkCellValue(record.getValueByColumnIndex(0))) {
                if (record.getValueByColumnName("Attribute") != null
                        && record.getValueByColumnName("Attribute").length() > 0) {
                    if (!dasaContent.containsKey(record.getValueByColumnIndex(0))) {
                        List<String> dasa_entry = new ArrayList<String>();
                        dasa_entry.add(record.getValueByColumnName("Attribute"));
                        dasa_entry.add(record.getValueByColumnName("attributeOf"));
                        dasaContent.put(record.getValueByColumnIndex(0),dasa_entry);
                    }
                }
            }
        }

        for (Record record : file.getRecords()) {

            if (checkCellValue(record.getValueByColumnIndex(0))) {

                String columnCell = record.getValueByColumnIndex(0);
                String labelCell = record.getValueByColumnName("Label");
                String attrCell = record.getValueByColumnName("Attribute");
                String unitCell = record.getValueByColumnName("Unit");
                String timeCell = record.getValueByColumnName("Time");
                String entityCell = record.getValueByColumnName("Entity");
                String inRelationToCell = record.getValueByColumnName("inRelationTo");
                String attributeOfCell = record.getValueByColumnName("attributeOf");
                String dfCell = record.getValueByColumnName("wasDerivedFrom");

                if (dasaContent.containsKey(columnCell) && (attributeOfCell.startsWith("??"))) {

                    //System.out.println("listEAmergeTrigger: " + columnCell + " " + attributeOfCell);

                    List<String> listEAmerge = new ArrayList<String>();

                    if (dasaContent.containsKey(attributeOfCell)) {

                        if (columnCell != null) {
                            listEAmerge.add(columnCell);
                        } else {
                            listEAmerge.add("");
                        }

                        if (labelCell != null) {
                            listEAmerge.add(labelCell);
                        } else {
                            listEAmerge.add("");
                        }

                        if (unitCell != null) {
                            listEAmerge.add(unitCell);
                        } else {
                            listEAmerge.add("");
                        }

                        if (timeCell != null) {
                            listEAmerge.add(timeCell);
                        } else {
                            listEAmerge.add("");
                        }

                        if (attrCell != null) {
                            listEAmerge.add(attrCell);
                        } else {
                            listEAmerge.add("");
                        }

                        mapEAmerge.put(attributeOfCell, listEAmerge);
                    }
                    //System.out.println("listEAmerge :" + listEAmerge);
                } else if (dasaContent.containsKey(columnCell)) {

                    List<String> listAAmerge = new ArrayList<String>();

                    if (dasaContent.containsKey(attributeOfCell)) {

                        if (attributeOfCell != null) {
                            listAAmerge.add(attributeOfCell);
                        } else {
                            listAAmerge.add("");
                        }

                        if (dasaContent.get(attributeOfCell).get(0) != null) {
                            listAAmerge.add(dasaContent.get(attributeOfCell).get(0));
                        } else {
                            listAAmerge.add("");
                        }

                        if (dasaContent.get(attributeOfCell).get(1) != null) {
                            listAAmerge.add(dasaContent.get(attributeOfCell).get(1));
                        } else {
                            listAAmerge.add("");
                        }

                        mapAAmerge.put(columnCell, listAAmerge);
                    }
                }
            }
        }

        //System.out.println("SDD: l_dasa :" + l_dasa);
        //System.out.println("SDD: mapEAmerge :" + mapEAmerge.keySet());
        return response;
    }
    // public void getCellValues(RecordFile file){
    //     if (file.getHeaders().size() > 0) {
    //         logger.println(file.getHeaders().size() + " columns!!!!!");
    //     }
    //     List<String> cvalues=file.getRowValues;
    //     for (int i=0;i<cvalues.size();i++){
    //         logger.println(cvalues.get(i));
    //     }
    // }

    public boolean readDataDictionary(RecordFile file) {

        if (!file.isValid()) {
            return false;
        }

        if (file.getHeaders().size() > 0) {
            logger.println("The Dictionary Mapping has " + file.getHeaders().size() + " columns.");
        } else {
            logger.printExceptionById("SDD_00007");
        }

        //Boolean uriResolvable = true;
        Boolean namespaceRegistered = true;
        Boolean hasLabel = true;
        Boolean isIndicator = true;

        List<String> checkUriNamespaceResults = new ArrayList<String>();
        List<String> checkUriLabelResults = new ArrayList<String>();
        List<String> checkCellValResults = new ArrayList<String>();
        //List<String> checkUriResolveResults = new ArrayList<String>();
        List<String> checkStudyIndicatorPathResults = new ArrayList<String>();
        List<String> dasaList = new ArrayList<String>();
        List<String> dasoList = new ArrayList<String>();
        Map<String, String> sa2so = new HashMap<String, String>();
        Map<String, String> so2so2 = new HashMap<String, String>();
        Map<String, String> so2type = new HashMap<String, String>();
        Map<String, String> so2role = new HashMap<String, String>();
        Map<String, String> so2df = new HashMap<String, String>();

        long rowNumber = 0;
        for (Record record : file.getRecords()) {
            rowNumber++;
            if (checkCellValue(record.getValueByColumnIndex(0))) {
                String attributeCell = record.getValueByColumnName("Attribute");
                String entityCell = record.getValueByColumnName("Entity");
                String roleCell = record.getValueByColumnName("Role");
                String relationCell = record.getValueByColumnName("Relation");
                String inRelationToCell = record.getValueByColumnName("inRelationTo");
                String attributeOfCell = record.getValueByColumnName("attributeOf");
                String dfCell = record.getValueByColumnName("wasDerivedFrom");

                //System.out.println("A: " + attributeCell + " (" + FirstLabel.getLabel(attributeCell)+ ")");
                //System.out.println("E: " + entityCell + " (" + FirstLabel.getLabel(entityCell)+ ")");
                //System.out.println("R: " + roleCell + " (" + FirstLabel.getLabel(roleCell)+ ")");
                //System.out.println("Rel: " + relationCell + " (" + FirstLabel.getLabel(relationCell)+ ")");

                /*
                if (checkCellUriResolvable(attributeCell)) {
                    if (checkCellUriResolvable(entityCell)) {
                        if (checkCellUriResolvable(roleCell)) {
                            if (checkCellUriResolvable(relationCell)) {

                            } else {
                                uriResolvable = false;
                                checkUriResolveResults.add(relationCell);
                            }
                        } else {
                            uriResolvable = false;
                            checkUriResolveResults.add(roleCell);
                        }
                    } else {
                        uriResolvable = false;
                        checkUriResolveResults.add(entityCell);
                    }
                } else {
                    uriResolvable = false;
                    checkUriResolveResults.add(attributeCell);
		    }*/

                /* 
                 *  Check if cell has valid namespace. If yes, further check 
                 *  if a label can be retrived from the cell's value
                 */

                if (attributeCell != null && !attributeCell.equals("")) {
                    if (!checkCellNamespace(attributeCell)) {
                        namespaceRegistered = false;
                        if (!checkUriNamespaceResults.contains(attributeCell)) {
                            checkUriNamespaceResults.add(attributeCell);
                        }
                    } else if (!checkCellLabel(attributeCell)) {
                        hasLabel = false;
                        if (!checkUriLabelResults.contains(attributeCell)) {
                            checkUriLabelResults.add(attributeCell);
                        }
                    } 
                }
                if (entityCell != null && !entityCell.equals("")) {
                    if (!checkCellNamespace(entityCell)) {
                        namespaceRegistered = false;
                        if (!checkUriNamespaceResults.contains(entityCell)) {
                            checkUriNamespaceResults.add(entityCell);
                        }
                    } else if (!checkCellLabel(entityCell)) {
                        hasLabel = false;
                        if (!checkUriLabelResults.contains(entityCell)) {
                            checkUriLabelResults.add(entityCell);
                        }
                    }
                } 
                if (roleCell != null && !roleCell.equals("")) {
                    if (!checkCellNamespace(roleCell)) {
                        namespaceRegistered = false;
                        if (!checkUriNamespaceResults.contains(roleCell)) {
                            checkUriNamespaceResults.add(roleCell);
                        }
                    } else if (!checkCellLabel(roleCell)) {
                        hasLabel = false;
                        if (!checkUriLabelResults.contains(roleCell)) {
                            checkUriLabelResults.add(roleCell);
                        }
                    } 
                }
                if (relationCell != null && !relationCell.equals("")) {
                    if (!checkCellNamespace(relationCell)) {
                        namespaceRegistered = false;
                        if (!checkUriNamespaceResults.contains(relationCell)) {
                            checkUriNamespaceResults.add(relationCell);
                        }
                    } else if (!checkCellLabel(relationCell)) {
                        hasLabel = false;
                        if (!checkUriLabelResults.contains(relationCell)) {
                            checkUriLabelResults.add(relationCell);
                        }
                    } 
                }

                /* 
                 *  Check if the values of attribute cells are subclasses of  
                 *  study indicators
                 */

                if (URIUtils.isValidURI(attributeCell)) {
                    isIndicator = checkIndicatorPath(attributeCell);
                    if (!isIndicator) {
                        System.out.println("Adding " + attributeCell);
                        checkStudyIndicatorPathResults.add(attributeCell);
                    }
                } else {
                    if (entityCell == null || entityCell.length() == 0) {
                        isIndicator = false;
                        if (attributeCell != null && !attributeCell.isEmpty()) {
                            checkStudyIndicatorPathResults.add(attributeCell);
                        }
                    }
                }

                /* 
                 *  Check if the values of attributeOf cells references to   
                 *  objects defined in the SDD
                 */

                if (attributeCell != null && attributeCell.length() > 0) {
                    dasaList.add(record.getValueByColumnIndex(0));
                    if (attributeOfCell.length() > 0) {
                        sa2so.put(record.getValueByColumnIndex(0), attributeOfCell);
                    } else {
                        logger.printExceptionByIdWithArgs("SDD_00008", record.getValueByColumnIndex(0));
                    }
                }

                if (entityCell != null && entityCell.length() > 0) {
                    if (URIUtils.isValidURI(entityCell)) {
                        dasoList.add(record.getValueByColumnIndex(0));
                        if (inRelationToCell.length() > 0) {
                            so2so2.put(record.getValueByColumnIndex(0), inRelationToCell);
                        } else {

                        }

                        so2type.put(record.getValueByColumnIndex(0), entityCell);

                        if (roleCell.length() > 0) {
                            so2role.put(record.getValueByColumnIndex(0), roleCell);
                        } else {

                        }

                        if (dfCell.length() > 0) {
                            so2df.put(record.getValueByColumnIndex(0), dfCell);
                        } else {

                        }
                    } else if (codeMappings.containsKey(entityCell)) {
                        if (URIUtils.isValidURI(codeMappings.get(entityCell))) {
                            dasoList.add(record.getValueByColumnIndex(0));
                            if (inRelationToCell.length() > 0) {
                                so2so2.put(record.getValueByColumnIndex(0), inRelationToCell);
                            } else {

                            }

                            so2type.put(record.getValueByColumnIndex(0), codeMappings.get(entityCell));

                            if (roleCell.length() > 0) {
                                so2role.put(record.getValueByColumnIndex(0), roleCell);
                            } else {

                            }

                            if (dfCell.length() > 0) {
                                so2df.put(record.getValueByColumnIndex(0), dfCell);
                            } else {

                            }
                        }
                    } else {
                        logger.printExceptionByIdWithArgs("SDD_00009", entityCell);
                        return false;
                    }
                }

                if (checkCellValue(record.getValueByColumnName("attributeOf"))) {
                    mapAttrObj.put(record.getValueByColumnIndex(0), record.getValueByColumnName("attributeOf"));
                } else {
                    logger.printExceptionByIdWithArgs("SDD_00010", record.getValueByColumnName("attributeOf"), rowNumber);
                    return false;
                }

            } else {
                logger.printExceptionByIdWithArgs("SDD_00011", record.getValueByColumnName("Column"));
                return false;
            }

            mapAttrObj.put(record.getValueByColumnName(Templates.LABEL),
                    record.getValueByColumnName(Templates.ATTTRIBUTEOF));
        }

        /*if (checkUriResolveResults.size() > 0) {
            logger.printException("The Dictionary Mapping has unresolvable uris in the following cells: "
                    + String.join(", ", checkUriResolveResults) + " .");
            return false;
	    }*/

        if (checkUriNamespaceResults.size() > 0) {
            logger.printExceptionByIdWithArgs("SDD_00012", String.join(", ", checkUriNamespaceResults));
            return false;
        }

        if (checkUriLabelResults.size() > 0) {
            logger.printWarningByIdWithArgs("SDD_00013", String.join(", ", checkUriLabelResults));
        }

        if (checkStudyIndicatorPathResults.size() > 0) {
            logger.printWarningByIdWithArgs("SDD_00014", String.join(", ", checkStudyIndicatorPathResults));
        }

        if (checkCellValResults.size() > 0) {
            logger.printExceptionByIdWithArgs("SDD_00015", String.join(", ", checkCellValResults));
            return false;
        }

        //if (uriResolvable == true) {
        //    AnnotationLog.println("The Dictionary Mapping has resolvable uris.", sddfile.getFileName());
        //}
        if (namespaceRegistered == true) {
            logger.println("The Dictionary Mapping's namespaces are all registered.");
        }
        if (hasLabel == true) {
            logger.println("The Dictionary Mapping's terms have labels.");
        }
        if (isIndicator == true) {
            logger.println("The Dictionary Mapping's attributes are all subclasses of hasco:StudyIndicator or hasco:SampleIndicator.");
        }

        logger.println("The Dictionary Mapping has correct content under \"Column\" and \"attributeOf\" columns.");
        //System.out.println("[SDD] mapAttrObj: " + mapAttrObj);

        return true;
    }

    public boolean readCodeMapping(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            String code = record.getValueByColumnName("code");
            String uri = record.getValueByColumnName("uri");
            if (uri.startsWith("obo:UO_")) {
                uri = uri.replace("obo:UO_", "uo:");
            }
            codeMappings.put(code, uri);
        }

        if (codeMappings.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean readCodebook(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            if (!record.getValueByColumnName(Templates.LABEL).isEmpty()) {
                String colName = record.getValueByColumnName(Templates.LABEL);
                Map<String, String> mapCodeClass = null;
                if (!codebook.containsKey(colName)) {
                    mapCodeClass = new HashMap<String, String>();
                    codebook.put(colName, mapCodeClass);
                } else {
                    mapCodeClass = codebook.get(colName);
                }
                String classUri = "";
                if (!record.getValueByColumnName(Templates.CLASS).isEmpty()) {
                    // Class column
                    // System.out.println("[SDD] CLASS " +
                    // record.getValueByColumnName(Templates.CLASS));
                    classUri = URIUtils.replacePrefixEx(record.getValueByColumnName(Templates.CLASS));
                }
                // else {
                // // Resource column
                // classUri = URIUtils.replacePrefixEx(record.get(4));
                // }
                // System.out.println("[SDD] CODE " +
                // record.getValueByColumnName(Templates.CODE));
                mapCodeClass.put(record.getValueByColumnName(Templates.CODE), classUri);
            }
        }

        if (codebook.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }

    public boolean readTimeline(RecordFile file) {
        if (!file.isValid()) {
            return false;
        }

        for (Record record : file.getRecords()) {
            if (record.getValueByColumnName("Name") != null && !record.getValueByColumnName("Name").isEmpty()) {
                String primaryKey = record.getValueByColumnName("Name");

                Map<String, String> timelineRow = new HashMap<String, String>();
                List<String> colNames = new ArrayList<String>();
                colNames.add("Label");
                colNames.add("Type");
                colNames.add("Start");
                colNames.add("End");
                colNames.add("Unit");
                colNames.add("inRelationTo");

                for (String col : colNames) {
                    if (!record.getValueByColumnName(col).isEmpty()) {
                        timelineRow.put(col, record.getValueByColumnName(col));
                    } else {
                        timelineRow.put(col, "null");
                    }
                }
                timeline.put(primaryKey, timelineRow);
            }
        }

        if (timeline.isEmpty()) {
            return false;
        } else {
            return true;
        }
    }
}
