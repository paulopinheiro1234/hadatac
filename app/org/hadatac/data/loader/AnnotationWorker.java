package org.hadatac.data.loader;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.lang.String;
import java.net.URLDecoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.hadatac.console.controllers.annotator.AnnotationLog;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.entity.pojo.Credential;
import org.hadatac.entity.pojo.DASVirtualObject;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.SDD;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;

import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;

import java.lang.Exception;

public class AnnotationWorker {
	public static Map<String, List<DASVirtualObject>> templateLibrary = new HashMap<String, List<DASVirtualObject>>();
	public static String study_id = "default-study";
	public static final String kbPrefix = ConfigProp.getKbPrefix();
	
	public AnnotationWorker() {}
    
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
			log.save();
			
			RecordFile recordFile = null;
			if (file_name.endsWith(".csv")) {
				recordFile = new CSVRecordFile(new File(filePath));
			} else if (file_name.endsWith(".xlsx")) {
				recordFile = new SpreadsheetRecordFile(new File(filePath));
			} else {
				log.addline(Feedback.println(Feedback.WEB, String.format(
						"[ERROR] Unknown file format: %s", file_name)));
				log.save();
				return;
			}
			
			boolean bSucceed = false;
			if (file_name.startsWith("DA")) {
				bSucceed = annotateDAFile(file, recordFile);
			}
			else if (file_name.startsWith("PID")) {
				bSucceed = annotateSubjectIdFile(recordFile);
			}
			else if (file_name.startsWith("STD")) {
				bSucceed = annotateStudyIdFile(recordFile);
			}
			else if (file_name.startsWith("MAP")) {
				bSucceed = annotateMapFile(recordFile);
			}
			else if (file_name.startsWith("ACQ")) {
				bSucceed = annotateACQFile(recordFile);
			}
			else if (file_name.startsWith("SDD")) {
				bSucceed = annotateDataAcquisitionSchemaFile(recordFile);
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
	
	public static boolean annotateMapFile(RecordFile file) {
		boolean bSuccess = true;	
		try {
			SampleSubjectMapper mapper = new SampleSubjectMapper(file);
			mapper.createRows();
		} catch (Exception e) {
			e.printStackTrace();
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}
		
		try {
			SampleSubjectMapper mapper = new SampleSubjectMapper(file);
			System.out.println("Start Update Mappings:");
			bSuccess = mapper.updateMappings();
		} catch (Exception e) {
			e.printStackTrace();
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}
		
		return bSuccess;
	}

	public static boolean annotateStudyIdFile(RecordFile file) {		
		boolean bSuccess = true;
		try {
			StudyGenerator studyGenerator = new StudyGenerator(file);
			bSuccess = commitRows(studyGenerator.createRows(), studyGenerator.toString(), 
					file.getFile().getName(), "Study", true);
		} catch (Exception e) {
			System.out.println("Error: annotateStudyIdFile() - Unable to generate study");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}
		try {
			SampleCollectionGenerator sampleCollectionGenerator = new SampleCollectionGenerator(file);
			bSuccess = commitRows(sampleCollectionGenerator.createRows(), sampleCollectionGenerator.toString(), 
					file.getFile().getName(), "SampleCollection", true);

		} catch (Exception e) {
			System.out.println("Error: annotateStudyIdFile() - Unable to generate Sample Collection");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}    	
		try {
			AgentGenerator agentGenerator = new AgentGenerator(file);
			bSuccess = commitRows(agentGenerator.createRows(), agentGenerator.toString(), 
					file.getFile().getName(), "Agent", true);

		} catch (Exception e) {
			System.out.println("Error: annotateStudyIdFile() - Unable to generate Agent");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}

		return bSuccess;
	}

	public static boolean annotateSampleIdFile(RecordFile file) {
		try {
			SampleGenerator sampleGenerator = new SampleGenerator(file);
			sampleGenerator.createRows();
		} catch (Exception e) {
			System.out.println("Error: annotateSampleIdFile() - Unable to generate Sample");
			e.printStackTrace();
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}

		return true;
	}

	public static boolean annotateSubjectIdFile(RecordFile file) {
		try {
			SubjectGenerator subjectGenerator = new SubjectGenerator(file);
			subjectGenerator.createRows();
		}
		catch (Exception e) {
			System.out.println("Error: annotateSubjectIdFile() - Unable to generate Subject");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}
		
		return true;
	}
	
	public static boolean annotateACQFile(RecordFile file) {
		boolean bSuccess = true;
		try {
			GeneralGenerator generalGenerator = new GeneralGenerator();
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("hasURI", kbPrefix + "INS-GENERIC-PHYSICAL-INSTRUMENT");
			row.put("a", "vstoi:PhysicalInstrument");
			row.put("rdfs:label", "Generic Physical Instrument");
			generalGenerator.addRow(row);

			row = new HashMap<String, Object>();
			row.put("hasURI", kbPrefix + "INS-GENERIC-QUESTIONNAIRE");
			row.put("a", "hasco:Questionnaire");
			row.put("rdfs:label", "Generic Questionnaire");
			generalGenerator.addRow(row);
			bSuccess = commitRows(generalGenerator.getRows(), generalGenerator.toString(), 
					file.getFile().getName(), "Instrument", true);

			DateFormat isoFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
			String startTime = isoFormat.format(new Date());
			
			try {
				DeploymentGenerator deploymentGenerator = new DeploymentGenerator(file, startTime);
				bSuccess = commitRows(deploymentGenerator.createRows(), deploymentGenerator.toString(), 
						file.getFile().getName(), "Deployment", true);
			} catch (Exception e) {
				System.out.println("Error in annotateDataAcquisitionFile: Deployment Generator");
				AnnotationLog.printException(e, file.getFile().getName());
				return false;
			}
			try {
				DataAcquisitionGenerator daGenerator = new DataAcquisitionGenerator(file, startTime);
				bSuccess = commitRows(daGenerator.createRows(), daGenerator.toString(), 
						file.getFile().getName(), "DataAcquisition", true);
			} catch (Exception e) {
				System.out.println("Error in annotateDataAcquisitionFile: Data Acquisition Generator");
				AnnotationLog.printException(e, file.getFile().getName());
				return false;
			}
		} catch (Exception e) {
			System.out.println("Error in annotateDataAcquisitionFile");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}
		
		return bSuccess;
	}

	public static boolean annotateDataAcquisitionSchemaFile(RecordFile file) {
		System.out.println("Processing data acquisition schema file ...");
		
		String file_name = file.getFile().getName();
		boolean bSuccess = true;
		SDD sdd = new SDD(file.getFile());
		String sddName = sdd.getName();
		Map<String, String> mapCatalog = sdd.getCatalog();
		
		if (mapCatalog.containsKey("Study_ID")) {
			study_id = mapCatalog.get("Study_ID");
		}
		
		RecordFile codeMappingRecordFile = null;
		RecordFile dictionaryRecordFile = null;
		RecordFile codeBookRecordFile = null;
		RecordFile timelineRecordFile = null;
		
		File codeMappingFile = null;
		
		if (file_name.endsWith(".csv")) {
			String prefix = "sddtmp/" + file.getFile().getName().replace(".csv", "");
			File dictionaryFile = sdd.downloadFile(mapCatalog.get("Data_Dictionary"), prefix + "-dd.csv");
			File codeBookFile = sdd.downloadFile(mapCatalog.get("Codebook"), prefix + "-codebook.csv");
			File timelineFile = sdd.downloadFile(mapCatalog.get("Timeline"), prefix + "-timeline.csv");
			codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), prefix + "-code-mappings.csv");
			
			dictionaryRecordFile = new CSVRecordFile(dictionaryFile);
			codeBookRecordFile = new CSVRecordFile(codeBookFile);
			timelineRecordFile = new CSVRecordFile(timelineFile);
		} else if (file_name.endsWith(".xlsx")) {
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

		if (codeBookRecordFile.isValid()) {
			try{
				PVGenerator pvGenerator = new PVGenerator(codeBookRecordFile, file.getFile().getName(), 
						study_id, sdd.getMapAttrObj(), sdd.getCodeMapping());
				System.out.println("Calling PVGenerator");
				bSuccess = commitRows(pvGenerator.createRows(), pvGenerator.toString(), 
						file.getFile().getName(), "PossibleValue", true);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to read codebook");
				return false;
			}
		}

		if (dictionaryRecordFile.isValid()) {
			try {
				DASchemaAttrGenerator dasaGenerator = new DASchemaAttrGenerator(dictionaryRecordFile, 
						sddName, sdd.getCodeMapping());
				System.out.println("Calling DASchemaAttrGenerator");
				bSuccess = commitRows(dasaGenerator.createRows(), dasaGenerator.toString(), 
						file.getFile().getName(), "DASchemaAttribute", true);
			} catch (Exception e) {
				e.printStackTrace();
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to generate DASA.");
				AnnotationLog.printException(e, file.getFile().getName());
				return false;
			}
			
			try {
				DASchemaObjectGenerator dasoGenerator = new DASchemaObjectGenerator(dictionaryRecordFile, 
						sddName, sdd.getCodeMapping());
				System.out.println("Calling DASchemaObjectGenerator");
				bSuccess = commitRows(dasoGenerator.createRows(), dasoGenerator.toString(), 
						file.getFile().getName(), "DASchemaObject", true);
				
				String SDDUri = URIUtils.replacePrefixEx(kbPrefix + "DAS-" + dasoGenerator.getSDDName());
				templateLibrary.put(SDDUri, dasoGenerator.getTemplateList());
				System.out.println("[AutoAnnotator]: adding templates for SDD " + SDDUri);
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to generate DASO.");
				AnnotationLog.printException(e, file.getFile().getName());
				return false;
			}
			
			try {
				DASchemaEventGenerator daseGenerator = new DASchemaEventGenerator(dictionaryRecordFile, 
						sdd.getTimeLineMap(), sddName, sdd.getCodeMapping());
				System.out.println("Calling DASchemaEventGenerator");
				bSuccess = commitRows(daseGenerator.createRows(), daseGenerator.toString(), 
						file.getFile().getName(), "DASchemaEvent", true);
			} catch (Exception e) {
				System.out.println(e);
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to generate DASE.");
				AnnotationLog.printException(e, file.getFile().getName());
			}
		}

		try {
			GeneralGenerator generalGenerator = new GeneralGenerator();
			System.out.println("Calling GeneralGenerator");
			Map<String, Object> row = new HashMap<String, Object>();
			row.put("hasURI", kbPrefix + "DAS-" + study_id);
			row.put("a", "hasco:DASchema");
			row.put("rdfs:label", "Schema for " + study_id);
			row.put("rdfs:comment", "");
			generalGenerator.addRow(row);

			bSuccess = commitRows(generalGenerator.getRows(), generalGenerator.toString(), 
					file.getFile().getName(), "DASchema", true);	        	
		} catch (Exception e) {
			System.out.println("Error annotateDataAcquisitionSchemaFile: GeneralGenerator failed.");
			AnnotationLog.printException(e, file.getFile().getName());
			return false;
		}

		return bSuccess;
	}
	
	public static boolean annotateDAFile(DataFile dataFile, RecordFile recordFile) {
		System.out.println("annotateDAFile: [" + dataFile.getFileName() + "]");
		
		String file_name = dataFile.getFileName();    	
		AnnotationLog log = new AnnotationLog();
		log.setFileName(file_name);
		
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
					log.save();
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
			log.save();
			return false;
		} else {
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Found target data acquisition: %s", file_name)));
		}
		if (schema_uri == null) {
			log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load schema specified for data acquisition: %s", file_name)));
			log.save();
			return false;
		} else {
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Schema %s specified for data acquisition: %s", schema_uri, file_name)));
		}
		if (deployment_uri == null) {
			log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] Cannot load deployment specified for data acquisition: %s", file_name)));
			log.save();
			return false;
		} else {
			try {
				deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				log.addline(Feedback.println(Feedback.WEB, String.format("URL decoding error for deployment uri %s", deployment_uri)));
				log.save();
				return false;
			}
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] Deployment %s specified for data acquisition %s", deployment_uri, file_name)));
		}

		int status = -1;
		String message = "";

		Parser parser = new Parser();
		ParsingResult parsingResult;

		try {
			dataFile.setDatasetUri(DataFactory.getNextDatasetURI(da.getUri()));
			da.addDatasetUri(dataFile.getDatasetUri());
			parsingResult = parser.indexMeasurements(recordFile, da, dataFile);
			status = parsingResult.getStatus();
			message += parsingResult.getMessage();
			log.addline(Feedback.println(Feedback.WEB, message));
			log.save();
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			log.addline(Feedback.println(Feedback.WEB, String.format("[ERROR] parsing and indexing CSV file %s", errors.toString())));
			log.save();
			e.printStackTrace();
			return false;
		}

		if (status == 0) {
			return true;
		}

		return false;
	}
	
	public static Model createModel(List<Map<String, Object>> rows) {
		Model model = ModelFactory.createDefaultModel();
		for (Map<String, Object> row : rows) {
			Resource sub = model.createResource(URIUtils.replacePrefixEx((String)row.get("hasURI")));
			for (String key : row.keySet()) {
				if (!key.equals("hasURI")) {
					Property pred = null;
					if (key.equals("a")) {
						pred = model.createProperty(URIUtils.replacePrefixEx("rdf:type"));
					}
					else {
						pred = model.createProperty(URIUtils.replacePrefixEx(key));
					}

					String cellValue = (String)row.get(key);
					if (URIUtils.isValidURI(cellValue)) {
						Resource obj = model.createResource(URIUtils.replacePrefixEx(cellValue));
						model.add(sub, pred, obj);
					}
					else {
						Literal obj = model.createLiteral(
								cellValue.replace("\n", " ").replace("\r", " ").replace("\"", "''"));
						model.add(sub, pred, obj);
					}
				}
			}
		}

		return model;
	}
	
	private static void checkRows(List<Map<String, Object>> rows, String primaryKey) throws Exception {
		int i = 1;
		Set<String> values = new HashSet<>();
		for (Map<String, Object> row : rows) {
			String val = (String)row.get(primaryKey);
			if (null == val) {
				throw new Exception(String.format("Found Row %d without URI specified!", i));
			}
			if (values.contains(val)) {
				throw new Exception(String.format("Duplicate Concepts in Inputfile row %d :" + val + " would be duplicate URIs!", i));
			}
			else {
				values.add(val);
			}

			i++;
		}
	}

	private static boolean commitRows(List<Map<String, Object>> rows, String contentInCSV,
			String fileName, String tableName, boolean toTripleStore) {
		AnnotationLog log = AnnotationLog.find(fileName);
		if (null == log) {
			log = new AnnotationLog();
			log.setFileName(fileName);
		}
		
		try {
			checkRows(rows, "hasURI");
		} catch (Exception e) {
			log.addline(Feedback.println(Feedback.WEB, String.format(
					"[ERROR] Trying to commit invalid rows to LabKey Table %s: ", tableName)
					+ e.getMessage()));
			log.save();
		}

		Credential cred = Credential.find();
		if (null == cred) {
			log.resetLog();
			log.addline(Feedback.println(Feedback.WEB, "[ERROR] No LabKey credentials are provided!"));
			log.save();
		}

		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler labkeyDataHandler = new LabkeyDataHandler(
				site, cred.getUserName(), cred.getPassword(), path);
		try {
			//System.out.println(rows.size());
			log.addline(Feedback.println(Feedback.WEB, "The first Row is " + rows.get(0).toString()));
			int nRows = labkeyDataHandler.insertRows(tableName, rows);
			//System.out.println("insert rows succeed.");
			log.addline(Feedback.println(Feedback.WEB, String.format(
					"[OK] %d row(s) have been inserted into Table %s ", nRows, tableName)));
		} catch (CommandException e1) {
			try {
				labkeyDataHandler.deleteRows(tableName, rows);
				int nRows = labkeyDataHandler.insertRows(tableName, rows);
				//System.out.println("update " + nRows + " rows succeed in " + tableName + " .");
				log.addline(Feedback.println(Feedback.WEB, String.format("[OK] %d row(s) have been updated into Table %s ", nRows, tableName)));
			} catch (CommandException e) {
				log.addline(Feedback.println(Feedback.WEB, "[ERROR] CommitRows inside AutoAnnotator: " + e));
				log.save();
			}
		}

		if (toTripleStore) {
			DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(
					Collections.getCollectionsName(Collections.METADATA_GRAPH));
			Model model = createModel(rows);
			accessor.add(model); // check URI's
			log.addline(Feedback.println(Feedback.WEB, String.format("[OK] %d triple(s) have been committed to triple store", model.size())));
		}

		// THIS LINE IS EXCEEDING THE STORAGE CAPABILITY OF ONE CELL IN THE SOLR DATA COLLECTION FOR ANNOTATION LOG
		//log.addline(Feedback.println(Feedback.WEB, String.format(contentInCSV)));
		log.save();

		return true;
	}
	
	public static List<String> getPopulatedSDDUris(RecordFile file) {
		System.out.println("get populated SDD URIs ...");
		
		String file_name = file.getFile().getName();
		SDD sdd = new SDD(file.getFile());
		String sddName = sdd.getName();
		
		Map<String, String> mapCatalog = sdd.getCatalog();
		List<String> result = new ArrayList<String>();
		
		if (mapCatalog.containsKey("Study_ID")){
			study_id = mapCatalog.get("Study_ID");
		}
		
		RecordFile codeMappingRecordFile = null;
		RecordFile dictionaryRecordFile = null;
		RecordFile codeBookRecordFile = null;
		RecordFile timelineRecordFile = null;
		
		File codeMappingFile = null;
		
		if (file_name.endsWith(".csv")) {
			String prefix = "sddtmp/" + file.getFile().getName().replace(".csv", "");
			File dictionaryFile = sdd.downloadFile(mapCatalog.get("Data_Dictionary"), prefix + "-dd.csv");
			File codeBookFile = sdd.downloadFile(mapCatalog.get("Codebook"), prefix + "-codebook.csv");
			File timelineFile = sdd.downloadFile(mapCatalog.get("Timeline"), prefix + "-timeline.csv");
			codeMappingFile = sdd.downloadFile(mapCatalog.get("Code_Mappings"), prefix + "-code-mappings.csv");
			
			dictionaryRecordFile = new CSVRecordFile(dictionaryFile);
			codeBookRecordFile = new CSVRecordFile(codeBookFile);
			timelineRecordFile = new CSVRecordFile(timelineFile);
		} else if (file_name.endsWith(".xlsx")) {
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
				
		if (dictionaryRecordFile.isValid()) {
			try {
				DASchemaAttrGenerator dasaGenerator = new DASchemaAttrGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping());
				System.out.println("Calling createUris() ... ");
				List<String> dasaUris = dasaGenerator.createUris();
				result.addAll(dasaUris);
			} catch (Exception e) {
				System.out.println("Error createUris(): Unable to generate DASA Uris.");
				e.printStackTrace();
				AnnotationLog.printException(e, file.getFile().getName());
			}
		}

		if (dictionaryRecordFile.isValid()) {
			try {
				DASchemaObjectGenerator dasoGenerator = new DASchemaObjectGenerator(dictionaryRecordFile, sddName, sdd.getCodeMapping());
				System.out.println("Calling createUris() ... ");
				List<String> dasoUris = dasoGenerator.createUris();
				result.addAll(dasoUris);
			} catch (Exception e) {
				System.out.println("Error createUris(): Unable to generate DASO Uris.");
				e.printStackTrace();
				AnnotationLog.printException(e, file.getFile().getName());
			}
		}

		if (dictionaryRecordFile.isValid()) {
			try {
				DASchemaEventGenerator daseGenerator = new DASchemaEventGenerator(dictionaryRecordFile, 
						sdd.getTimeLineMap(), sddName, sdd.getCodeMapping());
				System.out.println("Calling createUris() ... ");
				List<String> daseUris = daseGenerator.createUris();
				result.addAll(daseUris);
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to generate DASE Uris.");
				e.printStackTrace();
				AnnotationLog.printException(e, file.getFile().getName());
			}
		}
		
		if (codeBookRecordFile.isValid()) {
			try {
				PVGenerator pvGenerator = new PVGenerator(codeBookRecordFile, file.getFile().getName(), study_id, sdd.getMapAttrObj(), sdd.getCodeMapping());
				System.out.println("Calling PVGenerator");
				List<String> pvUris = pvGenerator.createUris();
				result.addAll(pvUris);	
			} catch (Exception e) {
				System.out.println("Error annotateDataAcquisitionSchemaFile: Unable to generate PV Uris.");
				e.printStackTrace();
				AnnotationLog.printException(e, file.getFile().getName());
			}
		}

		return result;
	}
}
