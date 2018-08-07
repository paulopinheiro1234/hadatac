package org.hadatac.entity.pojo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

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
import org.hadatac.console.http.SolrUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;

import com.avaje.ebeaninternal.server.lib.util.Str;
import com.typesafe.config.ConfigFactory;

public class DataFile {

    // Process status for auto-annotator
    public static final String UNPROCESSED = "UNPROCESSED";
    public static final String PROCESSED = "PROCESSED";
    public static final String FREEZED = "FREEZED";

    // Process status for downloader
    public static final String CREATING = "CREATING";
    public static final String CREATED 	= "CREATED";
    public static final String DELETED  = "DELETED";

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

    public DataFile(String fileName) {
        this.fileName = fileName;
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

    public int save() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.CSV_DATASET).build();

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
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + CollectionUtil.CSV_DATASET).build();
            UpdateResponse response = solr.deleteById(this.getFileName());
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

    public static void create(String fileName, String ownerEmail) {
        DataFile dataFile = new DataFile(fileName);
        dataFile.setOwnerEmail(ownerEmail);
        dataFile.setStatus(DataFile.UNPROCESSED);
        dataFile.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));

        if (fileName.startsWith("DA-")) {
            String dataAcquisitionUri = ObjectAccessSpec.getProperDataAcquisitionUri(fileName);
            dataFile.setDataAcquisitionUri(dataAcquisitionUri == null ? "" : dataAcquisitionUri);
        }

        dataFile.save();
    }

    public static DataFile convertFromSolr(SolrDocument doc) {
        DataFile object = new DataFile(doc.getFieldValue("file_name").toString());

        object.setOwnerEmail(SolrUtils.getFieldValue(doc, "owner_email_str").toString());
        object.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str").toString());
        object.setDataAcquisitionUri(URIUtils.replaceNameSpaceEx(SolrUtils.getFieldValue(doc, "acquisition_uri_str").toString()));
        object.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str").toString());
        object.setStatus(SolrUtils.getFieldValue(doc, "status_str").toString());
        object.setCompletionPercentage(Integer.parseInt(SolrUtils.getFieldValue(doc, "completion_percentage_int").toString()));
        object.setSubmissionTime(SolrUtils.getFieldValue(doc, "submission_time_str").toString());
        object.setCompletionTime(SolrUtils.getFieldValue(doc, "completion_time_str").toString());
        object.setLastProcessTime(SolrUtils.getFieldValue(doc, "last_processed_time_str").toString());

        return object;
    }

    public static List<DataFile> findByQuery(SolrQuery query) {
        List<DataFile> list = new ArrayList<DataFile>();

        SolrClient solr = new HttpSolrClient.Builder(
                ConfigFactory.load().getString("hadatac.solr.data") 
                + CollectionUtil.CSV_DATASET).build();

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
            System.out.println("[ERROR] DataFile.find(SolrQuery) - Exception message: " + e.getMessage());
        }

        return list;
    }

    public static List<DataFile> find(String ownerEmail, String status) {
        if (status == UNPROCESSED || status == PROCESSED || status == CREATING || status == CREATED) {
            SolrQuery query = new SolrQuery();
            query.set("q", "owner_email_str:\"" + ownerEmail + "\"" + " AND " + "status_str:\"" + status + "\"");
            query.set("rows", "10000000");
            return findByQuery(query);
        }
        else {
            return new ArrayList<DataFile>();
        }
    }

    public static List<DataFile> findAll(String status) {
        SolrQuery query = new SolrQuery();
        query.set("q", "status_str:\"" + status + "\"");
        query.set("rows", "10000000");
        return findByQuery(query);
    }

    public static DataFile findByName(String ownerEmail, String fileName) {		
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

    public static DataFile findByName(String fileName) {
        return findByName(null, fileName);
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

    public static void includeUnrecognizedFiles(String path, List<DataFile> ownedFiles) {		
        File folder = new File(path);
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

    public static void includeUnrecognizedFiles(String path, String ownerEmail) {      
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        
        List<DataFile> unproc_files = DataFile.findAll(DataFile.UNPROCESSED);
        unproc_files.addAll(DataFile.findAll(DataFile.FREEZED));

        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile() && hasValidExtension(listOfFiles[i].getName())) {
                if (!search(listOfFiles[i].getName(), unproc_files)) {
                    DataFile.create(listOfFiles[i].getName(), ownerEmail);
                }
            }
        }
    }

    public static void filterNonexistedFiles(String path, List<DataFile> files) {
        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File[] listOfFiles = folder.listFiles();
        Iterator<DataFile> iterFile = files.iterator();
        while (iterFile.hasNext()) {
            DataFile file = iterFile.next();
            boolean isExisted = false;
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    if(file.getFileName().equals(listOfFiles[i].getName())) {
                        isExisted = true;
                        break;
                    }
                }
            }
            if (!isExisted) {
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
}
