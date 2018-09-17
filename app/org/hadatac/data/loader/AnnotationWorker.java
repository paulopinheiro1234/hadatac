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
import org.hadatac.utils.Feedback;
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
                        log.addline(Feedback.println(Feedback.WEB, "[ERROR] Missing InfoSheet. "));
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
            } else if (file_name.startsWith("OAS-")) {
                checkOASFile(recordFile);
                chain = annotateOASFile(recordFile, true);
            } else if (file_name.startsWith("SDD-")) {
                if (file_name.endsWith(".xlsx")) {
                    recordFile = new SpreadsheetRecordFile(new File(filePath), "InfoSheet");
                    if (!recordFile.isValid()) {
                        log.addline(Feedback.println(Feedback.WEB, 
                                "[ERROR] The Info sheet is missing in this SDD file. "));
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
                String study = URIUtils.getBaseName(chain.getStudyUri());
                String new_path = "";
                if (study.isEmpty()) {
                    new_path = path_proc;
                } else {
                    new_path = path_proc + "/" + study;
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
                
                File f = new File(path_unproc + "/" + file_name);
                f.renameTo(new File(destFolder + "/" + file_name));
                f.delete();
                
                log = AnnotationLog.find(file_name);
                if (null != log) {
                    AnnotationLog new_log = new AnnotationLog(file.getFileName());
                    new_log.setLog(log.getLog());
                    new_log.save();
                    log.delete();
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
        String studyUri = ConfigProp.getKbPrefix() + "STD-" + studyName;
        studyUri = URIUtils.replacePrefixEx(studyUri);
        
        AnnotationLog log = AnnotationLog.create(recordFile.getFile().getName());

        log.addline(Feedback.println(Feedback.WEB, "[OK] Study ID found: " + studyName));
        log.addline(Feedback.println(Feedback.WEB, "[OK] Study URI found: " + studyUri));

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

        Map<String, String> labelList = ObjectCollection.labelsByStudyUri(studyUri);
        //              for (Map.Entry entry : labelList.entrySet()) {
        //                  log.addline(Feedback.println(Feedback.WEB, "[OK] Label(s) found: " + entry.getKey() + ", " + entry.getValue()));
        //              }

        String das_uri = URIUtils.convertToWholeURI(ConfigProp.getKbPrefix() + "DAS-" + record.getValueByColumnName("data dict").replace("SDD-", ""));
        System.out.println("das_uri " + das_uri);
        DataAcquisitionSchema das = DataAcquisitionSchema.find(das_uri);
        if (das == null) {
            log.addline(Feedback.println(Feedback.WEB, 
                    "[ERROR] The SDD of study " + record.getValueByColumnName("Study ID") + " can not be found. Check if it is already ingested."));
        } else {
            //                  List<DataAcquisitionSchemaObject> loo = das.getObjects();
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
            log.addline(Feedback.println(Feedback.WEB, 
                    "[PATH COMPUTATION] The number of DASOs to be computed: " + loo2.toString()));

            for (DataAcquisitionSchemaObject i : loo) {
                if (i.getEntityLabel() == null || i.getEntityLabel().length() == 0) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            "[ERROR] The Entity Label of DASO : " + i.getLabel() + " can not be found. Check SDD."));
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
                                                        log.addline(Feedback.println(Feedback.WEB, 
                                                                "[PATH] DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\""));
                                                        dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    } else {
                                                        if (soln.get("o").isResource()){
                                                            if (soln.getResource("o") != null) {
                                                                if (tarList.contains(soln.getResource("o").toString())) {
                                                                    answer.add(das.getObject(soln.getResource("o").toString()).getEntityLabel());
                                                                    log.addline(Feedback.println(Feedback.WEB, 
                                                                            "[PATH] DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\""));
                                                                    dasoPL.put(i.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        } else if (soln.get("o").isLiteral()) {
                                                            if (soln.getLiteral("o") != null) {
                                                                if (refList.containsKey(soln.getLiteral("o").toString())) {
                                                                    answer.add(refList.get(soln.getLiteral("o").toString()));
                                                                    log.addline(Feedback.println(Feedback.WEB, 
                                                                            "[PATH] DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\""));
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
                                                        log.addline(Feedback.println(Feedback.WEB, 
                                                                "[PATH] DASO: " + i.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\""));
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
                        log.addline(Feedback.println(Feedback.WEB, 
                                "[PATH] DASO: " + i.getLabel() + " Path connections can not be found ! check the SDD definition. "));
                    }
                } else {
                    log.addline(Feedback.println(Feedback.WEB, 
                            "[PATH] Skipped :" + i.getLabel()));
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

    public static GeneratorChain annotateOASFile(RecordFile file, boolean bGenerate) {
        GeneratorChain chain = new GeneratorChain();
        DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        String startTime = isoFormat.format(new Date());
        chain.addGenerator(new OASGenerator(file, startTime));

        return chain;
    }

    public static GeneratorChain annotateDataAcquisitionSchemaFile(RecordFile file) {
        System.out.println("Processing data acquisition schema file ...");

        SDD sdd = new SDD(file);
        String file_name = file.getFile().getName();
        AnnotationLog log = AnnotationLog.create(file_name);
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

        if(!sdd.readCodeMapping(codeMappingRecordFile)){
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[WARNING] The CodeMapping of this SDD is empty. ", file_name)));
        } else {
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[Codemappings] " + sdd.getCodeMapping().get("U"), file_name)));
        }
        if(!sdd.readDataDictionary(dictionaryRecordFile)){
            AnnotationLog.printException("Read Data Dictionary failed, please refer to the error msg above.", file.getFile().getName());
            return null;
        }
        if(!sdd.readCodebook(codeBookRecordFile)){
            log.addline(Feedback.println(Feedback.WEB, 
                    String.format("[WARNING] The Codebook of this SDD is either invalid or empty. ", file_name)));
        }
        if(!sdd.readTimeline(timelineRecordFile)){
            AnnotationLog.println("The TimeLine of this SDD is empty.", file.getFile().getName());
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

        SSD ssd = new SSD(file);
        String file_name = file.getFile().getName();
        String ssdName = ssd.getNameFromFileName();
        Map<String, String> mapCatalog = ssd.getCatalog();
        Map<String, List<String>> mapContent = ssd.getMapContent();

        RecordFile SSDsheet = new SpreadsheetRecordFile(file.getFile(), "SSD");

        //System.out.println(file_name);
        //System.out.println(ssdName);

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
            AnnotationLog.printException("Cannot locate SSD's sheet ", file.getFile().getName());
        }

        String study_uri = chain.getStudyUri();
        for (String i : mapCatalog.keySet()) {
            if (mapCatalog.get(i).length()>0){
                RecordFile SOsheet = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get(i).replace("#", ""));
                //System.out.println(SOsheet.getSheetName() + " is parsed!");
                chain.addGenerator(new StudyObjectGenerator(SOsheet, mapContent.get(i), mapContent, study_uri));
                //System.out.println(SOsheet.getNumberOfSheets() + " number of sheets!");
                //System.out.println(SOsheet.getHeaders() + " number of sheets!");            	
                //for (Record ii : SOsheet.getRecords()){
                //    System.out.println(ii.getValueByColumnIndex(0) + " is added to chain!");	
                //}
            }
        }

        return chain;
    }

    public static GeneratorChain annotateDAFile(DataFile dataFile, RecordFile recordFile) {
        System.out.println("annotateDAFile: [" + dataFile.getFileName() + "]");

        GeneratorChain chain = new GeneratorChain();

        String file_name = dataFile.getFileName();
        AnnotationLog log = AnnotationLog.create(file_name);

        ObjectAccessSpec oas = null;
        String oas_uri = null;
        String deployment_uri = null;
        String schema_uri = null;

        if (dataFile != null) {
            oas = ObjectAccessSpec.findByUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
            if (oas != null) {
                if (!oas.isComplete()) {
                    log.addline(Feedback.println(Feedback.WEB, 
                            String.format("[WARNING] Specification of associated Object Access Specification is incomplete: %s", file_name)));
                    chain.setInvalid();
                } else {
                    log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Specification of associated Object Access Specification is complete: %s", file_name)));
                }
                oas_uri = oas.getUri();
                deployment_uri = oas.getDeploymentUri();
                schema_uri = oas.getSchemaUri();
            } else {
                log.addline(Feedback.println(Feedback.WEB, 
                        String.format("[WARNING] Cannot find associated Object Access Specification: %s", file_name)));
                chain.setInvalid();
            }
        }

        if (oas_uri == null) {
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

        if (oas != null) {
            dataFile.setStudyUri(oas.getStudyUri());
            dataFile.setDatasetUri(DataFactory.getNextDatasetURI(oas.getUri()));
            oas.addDatasetUri(dataFile.getDatasetUri());

            chain.addGenerator(new MeasurementGenerator(recordFile, oas, dataFile));
        }

        return chain;
    }
}
