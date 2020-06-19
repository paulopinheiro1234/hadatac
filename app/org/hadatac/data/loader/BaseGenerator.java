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
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.metadata.api.MetadataFactory;
import org.hadatac.utils.CollectionUtil;

public abstract class BaseGenerator {

    protected List<Record> records = null;
    protected RecordFile file;
    protected DataFile dataFile;

    protected List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
    protected List<HADatAcThing> objects = new ArrayList<HADatAcThing>();
    
    protected Map<String, Cache> caches = new HashMap<String, Cache>();

    protected HashMap<String, String> mapCol = new HashMap<String, String>();
    protected String fileName = "";
    protected String relativePath = "";

    protected String studyUri = "";
    protected String namedGraphUri = "";
    
    protected AnnotationLogger logger = null;

    public BaseGenerator(DataFile dataFile) {
    	this(dataFile, null);
    }
    
    public BaseGenerator(DataFile dataFile, String studyUri) {
    	if (studyUri != null) {
    		this.studyUri = studyUri;
    	}
    	if (dataFile != null) {
    		this.dataFile = dataFile;
    		file = dataFile.getRecordFile();
    		records = file.getRecords();
    		fileName = dataFile.getPureFileName();
    		logger = dataFile.getLogger();
    	}
    	
        initMapping();
    }

    public void initMapping() {}
    
    public void addCache(Cache cache) {
        if (!caches.containsKey(cache.getName())) {
            caches.put(cache.getName(), cache);
        }
    }
    
    public void clearCacheByName(String name) {
        if (caches.containsKey(name)) {
            caches.get(name).clear();
        }
    }
    
    public void clearAllCaches() {
        for (String name : caches.keySet()) {
            caches.get(name).clear();
        }
    }
    
    public AnnotationLogger getLogger() {
        return logger;
    }

    public String getTableName() {
        return null;
    }

    public String getErrorMsg(Exception e) {
        e.printStackTrace();
        StringWriter errors = new StringWriter();
        e.printStackTrace(new PrintWriter(errors));
        return "Errors in " + getClass().getSimpleName() + ": " + e.getMessage() + " " + errors.toString();
    }

    public DataFile getDataFile() {
        return dataFile;
    }
    
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

    public Map<String, Object> createRow(Record rec, int rowNumber) throws Exception { return null; }

    public HADatAcThing createObject(Record rec, int rowNumber, String selector) throws Exception { return null; }

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
        int skippedRows = 0;
        Record lastRecord = null;
        for (Record record : records) {
        	if (lastRecord != null && record.equals(lastRecord)) {
        		skippedRows++;
        	} else {
        		Map<String, Object> tempRow = createRow(record, ++rowNumber);
        		if (tempRow != null) {
        			rows.add(tempRow);
        			lastRecord = record;
        		}
        	}
        }
        if (skippedRows > 0) {
        	System.out.println("Skipped rows: " + skippedRows);
        }
    }

    public void createObjects() throws Exception {
        if (records == null) {
            return;
        }

        int rowNumber = 0;
        for (Record record : records) {
            HADatAcThing obj = createObject(record, ++rowNumber, null);
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
            logger.println(results + " have been created. ");
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

    public boolean commitRowsToTripleStore(List<Map<String, Object>> rows) {
        Model model = MetadataFactory.createModel(rows, getNamedGraphUri());
        int numCommitted = MetadataFactory.commitModelToTripleStore(
                model, CollectionUtil.getCollectionPath(
                        CollectionUtil.Collection.METADATA_GRAPH));

        if (numCommitted > 0) {
            logger.println(String.format("%d triple(s) have been committed to triple store", model.size()));
        }

        return true;
    }

    public boolean commitObjectsToTripleStore(List<HADatAcThing> objects) {
        int count = 0;
        for (HADatAcThing obj : objects) {
            obj.setNamedGraph(getNamedGraphUri());

            if (obj.saveToTripleStore()) {
                count++;
            }
        }
        
        for (String name : caches.keySet()) {
            if (caches.get(name).getNeedCommit()) {
                System.out.println("cache " + name + " size: Initial " + caches.get(name).getInitialCache().values().size());
                System.out.println("cache " + name + " size: New " + caches.get(name).getNewCache().values().size());
                System.out.println("cache " + name + " size: Total " + caches.get(name).getMapCache().values().size());
                for (Object obj : caches.get(name).getNewCache().values()) {
                    if (obj instanceof HADatAcThing) {
                        ((HADatAcThing) obj).saveToTripleStore();
                        count++;
                    }
                }
            }
        }

        if (count > 0) {
            logger.println(String.format("%d object(s) have been committed to triple store", count));
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
            logger.println(String.format("%d object(s) have been committed to solr", count));
        }

        return true;
    }

    public void deleteRowsFromTripleStore(List<Map<String, Object>> rows) {
        Model model = MetadataFactory.createModel(rows, "");

        Repository repo = new SPARQLRepository(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        repo.init();

        RepositoryConnection con = repo.getConnection();
        con.remove(model);
    }

    public boolean deleteObjectsFromTripleStore(List<HADatAcThing> objects) {
        for (HADatAcThing obj : objects) {
            if (obj.getDeletable()) {
                obj.deleteFromTripleStore();
            }
        }
        
        for (String name : caches.keySet()) {
            if (caches.get(name).getNeedCommit()) {
                for (Object obj : caches.get(name).getNewCache().values()) {
                    if (obj instanceof HADatAcThing) {
                        HADatAcThing object = (HADatAcThing)obj;
                        if (object.getDeletable()) {
                            object.deleteFromTripleStore();
                        }
                    }
                }
            }
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
