package org.hadatac.data.loader;

import java.lang.String;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.DOI;
import org.hadatac.entity.pojo.DPL;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.SDD;
import org.hadatac.entity.pojo.SSDSheet;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;

import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

public class AnnotationWorker {

	public AnnotationWorker() {}

    public static void scan() {
        DataFile.includeUnrecognizedFiles(
                ConfigProp.getPathUnproc(), "",
                DataFile.findByMultiStatus(Arrays.asList(DataFile.UNPROCESSED, DataFile.FREEZED)),
                ConfigProp.getDefaultOwnerEmail(),
                DataFile.UNPROCESSED);
    }

    public static GeneratorChain getGeneratorChain(DataFile dataFile) {
        GeneratorChain chain = null;
        String fileName = FilenameUtils.getBaseName(dataFile.getFileName());

        if (fileName.startsWith("DA-")) {
            chain = annotateDAFile(dataFile);
            
        } else if (fileName.startsWith("STD-")) {
            chain = annotateStudyIdFile(dataFile);
            
        } else if (fileName.startsWith("DPL-")) {
            chain = annotateDPLFile(dataFile);
            
        } else if (fileName.startsWith("STR-")) {
            //checkSTRFile(dataFile);
            chain = annotateSTRFile(dataFile);
            
        } else if (fileName.startsWith("SDD-")) {
            chain = annotateSDDFile(dataFile);
            
        } else if (fileName.startsWith("SSD-")) {
            chain = annotateSSDFile(dataFile);
            
        } else if (fileName.startsWith("DOI-")) {
            chain = annotateDOIFile(dataFile);
            
        } else {
            dataFile.getLogger().printExceptionById("GBL_00001");
            return null;
        }

        return chain;
    }

    public static void autoAnnotate() {
        if(ConfigProp.getAuto().equals("off")) {
            return;
        }

        String pathProc = ConfigProp.getPathProc();
        String pathUnproc = ConfigProp.getPathUnproc();
        List<DataFile> procFiles = DataFile.findByStatus(DataFile.PROCESSED);
        List<DataFile> unprocFiles = DataFile.findByStatus(DataFile.UNPROCESSED);
        DataFile.filterNonexistedFiles(pathProc, procFiles);
        DataFile.filterNonexistedFiles(pathUnproc, unprocFiles);

        unprocFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile o1, DataFile o2) {
                return o1.getLastProcessTime().compareTo(o2.getLastProcessTime());
            }
        });

        for (DataFile dataFile : unprocFiles) {
        	//System.out.println("Processing file: " + dataFile.getFileName());
            dataFile.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.getLogger().resetLog();
            dataFile.save();

            String fileName = dataFile.getFileName();

            // file is rejected if it already exists in the folder of processed files
            if (procFiles.contains(dataFile)) {
                dataFile.getLogger().printExceptionByIdWithArgs("GBL_00002", fileName);
                dataFile.freeze();
                return;
            }

            dataFile.getLogger().println(String.format("Processing file: %s", fileName));

            // file is rejected if it has an invalid extension
            RecordFile recordFile = null;
            File file = new File(dataFile.getAbsolutePath());
            if (fileName.endsWith(".csv")) {
                recordFile = new CSVRecordFile(file);
            } else if (fileName.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(file);
            } else if (dataFile.isMediaFile()) {
            	//System.out.println("Processing Media File " + dataFile.getFileName());
                processMediaFile(dataFile);
                return;
            } else {
                dataFile.getLogger().printExceptionByIdWithArgs("GBL_00003", fileName);
                dataFile.freeze();
                return;
            }
            
            dataFile.setRecordFile(recordFile);

            boolean bSucceed = false;
            GeneratorChain chain = getGeneratorChain(dataFile);

            if (chain != null) {
                bSucceed = chain.generate();
            }

            if (bSucceed) {
            	
            	// if chain includes PVGenerator, executes PVGenerator.generateOthers()
            	if (chain.getPV()) {
            		
            		PVGenerator.generateOthers(chain.getCodebookFile(), chain.getSddName(), ConfigProp.getKbPrefix());
            	}
            	
                //Move the file to the folder for processed files
                String study = URIUtils.getBaseName(chain.getStudyUri());
                String new_path = "";
                if (study.isEmpty()) {
                    new_path = pathProc;
                } else {
                    new_path = Paths.get(pathProc, study).toString();
                }

                File destFolder = new File(new_path);
                if (!destFolder.exists()) {
                    destFolder.mkdirs();
                }

                dataFile.delete();

                dataFile.setStatus(DataFile.PROCESSED);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                if (study.isEmpty()) {
                    dataFile.setStudyUri("");
                } else {
                    dataFile.setDir(study);
                    dataFile.setStudyUri(chain.getStudyUri());
                }
                dataFile.save();

                file.renameTo(new File(destFolder + "/" + dataFile.getStorageFileName()));
                file.delete();
            } else {
                dataFile.freeze();
            }
        }
    }

    /* 
     * Move any file that isMediaFile() into a media folder in processed files.
     * At the moment, no other kind of processing is performed by this code. 
     */
    public static void processMediaFile(DataFile dataFile) {
    	//Move the file to the folder for processed files
        String new_path = ConfigProp.getPathMedia();
 
        File file = new File(dataFile.getAbsolutePath());

        File destFolder = new File(new_path);
        if (!destFolder.exists()) {
            destFolder.mkdirs();
        }

        dataFile.setStatus(DataFile.PROCESSED);
        dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        dataFile.setDir(ConfigProp.MEDIA_FOLDER);
        dataFile.setStudyUri("");
        dataFile.save();

        file.renameTo(new File(destFolder + "/" + dataFile.getStorageFileName()));
        file.delete();
    }

    /*===========================================================================================*
     *                                  METADATA TEMPLATE ANNOTATORS                             *
     *===========================================================================================*/
    
    /****************************
     *    STD                   *
     ****************************/    
    
    public static GeneratorChain annotateStudyIdFile(DataFile dataFile) {
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new StudyGenerator(dataFile));
        chain.addGenerator(new AgentGenerator(dataFile));

        return chain;
    }

    /****************************
     *    DOI                   *
     ****************************/    
    
    public static GeneratorChain annotateDOIFile(DataFile dataFile) {
        System.out.println("Processing DOI file ...");
        RecordFile recordFile = new SpreadsheetRecordFile(dataFile.getFile(), "InfoSheet");
        if (!recordFile.isValid()) {
            dataFile.getLogger().printExceptionById("DOI_00001");
            return null;
        } else {
            dataFile.setRecordFile(recordFile);
        }
        
        DOI doi = new DOI(dataFile);
        Map<String, String> mapCatalog = doi.getCatalog();
        GeneratorChain chain = new GeneratorChain();

        String studyId = doi.getStudyId();
        if (studyId == null || studyId.isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("DOI_00002", studyId);
            return null;
        } else {
            Study study = Study.findById(studyId);
            if (study != null) {
                chain.setStudyUri(study.getUri());
                dataFile.getLogger().println("DOI ingestion: Found study id [" + studyId + "]");
            } else {
                dataFile.getLogger().printExceptionByIdWithArgs("DOI_00003", studyId);
                return null;
            }
        }

        chain.setDataFile(dataFile);
        
        String doiVersion = doi.getVersion();
        if (doiVersion != null && !doiVersion.isEmpty()) {
            dataFile.getLogger().println("DOI ingestion: version is [" + doiVersion + "]");
        } else {
            dataFile.getLogger().printExceptionById("DOI_00004");
            return null;
        }

        if (mapCatalog.get("Filenames") == null) {
            dataFile.getLogger().printExceptionById("DOI_00005");
            return null;
        }

        String sheetName = mapCatalog.get("Filenames").replace("#", "");
        RecordFile sheet = new SpreadsheetRecordFile(dataFile.getFile(), sheetName);
                
        try {
        	DataFile dataFileForSheet = (DataFile)dataFile.clone();
        	dataFileForSheet.setRecordFile(sheet);
        	chain.addGenerator(new DOIGenerator(dataFileForSheet));
        } catch (CloneNotSupportedException e) {
        	e.printStackTrace();
            dataFile.getLogger().printExceptionById("DOI_00006");
            return null;
        }

        return chain;
    }

    /****************************
     *    DPL                   *
     ****************************/    
    
    public static GeneratorChain annotateDPLFile(DataFile dataFile) {
        RecordFile recordFile = new SpreadsheetRecordFile(dataFile.getFile(), "InfoSheet");
        if (!recordFile.isValid()) {
            dataFile.getLogger().printExceptionById("DPL_00001");
            return null;
        } else {
            dataFile.setRecordFile(recordFile);
        }
        
        DPL dpl = new DPL(dataFile);
        Map<String, String> mapCatalog = dpl.getCatalog();

        GeneratorChain chain = new GeneratorChain();
        for (String key : mapCatalog.keySet()) {
            if (mapCatalog.get(key).length() > 0) {
                String sheetName = mapCatalog.get(key).replace("#", "");
                RecordFile sheet = new SpreadsheetRecordFile(dataFile.getFile(), sheetName);
                
                try {
                    DataFile dataFileForSheet = (DataFile)dataFile.clone();
                    dataFileForSheet.setRecordFile(sheet);
                    chain.addGenerator(new DPLGenerator(dataFileForSheet));
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        return chain;
    }

    /****************************
     *    STR                   *
     ****************************/    
    
    public static GeneratorChain annotateSTRFile(DataFile dataFile) {
        System.out.println("Processing STR file ...");
        
        // verifies if data file is an Excel spreadsheet
        String fileName = dataFile.getFileName();
        if (!fileName.endsWith(".xlsx")) {
            dataFile.getLogger().printExceptionById("STR_00004");
            return null;
        } 

        // verifies if data file contains an InfoSheet sheet
        RecordFile recordFile = new SpreadsheetRecordFile(dataFile.getFile(), "InfoSheet");
        if (!recordFile.isValid()) {
            dataFile.getLogger().printExceptionById("STR_00001");
            return null;
        } else {
            dataFile.setRecordFile(recordFile);
        }

        STRInfoGenerator strInfo = new STRInfoGenerator(dataFile);        
        Study strStudy = strInfo.getStudy();
        String strVersion = strInfo.getVersion();
                
        // verifies if study is specified
        if (strStudy == null) {
            dataFile.getLogger().printExceptionByIdWithArgs("STR_00002", strInfo.getStudyId());
            return null;
        }
        // verifies if version is specified
        if (strVersion == "") {
            dataFile.getLogger().printExceptionById("STR_00003");
            return null;
        }
        Map<String, String> mapCatalog = strInfo.getCatalog();

        RecordFile fileStreamRecordFile = null;
        RecordFile messageStreamRecordFile = null;
        RecordFile messageTopicRecordFile = null;

        // verifies if filestream sheet is available, even if no file stream is specified
        if (mapCatalog.get(STRInfoGenerator.FILESTREAM) == null) { 
        	dataFile.getLogger().printExceptionById("STR_00005");
        	return null;
        }
        fileStreamRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get(STRInfoGenerator.FILESTREAM).replace("#", ""));
        
        // verifies if messagestream sheet is available, even if no message stream is specified
        if (mapCatalog.get(STRInfoGenerator.MESSAGESTREAM) == null) {
    		dataFile.getLogger().printExceptionById("STR_00006");
    		return null;
        }
        messageStreamRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get(STRInfoGenerator.MESSAGESTREAM).replace("#", ""));
        
        // verifies if messagetopic sheet is available, even if no message topic is specified
        if (mapCatalog.get(STRInfoGenerator.MESSAGETOPIC) == null) {
    		dataFile.getLogger().printExceptionById("STR_00016");
    		return null;
        }
        messageTopicRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get(STRInfoGenerator.MESSAGETOPIC).replace("#", ""));
        
        // verifies if not both fileStream sheet and messageStream sheet are empty
        if (fileStreamRecordFile.getNumberOfRows() <= 0 && messageStreamRecordFile.getNumberOfRows() <= 0) {
    		dataFile.getLogger().printExceptionById("STR_00007");
    		return null;        	
        }
        // verifies that there is info in messageTopics in case messageStream is not empty
        if ((messageStreamRecordFile.getNumberOfRows() <= 0 && messageTopicRecordFile.getNumberOfRows() > 0) ||
            (messageStreamRecordFile.getNumberOfRows() > 0 && messageTopicRecordFile.getNumberOfRows() <= 0)) {
    		dataFile.getLogger().printExceptionById("STR_00010");
    		return null;
        }

        GeneratorChain chain = new GeneratorChain();
        chain.setStudyUri(strStudy.getUri());
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String startTime = isoFormat.format(new Date());
        if (fileStreamRecordFile.getNumberOfRows() > 1 && fileStreamRecordFile.getRecords().size() > 0) {
        	chain.addGenerator(new STRFileGenerator(dataFile, strStudy, fileStreamRecordFile, startTime));
        }
        if (messageStreamRecordFile.getNumberOfRows() > 1 && messageStreamRecordFile.getRecords().size() > 0) {
        	STRMessageGenerator messageGen = new STRMessageGenerator(dataFile, strStudy, messageStreamRecordFile, startTime);
        	if (!messageGen.isValid()) {
        		dataFile.getLogger().printExceptionByIdWithArgs(messageGen.getErrorMessage(),messageGen.getErrorArgument());
            	return null;
        	}
        	chain.addGenerator(messageGen);
        }
        if (messageTopicRecordFile.getNumberOfRows() > 1 && messageTopicRecordFile.getRecords().size() > 0) {
        	STRTopicGenerator topicGen = new STRTopicGenerator(dataFile, messageTopicRecordFile, startTime);
        	if (!topicGen.isValid()) {
        		dataFile.getLogger().printExceptionByIdWithArgs(topicGen.getErrorMessage(),topicGen.getErrorArgument());
            	return null;
        	}
        	chain.addGenerator(topicGen);
        }
        return chain;
    }

    /****************************
     *    SDD                   *
     ****************************/    
    
    public static GeneratorChain annotateSDDFile(DataFile dataFile) {
        System.out.println("Processing SDD file ...");
        
        RecordFile recordFile = new SpreadsheetRecordFile(dataFile.getFile(), "InfoSheet");
        if (!recordFile.isValid()) {
            dataFile.getLogger().printExceptionById("SDD_00001");
            return null;
        } else {
            dataFile.setRecordFile(recordFile);
        }

        SDD sdd = new SDD(dataFile);
        String fileName = dataFile.getFileName();
        String sddName = sdd.getName();
        String sddVersion = sdd.getVersion();
        if (sddName == "") {
            dataFile.getLogger().printExceptionById("SDD_00003");
            return null;
        }
        if (sddVersion == "") {
            dataFile.getLogger().printExceptionById("SDD_00018");
            return null;
        }
        Map<String, String> mapCatalog = sdd.getCatalog();

        RecordFile codeMappingRecordFile = null;
        RecordFile dictionaryRecordFile = null;
        RecordFile codeBookRecordFile = null;
        RecordFile timelineRecordFile = null;

        File codeMappingFile = null;

        if (fileName.endsWith(".csv")) {
            String prefix = "sddtmp/" + fileName.replace(".csv", "");
            File dictionaryFile = sdd.downloadFile(mapCatalog.get("Data_Dictionary"), prefix + "-dd.csv");
            File codeBookFile = sdd.downloadFile(mapCatalog.get("Codebook"), prefix + "-codebook.csv");
            File timelineFile = sdd.downloadFile(mapCatalog.get("Timeline"), prefix + "-timeline.csv");
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), prefix + "-code-mappings.csv");

            dictionaryRecordFile = new CSVRecordFile(dictionaryFile);
            codeBookRecordFile = new CSVRecordFile(codeBookFile);
            timelineRecordFile = new CSVRecordFile(timelineFile);
        } else if (fileName.endsWith(".xlsx")) {
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), 
                    "sddtmp/" + fileName.replace(".xlsx", "") + "-code-mappings.csv");

            if (mapCatalog.get("Codebook") != null) { 
                codeBookRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get("Codebook").replace("#", ""));
            }
            
            if (mapCatalog.get("Data_Dictionary") != null) {
                dictionaryRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get("Data_Dictionary").replace("#", ""));
            }
            
            if (mapCatalog.get("Timeline") != null) {
                timelineRecordFile = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get("Timeline").replace("#", ""));
            }
        }

        if (null != codeMappingFile) {
            codeMappingRecordFile = new CSVRecordFile(codeMappingFile);
            if (!sdd.readCodeMapping(codeMappingRecordFile)) {
                dataFile.getLogger().printWarningById("SDD_00016");
            } else {
                dataFile.getLogger().println(String.format("Codemappings: " + sdd.getCodeMapping().get("U"), fileName));
            }
        } else {
            dataFile.getLogger().printWarningById("SDD_00017");
        }

        if (!sdd.readDataDictionary(dictionaryRecordFile)) {
            dataFile.getLogger().printExceptionById("SDD_00004");
            return null;
        }
        if (codeBookRecordFile == null || !sdd.readCodebook(codeBookRecordFile)) {
            dataFile.getLogger().printWarningById("SDD_00005");
        }
        if (timelineRecordFile == null || !sdd.readTimeline(timelineRecordFile)) {
            dataFile.getLogger().printWarningById("SDD_00006");
        }

        GeneratorChain chain = new GeneratorChain();
        chain.setPV(true);
        
        if (dictionaryRecordFile != null && dictionaryRecordFile.isValid()) {
            DataFile dictionaryFile;
            try {
                dictionaryFile = (DataFile)dataFile.clone();
                dictionaryFile.setRecordFile(dictionaryRecordFile);
                chain.addGenerator(new DASchemaAttrGenerator(dictionaryFile, sddName, sdd.getCodeMapping(), sdd.readDDforEAmerge(dictionaryRecordFile)));
                chain.addGenerator(new DASchemaObjectGenerator(dictionaryFile, sddName, sdd.getCodeMapping()));
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        // codebook needs to be processed after data dictionary because codebook relies on 
        // data dictionary's attributes (DASAs) to group codes for categorical variables
        
        if (codeBookRecordFile != null && codeBookRecordFile.isValid()) {
            DataFile codeBookFile;
            try {
                codeBookFile = (DataFile)dataFile.clone();
                codeBookFile.setRecordFile(codeBookRecordFile);
                chain.setCodebookFile(codeBookFile);
                chain.setSddName(URIUtils.replacePrefixEx(ConfigProp.getKbPrefix() + "DAS-" + sddName));
                chain.addGenerator(new PVGenerator(codeBookFile, sddName, sdd.getMapAttrObj(), sdd.getCodeMapping()));
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
        }

        GeneralGenerator generalGenerator = new GeneralGenerator(dataFile, "DASchema");
        String sddUri = ConfigProp.getKbPrefix() + "DAS-" + sddName;
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", sddUri);
        dataFile.getLogger().println("This SDD is assigned with uri: " + sddUri + " and is of type hasco:DASchema");
        row.put("a", "hasco:DASchema");
        row.put("rdfs:label", "SDD-" + sddName);
        row.put("rdfs:comment", "");
        row.put("hasco:hasVersion", sddVersion);
        generalGenerator.addRow(row);
        chain.addGenerator(generalGenerator);
        chain.setNamedGraphUri(URIUtils.replacePrefixEx(sddUri));

        return chain;
    }

    /****************************
     *    SSD                   *
     ****************************/    
    
    public static GeneratorChain annotateSSDFile(DataFile dataFile) {
        String studyId = dataFile.getBaseName().replaceAll("SSD-", "");
        System.out.println("Processing SSD file of " + studyId + "...");

        SSDSheet ssd = new SSDSheet(dataFile);
        Map<String, String> mapCatalog = ssd.getCatalog();
        Map<String, List<String>> mapContent = ssd.getMapContent();
        
        RecordFile SSDsheet = new SpreadsheetRecordFile(dataFile.getFile(), "SSD");
        dataFile.setRecordFile(SSDsheet);

        SSDGeneratorChain chain = new SSDGeneratorChain();

        Study study = null;

        if (SSDsheet.isValid()) {

            VirtualColumnGenerator vcgen = new VirtualColumnGenerator(dataFile);
            chain.addGenerator(vcgen);
            //System.out.println("added VirtualColumnGenerator for " + dataFile.getAbsolutePath());
            
            SSDGenerator socgen = new SSDGenerator(dataFile);
            chain.addGenerator(socgen);
            //System.out.println("added SSDGenerator for " + dataFile.getAbsolutePath());

            String studyUri = socgen.getStudyUri();
            if (studyUri == null || studyUri.isEmpty()) {
                return null;
            } else {
                chain.setStudyUri(studyUri);
                study = Study.find(studyUri);
                if (study != null) {
                    String updatedSSDUri =studyUri.replaceAll("STD","SSD");
                    dataFile.getLogger().println("SSD ingestion: The study uri :" + studyUri.replaceAll("STD","SSD") + " is in the TS.");
//                    dataFile.getLogger().println("SSD ingestion: The study uri :" + studyUri + " is in the TS.");
                    socgen.setStudyUri(updatedSSDUri);
                } else {
                    dataFile.getLogger().printExceptionByIdWithArgs("SSD_00005", studyUri);
                    return null;
                }
            }

            chain.setDataFile(dataFile);

        } else {
            //chain.setInvalid();
            dataFile.getLogger().printException("Cannot locate SSD's sheet ");
        }

        //System.out.println("AnnotationWork: pre-processing StudyObjectGenerator. Study Id is  " + study.getId());
        
        String study_uri = chain.getStudyUri();
        for (String i : mapCatalog.keySet()) {
            if (mapCatalog.get(i) != null && mapCatalog.get(i).length() > 0) {
                try {
                    RecordFile SOsheet = new SpreadsheetRecordFile(dataFile.getFile(), mapCatalog.get(i).replace("#", ""));
                    DataFile dataFileForSheet = (DataFile)dataFile.clone();
                    dataFileForSheet.setRecordFile(SOsheet);
                    if (mapContent == null || mapContent.get(i) == null) {
                        dataFile.getLogger().printException("No value for MapContent with index [" + i + "]");
                    } else {
                        study_uri= (study_uri.contains("STD")) ? study_uri.replaceAll("STD","SSD"):study_uri;
                    	chain.addGenerator(new StudyObjectGenerator(dataFileForSheet, mapContent.get(i), mapContent, study_uri, study.getId()));
                    }
                } catch (CloneNotSupportedException e) {
                    e.printStackTrace();
                }
            }
        }

        return chain;
    }

    /****************************
     *    DA                    *
     ****************************/    
    
    public static GeneratorChain annotateDAFile(DataFile dataFile) {
        System.out.println("Processing DA file " + dataFile.getFileName());

        GeneratorChain chain = new GeneratorChain();

        STR str = null;
        String str_uri = null;
        String deployment_uri = null;
        String schema_uri = null;
        String study_uri = null;

        if (dataFile != null) {
            str_uri = URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri());
            str = STR.findByUri(str_uri);
            if (str != null) {
                if (!str.isComplete()) {
                    dataFile.getLogger().printWarningByIdWithArgs("DA_00003", str_uri);
                    chain.setInvalid();
                    return chain;
                } else {
                    dataFile.getLogger().println(String.format("STR <%s> has been located", str_uri));
                }
                study_uri = str.getStudy().getUri();
                deployment_uri = str.getDeploymentUri();
                schema_uri = str.getSchemaUri();
            } else {
                dataFile.getLogger().printWarningByIdWithArgs("DA_00004", str_uri);
                chain.setInvalid();
                return chain;
            }
        }

        if (study_uri == null || study_uri.isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("DA_00008", str_uri);
            chain.setInvalid();
            return chain;
        } else {
            try {
                study_uri = URLDecoder.decode(study_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                dataFile.getLogger().printException(String.format("URL decoding error for study uri <%s>", study_uri));
                chain.setInvalid();
                return chain;
            }
            dataFile.getLogger().println(String.format("Study <%s> specified for data acquisition <%s>", study_uri, str_uri));
        }

        if (schema_uri == null || schema_uri.isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("DA_00005", str_uri);
            chain.setInvalid();
            return chain;
        } else {
            dataFile.getLogger().println(String.format("Schema <%s> specified for data acquisition: <%s>", schema_uri, str_uri));
        }
        
        if (deployment_uri == null || deployment_uri.isEmpty()) {
            dataFile.getLogger().printExceptionByIdWithArgs("DA_00006", str_uri);
            chain.setInvalid();
            return chain;
        } else {
            try {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                dataFile.getLogger().printException(String.format("URL decoding error for deployment uri <%s>", deployment_uri));
                chain.setInvalid();
                return chain;
            }
            dataFile.getLogger().println(String.format("Deployment <%s> specified for data acquisition <%s>", deployment_uri, str_uri));
        }

        if (str != null) {
            dataFile.setStudyUri(str.getStudy().getUri());
            dataFile.setDatasetUri(DataFactory.getNextDatasetURI(str.getUri()));
            str.addDatasetUri(dataFile.getDatasetUri());

            DataAcquisitionSchema schema = DataAcquisitionSchema.find(str.getSchemaUri());
            if (schema == null) {
                dataFile.getLogger().printExceptionByIdWithArgs("DA_00007", str.getSchemaUri());
                chain.setInvalid();
                return chain;
            }

            if (!str.hasCellScope()) {
            	// Need to be fixed here by getting codeMap and codebook from sparql query
            	DASOInstanceGenerator dasoInstanceGen = new DASOInstanceGenerator(
            			dataFile, str, dataFile.getFileName());
            	chain.addGenerator(dasoInstanceGen);	
            	chain.addGenerator(new MeasurementGenerator(MeasurementGenerator.FILEMODE, dataFile, str, schema, dasoInstanceGen));
            } else {
                chain.addGenerator(new MeasurementGenerator(MeasurementGenerator.FILEMODE, dataFile, str, schema, null));
            }
            chain.setNamedGraphUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
        }

        return chain;
    }
}
