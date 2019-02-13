package org.hadatac.data.loader;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.metadata.api.MetadataFactory;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.LabKeyException;
import org.labkey.remoteapi.CommandException;


public abstract class BaseGenerator {

    protected List<Record> records = null;
    protected RecordFile file;

    protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    protected List<HADatAcThing> objects = new ArrayList<HADatAcThing>();

    protected HashMap<String, String> mapCol = new HashMap<String, String>();
    protected String fileName = "";
    protected String relativePath = "";

    protected String studyUri = "";
    protected String namedGraphUri = "";

    public BaseGenerator(RecordFile file) {
        this.file = file;
        records = file.getRecords();
        fileName = file.getFile().getName();

        String parentDir = file.getFile().getParent();
        parentDir = parentDir.replace(ConfigProp.getPathUnproc().replace("/", ""), "")
                .replace(ConfigProp.getPathProc().replace("/", ""), "")
                .replace("/", "");
        if (parentDir.trim().isEmpty()) {
            relativePath = fileName;
        } else {
            relativePath = parentDir + "/" + fileName;
        }

        initMapping();
    }

    public void initMapping() {}

    public String getTableName() {
        return null;
    }

    public String getErrorMsg(Exception e) {
        e.printStackTrace();
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return "Errors in " + getClass().getSimpleName() + ": " + e.getMessage() + " " + errors.toString();
    }

    public String getFileName() {
        return fileName;
    }

    public String getRelativePath() {
        return relativePath;
    }

    public RecordFile getRecordFile() {
        return file;
    }

    public String getStudyUri() {
        return studyUri;
    }
    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    public String getNamedGraphUri() {
        return namedGraphUri;
    }

    public void setNamedGraphUri(String namedGraphUri) {
        this.namedGraphUri = namedGraphUri;
    }

    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception { return null; }

    public HADatAcThing createObject(Record rec, int rowNumber) throws Exception { return null; }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public List<HADatAcThing> getObjects() {
        return objects;
    }
    
    public void addRow(Map<String, Object> row) {
        rows.add(row);
    }
    
    public void addObject(HADatAcThing object) {
        objects.add(object);
    }

    public void preprocess() throws Exception {}
    public void postprocess() throws Exception {}

    public void createRows() throws Exception {        
        if (records == null) {
            return;
        }

        int rowNumber = 0;
        for (Record record : records) {
            Map<String, Object> tempRow = createRow(record, ++rowNumber);
            if (tempRow != null) {
                rows.add(tempRow);
            }
        }
    }

    public void createObjects() throws Exception {
        if (records == null) {
            return;
        }

        int rowNumber = 0;
        for (Record record : records) {
            HADatAcThing obj = createObject(record, ++rowNumber);
            if (obj != null) {
                objects.add(obj);
            }
        }

        Map<String, Integer> mapStats = new HashMap<String, Integer>();
        for (HADatAcThing obj : objects) {
            String clsName = obj.getClass().getSimpleName();
            if (mapStats.containsKey(clsName)) {
                mapStats.put(clsName, mapStats.get(clsName) + 1);
            } else {
                mapStats.put(clsName, 1);
            }
        }
        String results = String.join(" and ", mapStats.entrySet().stream()
                .map(e -> e.getValue() + " " + e.getKey() + "(s)")
                .collect(Collectors.toList()));
        if (!results.isEmpty()) {
            AnnotationLog.println(results + " have been created. ", fileName);
        }
    }

    @Override
    public String toString() {
        if (rows.isEmpty()) {
            return "";
        }
        String result = "";
        result = String.join(",", rows.get(0).keySet());
        for (Map<String, Object> row : rows) {
            List<String> values = new ArrayList<String>();
            for (String colName : rows.get(0).keySet()) {
                if (row.containsKey(colName)) {
                    values.add((String) row.get(colName));
                } else {
                    values.add("");
                }
            }
            result += "\n";
            result += String.join(",", values);
        }

        return result;
    }

    private void checkRows(List<Map<String, Object>> rows, String primaryKey) throws Exception {
        int i = 1;
        Set<String> values = new HashSet<>();
        for (Map<String, Object> row : rows) {
            String val = (String)row.get(primaryKey);
            if (null == val) {
                throw new Exception(String.format("Found Row %d without URI specified!", i));
            }
            if (values.contains(val)) {
                throw new Exception(String.format("Duplicate Concepts in Inputfile row %d :" + val + " would be duplicate URIs!", i));
            }
            else {
                values.add(val);
            }

            i++;
        }
    }

    public boolean commitRowsToLabKey(List<Map<String, Object>> rows) {
        if (rows.isEmpty()) {
            return true;
        }
        
        String tableName = getTableName();
        if (null == tableName) {
            AnnotationLog.printException("No LabKey table name is specified", fileName);
            return false;
        }

        try {
            checkRows(rows, "hasURI");
        } catch (Exception e) {
            AnnotationLog.printException(String.format(
                    "Trying to commit invalid rows to LabKey Table %s: ", tableName)
                    + e.getMessage(), fileName);
            return false;
        }

        Credential cred = Credential.find();
        if (null == cred) {
            AnnotationLog.printException("No LabKey credentials are provided!", fileName);
            return false;
        }

        LabkeyDataHandler labkeyDataHandler = LabkeyDataHandler.createDefault(
                cred.getUserName(), cred.getPassword());
        try {
            int nRows = labkeyDataHandler.insertRows(tableName, rows);
            AnnotationLog.println(String.format(
                    "%d row(s) have been inserted into Table %s ", nRows, tableName), fileName);
        } catch (CommandException e1) {
            try {
                labkeyDataHandler.deleteRows(tableName, rows);
                int nRows = labkeyDataHandler.insertRows(tableName, rows);
                AnnotationLog.println(String.format("%d row(s) have been updated into Table %s ", nRows, tableName), fileName);
            } catch (CommandException e) {
                AnnotationLog.printException("CommitRows inside AutoAnnotator: " + e, fileName);
                return false;
            }
        }

        return true;
    }

    public boolean commitRowsToTripleStore(List<Map<String, Object>> rows) {
        Model model = MetadataFactory.createModel(rows, getNamedGraphUri());
        int numCommitted = MetadataFactory.commitModelToTripleStore(
                model, CollectionUtil.getCollectionPath(
                        CollectionUtil.Collection.METADATA_GRAPH));

        if (numCommitted > 0) {
            AnnotationLog.println(String.format("%d triple(s) have been committed to triple store", model.size()), fileName);
        }

        return true;
    }

    public void commitObjectsToLabKey(List<HADatAcThing> objects) throws Exception {
        Credential cred = Credential.find();
        if (null == cred) {
            throw new Exception("[ERROR] No LabKey credentials are provided!");
        }

        int count = 0;
        for (HADatAcThing obj : objects) {
            count += obj.saveToLabKey(cred.getUserName(), cred.getPassword());
        }

        if (count > 0) {
            AnnotationLog.println(String.format("%d object(s) have been committed to LabKey", count), fileName);
        }
    }

    public boolean commitObjectsToTripleStore(List<HADatAcThing> objects) {
        int count = 0;
        for (HADatAcThing obj : objects) {
            obj.setNamedGraph(getNamedGraphUri());

            if (obj.saveToTripleStore()) {
                count++;
            }
        }

        if (count > 0) {
            AnnotationLog.println(String.format("%d object(s) have been committed to triple store", count), fileName);
        }

        return true;
    }

    public boolean commitObjectsToSolr(List<HADatAcThing> objects) throws Exception {
        int count = 0;
        for (HADatAcThing obj : objects) {
            if (obj.saveToSolr()) {
                count++;
            }
        }

        if (count > 0) {
            AnnotationLog.println(String.format("%d object(s) have been committed to solr", count), fileName);
        }

        return true;
    }

    public void deleteRowsFromTripleStore(List<Map<String, Object>> rows) {
        Model model = MetadataFactory.createModel(rows, "");

        Repository repo = new SPARQLRepository(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        repo.initialize();

        RepositoryConnection con = repo.getConnection();
        con.remove(model);
    }

    public void deleteRowsFromLabKey(List<Map<String, Object>> rows) throws Exception {
        if (rows.isEmpty()) {
            return;
        }

        checkRows(rows, "hasURI");

        Credential cred = Credential.find();
        if (null == cred) {
            throw new LabKeyException("[ERROR] No LabKey credentials are provided!");
        }

        LabkeyDataHandler labkeyDataHandler = LabkeyDataHandler.createDefault(
                cred.getUserName(), cred.getPassword());
        
        try {
            String tableName = getTableName();
            if (null == tableName) {
                AnnotationLog.printException("No LabKey table name is specified", fileName);
            }
            labkeyDataHandler.deleteRows(tableName, rows);
        } catch (CommandException e) {
            AnnotationLog.printException("Delete rows from LabKey: " + e, fileName);
            throw new LabKeyException("[ERROR] Delete rows from LabKey: " + e);
        }
    }

    public void deleteObjectsFromLabKey(List<HADatAcThing> objects) throws Exception {
        Credential cred = Credential.find();
        if (null == cred) {
            throw new LabKeyException("[ERROR] No LabKey credentials are provided!");
        }

        for (HADatAcThing obj : objects) {
            obj.deleteFromLabKey(cred.getUserName(), cred.getPassword());
        }
    }

    public boolean deleteObjectsFromTripleStore(List<HADatAcThing> objects) {
        for (HADatAcThing obj : objects) {
            obj.deleteFromTripleStore();
        }

        return true;
    }

    public boolean deleteObjectsFromSolr(List<HADatAcThing> objects) throws Exception {
        for (HADatAcThing obj : objects) {
            obj.deleteFromSolr();
        }

        return true;
    }
}
