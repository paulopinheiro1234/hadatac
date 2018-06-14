package org.hadatac.data.loader;

import java.lang.String;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.DPL;
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
        List<DataFile> proc_files = DataFile.findAll(DataFile.PROCESSED);
        List<DataFile> unproc_files = DataFile.findAll(DataFile.UNPROCESSED);
        DataFile.filterNonexistedFiles(path_proc, proc_files);
        DataFile.filterNonexistedFiles(path_unproc, unproc_files);
        
        unproc_files.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile o1, DataFile o2) {
                return o1.getLastProcessTime().compareTo(o2.getLastProcessTime());
            }
        });

        for (DataFile file : unproc_files) {
            file.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            file.save();
            
            String file_name = file.getFileName();
            String filePath = path_unproc + "/" + file_name;
            AnnotationLog log = new AnnotationLog(file_name);
            
            if (proc_files.contains(file)) {
                log.addline(Feedback.println(Feedback.WEB, String.format(
                        "[ERROR] Already processed a file with the same name %s . "
                        + "Please delete the old file before moving forward ", file_name)));
                return;
            }

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
            GeneratorChain chain = null;
            
            if (file_name.startsWith("DA-")) {
                chain = annotateDAFile(file, recordFile);
            } else if (file_name.startsWith("PID-")) {
                if (recordFile.getNumberOfSheets() > 1) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            "[ERROR] PID file has more than one sheet. "));
                    return;
                }
                chain = annotateSubjectIdFile(recordFile);
            } else if (file_name.startsWith("STD-")) {
                chain = annotateStudyIdFile(recordFile);
            } else if (file_name.startsWith("DPL-")) {
                if (file_name.endsWith(".xlsx")) {
                    recordFile = new SpreadsheetRecordFile(new File(filePath), "InfoSheet");
                    if (!recordFile.isValid()) {
                        log.addline(Feedback.println(Feedback.WEB, 
                                "[ERROR] Missing InfoSheet. "));
                        return;
                    }
                }
                chain = annotateDPLFile(recordFile);
            } else if (file_name.startsWith("MAP-")) {
                if (recordFile.getNumberOfSheets() > 1) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            "[ERROR] MAP file has more than one sheet. "));
                    return;
                }
                chain = annotateMapFile(recordFile);
            } else if (file_name.startsWith("ACQ-")) {
                chain = annotateACQFile(recordFile, true);
            } else if (file_name.startsWith("SDD-")) {
                if (file_name.endsWith(".xlsx")) {
                    recordFile = new SpreadsheetRecordFile(new File(filePath), "InfoSheet");
                    if (!recordFile.isValid()) {
                        log.addline(Feedback.println(Feedback.WEB, 
                                "[ERROR] Missing InfoSheet. "));
                        return;
                    }
                }
                chain = annotateDataAcquisitionSchemaFile(recordFile);
            } else if (file_name.startsWith("SSD-")) {
                chain = annotateSSDFile(recordFile);
            } else {
                log.addline(Feedback.println(Feedback.WEB, 
                        "[ERROR] Unsupported file name prefix, only accept prefixes "
                                + "STD-, DPL-, PID-, MAP-, SDD-, ACQ-, DA-. "));
                return;
            }

            if (chain != null) {
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
                file.setStudyUri(chain.getStudyUri());
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
        chain.addGenerator(new AgentGenerator(file));

        return chain;
    }


    public static GeneratorChain annotateDPLFile(RecordFile file) {

	System.out.println("Processing DPL file...");

	DPL dpl = new DPL(file);
        String file_name = dpl.getFileName();
        Map<String, String> mapCatalog = dpl.getCatalog();

	String sheetName = "";
	RecordFile sheet = null;
        GeneratorChain chain = new GeneratorChain();

	// Deployment Generator
	if(mapCatalog.containsKey("Deployments")) {

		sheetName = mapCatalog.get("Deployments").replace("#", "");
		sheet = new SpreadsheetRecordFile(file.getFile(), sheetName);
		chain.addGenerator(new DeploymentGenerator(sheet));

	}

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
        String file_name = file.getFile().getName();
        String sddName = sdd.getName();
        if (sddName == ""){
            AnnotationLog.printException("This SDD has no Study_ID filled.", file_name);
        }
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
        sdd.readTimeline(timelineRecordFile);

        GeneratorChain chain = new GeneratorChain();
        if (codeBookRecordFile.isValid()) {
            chain.addGenerator(new PVGenerator(codeBookRecordFile, sddName, sdd.getMapAttrObj(), sdd.getCodeMapping()));
        }

        if (dictionaryRecordFile.isValid()) {
            chain.addGenerator(new DASchemaAttrGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            chain.addGenerator(new DASchemaObjectGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            chain.addGenerator(new DASchemaEventGenerator(dictionaryRecordFile, sdd.getTimeLine(), sddName, sdd.getCodeMapping()));
        }

        GeneralGenerator generalGenerator = new GeneralGenerator(file, "DASchema");
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", ConfigProp.getKbPrefix() + "DAS-" + sddName);
        AnnotationLog.println("This SDD is assigned with uri: " + ConfigProp.getKbPrefix() + "DAS-" + sddName + " and is of type hasco:DASchema", file.getFile().getName());
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
        RecordFile SBJsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-SUBJECTS");
        RecordFile MOMsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-MOTHERS");
        RecordFile SSAPsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-SSAMPLES");
        RecordFile MSAPsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-MSAMPLES");
        RecordFile TIMEsheet = new SpreadsheetRecordFile(file.getFile(), "SOC-VISITS");

        GeneratorChain chain = new GeneratorChain();
        if (SSDsheet.isValid()) {
            chain.addGenerator(new SSDGenerator(SSDsheet));
        } else {
            //chain.setInvalid();
            AnnotationLog.printException("Cannot sheet SSD ", file.getFile().getName());
        }
        if (SBJsheet.isValid()) {
            chain.addGenerator(new SubjectGenerator(SBJsheet));
        } else {
            //chain.setInvalid();
            AnnotationLog.printException("Cannot sheet SOC-SUBJECTS ", file.getFile().getName());
        }
        if (SSAPsheet.isValid()) {
            chain.addGenerator(new SSDSampleMapper(SSAPsheet));
        } 
        else {
            //chain.setInvalid();
            AnnotationLog.printException("Cannot sheet SOC-SSAMPLES ", file.getFile().getName());
        }
        if (MOMsheet.isValid()) {
            MotherGenerator motherGenerator = new MotherGenerator(MOMsheet);
            chain.addGenerator(motherGenerator);
            chain.addGenerator(new SSDSampleMapper(MSAPsheet, motherGenerator));
        } else {
            //chain.setInvalid();
            AnnotationLog.printException("Cannot sheet SOC-MOTHERS ", file.getFile().getName());
        }
        if (TIMEsheet.isValid()) {
        	chain.addGenerator(new TimeInstantGenerator(TIMEsheet));
        } else {
        	AnnotationLog.printException("Cannot sheet SOC-VISITS ", file.getFile().getName());
        }
        return chain;
    }

    public static GeneratorChain annotateDAFile(DataFile dataFile, RecordFile recordFile) {
        System.out.println("annotateDAFile: [" + dataFile.getFileName() + "]");
        
        GeneratorChain chain = new GeneratorChain();

        String file_name = dataFile.getFileName();
        AnnotationLog log = AnnotationLog.create(file_name);

        ObjectAccessSpec da = null;
        String da_uri = null;
        String deployment_uri = null;
        String schema_uri = null;

        if (dataFile != null) {
            da = ObjectAccessSpec.findByUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
            if (da != null) {
                if (!da.isComplete()) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            String.format("[WARNING] Specification of associated Object Access Specification is incomplete: %s", file_name)));
                    chain.setInvalid();
                } else {
                    log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Specification of associated Object Access Specification is complete: %s", file_name)));
                }
                da_uri = da.getUri();
                deployment_uri = da.getDeploymentUri();
                schema_uri = da.getSchemaUri();
            }
        }

        if (da_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot find target data acquisition: %s", file_name)));
            chain.setInvalid();
        } else {
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Found target data acquisition: %s", file_name)));
        }
        if (schema_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load schema specified for data acquisition: %s", file_name)));
            chain.setInvalid();
        } else {
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Schema %s specified for data acquisition: %s", schema_uri, file_name)));
        }
        if (deployment_uri == null) {
            log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load deployment specified for data acquisition: %s", file_name)));
            chain.setInvalid();
        } else {
            try {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.addline(Feedback.println(Feedback.WEB, String.format("URL decoding error for deployment uri %s", deployment_uri)));
                chain.setInvalid();
            }
            log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Deployment %s specified for data acquisition %s", deployment_uri, file_name)));
        }

        dataFile.setStudyUri(da.getStudyUri());
        dataFile.setDatasetUri(DataFactory.getNextDatasetURI(da.getUri()));
        da.addDatasetUri(dataFile.getDatasetUri());
        
        chain.addGenerator(new MeasurementGenerator(recordFile, da, dataFile));

        return chain;
    }
}
