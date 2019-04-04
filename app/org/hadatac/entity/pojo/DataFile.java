package org.hadatac.entity.pojo;

import java.io.File;
import java.io.FilenameFilter;
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
import java.util.stream.Collectors;

import org.apache.commons.io.FilenameUtils;
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
import org.hadatac.console.http.SolrUtils;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;


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

    @Field("file_name")
    private String fileName = "";
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

    public String getStatus() {
        return status;
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
        fileName = fileName.replace("/", "[SLASH]");
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
            UpdateResponse response = solr.deleteById(this.getFileName().replace("/", "[SLASH]"));
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

    public boolean existsInFileSystem(String path) {
        File folder = new File(path);
        if (!folder.exists()) {
            return false;
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && getFileName().equals(listOfFiles[i].getName())) {
                return true;
            }
        }

        return false;
    }

    public static DataFile create(String fileName, String ownerEmail, String status) {
        DataFile dataFile = new DataFile(fileName);
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
        DataFile object = new DataFile(doc.getFieldValue("file_name").toString().replace("[SLASH]", "/"));

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

    public static DataFile findByNameAndEmail(String ownerEmail, String fileName) {
        fileName = fileName.replace("/", "[SLASH]");
        
        SolrQuery query = new SolrQuery();
        if (null == ownerEmail) {
            query.set("q", "file_name:\"" + fileName + "\"");
        }
        else {
            query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "file_name:\"" + fileName + "\"");
        }
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }
    
    public static DataFile findByNameAndStatus(String status, String fileName) {
        fileName = fileName.replace("/", "[SLASH]");
        
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"" + " AND " + "file_name:\"" + fileName + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    public static DataFile findByName(String fileName) {
        return findByNameAndEmail(null, fileName);
    }

    public static boolean hasValidExtension(String fileName) {
        List<String> validExtensions = Arrays.asList(".csv", ".xlsx");
        for (String ext : validExtensions) {
            fileName.endsWith(ext);
            return true;
        }

        return false;
    }

    public static boolean search(String fileName, List<DataFile> pool) {
        for (DataFile file : pool) {
            if (file.getFileName().equals(fileName)) {
                return true;
            }
        }
        return false;
    }

    public static void includeUnrecognizedFiles(String path, String dir, List<DataFile> ownedFiles) {
    	if (path == null) {
    		System.out.println("DataFile: [ERROR] parameter path=null when calling includeUnrecognizedFiles()");
    		return;
    	}
    	if (dir == null) {
    		System.out.println("DataFile: [ERROR] parameter dir=null when calling includeUnrecognizedFiles()");
    		return;
    	}
    	//System.out.println("Path inside includeUnrecognized: " + path);
    	if (path.endsWith("/") && dir.startsWith("/")) {
    		path = path.substring(0, path.length() - 1);
    	}
    	String fullPath = path + dir;
    	//System.out.println("FullPath inside includeUnrecognized: " + fullPath);
    	File folder = new File(fullPath);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && hasValidExtension(listOfFiles[i].getName())) {
                if (!search(listOfFiles[i].getName(), ownedFiles)) {
                    DataFile newFile = new DataFile(listOfFiles[i].getName());
                    newFile.save();
                    ownedFiles.add(newFile);
                }
            }
        }
    }

    public static void includeUnrecognizedFiles(String path, List<DataFile> dataFiles, 
            String ownerEmail, String defaultStatus) {      
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && hasValidExtension(listOfFiles[i].getName())) {
                if (!search(listOfFiles[i].getName(), dataFiles)) {
                    DataFile.create(listOfFiles[i].getName(), ownerEmail, defaultStatus);
                }
            }
        }
    }

    public static void filterNonexistedFiles(String path, List<DataFile> files) {
        Iterator<DataFile> iterFile = files.iterator();
        while (iterFile.hasNext()) {
            DataFile file = iterFile.next();
            try {
                Path p = Paths.get(path + "/" + file.getFileName());
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
            folder.mkdirs();
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
    	String currentDir = dir.replaceAll("/", "");
        if (status == UNPROCESSED || status == PROCESSED || status == CREATING || status == CREATED || status == WORKING) {
            SolrQuery query = new SolrQuery();
            query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"");
            query.set("rows", "10000000");
            List<DataFile> all = findByQuery(query);
            List<DataFile> selected = new ArrayList<DataFile>();
        	for (DataFile df : all) {
        		if (currentDir.equals("")) {
        			if (df.getFileName().indexOf('/') <= -1) {
            			selected.add(df);
            		} 
        		} else {
            		if (df.getFileName().indexOf(currentDir) >= 0) {
        				selected.add(df);
            		}
            	}        	
            }
        	return selected;
        }
        else {
            return new ArrayList<DataFile>();
        }
    }


    public static List<DataFile> findInDir(String dir, String status) {
		System.out.println("DataFile: =====> dir: [" + dir + "]   status: [" + status + "]");
    	String currentDir = dir;
    	currentDir = currentDir.replaceAll("/", "");
    	if (status != WORKING && dir != null && !dir.equals("/") && dir.startsWith("/")) {
    		dir = dir.substring(1, dir.length());
    	}
		System.out.println("DataFile: dir: [" + dir + "]   currentDir: [" + currentDir + "]");
    	SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        List<DataFile> all = findByQuery(query);
        List<DataFile> selected = new ArrayList<DataFile>();
    	for (DataFile df : all) {
    		if (currentDir.equals("")) {
    			if (df.getFileName().indexOf('/') <= -1) {
        			selected.add(df);
        		} 
    		} else {
    			System.out.println("DataFile: filename: [" + df.getFileName() + "]");
    			// starts with the current dir
                if (df.getFileName().indexOf(dir) >= 0) {
                	// check if there is no folder in the reminder of the filename after subtracting current dir
                	String subtractedStr = df.getFileName().replace(dir,"");
        			System.out.println("DataFile: dir: [" + dir + "]   filename: [" + subtractedStr + "]");
                	if (subtractedStr.indexOf("/") <= -1) {
                		selected.add(df);
            			System.out.println("DataFile: added");
                	}
                }
            }   
    	}
    	
    	return selected;
    }

    public static List<String> findAllFolders(String dir) {
    	return findAllFolders(dir, PROCESSED);
    }

    public static List<String> findAllFolders(String dir, String status) {
        List<String> results = new ArrayList<String>();
    	if (!dir.equals("/") && !dir.equals("")) {
    		results.add("..");
        	if (!status.equals(WORKING)) {
        		return results;
        	}
    	}
    	
    	if (status.equals(WORKING)) {
    		String fullDir = ConfigProp.getPathWorking().substring(0, ConfigProp.getPathWorking().length() - 1) + dir;
    		File file = new File(fullDir);
    		//System.out.println("findAllFolders: [" + fullDir + "]");
    		String[] directories = file.list(new FilenameFilter() {
    			@Override
    			public boolean accept(File current, String name) {
    				return new File(current, name).isDirectory();
    			}
    		});
    		if (directories != null && directories.length > 0) {
    			for (int i=0; i < directories.length; i++) {
    				results.add(directories[i] + "/");
    			}
    		}
    		//System.out.println("findAllFolders: to string: " + results);
    	
    	} else {
    	
    		//System.out.println("findAllFolders: dir : [" + dir + "]");
    		List<DataFile> datafiles = findByStatus(status);
    		for (DataFile df : datafiles) {
    			String fName = "";
    			int pos = df.getFileName().indexOf('/');
    			if (pos >=0) {
    				fName = df.getFileName().substring(0, df.getFileName().indexOf('/'));
    			}
    			System.out.println("[" + fName + "] " + df.getFileName());
    			if (!fName.equals("")) {
    				String newFolder = fName + "/";
    				if (!results.contains(newFolder)) {
    					results.add(newFolder);
    				}
    			}
    		}
    	}
    	
        return results;
    }

    public static List<String> findFolders(String dir, String ownerEmail) {
        List<String> results = new ArrayList<String>();

        return results;
    }

}
