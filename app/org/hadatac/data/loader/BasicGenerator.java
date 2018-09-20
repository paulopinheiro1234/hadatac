package org.hadatac.data.loader;

import java.lang.String;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ModelFactory;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModelFactory;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.sparql.SPARQLRepository;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.LabKeyException;
import org.labkey.remoteapi.CommandException;


public abstract class BasicGenerator {

    protected List<Record> records = null;
    protected RecordFile file;

    protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    protected List<HADatAcThing> objects = new ArrayList<HADatAcThing>();

    protected HashMap<String, String> mapCol = new HashMap<String, String>();
    protected String fileName = "";
    
    protected String studyUri = "";
    protected String namedGraphUri = "";
    
    public BasicGenerator(RecordFile file) {
        this.file = file;
        records = file.getRecords();
        fileName = file.getFile().getName();
        initMapping();
    }

    public void initMapping() {}

    abstract public String getTableName();

    abstract public String getErrorMsg(Exception e);

    public String getFileName() {
        return fileName;
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

    public Model createModel(List<Map<String, Object>> rows, String namedGraphUri) {        
        ModelFactory modelFactory = new LinkedHashModelFactory();
        Model model = modelFactory.createEmptyModel();

        ValueFactory factory = SimpleValueFactory.getInstance();
        IRI namedGraph = null;
        if (!namedGraphUri.isEmpty()) {
            namedGraph = factory.createIRI(namedGraphUri);
        }
        
        for (Map<String, Object> row : rows) {
            IRI sub = factory.createIRI(URIUtils.replacePrefixEx((String)row.get("hasURI")));
            for (String key : row.keySet()) {
                if (!key.equals("hasURI")) {
                    IRI pred = null;
                    if (key.equals("a")) {
                        pred = factory.createIRI(URIUtils.replacePrefixEx("rdf:type"));
                    } else {
                        pred = factory.createIRI(URIUtils.replacePrefixEx(key));
                    }

                    String cellValue = (String)row.get(key);
                    if (URIUtils.isValidURI(cellValue)) {
                        IRI obj = factory.createIRI(URIUtils.replacePrefixEx(cellValue));
                        
                        if (namedGraph == null) {
                            model.add(sub, pred, obj);
                        } else {
                            model.add(sub, pred, obj, (Resource)namedGraph);
                        }
                    } else {
                        Literal obj = factory.createLiteral(
                                cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
                        
                        if (namedGraph == null) {
                            model.add(sub, pred, obj);
                        } else {
                            model.add(sub, pred, obj, (Resource)namedGraph);
                        }
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
        if (rows.isEmpty()) {
            return true;
        }

        try {
            checkRows(rows, "hasURI");
        } catch (Exception e) {
            log.addline(Feedback.println(Feedback.WEB, String.format(
                    "[ERROR] Trying to commit invalid rows to LabKey Table %s: ", getTableName())
                    + e.getMessage()));
        }

        Credential cred = Credential.find();
        if (null == cred) {
            log.resetLog();
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
        }

        LabkeyDataHandler labkeyDataHandler = LabkeyDataHandler.createDefault(
                cred.getUserName(), cred.getPassword());
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
            }
        }

        return true;
    }

    public boolean commitRowsToTripleStore(List<Map<String, Object>> rows) {
        Model model = createModel(rows, getNamedGraphUri());
        
        Repository repo = new SPARQLRepository(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_GRAPH));
        repo.initialize();
        
        RepositoryConnection con = repo.getConnection();
        con.add(model);

        if (model.size() > 0) {
            AnnotationLog log = AnnotationLog.create(fileName);
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[OK] %d triple(s) have been committed to triple store", model.size())));
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
            AnnotationLog log = AnnotationLog.create(fileName);
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[OK] %d object(s) have been committed to LabKey", count)));
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
            AnnotationLog log = AnnotationLog.create(fileName);
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[OK] %d object(s) have been committed to triple store", count)));
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
            AnnotationLog log = AnnotationLog.create(fileName);
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[OK] %d object(s) have been committed to solr", count)));
        }

        return true;
    }

    public void deleteRowsFromTripleStore(List<Map<String, Object>> rows) {
        Model model = createModel(rows, "");
        
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

        AnnotationLog log = AnnotationLog.create(fileName);

        checkRows(rows, "hasURI");

        Credential cred = Credential.find();
        if (null == cred) {
            throw new LabKeyException("[ERROR] No LabKey credentials are provided!");
        }

        LabkeyDataHandler labkeyDataHandler = LabkeyDataHandler.createDefault(
                cred.getUserName(), cred.getPassword());
        try {
            labkeyDataHandler.deleteRows(getTableName(), rows);
        } catch (CommandException e) {
            log.addline(Feedback.println(Feedback.WEB, "[ERROR] Delete rows from LabKey: " + e));
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
