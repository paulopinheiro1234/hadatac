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
import org.hadatac.console.controllers.annotator.AnnotationLogger;
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

    public static GeneratorChain getGeneratorChain(
            String fileName, DataFile dataFile, RecordFile recordFile) {
        GeneratorChain chain = null;
        AnnotationLogger logger = AnnotationLogger.getLogger(fileName);

        if (fileName.startsWith("DA-")) {
            chain = annotateDAFile(dataFile, recordFile);
        } else if (fileName.startsWith("STD-")) {
            chain = annotateStudyIdFile(recordFile);
        } else if (fileName.startsWith("DPL-")) {
            if (fileName.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(recordFile.getFile(), "InfoSheet");
                if (!recordFile.isValid()) {
                    logger.printExceptionById("DPL_00001");
                    return null;
                }
            }
            chain = annotateDPLFile(recordFile);
        //} else if (fileName.startsWith("ACQ-")) {
        //    chain = annotateACQFile(recordFile, true);
        } else if (fileName.startsWith("OAS-")) {
            checkOASFile(recordFile);
            chain = annotateOASFile(recordFile, true);
        } else if (fileName.startsWith("SDD-")) {
            if (fileName.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(recordFile.getFile(), "InfoSheet");
                if (!recordFile.isValid()) {
                    AnnotationLogger.getLogger(fileName).printExceptionById("SDD_00001");
                    return null;
                }
            }
            chain = annotateSDDFile(recordFile);
        } else if (fileName.startsWith("SSD-")) {
            chain = annotateSSDFile(recordFile);
        } else {
            AnnotationLogger.getLogger(fileName).printExceptionById("GBL_00001");
            return null;
        }

        return chain;
    }

    public static void autoAnnotate() {
        if(ConfigProp.getPropertyValue("autoccsv.config", "auto").equals("off")) {
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

        for (DataFile dataFile : unprocFiles) {
            dataFile.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.save();

            String fileName = dataFile.getFileName();
            String filePath = pathUnproc + "/" + fileName;

            if (procFiles.contains(dataFile)) {
                AnnotationLogger.getLogger(fileName).printExceptionByIdWithArgs("GBL_00002", fileName);
                return;
            }

            AnnotationLogger.getLogger(fileName).println(String.format("Processing file: %s", fileName));

            RecordFile recordFile = null;
            File file = new File(filePath);
            if (fileName.endsWith(".csv")) {
                recordFile = new CSVRecordFile(file);
            } else if (fileName.endsWith(".xlsx")) {
                recordFile = new SpreadsheetRecordFile(file);
            } else {
                AnnotationLogger.getLogger(fileName).printExceptionByIdWithArgs("GBL_00003", fileName);
                return;
            }

            boolean bSucceed = false;
            GeneratorChain chain = getGeneratorChain(fileName, dataFile, recordFile);

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

                dataFile.delete();

                dataFile.setStatus(DataFile.PROCESSED);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                if (study.isEmpty()) {
                    dataFile.setFileName(dataFile.getFileName());
                    dataFile.setStudyUri("");
                } else {
                    dataFile.setFileName(study + "/" + dataFile.getFileName());
                    dataFile.setStudyUri(chain.getStudyUri());
                }
                dataFile.save();

                File f = new File(pathUnproc + "/" + fileName);
                f.renameTo(new File(destFolder + "/" + fileName));
                f.delete();

                AnnotationLogger logger = AnnotationLogger.getLogger(fileName);
                if (null != logger) {
                    AnnotationLogger newLog = AnnotationLogger.getLogger(dataFile.getFileName());
                    newLog.setLog(logger.getLog());
                    logger.delete();
                    newLog.save();
                }
            } else {
                // Freeze file
                System.out.println("Freezed file " + dataFile.getFileName());
                dataFile.setStatus(DataFile.FREEZED);
                dataFile.save();
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

        AnnotationLogger.getLogger(fileName).println("Study ID found: " + studyName);
        AnnotationLogger.getLogger(fileName).println("Study URI found: " + studyUri);

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
            AnnotationLogger.getLogger(fileName).printExceptionByIdWithArgs("OAS_00001", record.getValueByColumnName("Study ID"));
        } else {
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
            AnnotationLogger.getLogger(fileName).println("PATH COMPUTATION: The number of DASOs to be computed: " + loo2.toString());

            for (DataAcquisitionSchemaObject daso : loo) {
                if (null == daso) {
                    continue;
                }

                if (daso.getEntityLabel() == null || daso.getEntityLabel().length() == 0) {
                    AnnotationLogger.getLogger(fileName).printExceptionByIdWithArgs("OAS_00002", daso.getLabel());
                } else if (!refList.containsKey(daso.getLabel())) {

                    List<String> answer = new ArrayList<String>();
                    answer.add(daso.getEntityLabel());
                    Boolean found = false;

                    for (String j : refList.keySet()) {

                        if (found == false) {
                            String target = kbPrefix + "DASO-" + studyName + "-" + j.trim().replace(" ","").replace("_","-").replace("??", "");
                            String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                                    "SELECT ?x ?o WHERE { \n" + 
                                    "<" + daso.getUri() + "> ?p ?x . \n" + 
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
                                                        AnnotationLogger.getLogger(fileName).println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                        dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                        found = true;
                                                        break;
                                                    } else {
                                                        if (soln.get("o").isResource()){
                                                            if (soln.getResource("o") != null) {
                                                                if (tarList.contains(soln.getResource("o").toString())) {
                                                                    answer.add(das.getObject(soln.getResource("o").toString()).getEntityLabel());
                                                                    dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
                                                                    found = true;
                                                                    break;
                                                                }
                                                            }
                                                        } else if (soln.get("o").isLiteral()) {
                                                            if (soln.getLiteral("o") != null) {
                                                                if (refList.containsKey(soln.getLiteral("o").toString())) {
                                                                    answer.add(refList.get(soln.getLiteral("o").toString()));
                                                                    AnnotationLogger.getLogger(fileName).println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                                    dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
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
                                                        AnnotationLogger.getLogger(fileName).println("PATH: DASO: " + daso.getLabel() + ": \"" + answer.get(1) + " " + answer.get(0) + "\"");
                                                        dasoPL.put(daso.getUri(), answer.get(1) + " " + answer.get(0));
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
                                    AnnotationLogger.getLogger(fileName).printException(e.getMessage());
                                }
                            }
                        }

                    }
                    if (found == false) {
                        AnnotationLogger.getLogger(fileName).println("PATH: DASO: " + daso.getLabel() + " Path connections can not be found ! check the SDD definition. ");
                    }
                } else {
                    AnnotationLogger.getLogger(fileName).println("PATH: Skipped :" + daso.getLabel());
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
            AnnotationLogger.getLogger(fileName).printExceptionById("SDD_00003");
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

            if (mapCatalog.get("Codebook") != null) { 
                codeBookRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Codebook").replace("#", ""));
            }
            dictionaryRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Data_Dictionary").replace("#", ""));
            if (mapCatalog.get("Timeline") != null) {
                timelineRecordFile = new SpreadsheetRecordFile(file.getFile(), mapCatalog.get("Timeline").replace("#", ""));
            }
        }

        if (null != codeMappingFile) {
            codeMappingRecordFile = new CSVRecordFile(codeMappingFile);
            if (!sdd.readCodeMapping(codeMappingRecordFile)) {
                AnnotationLogger.getLogger(fileName).printWarning(String.format("The CodeMapping of this SDD is empty. ", fileName));
            } else {
                AnnotationLogger.getLogger(fileName).println(String.format("Codemappings: " + sdd.getCodeMapping().get("U"), fileName));
            }
        } else {
            AnnotationLogger.getLogger(fileName).printWarning(String.format("Failed to download the CodeMapping of this SDD.", fileName));
        }

        if (!sdd.readDataDictionary(dictionaryRecordFile)) {
            AnnotationLogger.getLogger(file.getFileName()).printExceptionById("SDD_00004");
            return null;
        }
        if (codeBookRecordFile == null || !sdd.readCodebook(codeBookRecordFile)) {
            AnnotationLogger.getLogger(fileName).printWarningById("SDD_00005");
        }
        if (timelineRecordFile == null || !sdd.readTimeline(timelineRecordFile)) {
            AnnotationLogger.getLogger(file.getFileName()).printWarningById("SDD_00006");
        }

        GeneratorChain chain = new GeneratorChain();
        if (codeBookRecordFile != null && codeBookRecordFile.isValid()) {
            chain.addGenerator(new PVGenerator(codeBookRecordFile, sddName, sdd.getMapAttrObj(), sdd.getCodeMapping()));
        }

        if (dictionaryRecordFile.isValid()) {
            chain.addGenerator(new DASchemaAttrGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping(), sdd.readDDforEAmerge(dictionaryRecordFile)));
            chain.addGenerator(new DASchemaObjectGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping()));
            //chain.addGenerator(new DASchemaEventGenerator(dictionaryRecordFile, sdd.getTimeLine(), sddName, sdd.getCodeMapping()));
        }

        GeneralGenerator generalGenerator = new GeneralGenerator(file, "DASchema");
        String sddUri = ConfigProp.getKbPrefix() + "DAS-" + sddName;
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", sddUri);
        AnnotationLogger.getLogger(file.getFileName()).println("This SDD is assigned with uri: " + sddUri + " and is of type hasco:DASchema");
        row.put("a", "hasco:DASchema");
        row.put("rdfs:label", "SDD-" + sddName);
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

            VirtualColumnGenerator vcgen = new VirtualColumnGenerator(SSDsheet, file_name);
            chain.addGenerator(vcgen);
            
            SSDGenerator socgen = new SSDGenerator(SSDsheet, file_name);
            chain.addGenerator(socgen);

            String studyUri = socgen.getStudyUri();
            if (studyUri == null || studyUri == "") {
                return null;
            } else {
                chain.setStudyUri(studyUri);
                Study study = Study.find(studyUri);
                if (study != null) {
                    AnnotationLogger.getLogger(file_name).println("SSD ingestion: The study uri :" + studyUri + " is in the TS.");
                } else {
                    AnnotationLogger.getLogger(file_name).printException("SSD ingestion: Could not find the study uri : " + studyUri + " in the TS, check the study uri in the SSD sheet.");
                    return null;
                }
            }

            chain.setRecordFile(file);

        } else {
            //chain.setInvalid();
            AnnotationLogger.getLogger(file.getFileName()).printException("Cannot locate SSD's sheet ");
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
                    AnnotationLogger.getLogger(fileName).printWarning(String.format("Specification of associated Object Access Specification is incomplete: %s", fileName));
                    chain.setInvalid();
                } else {
                    AnnotationLogger.getLogger(fileName).println(String.format("Specification of associated Object Access Specification is complete: %s", fileName));
                }
                oas_uri = oas.getUri();
                deployment_uri = oas.getDeploymentUri();
                schema_uri = oas.getSchemaUri();
            } else {
                AnnotationLogger.getLogger(fileName).printWarning(String.format("Cannot find associated Object Access Specification: %s", fileName));
                chain.setInvalid();
            }
        }

        if (oas_uri == null) {
            AnnotationLogger.getLogger(fileName).printException(String.format("Cannot find target data acquisition: %s", fileName));
            chain.setInvalid();
        } else {
            AnnotationLogger.getLogger(fileName).println(String.format("Found target data acquisition: %s", fileName));
        }
        if (schema_uri == null) {
            AnnotationLogger.getLogger(fileName).printException(String.format("Cannot load schema specified for data acquisition: %s", fileName));
            chain.setInvalid();
        } else {
            AnnotationLogger.getLogger(fileName).println(String.format("Schema %s specified for data acquisition: %s", schema_uri, fileName));
        }
        if (deployment_uri == null) {
            AnnotationLogger.getLogger(fileName).printException(String.format("Cannot load deployment specified for data acquisition: %s", fileName));
            chain.setInvalid();
        } else {
            try {
                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                AnnotationLogger.getLogger(fileName).printException(String.format("URL decoding error for deployment uri %s", deployment_uri));
                chain.setInvalid();
            }
            AnnotationLogger.getLogger(fileName).println(String.format("Deployment %s specified for data acquisition %s", deployment_uri, fileName));
        }

        if (oas != null) {
            dataFile.setStudyUri(oas.getStudyUri());
            dataFile.setDatasetUri(DataFactory.getNextDatasetURI(oas.getUri()));
            oas.addDatasetUri(dataFile.getDatasetUri());

            DataAcquisitionSchema schema = DataAcquisitionSchema.find(oas.getSchemaUri());
            if (schema == null) {
                AnnotationLogger.getLogger(fileName).printException(String.format("Schema %s cannot be found", oas.getSchemaUri()));
                chain.setInvalid();
            }

            // Need to be fixed here by getting codeMap and codebook from sparql query
            DASOInstanceGenerator dasoInstanceGen = new DASOInstanceGenerator(recordFile, oas.getStudyUri(), oas.getUri(), schema, dataFile.getFileName());

            chain.addGenerator(dasoInstanceGen);
            chain.addGenerator(new MeasurementGenerator(recordFile, oas, schema, dataFile, dasoInstanceGen));
            chain.setNamedGraphUri(URIUtils.replacePrefixEx(dataFile.getDataAcquisitionUri()));
        }

        return chain;
    }
}
