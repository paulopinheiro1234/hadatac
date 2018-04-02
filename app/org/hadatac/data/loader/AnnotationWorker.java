package org.hadatac.data.loader;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.SDD;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

public class AnnotationWorker {

    public AnnotationWorker() {}

    public static void scan() {
        DataFile.includeUnrecognizedFiles(
                ConfigProp.getPathUnproc(), 
                ConfigProp.getDefaultOwnerEmail());
    }

    public static void autoAnnotate() {
        if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("off")){
            return;
        }

        String path_proc = ConfigProp.getPathProc();
        String path_unproc = ConfigProp.getPathUnproc();
        List<DataFile> unproc_files = DataFile.findAll(DataFile.UNPROCESSED);
        DataFile.filterNonexistedFiles(path_unproc, unproc_files);

        for (DataFile file : unproc_files) {
            String file_name = file.getFileName();
            String filePath = path_unproc + "/" + file_name;

            AnnotationLog log = new AnnotationLog(file_name);
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Processing file: %s", file_name)));

            RecordFile recordFile = null;
            if (file_name.endsWith(".csv")) {
                recordFile = new CSVRecordFile(new File(filePath));
            } else if (file_name.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(new File(filePath));
            } else {
                log.addline(Feedback.println(Feedback.WEB, String.format(
                        "[ERROR] Unknown file format: %s", file_name)));
                return;
            }

            boolean bSucceed = false;
            if (file_name.startsWith("DA-")) {
                bSucceed = annotateDAFile(file, recordFile);
            } else {
                GeneratorChain chain = null;
                if (file_name.startsWith("PID-")) {
                    chain = annotateSubjectIdFile(recordFile);
                }
                else if (file_name.startsWith("STD-")) {
                    chain = annotateStudyIdFile(recordFile);
                }
                else if (file_name.startsWith("MAP-")) {
                    chain = annotateMapFile(recordFile);
                }
                else if (file_name.startsWith("ACQ-")) {
                    chain = annotateACQFile(recordFile, true);
                }
                else if (file_name.startsWith("SDD-")) {
                    chain = annotateDataAcquisitionSchemaFile(recordFile);
                }
                else if (file_name.startsWith("SSD-")) {
                    chain = annotateSSDFile(recordFile);
                }

                bSucceed = chain.generate();
            }

            if (bSucceed) {
                //Move the file to the folder for processed files
                File destFolder = new File(path_proc);
                if (!destFolder.exists()){
                    destFolder.mkdirs();
                }

                file.delete();

                file.setStatus(DataFile.PROCESSED);
                file.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                file.save();
                File f = new File(path_unproc + "/" + file_name);
                f.renameTo(new File(destFolder + "/" + file_name));
                f.delete();
            }
        }
    }

    public static GeneratorChain annotateMapFile(RecordFile file) {
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new SampleSubjectMapper(file));

        return chain;
    }

    public static GeneratorChain annotateStudyIdFile(RecordFile file) {
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new StudyGenerator(file));
        chain.addGenerator(new SampleCollectionGenerator(file));
        chain.addGenerator(new AgentGenerator(file));

        return chain;
    }

    public static GeneratorChain annotateSampleIdFile(RecordFile file) {
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new SampleGenerator(file));

        return chain;
    }

    public static GeneratorChain annotateSubjectIdFile(RecordFile file) {
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new SubjectGenerator(file));

        return chain;
    }

    public static GeneratorChain annotateACQFile(RecordFile file, boolean bGenerate) {
        GeneratorChain chainForInstrument = new GeneratorChain();
        GeneralGenerator generalGenerator = new GeneralGenerator(file, "Instrument");
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", ConfigProp.getKbPrefix() + "INS-GENERIC-PHYSICAL-INSTRUMENT");
        row.put("a", "vstoi:PhysicalInstrument");
        row.put("rdfs:label", "Generic Physical Instrument");
        generalGenerator.addRow(row);

        row = new HashMap<String, Object>();
        row.put("hasURI", ConfigProp.getKbPrefix() + "INS-GENERIC-QUESTIONNAIRE");
        row.put("a", "hasco:Questionnaire");
        row.put("rdfs:label", "Generic Questionnaire");
        generalGenerator.addRow(row);
        chainForInstrument.addGenerator(generalGenerator);
        if (bGenerate) {
            chainForInstrument.generate();
        } else {
            chainForInstrument.delete();
        }

        GeneratorChain chainForDeployment = new GeneratorChain();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String startTime = isoFormat.format(new Date());
        chainForDeployment.addGenerator(new DeploymentGenerator(file, startTime));
        if (bGenerate) {
            chainForDeployment.generate();
        } else {
            chainForDeployment.delete();
        }

        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new DataAcquisitionGenerator(file, startTime));

        return chain;
    }

    public static GeneratorChain annotateDataAcquisitionSchemaFile(RecordFile file) {
        System.out.println("Processing data acquisition schema file ...");

        SDD sdd = new SDD(file);

        String sddName = sdd.getName();
        Map<String, String> mapCatalog = sdd.getCatalog();

        RecordFile codeMappingRecordFile = null;
        RecordFile dictionaryRecordFile = null;
        RecordFile codeBookRecordFile = null;
        RecordFile timelineRecordFile = null;

        File codeMappingFile = null;

        if (file.getFile().getName().endsWith(".csv")) {
            String prefix = "sddtmp/" + file.getFile().getName().replace(".csv", "");
            File dictionaryFile = sdd.downloadFile(mapCatalog.get("Data_Dictionary"), prefix + "-dd.csv");
            File codeBookFile = sdd.downloadFile(mapCatalog.get("Codebook"), prefix + "-codebook.csv");
            File timelineFile = sdd.downloadFile(mapCatalog.get("Timeline"), prefix + "-timeline.csv");
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), prefix + "-code-mappings.csv");

            dictionaryRecordFile = new CSVRecordFile(dictionaryFile);
            codeBookRecordFile = new CSVRecordFile(codeBookFile);
            timelineRecordFile = new CSVRecordFile(timelineFile);
        } else if (file.getFile().getName().endsWith(".xlsx")) {
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), 
                    "sddtmp/" + file.getFile().getName().replace(".xlsx", "") + "-code-mappings.csv");

            codeBookRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Codebook").replace("#", ""));
            dictionaryRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Data_Dictionary").replace("#", ""));
            timelineRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Timeline").replace("#", ""));
        }

        codeMappingRecordFile = new CSVRecordFile(codeMappingFile);

        sdd.readCodeMapping(codeMappingRecordFile);
        sdd.readDataDictionary(dictionaryRecordFile);
        sdd.readCodebook(codeBookRecordFile);
        sdd.readtimelineFile(timelineRecordFile);

        GeneratorChain chain = new GeneratorChain();
        if (codeBookRecordFile.isValid()) {
            chain.addGenerator(new PVGenerator(codeBookRecordFile, sddName, sdd.getMapAttrObj(), sdd.getCodeMapping()));
        }

        if (dictionaryRecordFile.isValid()) {
            chain.addGenerator(new DASchemaAttrGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            chain.addGenerator(new DASchemaObjectGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            chain.addGenerator(new DASchemaEventGenerator(dictionaryRecordFile, sdd.getTimeLineMap(), sddName, sdd.getCodeMapping()));
        }

        GeneralGenerator generalGenerator = new GeneralGenerator(file, "DASchema");
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", ConfigProp.getKbPrefix() + "DAS-" + sddName);
        row.put("a", "hasco:DASchema");
        row.put("rdfs:label", "Schema for " + sddName);
        row.put("rdfs:comment", "");
        generalGenerator.addRow(row);
        chain.addGenerator(generalGenerator);

        return chain;
    }
    
    public static GeneratorChain annotateSSDFile(RecordFile file) {
        
        String Pilot_Num = file.getFile().getName().replaceAll("SSD-", "");
        System.out.println("Processing SSD file of " + Pilot_Num + "...");
        RecordFile SSDsheet = new SpreadsheetRecordFile(file.getFile(), "SSD");
        RecordFile SBJsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-2016-34-SUBJECTS");     
        RecordFile MOMsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-2016-34-MOTHERS");
        RecordFile SAPsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-2016-34-SSAMPLES");
        
        GeneratorChain chain = new GeneratorChain();
        chain.addGenerator(new SSDGenerator(SSDsheet));  
        chain.addGenerator(new SubjectGenerator(SBJsheet));
        chain.addGenerator(new MotherGenerator(MOMsheet));
        chain.addGenerator(new SampleSubjectMapper(SAPsheet));
//        chain.addGenerator(new MotherSampleGenerator());

        return chain;
    }

    public static boolean annotateDAFile(DataFile dataFile, RecordFile recordFile) {
        System.out.println("annotateDAFile: [" + dataFile.getFileName() + "]");
        
        String file_name = dataFile.getFileName();
        AnnotationLog log = AnnotationLog.create(file_name);
        
        DataAcquisition da = null;
        String da_uri = null;
        String deployment_uri = null;
        String schema_uri = null;

        if (dataFile != null) {
            da = DataAcquisition.findByUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
            if (da != null) {
                if (!da.isComplete()) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            String.format("[WARNING] Specification of associated Data Acquisition is incomplete: %s", file_name)));
                    return false;
                } else {
                    log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Specification of associated Data Acquisition is complete: %s", file_name)));
                }
                da_uri = da.getUri();
                deployment_uri = da.getDeploymentUri();
                schema_uri = da.getSchemaUri();
            }
        }

        if (da_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot find target data acquisition: %s", file_name)));
            return false;
        } else {
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Found target data acquisition: %s", file_name)));
        }
        if (schema_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load schema specified for data acquisition: %s", file_name)));
            return false;
        } else {
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Schema %s specified for data acquisition: %s", schema_uri, file_name)));
        }
        if (deployment_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load deployment specified for data acquisition: %s", file_name)));
            return false;
        } else {
            try {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.addline(Feedback.println(Feedback.WEB, String.format("URL decoding error for deployment uri %s", deployment_uri)));
                return false;
            }
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Deployment %s specified for data acquisition %s", deployment_uri, file_name)));
        }

        int status = -1;
        
        try {
            dataFile.setDatasetUri(DataFactory.getNextDatasetURI(da.getUri()));
            da.addDatasetUri(dataFile.getDatasetUri());
            
            Parser parser = new Parser();
            ParsingResult parsingResult = parser.indexMeasurements(recordFile, da, dataFile);
            status = parsingResult.getStatus();
        } catch (Exception e) {
            StringWriter errors = new StringWriter();
            e.printStackTrace(new PrintWriter(errors));
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] parsing and indexing CSV file %s", errors.toString())));
            e.printStackTrace();
            return false;
        }

        if (status == 0) {
            return true;
        }

        return false;
    }
}
