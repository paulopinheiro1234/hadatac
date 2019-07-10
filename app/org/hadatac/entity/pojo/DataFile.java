package org.hadatac.entity.pojo;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import play.mvc.Controller;
import play.mvc.Result;
import play.data.*;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.controllers.sandbox.Sandbox;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.TreeNode;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.typesafe.config.ConfigFactory;


public class DataFile implements Cloneable {

    // Process status for auto-annotator
    public static final String UNPROCESSED = "UNPROCESSED";
    public static final String PROCESSED = "PROCESSED";
    public static final String FREEZED = "FREEZED";
    public static final String WORKING = "WORKING";

    // Process status for downloader
    public static final String CREATING = "CREATING";
    public static final String CREATED 	= "CREATED";
    public static final String DELETED  = "DELETED";
    
    // Process status for SDD generator
    public static final String DD_UNPROCESSED = "DD_UNPROCESSED";
    public static final String DD_PROCESSED = "DD_PROCESSED";
    public static final String DD_FREEZED = "DD_FREEZED";

    @Field("id")
    private String id;
    @Field("shared_id_str")
    private String sharedId = "";
    @Field("file_name_str")
    private String fileName = "";
    @Field("dir_str")
    private String dir = "";
    @Field("owner_email_str")
    private String ownerEmail = "";
    @Field("study_uri_str")
    private String studyUri = "";
    @Field("acquisition_uri_str")
    private String dataAcquisitionUri = "";
    @Field("dataset_uri_str")
    private String datasetUri = "";
    @Field("status_str")
    private String status = "";
    @Field("completion_percentage_int")
    private int completionPercentage = 0;
    @Field("submission_time_str")
    private String submissionTime = "";
    @Field("completion_time_str")
    private String completionTime = "";
    @Field("last_processed_time_str")
    private String lastProcessTime = "";
    @Field("log_str")
    private String log = "";
    
    private AnnotationLogger logger = null;
    private RecordFile recordFile = null;
    private File file = null;

    public DataFile(String fileName) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
        logger = new AnnotationLogger(this);
    }
    
    public DataFile(RecordFile recordFile) {
        this(recordFile.getFileName());
        this.file = recordFile.getFile();
    }
    
    public Object clone()throws CloneNotSupportedException {
        return (DataFile)super.clone();  
    }

    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof DataFile) {
            return fileName.equals(((DataFile) o).fileName);
        }
        return false;
    }
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getSharedId() {
        return sharedId;
    }
    public void setSharedId(String sharedId) {
        this.sharedId = sharedId;
    }
    
    public String getAbsolutePath() {
        if (getStatus().equals(UNPROCESSED)) {
            return Paths.get(ConfigProp.getPathUnproc(), getDir(), getFileName()).toString();
        } else if (getStatus().equals(PROCESSED)) {
            return Paths.get(ConfigProp.getPathProc(), getDir(), getFileName()).toString();
        } else if (getStatus().equals(WORKING)) {
            return Paths.get(ConfigProp.getPathWorking(), getDir(), getFileName()).toString();
        } else if (Arrays.asList(CREATED, CREATING, DELETED).contains(getStatus())) {
            return Paths.get(ConfigProp.getPathDownload(), getDir(), getFileName()).toString();
        } else if (Arrays.asList(DD_UNPROCESSED, DD_PROCESSED, DD_FREEZED).contains(getStatus())) {
            return Paths.get(ConfigProp.getPathDataDictionary(), getDir(), getFileName()).toString();
        }
        
        return "";
    }
    
    public static String getMediaUrl(String filename) {
    	if (filename == null || !filename.startsWith("file:///media/")) {
    		return "";
    	}
    	return ConfigFactory.load().getString("hadatac.console.host") + 
    			org.hadatac.console.controllers.routes.Portal.index().url() + 
    			filename.replace("file:///", "");
    }
    
    public AnnotationLogger getLogger() {
        return logger;
    }
    public void setLogger(AnnotationLogger logger) {
        this.logger = logger;
    }
    
    public RecordFile getRecordFile() {
        return recordFile;
    }
    public void setRecordFile(RecordFile recordFile) {
        this.recordFile = recordFile;
        this.file = recordFile.getFile();
    }
    
    public File getFile() {
        return file;
    }
    
    public String getFileExtention() {
        return FilenameUtils.getExtension(fileName);
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getStudyUri() {
        return studyUri;
    }
    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    public String getDataAcquisitionUri() {
        return dataAcquisitionUri;
    }
    public void setDataAcquisitionUri(String dataAcquisitionUri) {
        this.dataAcquisitionUri = dataAcquisitionUri;
    }

    public String getDatasetUri() {
        return datasetUri;
    }
    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public String getDir() {
        return dir;
    }
    public void setDir(String dir) {
        dir = Paths.get(dir).toString();
        if (dir.startsWith("/")) {
            dir = dir.substring(1, dir.length());
        }
        this.dir = dir;
    }

    public String getStatus() {
        return status;
    }
    
    public boolean isMediaFile() {
    	System.out.println("isMediaFile: [" + fileName + "]   status: [" + status +"]");
    	if (fileName == null || fileName.isEmpty()) {
    		return false;
    	}
    	return  (fileName.endsWith(".png") || fileName.endsWith(".PNG") ||
        		 fileName.endsWith(".jpg") || fileName.endsWith(".JPG") ||
        		 fileName.endsWith(".jpeg") || fileName.endsWith(".JPEG") ||
        		 fileName.endsWith(".pdf") || fileName.endsWith(".PDF"));
    }
    
    public void setStatus(String status) {
        this.status = status;
    }

    public int getCompletionPercentage() {
        return completionPercentage;
    }
    public void setCompletionPercentage(int completionPercentage) {
        this.completionPercentage = completionPercentage;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }
    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getCompletionTime() {
        return completionTime;
    }
    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public String getLastProcessTime() {
        return lastProcessTime;
    }
    public void setLastProcessTime(String lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    
    public String getLog() {
        return getLogger().getLog();
    }
    public void setLog(String log) {
        getLogger().setLog(log);
        this.log = log;
    }

    public int save() {
        log = getLogger().getLog();
        
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.CSV_DATASET)).build();

            int status = client.addBean(this).getStatus();
            client.commit();
            client.close();
            return status;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] DataFile.save() - e.Message: " + e.getMessage());
            return -1;
        }
    }

    public int delete() {
    	
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.CSV_DATASET)).build();
            
            UpdateResponse response = solr.deleteById(getId());
            solr.commit();
            solr.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] DataFile.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] DataFile.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] DataFile.delete() - Exception message: " + e.getMessage());
        }

        return -1;
    }
    
    public String getPureFileName() {
        return Paths.get(fileName).getFileName().toString();
    }
    
    public void resetForUnprocessed() {
        setStatus(DataFile.UNPROCESSED);
        getLogger().resetLog();
        setDir("");
        setFileName(getPureFileName());
        setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        setCompletionTime("");
    }

    public boolean existsInFileSystem(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            return false;
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && getPureFileName().equals(listOfFiles[i].getName())) {
                return true;
            }
        }

        return false;
    }

    public static DataFile create(String fileName, String dir, String ownerEmail, String status) {
        DataFile dataFile = new DataFile(fileName);
        dataFile.setDir(dir);
        dataFile.setOwnerEmail(ownerEmail);
        dataFile.setStatus(status);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        if (fileName.startsWith("DA-")) {
            String dataAcquisitionUri = ObjectAccessSpec.getProperDataAcquisitionUri(fileName);
            dataFile.setDataAcquisitionUri(dataAcquisitionUri == null ? "" : dataAcquisitionUri);
        }

        dataFile.save();
        
        return dataFile;
    }

    public static DataFile convertFromSolr(SolrDocument doc) {
        DataFile object = new DataFile(SolrUtils.getFieldValue(doc, "file_name_str").toString());

        object.setId(SolrUtils.getFieldValue(doc, "id").toString());
        object.setSharedId(SolrUtils.getFieldValue(doc, "shared_id_str").toString());
        object.setDir(SolrUtils.getFieldValue(doc, "dir_str").toString());
        object.setOwnerEmail(SolrUtils.getFieldValue(doc, "owner_email_str").toString());
        object.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str").toString());
        object.setDataAcquisitionUri(URIUtils.replaceNameSpaceEx(SolrUtils.getFieldValue(doc, "acquisition_uri_str").toString()));
        object.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str").toString());
        object.setStatus(SolrUtils.getFieldValue(doc, "status_str").toString());
        object.setCompletionPercentage(Integer.parseInt(SolrUtils.getFieldValue(doc, "completion_percentage_int").toString()));
        object.setSubmissionTime(SolrUtils.getFieldValue(doc, "submission_time_str").toString());
        object.setCompletionTime(SolrUtils.getFieldValue(doc, "completion_time_str").toString());
        object.setLastProcessTime(SolrUtils.getFieldValue(doc, "last_processed_time_str").toString());
        object.setLogger(new AnnotationLogger(object, SolrUtils.getFieldValue(doc, "log_str").toString()));

        return object;
    }

    public static List<DataFile> findByQuery(SolrQuery query) {
        List<DataFile> list = new ArrayList<DataFile>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.CSV_DATASET)).build();

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            Iterator<SolrDocument> i = results.iterator();
            while (i.hasNext()) {
                list.add(convertFromSolr(i.next()));
            }

            list.sort(new Comparator<DataFile>() {
                @Override
                public int compare(DataFile o1, DataFile o2) {
                    return o1.getSubmissionTime().compareTo(o2.getSubmissionTime());
                }
            });
        } catch (Exception e) {
            list.clear();
            System.out.println("[ERROR] DataFile.findByQuery(SolrQuery) - Exception message: " + e.getMessage());
        }

        return list;
    }

    public static List<DataFile> find(String ownerEmail) {
        SolrQuery query = new SolrQuery();
        query.set("q", "owner_email_str:\"" + ownerEmail + "\"");
        query.set("rows", "10000000");
        return findByQuery(query);
    }

    public static List<DataFile> find(String ownerEmail, String status) {
        if (status == UNPROCESSED || status == PROCESSED || status == CREATING || status == CREATED || status == WORKING) {
            SolrQuery query = new SolrQuery();
            query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"");
            query.set("rows", "10000000");
            return findByQuery(query);
        }
        else {
            return new ArrayList<DataFile>();
        }
    }

    public static String fileNameFromPath(String path) {
    	String[] tokens = path.split("/");
    	return tokens[tokens.length - 1];
    }
    
    public static List<DataFile> findByStatus(String status) {
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        return findByQuery(query);
    }
    
    public static List<DataFile> findByMultiStatus(List<String> status) {
        SolrQuery query = new SolrQuery();
        query.set("q", String.join(" OR ", status.stream()
                .map(s -> "status_str:\"" + s + "\"")
                .collect(Collectors.toList())));
        query.set("rows", "10000000");
        return findByQuery(query);
    }
    
    public static DataFile findByIdAndEmail(String id, String ownerEmail) {        
        SolrQuery query = new SolrQuery();
        if (null == ownerEmail) {
            query.set("q", "id:\"" + id + "\"");
        } else {
            query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "id:\"" + id + "\"");
        }
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }
    
    public static DataFile findByIdAndStatus(String id, String status) {
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"" + " AND " + "id:\"" + id + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }
    
    public static DataFile findByNameAndStatus(String fileName, String status) {
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"" + " AND " + "file_name_str:\"" + fileName + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }
    
    public static DataFile findByIdAndOwnerEmailAndStatus(String id, String ownerEmail, String status) {        
        SolrQuery query = new SolrQuery();
        query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"" + " AND " + "id:\"" + id + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }
    
    public static DataFile findBySharedId(String id) {
        SolrQuery query = new SolrQuery();
        query.set("q", "shared_id_str:\"" + id + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    public static DataFile findById(String id) {
        return findByIdAndEmail(id, null);
    }

    public static boolean hasValidExtension(String fileName) {
        List<String> validExtensions = Arrays.asList(".csv", ".xlsx");
        for (String ext : validExtensions) {
            fileName.endsWith(ext);
            return true;
        }

        return false;
    }

    public static boolean search(String fileName, String dir, List<DataFile> pool) {
        for (DataFile file : pool) {
            if (file.getFileName().equals(fileName) && file.getDir().equals(dir)) {
                return true;
            }
        }
        return false;
    }

    public static void includeUnrecognizedFiles(String curPath, String basePath, 
            List<DataFile> dataFiles, String ownerEmail, String defaultStatus) {
        File folder = new File(curPath);
        if (!folder.exists()) {
            return;
        }
        
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() 
                    && hasValidExtension(listOfFiles[i].getName())
                    && !listOfFiles[i].getName().startsWith(".") 
                    && !search(listOfFiles[i].getName(), basePath, dataFiles)) {
                DataFile df = DataFile.create(listOfFiles[i].getName(), basePath, ownerEmail, defaultStatus);
                dataFiles.add(df);
            }
        }
    }

    public static void filterNonexistedFiles(String path, List<DataFile> files) {
        Iterator<DataFile> iterFile = files.iterator();
        while (iterFile.hasNext()) {
            DataFile file = iterFile.next();
            try {
                Path p = Paths.get(path, file.getDir(), file.getFileName());
                if (!Files.exists(p) || Files.isHidden(p)) {
                    iterFile.remove();
                }
            } catch (IOException e) {
                iterFile.remove();
            }
        }
    }

    public static List<File> findFilesByExtension(String path, String ext) {
        List<File> results = new ArrayList<File>();

        File folder = new File(path);
        if (!folder.exists()) {
            return results;
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() 
                    && FilenameUtils.getExtension(listOfFiles[i].getName()).equals(ext)) {
                results.add(listOfFiles[i]);
            }
        }
        
        return results;
    }

    public static List<DataFile> findInDir(String dir, String ownerEmail, String status) {
        if (dir.startsWith("/")) {
            dir = dir.substring(1, dir.length());
        }
        
        SolrQuery query = new SolrQuery();
        query.set("q", "dir_str:\"" + dir + "\"" + " AND " + "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        
        return findByQuery(query);
    }

    public static List<DataFile> findInDir(String dir, String status) {
        if (dir.startsWith("/")) {
            dir = dir.substring(1, dir.length());
        }
        
        SolrQuery query = new SolrQuery();
        query.set("q", "dir_str:\"" + dir + "\"" + " AND " + "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        
        return findByQuery(query);
    }

    public static boolean isEmptyDir(File dir) {
        if (!dir.exists()) {
            return true;
        }
        
        return dir.listFiles().length == 0;
    }
    
    public static List<String> findFolders(String dir, boolean ignoreEmptyFolders) {
        List<String> results = new ArrayList<String>();

        File folder = new File(dir);
        if (!folder.exists()) {
            return results;
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isDirectory() && !listOfFiles[i].getName().equals(Sandbox.SUFFIX)) {
                if (ignoreEmptyFolders) {
                    if (!isEmptyDir(listOfFiles[i])) {
                        results.add(listOfFiles[i].getName() + "/");
                    }
                } else {
                    results.add(listOfFiles[i].getName() + "/");
                }
            }
        }
        
        return results;
    }

    @JsonIgnore
    public static TreeNode getHierarchy(String current, String path, boolean justDir) {
    	if (current == null) {
    		return null;
    	}
    	DataFile dirFile = null;
    	if (path.equals("")) {
    		path = current;
    	} else if (path.equals("/")) {
    		path = path + current;
    	} else {
    		path = path + "/" + current;
    	}
		dirFile = new DataFile(path);
		dirFile.setStatus(DataFile.WORKING);
        File fileAux = new File(dirFile.getAbsolutePath());
        TreeNode node = null;
        if (fileAux.isDirectory() && !justDir) {
        	node = new TreeNode("+" + path);
        } else {
        	node = new TreeNode(path);
        }
        if (fileAux.isDirectory() && fileAux.listFiles() != null) {
        	File[] children = fileAux.listFiles();
        	for (File child : children) {
        		DataFile childDataFile = new DataFile(child.getName());
        		childDataFile.setStatus(DataFile.WORKING);
        		if (justDir) {
        			if (child.isDirectory()) {
        				node.addChild(DataFile.getHierarchy(childDataFile.getFileName(), path, justDir));
        			}
        		} else {
        			node.addChild(DataFile.getHierarchy(childDataFile.getFileName(), path, justDir));
        		}
        	}
        }
        return node;
    }
    
    public static String getFolderLabel(String folderPath) {
    	if (folderPath == null) {
    		return "";
    	}
    	if (folderPath.startsWith("+")) {
    		folderPath = folderPath.substring(1);
    	}
    	if (folderPath.equals("/")) {
    		return folderPath;
    	}
    	String[] pieces = folderPath.split("/");
    	return pieces[pieces.length -1];
    }

}
