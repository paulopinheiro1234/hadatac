package org.hadatac.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.typesafe.config.ConfigFactory;
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
import org.hadatac.console.controllers.sandbox.Sandbox;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.TreeNode;
import org.hadatac.data.loader.CSVRecordFile;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.data.loader.SpreadsheetRecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.vocabularies.HASCO;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@JsonFilter("dataFileFilter")
public class DataFile extends HADatAcThing implements Cloneable {

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

    // constant used for dataset generation
    public static final String DS_GENERATION = "download";

    @Field("id")
    private String id;
    @Field("viewable_id_str")
    private String viewableId = "";
    @Field("editable_id_str")
    private String editableId = "";
    @Field("file_name_str")
    private String fileName = "";
    @Field("dir_str")
    private String dir = "";
    @Field("owner_email_str")
    private String ownerEmail = "";
    @Field("viewer_email_str_multi")
    private List<String> viewerEmails;
    @Field("editor_email_str_multi")
    private List<String> editorEmails;
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
    @Field("was_derived_from_str_multi")
    private List<String> wasDerivedFrom;
    @Field("log_str")
    private String log = "";

    private AnnotationLogger logger = null;
    private RecordFile recordFile = null;
    private File file = null;

    // Permissible actions depending on user
    private boolean allowViewing = false;
    private boolean allowEditing = false;
    private boolean allowRenaming = false;
    private boolean allowMoving = false;
    private boolean allowDeleting = false;
    private boolean allowSharing = false;
    private boolean allowDownloading = false;
    private boolean allowIngesting = false;
    private boolean allowVerifying = false;

    public DataFile(String fileName) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
        logger = new AnnotationLogger(this);
    }

    public boolean attachFile(File file) {
        RecordFile recordFile = null;
        if (file.getName().endsWith(".csv")) {
            recordFile = new CSVRecordFile(file);
        } else if (file.getName().endsWith(".xlsx")) {
            recordFile = new SpreadsheetRecordFile(file);
        } else {
            getLogger().addLine(Feedback.println(Feedback.WEB, String.format(
                    "[ERROR] Unknown file format: %s", file.getName())));
            return false;
        }

        setRecordFile(recordFile);
        return true;
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

    @JsonIgnore
    public boolean getAllowViewing() {
        return allowViewing;
    }
    public void setAllowViewing(boolean allowViewing) {
        this.allowViewing = allowViewing;
    }

    @JsonIgnore
    public boolean getAllowEditing() {
        return allowEditing;
    }
    public void setAllowEditing(boolean allowEditing) {
        this.allowEditing = allowEditing;
    }

    @JsonIgnore
    public boolean getAllowRenaming() {
        return allowRenaming;
    }
    public void setAllowRenaming(boolean allowRenaming) {
        this.allowRenaming = allowRenaming;
    }

    @JsonIgnore
    public boolean getAllowMoving() {
        return allowMoving;
    }
    public void setAllowMoving(boolean allowMoving) {
        this.allowMoving = allowMoving;
    }

    @JsonIgnore
    public boolean getAllowDeleting() {
        return allowDeleting;
    }
    public void setAllowDeleting(boolean allowDeleting) {
        this.allowDeleting = allowDeleting;
    }

    @JsonIgnore
    public boolean getAllowSharing() {
        return allowSharing;
    }
    public void setAllowSharing(boolean allowSharing) {
        this.allowSharing = allowSharing;
    }

    @JsonIgnore
    public boolean getAllowDownloading() {
        return allowDownloading;
    }
    public void setAllowDownloading(boolean allowDownloading) {
        this.allowDownloading = allowDownloading;
    }

    @JsonIgnore
    public boolean getAllowIngesting() {
        return allowIngesting;
    }
    public void setAllowIngesting(boolean allowIngesting) {
        this.allowIngesting = allowIngesting;
    }

    @JsonIgnore
    public boolean getAllowVerifying() {
        return allowVerifying;
    }
    public void setAllowVerifying(boolean allowVerifying) {
        this.allowVerifying = allowVerifying;
    }

    public static void updatePermission(List<DataFile> dataFiles, String userEmail) {
        for (DataFile dataFile : dataFiles) {
            dataFile.updatePermissionByUserEmail(userEmail);
        }
    }

    public void updatePermissionByUserEmail(String userEmail) {
        if (getOwnerEmail().equals(userEmail)) {
            setAllowViewing(true);
            setAllowEditing(true);
            setAllowRenaming(true);
            setAllowMoving(true);
            setAllowDeleting(true);
            setAllowSharing(true);
            setAllowDownloading(true);
            setAllowIngesting(true);
            setAllowVerifying(true);
        } else if (getEditorEmails().contains(userEmail)) {
            setAllowViewing(true);
            setAllowEditing(true);
            setAllowDownloading(true);
        } else if (getViewerEmails().contains(userEmail)) {
            setAllowViewing(true);
            setAllowDownloading(true);
        }
    }

    @JsonIgnore
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    @JsonIgnore
    public String getViewableId() {
        return viewableId;
    }
    public void setViewableId(String viewableId) {
        this.viewableId = viewableId;
    }

    @JsonIgnore
    public String getEditableId() {
        return editableId;
    }
    public void setEditableId(String editableId) {
        this.editableId = editableId;
    }

    @JsonIgnore
    public String getAbsolutePath() {
        if (Arrays.asList(UNPROCESSED, FREEZED).contains(getStatus())) {
            return Paths.get(ConfigProp.getPathUnproc(), getDir(), getStorageFileName()).toString();
        } else if (getStatus().equals(PROCESSED)) {
            return Paths.get(ConfigProp.getPathProc(), getDir(), getStorageFileName()).toString();
        } else if (getStatus().equals(WORKING)) {
            return Paths.get(ConfigProp.getPathWorking(), getDir(), getStorageFileName()).toString();
        } else if (Arrays.asList(CREATED, CREATING, DELETED).contains(getStatus())) {
            return Paths.get(getDir(), getStorageFileName()).toString();
        } else if (Arrays.asList(DD_UNPROCESSED, DD_PROCESSED, DD_FREEZED).contains(getStatus())) {
            return Paths.get(ConfigProp.getPathDataDictionary(), getDir(), getStorageFileName()).toString();
        }

        return "";
    }

    @JsonIgnore
    public static String getMediaUrl(String filename) {
    	if (filename == null || !filename.startsWith("file:///media/")) {
    		return "";
    	}
    	return ConfigFactory.load().getString("hadatac.console.host") +
    			org.hadatac.console.controllers.routes.Portal.index().url() +
    			filename.replace("file:///", "");
    }

    @JsonIgnore
    public AnnotationLogger getLogger() {
        return logger;
    }
    public void setLogger(AnnotationLogger logger) {
        this.logger = logger;
    }

    @JsonIgnore
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

    @JsonIgnore
    public String getBaseName() {
        return FilenameUtils.getBaseName(fileName);
    }

    @JsonIgnore
    public String getFileExtention() {
        return FilenameUtils.getExtension(fileName);
    }

    @JsonIgnore
    public String getOwnerEmail() {
        return ownerEmail;
    }
    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public User getDataOwner() {
        return User.findByEmail(this.ownerEmail);
    }

    @JsonIgnore
    public List<String> getViewerEmails() {
        return viewerEmails;
    }
    public void setViewerEmails(List<String> viewerEmails) {
        this.viewerEmails = viewerEmails;
    }
    public void addViewerEmail(String viewerEmail) {
        if (!viewerEmails.contains(viewerEmail)) {
            viewerEmails.add(viewerEmail);
        }
    }
    public void removeViewerEmail(String viewerEmail) {
        if (viewerEmails.contains(viewerEmail)) {
            viewerEmails.remove(viewerEmail);
        }
    }

    @JsonIgnore
    public List<String> getEditorEmails() {
        return editorEmails;
    }
    public void setEditorEmails(List<String> editorEmails) {
        this.editorEmails = editorEmails;
    }
    public void addEditorEmail(String editorEmail) {
        if (!editorEmails.contains(editorEmail)) {
            editorEmails.add(editorEmail);
        }
    }
    public void removeEditorEmail(String editorEmail) {
        if (editorEmails.contains(editorEmail)) {
            editorEmails.remove(editorEmail);
        }
    }

    @JsonIgnore
    public String getStudyUri() {
        return studyUri;
    }

    public Study getStudy() {
        return Study.find(studyUri);
    }

    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    @JsonIgnore
    public String getDataAcquisitionUri() {
        return dataAcquisitionUri;
    }

    public void setDataAcquisitionUri(String dataAcquisitionUri) {
        this.dataAcquisitionUri = dataAcquisitionUri;
    }

    public STR getDataStream() {
        return STR.findByUri(URIUtils.replacePrefixEx(dataAcquisitionUri));
    }

    @Override
    public String getTypeUri() {
        return HASCO.DATA_FILE;
    }

    @Override
    public String getHascoTypeUri() {
        return HASCO.DATA_FILE;
    }

    @Override
    public void setUri(String uri) {
        setDatasetUri(uri);
    }

    @Override
    public String getUri() {
        return this.datasetUri;
    }

    @JsonIgnore
    public String getDatasetUri() {
        return datasetUri;
    }

    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

    @Override
    public void setLabel(String label) {
        setFileName(label);
    }

    @Override
    public String getLabel() {
        return this.fileName;
    }

    public List<Measurement> getValues() {
        return Measurement.findByConceptAndUri(HASCO.DATA_FILE, this.datasetUri);
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
        if ( dir.startsWith("/") && !dir.contains(ConfigProp.getPathWorking()) ) {
            dir = dir.substring(1, dir.length());
        }
        this.dir = dir;
    }

    @JsonIgnore
    public String getPureFileName() {
        return Paths.get(fileName).getFileName().toString();
    }

    @JsonIgnore
    public String getStorageFileName() {
        if (FilenameUtils.getExtension(fileName).isEmpty()) {
            return fileName;
        }

        return FilenameUtils.getBaseName(fileName) + "_" + id
                + "." + FilenameUtils.getExtension(fileName);
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

    public List<String> getWasDerivedFrom() {
        return wasDerivedFrom;
    }
    public void setWasDerivedFrom(List<String> wasDerivedFromList) {
        this.wasDerivedFrom = wasDerivedFromList;
    }
    public void addWasDerivedFrom(String wasDerivedFromInd) {
        if (!wasDerivedFrom.contains(wasDerivedFromInd)) {
            wasDerivedFrom.add(wasDerivedFromInd);
        }
    }
    public void removeWasDerivedFrom(String wasDerivedFromInd) {
        if (wasDerivedFrom.contains(wasDerivedFromInd)) {
            wasDerivedFrom.remove(wasDerivedFromInd);
        }
    }

    public String getLog() {
        return getLogger().getLog();
    }
    public void setLog(String log) {
        getLogger().setLog(log);
        this.log = log;
    }

    @Override
    public void save() {
        saveToSolr();
    }

    @Override
    public boolean saveToSolr() {
        log = getLogger().getLog();

        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.CSV_DATASET)).build();

            int status = client.addBean(this).getStatus();
            client.commit();
            client.close();
            return true;
            //return status;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] DataFile.save() - e.Message: " + e.getMessage());
            return false;
            //return -1;
        }
    }

    @Override
    public void delete() {
        deleteFromSolr();
    }

    @Override
    public int deleteFromSolr() {
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

    public void freeze() {
        setStatus(FREEZED);
        save();
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
            if (listOfFiles[i].isFile() && getStorageFileName().equals(listOfFiles[i].getName())) {
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
            String dataAcquisitionUri = STR.getProperDataAcquisitionUri(fileName);
            dataFile.setDataAcquisitionUri(dataAcquisitionUri == null ? "" : dataAcquisitionUri);
        }

        dataFile.save();

        return dataFile;
    }

    public static DataFile convertFromSolr(SolrDocument doc) {
        DataFile object = new DataFile(SolrUtils.getFieldValue(doc, "file_name_str").toString());
        object.setUri(SolrUtils.getFieldValue(doc, "dataset_uri_str").toString());
        object.setLabel(SolrUtils.getFieldValue(doc, "file_name_str").toString());
        object.setTypeUri(HASCO.DATA_FILE);
        object.setHascoTypeUri(HASCO.DATA_FILE);

        object.setId(SolrUtils.getFieldValue(doc, "id").toString());
        object.setViewableId(SolrUtils.getFieldValue(doc, "viewable_id_str").toString());
        object.setEditableId(SolrUtils.getFieldValue(doc, "editable_id_str").toString());
        object.setDir(SolrUtils.getFieldValue(doc, "dir_str").toString());
        object.setOwnerEmail(SolrUtils.getFieldValue(doc, "owner_email_str").toString());
        object.setViewerEmails(SolrUtils.getFieldValues(doc, "viewer_email_str_multi"));
        object.setEditorEmails(SolrUtils.getFieldValues(doc, "editor_email_str_multi"));
        object.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str").toString());
        object.setDataAcquisitionUri(URIUtils.replaceNameSpaceEx(SolrUtils.getFieldValue(doc, "acquisition_uri_str").toString()));
        object.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str").toString());
        object.setStatus(SolrUtils.getFieldValue(doc, "status_str").toString());
        object.setCompletionPercentage(Integer.parseInt(SolrUtils.getFieldValue(doc, "completion_percentage_int").toString()));
        object.setSubmissionTime(SolrUtils.getFieldValue(doc, "submission_time_str").toString());
        object.setCompletionTime(SolrUtils.getFieldValue(doc, "completion_time_str").toString());
        object.setLastProcessTime(SolrUtils.getFieldValue(doc, "last_processed_time_str").toString());
        object.setWasDerivedFrom(SolrUtils.getFieldValues(doc, "was_derived_from_str_multi"));
        object.setLogger(new AnnotationLogger(object, SolrUtils.getFieldValue(doc, "log_str").toString()));

        return object;
    }

    public static List<DataFile> findByQuery(SolrQuery query) {
        List<DataFile> list = new ArrayList<DataFile>();
        Date date = new Date();

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

    public static int totalByQuery(SolrQuery query) {
    	int total = 0;

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.CSV_DATASET)).build();

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            total = response.getResults().size();

        } catch (Exception e) {
            System.out.println("[ERROR] DataFile.totalByQuery(SolrQuery) - Exception message: " + e.getMessage());
        }

        return total;
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

    public static int totalByStatus(String status) {
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        return totalByQuery(query);
    }

    public static List<DataFile> findByMultiStatus(List<String> status) {
        SolrQuery query = new SolrQuery();
        query.set("q", String.join(" OR ", status.stream()
                .map(s -> "status_str:\"" + s + "\"")
                .collect(Collectors.toList())));
        query.set("rows", "10000000");
        return findByQuery(query);
    }

    public static List<DataFile> findByDataAcquisition(String dataAcquisitionUri) {
    	/*
    	 * this is a hack. acquisition_uri_str is supposed to be the full uri
    	 */
    	String ns_dataAcquisition = URIUtils.replaceNameSpaceEx(dataAcquisitionUri);
        SolrQuery query = new SolrQuery();
        query.set("q", "acquisition_uri_str:\"" + ns_dataAcquisition + "\"");
        query.set("rows", "10000000");
        return findByQuery(query);
    }

    public static int totalByMultiStatus(List<String> status) {
        SolrQuery query = new SolrQuery();
        query.set("q", String.join(" OR ", status.stream()
                .map(s -> "status_str:\"" + s + "\"")
                .collect(Collectors.toList())));
        query.set("rows", "10000000");
        return totalByQuery(query);
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

    public static DataFile findByUri(String dataset_uri) {
        SolrQuery query = new SolrQuery();
        query.set("q", "dataset_uri_str:\"" + dataset_uri + "\"");
        query.set("rows", "10");

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

    public static DataFile findByViewableId(String id) {
        SolrQuery query = new SolrQuery();
        query.set("q", "viewable_id_str:\"" + id + "\"");
        query.set("rows", "10000000");

        List<DataFile> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    public static DataFile findByEditableId(String id) {
        SolrQuery query = new SolrQuery();
        query.set("q", "editable_id_str:\"" + id + "\"");
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
            if (file.getStorageFileName().equals(fileName) && file.getDir().equals(dir)) {
                return true;
            }
        }
        return false;
    }

    public static DataFile find(String fileName, String dir, List<DataFile> pool) {
        for (DataFile dataFile : pool) {
            if (dataFile.getPureFileName().equals(fileName)
                    && dataFile.getDir().equals(dir)) {
                return dataFile;
            }
        }

        return null;
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
                DataFile dataFile = find(listOfFiles[i].getName(), basePath, dataFiles);
                if (null == dataFile) {
                    dataFile = DataFile.create(listOfFiles[i].getName(), basePath, ownerEmail, defaultStatus);
                }

                String originalPath = Paths.get(curPath, dataFile.getPureFileName()).toString();
                File file = new File(originalPath);
                String newPath = originalPath.replace(
                        "/" + dataFile.getPureFileName(),
                        "/" + dataFile.getStorageFileName());
                file.renameTo(new File(newPath));
                file.delete();

                dataFiles.add(dataFile);
            }
        }
    }

    public static void filterNonexistedFiles(String path, List<DataFile> files) {
        Iterator<DataFile> iterFile = files.iterator();
        while (iterFile.hasNext()) {
            DataFile file = iterFile.next();
            try {
                Path p = Paths.get(path, file.getDir(), file.getStorageFileName());
                if (!Files.exists(p) ) {
                    iterFile.remove();
                }
                if (Files.isHidden(p)) {
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

    public static List<DataFile> findInDir(String dir, String userEmail, String status) {
        if (dir.startsWith("/")) {
            dir = dir.substring(1, dir.length());
        }

        SolrQuery query = new SolrQuery();
        query.set("q", String.format("dir_str:\"%s\" "
                + "AND ( owner_email_str:\"%s\" OR viewer_email_str_multi:\"%s\" OR editor_email_str_multi:\"%s\" ) "
                + "AND status_str:\"%s\"", dir, userEmail, userEmail, userEmail, status));
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

    public static List<DataFile> findDownloadedFilesInDir(String dir, String userEmail, String status) {

        if ( dir.startsWith("/") && !dir.contains(ConfigProp.getPathWorking()) ) {
            dir = dir.substring(1, dir.length());
        }

        SolrQuery query = new SolrQuery();
        query.set("q", String.format("dir_str:\"%s\" "
                + "AND ( owner_email_str:\"%s\" OR viewer_email_str_multi:\"%s\" OR editor_email_str_multi:\"%s\" ) "
                + "AND (status_str:\"%s\" OR status_str:\"%s\")", dir, userEmail, userEmail, userEmail, status, "CREATED"));
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    public static List<DataFile> findDownloadedFilesInDir(String dir, String status) {

        if ( dir.startsWith("/") && !dir.contains(ConfigProp.getPathWorking()) ) {
            dir = dir.substring(1, dir.length());
        }

        SolrQuery query = new SolrQuery();
        query.set("q", "dir_str:\"" + dir + "\"" + " AND " + "(status_str:\"" + status + "\" OR status_str:\"CREATED\")");
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

        if ( !Files.isDirectory(Paths.get(dir))) {
            // if the path does not exist
            return results;
        }

        // now we know the directory exists, we can do the following
        File[] listOfFiles = (new File(dir)).listFiles();
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

      if (path.equals("")) {
         path = current;
      } else if (path.equals("/")) {
         path = path + current;
      } else {
         path = path + "/" + current;
      }

      String absPath = Paths.get(ConfigProp.getPathWorking(), path).toString();
      File fileAux = new File(absPath);

      // Generate the new node
      TreeNode node = null;
      if (fileAux.isDirectory() && !justDir) { // Directory with modified string
         node = new TreeNode("+" + path);
      }
      else { // Directory or File node created
         node = new TreeNode(path);
      }

      // Add directory children
      if (fileAux.isDirectory() && fileAux.listFiles() != null) {
         File[] children = fileAux.listFiles();
         for (File child : children) {
            DataFile childDataFile = new DataFile(child.getName());
            childDataFile.setStatus(DataFile.WORKING);
            node.addChild(DataFile.getHierarchy(childDataFile.getFileName(), path, justDir));
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
