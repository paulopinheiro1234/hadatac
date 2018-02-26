package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.LabKeyException;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;


public abstract class BasicGenerator {

    protected List<Record> records = null;

    protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    protected List<HADatAcThing> objects = new ArrayList<HADatAcThing>();

    protected HashMap<String, String> mapCol = new HashMap<String, String>();
    protected String fileName = "";

    public BasicGenerator() {}

    public BasicGenerator(RecordFile file) {
        records = file.getRecords();
        fileName = file.getFile().getName();
        initMapping();
    }

    abstract void initMapping();

    abstract public String getTableName();

    abstract public String getErrorMsg(Exception e);

    public String getFileName() {
        return fileName;
    }

    Map<String, Object> createRow(Record rec, int row_number) throws Exception { return null; }

    HADatAcThing createObject(Record rec, int row_number) throws Exception { return null; }

    public List<Map<String, Object>> getRows() {
        return rows;
    }

    public List<HADatAcThing> getObjects() {
        return objects;
    }

    public void preprocess() throws Exception {}
    public void postprocess() throws Exception {}

    public void createRows() throws Exception {
        if (records == null) {
            return;
        }
        
        int row_number = 0;
        for (Record record : records) {
            Map<String, Object> tempRow = createRow(record, ++row_number);
            if (tempRow != null) {
                rows.add(tempRow);
            }
        }
    }

    public void createObjects() throws Exception {
        if (records == null) {
            return;
        }
        
        int row_number = 0;
        for (Record record : records) {
            HADatAcThing obj = createObject(record, ++row_number);
            if (obj != null) {
                objects.add(obj);
            }
        }
    }

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

    public static Model createModel(List<Map<String, Object>> rows) {
        Model model = ModelFactory.createDefaultModel();
        for (Map<String, Object> row : rows) {
            Resource sub = model.createResource(URIUtils.replacePrefixEx((String)row.get("hasURI")));
            for (String key : row.keySet()) {
                if (!key.equals("hasURI")) {
                    Property pred = null;
                    if (key.equals("a")) {
                        pred = model.createProperty(URIUtils.replacePrefixEx("rdf:type"));
                    }
                    else {
                        pred = model.createProperty(URIUtils.replacePrefixEx(key));
                    }

                    String cellValue = (String)row.get(key);
                    if (URIUtils.isValidURI(cellValue)) {
                        Resource obj = model.createResource(URIUtils.replacePrefixEx(cellValue));
                        model.add(sub, pred, obj);
                    }
                    else {
                        Literal obj = model.createLiteral(
                                cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
                        model.add(sub, pred, obj);
                    }
                }
            }
        }

        return model;
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
        AnnotationLog log = AnnotationLog.create(fileName);

        try {
            checkRows(rows, "hasURI");
        } catch (Exception e) {
            log.addline(Feedback.println(Feedback.WEB, String.format(
                    "[ERROR] Trying to commit invalid rows to LabKey Table %s: ", getTableName())
                    + e.getMessage()));
            log.save();
        }

        Credential cred = Credential.find();
        if (null == cred) {
            log.resetLog();
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
            log.save();
        }

        String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
        LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
                site, cred.getUserName(), cred.getPassword(), path);
        try {
            int nRows = labkeyDataHandler.insertRows(getTableName(), rows);
            log.addline(Feedback.println(Feedback.WEB, String.format(
                    "[OK] %d row(s) have been inserted into Table %s ", nRows, getTableName())));
        } catch (CommandException e1) {
            try {
                labkeyDataHandler.deleteRows(getTableName(), rows);
                int nRows = labkeyDataHandler.insertRows(getTableName(), rows);
                log.addline(Feedback.println(Feedback.WEB, String.format("[OK] %d row(s) have been updated into Table %s ", nRows, getTableName())));
            } catch (CommandException e) {
                log.addline(Feedback.println(Feedback.WEB, "[ERROR] CommitRows inside AutoAnnotator: " + e));
                log.save();
            }
        }

        log.save();

        return true;
    }

    public boolean commitRowsToTripleStore(List<Map<String, Object>> rows) {
        DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
                Collections.getCollectionsName(Collections.METADATA_GRAPH));
        Model model = createModel(rows);
        accessor.add(model);

        AnnotationLog log = AnnotationLog.create(fileName);
        log.addline(Feedback.println(Feedback.WEB, String.format("[OK] %d triple(s) have been committed to triple store", model.size())));
        log.save();

        return true;
    }

    public void commitObjectsToLabKey(List<HADatAcThing> objects) throws Exception {
        Credential cred = Credential.find();
        if (null == cred) {
            throw new Exception("[ERROR] No LabKey credentials are provided!");
        }

        for (HADatAcThing obj : objects) {
            obj.saveToLabKey(cred.getUserName(), cred.getPassword());
        }
    }

    public boolean commitObjectsToTripleStore(List<HADatAcThing> objects) {
        for (HADatAcThing obj : objects) {
            obj.saveToTripleStore();
        }

        return true;
    }
    
    public boolean commitObjectsToSolr(List<HADatAcThing> objects) {
        for (HADatAcThing obj : objects) {
            obj.saveToSolr();
        }

        return true;
    }

    public void deleteRowsFromTripleStore(List<Map<String, Object>> rows) {
        Model model = createModel(rows);

        String query = NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "DELETE WHERE { \n";
        StmtIterator iter = model.listStatements();
        while (iter.hasNext()) {
            Statement stmt = iter.nextStatement();
            query += " <" + stmt.getSubject().getURI() + "> ";
            query += " <" + stmt.getPredicate().getURI() + "> ";
            if (stmt.getObject().isLiteral()) {
                query += " \"" + stmt.getObject().toString() + "\" . \n";
            } else {
                query += " <" + stmt.getObject().toString() + "> . \n";
            }
        }
        query += " } \n";

        try {
            UpdateRequest request = UpdateFactory.create(query);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
            processor.execute();
        } catch (Exception e) {
            System.out.println("[ERROR] " + e.getMessage());
        }
    }

    public void deleteRowsFromLabKey(List<Map<String, Object>> rows) throws Exception {
        AnnotationLog log = AnnotationLog.create(fileName);

        checkRows(rows, "hasURI");

        Credential cred = Credential.find();
        if (null == cred) {
            throw new LabKeyException("[ERROR] No LabKey credentials are provided!");
        }

        String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
        LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
                site, cred.getUserName(), cred.getPassword(), path);
        try {
            labkeyDataHandler.deleteRows(getTableName(), rows);
        } catch (CommandException e) {
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] Delete rows from LabKey: " + e));
            log.save();
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
