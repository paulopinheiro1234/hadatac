package org.hadatac.data.loader;

import java.lang.String;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.data.api.DataFactory;
import org.hadatac.entity.pojo.ObjectAccessSpec;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.DPL;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.SDD;
import org.hadatac.entity.pojo.SSD;
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
                ConfigProp.getPathUnproc(), 
                ConfigProp.getDefaultOwnerEmail());
    }

    public static void autoAnnotate() {
        if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("off")){
            return;
        }

        String pathProc = ConfigProp.getPathProc();
        String pathUnproc = ConfigProp.getPathUnproc();
        List<DataFile> procFiles = DataFile.findAll(DataFile.PROCESSED);
        List<DataFile> unprocFiles = DataFile.findAll(DataFile.UNPROCESSED);
        DataFile.filterNonexistedFiles(pathProc, procFiles);
        DataFile.filterNonexistedFiles(pathUnproc, unprocFiles);

        unprocFiles.sort(new Comparator<DataFile>() {
            @Override
            public int compare(DataFile o1, DataFile o2) {
                return o1.getLastProcessTime().compareTo(o2.getLastProcessTime());
            }
        });

        for (DataFile file : unprocFiles) {
            file.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            file.save();

            String fileName = file.getFileName();
            String filePath = pathUnproc + "/" + fileName;

            if (procFiles.contains(file)) {
                AnnotationLog.printException(String.format(
                        "Already processed a file with the same name %s . "
                        + "Please delete the old file before moving forward ", fileName), 
                        fileName);
                return;
            }
            
            AnnotationLog.println(String.format("Processing file: %s", fileName), fileName);

            RecordFile recordFile = null;
            if (fileName.endsWith(".csv")) {
                recordFile = new CSVRecordFile(new File(filePath));
            } else if (fileName.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(new File(filePath));
            } else {
                AnnotationLog.printException(
                        String.format("Unknown file format: %s", fileName), 
                        fileName);
                return;
            }

            boolean bSucceed = false;
            GeneratorChain chain = null;

            if (fileName.startsWith("DA-")) {
                chain = annotateDAFile(file, recordFile);
            } else if (fileName.startsWith("PID-")) {
                if (recordFile.getNumberOfSheets() > 1) {
                    AnnotationLog.printException("PID file has more than one sheet. ", fileName);
                    return;
                }
                chain = annotateSubjectIdFile(recordFile);
            } else if (fileName.startsWith("STD-")) {
                chain = annotateStudyIdFile(recordFile);
            } else if (fileName.startsWith("DPL-")) {
                if (fileName.endsWith(".xlsx")) {
                    recordFile = new SpreadsheetRecordFile(new File(filePath), "InfoSheet");
                    if (!recordFile.isValid()) {
                        AnnotationLog.printException("Missing InfoSheet. ", fileName);
                        return;
                    }
                }
                chain = annotateDPLFile(recordFile);
            } else if (fileName.startsWith("MAP-")) {
                if (recordFile.getNumberOfSheets() > 1) {
                    AnnotationLog.printException("MAP file has more than one sheet. ", fileName);
                    return;
                }
                chain = annotateMapFile(recordFile);
            } else if (fileName.startsWith("ACQ-")) {
                chain = annotateACQFile(recordFile, true);
            } else if (fileName.startsWith("OAS-")) {
                checkOASFile(recordFile);
                chain = annotateOASFile(recordFile, true);
            } else if (fileName.startsWith("SDD-")) {
                if (fileName.endsWith(".xlsx")) {
                    recordFile = new SpreadsheetRecordFile(new File(filePath), "InfoSheet");
                    if (!recordFile.isValid()) {
                        AnnotationLog.printException("The Info sheet is missing in this SDD file. ", fileName);
                        return;
                    }
                }
                chain = annotateSDDFile(recordFile);

            } else if (fileName.startsWith("SSD-")) {
                chain = annotateSSDFile(recordFile);
            } else {
                AnnotationLog.printException(
                        "Unsupported file name prefix, only accept prefixes "
                        + "STD-, DPL-, PID-, MAP-, SDD-, ACQ-, DA-. ", fileName);
                return;
            }

            if (chain != null) {
                bSucceed = chain.generate();
            }

            if (bSucceed) {
                //Move the file to the folder for processed files
                String study = URIUtils.getBaseName(chain.getStudyUri());
                String new_path = "";
                if (study.isEmpty()) {
                    new_path = pathProc;
                } else {
                    new_path = pathProc + "/" + study;
                }
                
                File destFolder = new File(new_path);
                if (!destFolder.exists()){
                    destFolder.mkdirs();
                }

                file.delete();

                file.setStatus(DataFile.PROCESSED);
                file.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                if (study.isEmpty()) {
                    file.setFileName(file.getFileName());
                    file.setStudyUri("");
                } else {
                    file.setFileName(study + "/" + file.getFileName());
                    file.setStudyUri(chain.getStudyUri());
                }
                file.save();
                
                File f = new File(pathUnproc + "/" + fileName);
                f.renameTo(new File(destFolder + "/" + fileName));
                f.delete();
                
                AnnotationLog log = AnnotationLog.find(fileName);
                if (null != log) {
                    AnnotationLog newLog = new AnnotationLog(file.getFileName());
                    newLog.setLog(log.getLog());
                    log.delete();
                    newLog.save();
                }
            } else {
                // Freeze file
                System.out.println("Freezed file " + file.getFileName());
                file.setStatus(DataFile.FREEZED);
                file.save();
            }
        }
    }
    
    public static void checkOASFile(RecordFile recordFile) {
        System.out.println("OAS HERE!");
        final String kbPrefix = ConfigProp.getKbPrefix();
        Record record = recordFile.getRecords().get(0);
        String studyName = record.getValueByColumnName("Study ID");
        String studyUri = URIUtils.replacePrefixEx(ConfigProp.getKbPrefix() + "STD-" + studyName);
        String fileName = recordFile.getFileName();

        AnnotationLog.println("Study ID found: " + studyName, fileName);
        AnnotationLog.println("Study URI found: " + studyUri, fileName);

        List<ObjectCollection> ocList = ObjectCollection.findByStudyUri(studyUri);
        Map<String, String> refList = new HashMap<String, String>();
        List<String> tarList = new ArrayList<String>();

        for (ObjectCollection oc: ocList) {
            if (oc.getGroundingLabel().length() > 0) {
                refList.put(oc.getSOCReference(), oc.getGroundingLabel());
                tarList.add(kbPrefix + "DASO-" + studyName + "-" + oc.getSOCReference().trim().replace(" ","").replace("_","-").replace("??", ""));
                System.out.println("========================= " + oc.getGroundingLabel());
            }
        }

        String das_uri = URIUtils.convertToWholeURI(ConfigProp.getKbPrefix() + "DAS-" + record.getValueByColumnName("data dict").replace("SDD-", ""));
        System.out.println("das_uri " + das_uri);
        DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);
        if (das == null) {
            AnnotationLog.printException("The SDD of study " + record.getValueByColumnName("Study ID") + " can not be found. Check if it is already ingested.", fileName);
        } else {
            // List<DataAcquisitionSchemaObject> loo = das.getObjects();
            Map<String, String> dasoPL = new HashMap<String, String>();
            List<DataAcquisitionSchemaObject> loo = new ArrayList<DataAcquisitionSchemaObject>();
            List<String> loo2 = new ArrayList<String>();
            for (DataAcquisitionSchemaAttribute attr : das.getAttributes()) {
                System.out.println(" +++++++ " + attr.getLabel() + " --- " + attr.getObjectViewLabel());
                if (attr.getObjectViewLabel().length() > 0) {
                    if (!loo2.contains(attr.getObjectViewLabel())) {
                        loo2.add(attr.getObjectViewLabel());
                        loo.add(attr.getObject());
                    }
                }
            }
            AnnotationLog.println("PATH COMPUTATION: The number of DASOs to be computed: " + loo2.toString(), fileName);

            for (DataAcquisitionSchemaObject i : loo) {
                if (i.getEntityLabel() == null || i.getEntityLabel().length() == 0) {
                    AnnotationLog.printException("The Entity Label of DASO : " + i.getLabel() + " can not be found. Check SDD.", fileName);
                } else if (!refList.containsKey(i.getLabel())) {

                    List<String> answer = new ArrayList<String>();
                    answer.add(i.getEntityLabel());
                    Boolean found = false;

                    for (String j : refList.keySet()) {

                        if (found == false) {
                            String target = kbPrefix + "DASO-" + studyName + "-" + j.trim().replace(" ","").replace("_","-").replace("??", "");
                            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                                    "SELECT ?x ?o WHERE { \n" + 
                                    "<" + i.getUri() + "> ?p ?x . \n" + 
                                    "   ?x ?p1 ?o .  \n" + 
                                    "   OPTIONAL {?o ?p2 " + target + " } " +
                                    "}";

                            ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                                    CollectionUtil.Collection.METADATA_SPARQL), queryString);

                            if (!resultsrw.hasNext()) {
                                System.out.println("[WARNING] OAS ingestion: Could not find triples on OCs, SSD is probably not correctly ingested.");
                            }

                            while (resultsrw.hasNext()) {
                                QuerySolution soln = resultsrw.next();                      
                                try {
                                    if (soln != null) {
                                        try {                                       
                                            if (soln.get("x").isResource()){
                                                if (soln.getResource("x") != null) {
                                                    if (tarList.contains(soln.getResource("x").toString())) {                           
                                                        answer.add(das.getObject(soln.getResource("x").toString()).getEntityLabel());
                                                        AnnotationLog.println("PATH: DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"", fileName);
                                                        dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    } else {
                                                        if (soln.get("o").isResource()){
                                                            if (soln.getResource("o") != null) {
                                                                if (tarList.contains(soln.getResource("o").toString())) {
                                                                    answer.add(das.getObject(soln.getResource("o").toString()).getEntityLabel());
                                                                    dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        } else if (soln.get("o").isLiteral()) {
                                                            if (soln.getLiteral("o") != null) {
                                                                if (refList.containsKey(soln.getLiteral("o").toString())) {
                                                                    answer.add(refList.get(soln.getLiteral("o").toString()));
                                                                    AnnotationLog.printException("PATH: DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"", fileName);
                                                                    dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else if (soln.get("x").isLiteral()) {
                                                if (soln.getLiteral("x") != null) {
                                                    if (refList.containsKey(soln.getLiteral("x").toString())) {
                                                        answer.add(refList.get(soln.getLiteral("x").toString()));
                                                        AnnotationLog.println("PATH: DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"", fileName);
                                                        dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    }
                                                }
                                            }

                                        } catch (Exception e1) {
                                            return;
                                        }
                                    }
                                } catch (Exception e) {
                                    System.out.println("[ERROR] : " + e.getMessage());
                                }
                            }
                        }

                    }
                    if (found == false) {
                        AnnotationLog.println("PATH: DASO: " + i.getLabel() + " Path connections can not be found ! check the SDD definition. ", fileName);
                    }
                } else {
                    AnnotationLog.println("PATH: Skipped :" + i.getLabel(), fileName);
                }
            }
            //insert the triples

            for (String uri : dasoPL.keySet()) {
                String insert = "";
                insert += NameSpaces.getInstance().printSparqlNameSpaceList();
                insert += "INSERT DATA {  ";
                insert += "<" + uri + ">" + " hasco:hasRoleLabel  \"" + dasoPL.get(uri) + "\" . ";
                insert += "} ";

                try {
                    UpdateRequest request = UpdateFactory.create(insert);
                    UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                            request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
                    processor.execute();
                } catch (QueryParseException e) {
                    System.out.println("QueryParseException due to update query: " + insert);
                    throw e;
                }
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
        DPL dpl = new DPL(file);
        Map<String, String> mapCatalog = dpl.getCatalog();
        
        GeneratorChain chain = new GeneratorChain();
        for (String key : mapCatalog.keySet()) {
            if (mapCatalog.get(key).length() > 0) {
                String sheetName = mapCatalog.get(key).replace("#", "");
                RecordFile sheet = new SpreadsheetRecordFile(file.getFile(), sheetName);
                chain.addGenerator(new DPLGenerator(sheet));
            }
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

    public static GeneratorChain annotateOASFile(RecordFile file, boolean bGenerate) {
        GeneratorChain chain = new GeneratorChain();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String startTime = isoFormat.format(new Date());
        chain.addGenerator(new OASGenerator(file, startTime));

        return chain;
    }

    public static GeneratorChain annotateSDDFile(RecordFile file) {
        System.out.println("Processing data acquisition schema file ...");

        SDD sdd = new SDD(file);
        String fileName = file.getFileName();
        String sddName = sdd.getName();
        if (sddName == "") {
            AnnotationLog.printException("This SDD has no Study_ID filled.", fileName);
        }
        Map<String, String> mapCatalog = sdd.getCatalog();

        RecordFile codeMappingRecordFile = null;
        RecordFile dictionaryRecordFile = null;
        RecordFile codeBookRecordFile = null;
        RecordFile timelineRecordFile = null;

        File codeMappingFile = null;

        if (file.getFileName().endsWith(".csv")) {
            String prefix = "sddtmp/" + file.getFileName().replace(".csv", "");
            File dictionaryFile = sdd.downloadFile(mapCatalog.get("Data_Dictionary"), prefix + "-dd.csv");
            File codeBookFile = sdd.downloadFile(mapCatalog.get("Codebook"), prefix + "-codebook.csv");
            File timelineFile = sdd.downloadFile(mapCatalog.get("Timeline"), prefix + "-timeline.csv");
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), prefix + "-code-mappings.csv");

            dictionaryRecordFile = new CSVRecordFile(dictionaryFile);
            codeBookRecordFile = new CSVRecordFile(codeBookFile);
            timelineRecordFile = new CSVRecordFile(timelineFile);
        } else if (file.getFileName().endsWith(".xlsx")) {
            codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), 
                    "sddtmp/" + file.getFileName().replace(".xlsx", "") + "-code-mappings.csv");

            codeBookRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Codebook").replace("#", ""));
            dictionaryRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Data_Dictionary").replace("#", ""));
            timelineRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Timeline").replace("#", ""));
        }

        if (null != codeMappingFile) {
            codeMappingRecordFile = new CSVRecordFile(codeMappingFile);
            if (!sdd.readCodeMapping(codeMappingRecordFile)) {
                AnnotationLog.printWarning(String.format("The CodeMapping of this SDD is empty. ", fileName), fileName);
            } else {
                AnnotationLog.println(String.format("Codemappings: " + sdd.getCodeMapping().get("U"), fileName), fileName);
            }
        } else {
            AnnotationLog.printWarning(String.format("Failed to download the CodeMapping of this SDD.", fileName), fileName);
        }
        
        if (!sdd.readDataDictionary(dictionaryRecordFile)) {
            AnnotationLog.printException("Read Data Dictionary failed, please refer to the error msg above.", file.getFileName());
            return null;
        }
        if (!sdd.readCodebook(codeBookRecordFile)) {
            AnnotationLog.printWarning(String.format("The Codebook of this SDD is either invalid or empty. ", fileName), fileName);
        }
        if (!sdd.readTimeline(timelineRecordFile)) {
            AnnotationLog.println("The TimeLine of this SDD is empty.", file.getFileName());
        }

        GeneratorChain chain = new GeneratorChain();
        if (codeBookRecordFile.isValid()) {
            chain.addGenerator(new PVGenerator(codeBookRecordFile, sddName, sdd.getMapAttrObj(), sdd.getCodeMapping()));
        }

        if (dictionaryRecordFile.isValid()) {
            chain.addGenerator(new DASchemaAttrGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping(), sdd.readDDforEAmerge(dictionaryRecordFile)));
            chain.addGenerator(new DASchemaObjectGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            chain.addGenerator(new DASchemaEventGenerator(dictionaryRecordFile, sdd.getTimeLine(), sddName, sdd.getCodeMapping()));
        }

        GeneralGenerator generalGenerator = new GeneralGenerator(file, "DASchema");
        String sddUri = ConfigProp.getKbPrefix() + "DAS-" + sddName;
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", sddUri);
        AnnotationLog.println("This SDD is assigned with uri: " + sddUri + " and is of type hasco:DASchema", file.getFileName());
        row.put("a", "hasco:DASchema");
        row.put("rdfs:label", "Schema for " + sddName);
        row.put("rdfs:comment", "");
        generalGenerator.addRow(row);
        chain.addGenerator(generalGenerator);
        chain.setNamedGraphUri(URIUtils.replacePrefixEx(sddUri));

        return chain;
    }

    public static GeneratorChain annotateSSDFile(RecordFile file) {
        String Pilot_Num = file.getFileName().replaceAll("SSD-", "");
        System.out.println("Processing SSD file of " + Pilot_Num + "...");

        SSD ssd = new SSD(file);
        String file_name = file.getFileName();
        String ssdName = ssd.getNameFromFileName();
        Map<String, String> mapCatalog = ssd.getCatalog();
        Map<String, List<String>> mapContent = ssd.getMapContent();

        RecordFile SSDsheet = new SpreadsheetRecordFile(file.getFile(), "SSD");

        SSDGeneratorChain chain = new SSDGeneratorChain();

        if (SSDsheet.isValid()) {
            SSDGenerator gen = new SSDGenerator(SSDsheet);
            String studyUri = gen.getStudyUri();
            chain.addGenerator(gen);
            if (studyUri == null || studyUri == "") {
            	return null;
            } else {
                chain.setStudyUri(studyUri);
                Study study = Study.find(studyUri);
                if (study != null) {
                    AnnotationLog.println("SSD ingestion: The study uri :" + studyUri + " is in the TS.", file_name);
                } else {
                    AnnotationLog.printException("SSD ingestion: Could not find the study uri : " + studyUri + " in the TS, check the study uri in the SSD sheet.", file_name);
                    return null;
                }
            }

            chain.setRecordFile(file);
        } else {
            //chain.setInvalid();
            AnnotationLog.printException("Cannot locate SSD's sheet ", file.getFileName());
        }

        String study_uri = chain.getStudyUri();
        for (String i : mapCatalog.keySet()) {
            if (mapCatalog.get(i).length() > 0) {
                RecordFile SOsheet = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get(i).replace("#", ""));
                chain.addGenerator(new StudyObjectGenerator(SOsheet, mapContent.get(i), mapContent, study_uri));
            }
        }

        return chain;
    }

    public static GeneratorChain annotateDAFile(DataFile dataFile, RecordFile recordFile) {
        System.out.println("annotateDAFile: [" + dataFile.getFileName() + "]");

        GeneratorChain chain = new GeneratorChain();

        String fileName = dataFile.getFileName();

        ObjectAccessSpec oas = null;
        String oas_uri = null;
        String deployment_uri = null;
        String schema_uri = null;

        if (dataFile != null) {
            oas = ObjectAccessSpec.findByUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
            if (oas != null) {
                if (!oas.isComplete()) {
                    AnnotationLog.printWarning(String.format("Specification of associated Object Access Specification is incomplete: %s", fileName), fileName);
                    chain.setInvalid();
                } else {
                    AnnotationLog.println(String.format("Specification of associated Object Access Specification is complete: %s", fileName), fileName);
                }
                oas_uri = oas.getUri();
                deployment_uri = oas.getDeploymentUri();
                schema_uri = oas.getSchemaUri();
            } else {
                AnnotationLog.printWarning(String.format("Cannot find associated Object Access Specification: %s", fileName), fileName);
                chain.setInvalid();
            }
        }

        if (oas_uri == null) {
            AnnotationLog.printException(String.format("Cannot find target data acquisition: %s", fileName), fileName);
            chain.setInvalid();
        } else {
            AnnotationLog.println(String.format("Found target data acquisition: %s", fileName), fileName);
        }
        if (schema_uri == null) {
            AnnotationLog.printException(String.format("Cannot load schema specified for data acquisition: %s", fileName), fileName);
            chain.setInvalid();
        } else {
            AnnotationLog.println(String.format("Schema %s specified for data acquisition: %s", schema_uri, fileName), fileName);
        }
        if (deployment_uri == null) {
            AnnotationLog.printException(String.format("Cannot load deployment specified for data acquisition: %s", fileName), fileName);
            chain.setInvalid();
        } else {
            try {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                AnnotationLog.printException(String.format("URL decoding error for deployment uri %s", deployment_uri), fileName);
                chain.setInvalid();
            }
            AnnotationLog.println(String.format("Deployment %s specified for data acquisition %s", deployment_uri, fileName), fileName);
        }

        if (oas != null) {
            dataFile.setStudyUri(oas.getStudyUri());
            dataFile.setDatasetUri(DataFactory.getNextDatasetURI(oas.getUri()));
            oas.addDatasetUri(dataFile.getDatasetUri());

            chain.addGenerator(new MeasurementGenerator(recordFile, oas, dataFile));
            chain.setNamedGraphUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
        }

        return chain;
    }
}
