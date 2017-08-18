package org.hadatac.metadata.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Selector;
import org.apache.jena.rdf.model.SimpleSelector;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.rdf.model.impl.StatementImpl;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.deployments.newDeployment;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.TriggeringEvent;
import org.hadatac.metadata.loader.LabkeyDataHandler.PlainTriple;
import org.hadatac.metadata.model.SpreadsheetParsingResult;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;
import org.joda.time.format.DateTimeFormat;
import org.labkey.remoteapi.CommandException;

import org.hadatac.data.model.ParsingResult;
import play.mvc.Controller;

public class TripleProcessing {
	
	public static final String KB_FORMAT = "text/turtle";
	
	public static final String TTL_DIR = "tmp/ttl/";
	
	public static int count;
	
	public static String printFileWithLineNumber(int mode, String filename) {
		String str = "";
		int lineNumber = 1;

        LineNumberReader reader = null;
        String line = null;

        try {
            reader = new LineNumberReader(new FileReader(filename));
            while ((line = reader.readLine()) != null) {
                str += Feedback.println(mode, lineNumber++ + " " + line);
            }
            reader.close();
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return str;
	}
	
	public static SpreadsheetParsingResult generateTTL(int mode, 
			Map< String, List<PlainTriple> > sheet, List<String> predicates) {
		String shttl = "";
		String message = "";
		
		// Prints all identified predicates as a turtle comment 
		shttl = shttl + "# properties: ";
		for (String pred : predicates) {
			ValueCellProcessing.validateNameSpace(pred);
			shttl = shttl + "[" + pred + "] ";
		}
		shttl = shttl + "\n";
		
		int processedRows = 0;
		int processedTriples = 0;
		for (String uri : sheet.keySet()) {
			List<PlainTriple> row = sheet.get(uri);
			shttl = shttl + processTriplesOfRow(row, predicates);
			processedRows++;
			processedTriples += row.size();
		}
		
		System.out.println(String.format("%d rows processed!", processedRows));
		message += Feedback.println(mode, "processed " + processedRows + " row(s) " + "( " + processedTriples + " Triples ).");
		SpreadsheetParsingResult result = new SpreadsheetParsingResult(message, shttl);
		return result;
	}
	
	public static String processTriplesOfRow(List<PlainTriple> triples, List<String> predicates) {
		String clttl = "";
		
		boolean bListSubject = false;
		Iterator<PlainTriple> iterTriple = triples.iterator();
		while(iterTriple.hasNext()){
			PlainTriple triple = iterTriple.next();
			String cellValue = triple.obj.trim();
			String predicate = triple.pred.trim();
			
			if(!bListSubject){
				clttl = clttl + ValueCellProcessing.processSubjectValue(triple.sub.trim());
				bListSubject = true;
			}
			
			// cell has object value
			clttl = clttl + "   " + predicate + " ";
			if (ValueCellProcessing.isObjectSet(cellValue)) {
				StringTokenizer st;
				if(cellValue.contains("&")){
					st = new StringTokenizer(cellValue, "&");
				}
				else{
					st = new StringTokenizer(cellValue, ",");
				}
				while (st.hasMoreTokens()) {
					clttl = clttl + ValueCellProcessing.processObjectValue(st.nextToken().trim());
					if (st.hasMoreTokens()){
						clttl = clttl + ", ";
					}
				}
			}
			else{
				clttl = clttl + ValueCellProcessing.processObjectValue(cellValue);
			}
			if(iterTriple.hasNext()){
				clttl = clttl + " ; \n";
			}
			else{
				clttl = clttl + " . \n\n";
			}
		}
				
		return clttl;
	}
	
    public static List<String> getLabKeyMetadataLists(String labkey_site, String user_name, 
    		String password, String path) throws CommandException {
    	
    	LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		try {
			List<String> queryNames = loader.getMetadataQueryNames(false);
			return queryNames;
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
		}
		return null;
	}
    
    public static List<String> getLabKeyInstanceDataLists(String labkey_site, String user_name, 
    		String password, String path) throws CommandException {
    	
    	LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		try {
			List<String> queryNames = loader.getInstanceDataQueryNames();
			return queryNames;
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
		}
		return null;
	}
    
    public static List<String> getLabKeyFolders(String labkey_site, String user_name, 
    		String password, String path) throws CommandException {
    	
    	LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		try {
			List<String> folders = loader.getSubfolders();
			return folders;
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
		}
		return null;
	}
    
    private static String loadTriples(
    		LabkeyDataHandler loader, 
    		List<String> list_names, 
    		Map< String, Map< String, List<PlainTriple> > > mapSheets, 
    		Map< String, List<String> > mapPreds) throws CommandException {
    	
    	String message = "";
		try {
			List<String> queryNames = null;
			if(list_names == null){
				queryNames = loader.getAllQueryNames();
			}
			else{
				queryNames = new LinkedList<String>();
				queryNames.addAll(list_names);
			}
			for(String query : queryNames){
				List<String> cols = loader.getColumnNames(query, false);
				if(loader.containsInstanceData(cols) || loader.containsMetaData(cols)){
					mapSheets.put(query, loader.selectRows(query, cols));
				}
				mapPreds.put(query, cols);
			}
			System.out.println("Data extraction finished...");
		} catch (CommandException e) {
			if(e.getMessage().equals("Unauthorized")){
				throw e;
			}
			else{
				return e.getMessage();
			}
		}
		
		return message;
    }
    
    public static Model importStudy(String labkey_site, String user_name, 
    		String password, String path, String studyUri) throws CommandException {
    	
		LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		Map< String, Map< String, List<PlainTriple> > > mapSheets = 
				new HashMap< String, Map< String, List<PlainTriple> > >();
		Map< String, List<String> > mapPreds = 
				new HashMap< String, List<String> >();
		
		loadTriples(loader, loader.getAllQueryNames(), mapSheets, mapPreds);
		String ttl = NameSpaces.getInstance().printTurtleNameSpaceList();
		for(String queryName : mapSheets.keySet()){
			Map< String, List<PlainTriple> > sheet = mapSheets.get(queryName);
			SpreadsheetParsingResult result = generateTTL(Feedback.WEB, sheet, mapPreds.get(queryName));
			ttl = ttl + "\n# concept: " + queryName + result.getTurtle() + "\n";
		}
		String fileName = "";
		try {
			fileName = TTL_DIR + "labkey.ttl";
			FileUtils.writeStringToFile(new File(fileName), ttl);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		Model refModel = RDFDataMgr.loadModel(fileName);
		Model targetModel = ModelFactory.createDefaultModel();
		
		HashSet<String> visitedNodes = new HashSet<String>();
		
		Selector selector = new SimpleSelector(
				refModel.getResource(ValueCellProcessing.replacePrefixEx(studyUri)), (Property)null, (RDFNode)null);
		StmtIterator iter = refModel.listStatements(selector);
		if (iter.hasNext()) {
			Resource studyNode = iter.nextStatement().getSubject();
			forwardTraverseGraph(studyNode, visitedNodes, refModel, targetModel);
		}
		
		selector = new SimpleSelector(
				null, (Property)null, refModel.getResource(ValueCellProcessing.replacePrefixEx(studyUri)));
		iter = refModel.listStatements(selector);
		if (iter.hasNext()) {
			RDFNode studyNode = iter.nextStatement().getObject();
			backwardTraverseGraph((Resource)studyNode, visitedNodes, refModel, targetModel);
		}
		
		return targetModel;
    }
    
    private static void forwardTraverseGraph(Resource node, HashSet<String> visitedNodes, 
    		Model refModel, Model targetModel) {
    	StmtIterator iter = node.listProperties();
    	while (iter.hasNext()) {
    		Statement stmt = iter.nextStatement();
    		RDFNode object = stmt.getObject();
    		if (object.isResource()) {
    			targetModel.add(node, stmt.getPredicate(), object);
    			if (!visitedNodes.contains(object.toString())) {
    				visitedNodes.add(node.toString());
    				forwardTraverseGraph((Resource)object, visitedNodes, refModel, targetModel);
    			}
    		}
    		else {
    			targetModel.add(node, stmt.getPredicate(), object);
    		}
    	}
    }
    
    private static void backwardTraverseGraph(Resource node, HashSet<String> visitedNodes, 
    		Model refModel, Model targetModel) {
    	Selector selector = new SimpleSelector(null, (Property)null, node);
    	StmtIterator iter = refModel.listStatements(selector);
    	while (iter.hasNext()) {
    		Statement stmt = iter.nextStatement();
    		Resource subject = stmt.getSubject();
    		
    		targetModel.add(subject, stmt.getPredicate(), node);
    		
    		forwardTraverseGraph(subject, visitedNodes, refModel, targetModel);
    		backwardTraverseGraph(subject, visitedNodes, refModel, targetModel);
    	}
    }
    
    public static ParsingResult importDataAcquisition(String labkey_site, String user_name, 
    		String password, String path, String target_study_uri) throws CommandException {
    	
    	final SysUser user = AuthApplication.getLocalUser(Controller.session());
		String ownerUri = UserManagement.getUriByEmail(user.getEmail());
		
    	String message = "";
    	LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		Map< String, Map< String, List<PlainTriple> > > mapSheets = 
				new HashMap< String, Map< String, List<PlainTriple> > >();
		Map< String, List<String> > mapPreds = 
				new HashMap< String, List<String> >();
		
		List<String> list_names = new ArrayList<String>();
		list_names.add("DataAcquisition");
		String ret = loadTriples(loader, list_names, mapSheets, mapPreds);
		
		if(!ret.equals("")){
			return new ParsingResult(1, message + ret);
		}
		
		String filePath = TTL_DIR + "labkey.ttl";
		message += parseTriplesToTTL(Feedback.WEB, filePath, mapSheets, mapPreds);
		ParsingResult isValid = verifyTTL(Feedback.WEB, filePath);
		message += isValid.getMessage();
		if (isValid.getStatus() != 0) {
			return new ParsingResult(1, message);
		}
		
		for(String queryName : mapSheets.keySet()){
			Map< String, List<PlainTriple> > sheet = mapSheets.get(queryName);
			for (String uri : sheet.keySet()) {
				System.out.println(String.format("Processing data acquisition %s", uri));
				
				String dataAcquisitionUri = ValueCellProcessing.convertToWholeURI(uri);
				DataAcquisition dataAcquisition = DataAcquisition.findByUri(dataAcquisitionUri);
				if (null == dataAcquisition) {
					dataAcquisition = new DataAcquisition();
					dataAcquisition.setUri(dataAcquisitionUri);
					dataAcquisition.setOwnerUri(ownerUri);
					dataAcquisition.setPermissionUri(ownerUri);
					dataAcquisition.setTriggeringEvent(TriggeringEvent.INITIAL_DEPLOYMENT);
					dataAcquisition.setNumberDataPoints(
							Measurement.getNumByDataAcquisition(dataAcquisition));
				}
				
				boolean bCanSave = true;
				for (PlainTriple triple : sheet.get(uri)) {
					String cellValue = triple.obj.trim();
					String predicate = triple.pred.trim();
					
					if (predicate.equals("a")) {
						StringTokenizer st;
						if(cellValue.contains("&")){
							st = new StringTokenizer(cellValue, "&");
						}
						else{
							st = new StringTokenizer(cellValue, ",");
						}
						while (st.hasMoreTokens()) {
							dataAcquisition.addTypeUri(
									ValueCellProcessing.convertToWholeURI(st.nextToken().trim()));
						}
					}
					else if (predicate.equals("prov:wasAssociatedWith")) {
						StringTokenizer st;
						if(cellValue.contains("&")){
							st = new StringTokenizer(cellValue, "&");
						}
						else{
							st = new StringTokenizer(cellValue, ",");
						}
						while (st.hasMoreTokens()) {
							dataAcquisition.addAssociatedUri(
									ValueCellProcessing.convertToWholeURI(st.nextToken().trim()));
						}
					}
					else if (predicate.equals("rdfs:label")) {
						dataAcquisition.setLabel(cellValue);
					}
					else if (predicate.equals("rdfs:comment")) {
						dataAcquisition.setComment(cellValue);
					}
					else if (predicate.equals("prov:startedAtTime")) {
						String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
						System.out.println("prov:startedAtTime: " + cellValue);
						dataAcquisition.setStartedAt(DateTimeFormat.forPattern(pattern).parseDateTime(cellValue));
						
						//String pattern = "EEE MMM dd HH:mm:ss zzz yyyy";
						//System.out.println("prov:startedAtTime: " + cellValue);
						//dataAcquisition.setStartedAt(DateTimeFormat.forPattern(pattern).parseDateTime(cellValue.replace(" BRT ", " EDT ")));
					}
					else if (predicate.equals("prov:endedAtTime")) {
						String pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
						dataAcquisition.setEndedAt(DateTimeFormat.forPattern(pattern).parseDateTime(cellValue));
						
						//String pattern = "EEE MMM dd HH:mm:ss zzz yyyy";
						//dataAcquisition.setEndedAt(DateTimeFormat.forPattern(pattern).parseDateTime(cellValue.replace(" BRT ", " EDT ")));
					}
					else if (predicate.equals("prov:used")) {
						dataAcquisition.setParameter(cellValue);
					}
					else if (predicate.equals("hasco:isDataAcquisitionOf")) {
						String studyUri = ValueCellProcessing.convertToWholeURI(cellValue);
						if (!target_study_uri.equals("")) {
							if (!studyUri.equals(target_study_uri)) {
								bCanSave = false;
								break;
							}
						}
						dataAcquisition.setStudyUri(studyUri);
					}
					else if (predicate.equals("hasco:hasTriggeringEvent")) {
						dataAcquisition.setTriggeringEvent(dataAcquisition.getTriggeringEventByName(cellValue));
					}
					else if (predicate.equals("hasco:hasMethod")) {
						dataAcquisition.setMethodUri(ValueCellProcessing.convertToWholeURI(cellValue));
					}
					else if (predicate.equals("hasco:hasSchema")) {
						dataAcquisition.setSchemaUri(ValueCellProcessing.convertToWholeURI(cellValue));
					}
					else if (predicate.equals("hasco:hasDeployment")) {
						String deployment_uri = ValueCellProcessing.convertToWholeURI(cellValue);
						dataAcquisition.setDeploymentUri(deployment_uri);
						
						Deployment deployment = Deployment.find(deployment_uri);
						if (deployment != null) {
							if(deployment.getPlatform()!=null){
								dataAcquisition.setPlatformUri(deployment.getPlatform().getUri());
								dataAcquisition.setPlatformName(deployment.getPlatform().getLabel());
							}
							if(deployment.getInstrument()!=null){
								dataAcquisition.setInstrumentUri(deployment.getInstrument().getUri());
								dataAcquisition.setInstrumentModel(deployment.getInstrument().getLabel());
							}
							dataAcquisition.setStartedAtXsdWithMillis(deployment.getStartedAt());
						}
					}
				}
				
				if (bCanSave == false || dataAcquisition.getStartedAt() == null || dataAcquisition.getStartedAt().isEmpty() 
						|| dataAcquisition.getDeploymentUri() == null || dataAcquisition.getDeploymentUri().isEmpty()
						|| dataAcquisition.getSchemaUri() == null || dataAcquisition.getSchemaUri().isEmpty()) {
					continue;
				}
				
				dataAcquisition.save();
				System.out.println("Successfully saved " + dataAcquisition.getUri() + " in Solr");
			}
		}
		
		return new ParsingResult(0, message);
    }
    
    private static ParsingResult verifyTTL(int mode, String filePath) {
		String listing = "";
		try {
			listing = URLEncoder.encode(SpreadsheetProcessing.printFileWithLineNumber(mode, filePath), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		};
		
		String message = "";
		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + filePath + " and stored locally.");
		try {
			RDFDataMgr.loadModel(filePath);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM LABKEY ====");
			message += listing;
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) CODE GENERATED FROM LABKEY ====");
			message += listing;
			return new ParsingResult(1, message);
		}
		
		return new ParsingResult(0, message);
    }
    
    private static String parseTriplesToTTL(int mode, String filePath, 
    		Map< String, Map< String, List<PlainTriple> > > mapSheets, 
    		Map< String, List<String> > mapPreds) {
    	
    	String message = "";
		message += Feedback.println(mode, "   Parsing triples from LABKEY " );
		message += Feedback.println(mode, " ");
		
		String ttl = NameSpaces.getInstance().printTurtleNameSpaceList();
		for(String queryName : mapSheets.keySet()) {
			Map< String, List<PlainTriple> > sheet = mapSheets.get(queryName);
			message += Feedback.print(mode, "   Processing sheet " + queryName + "  ()   ");
			for (int i = queryName.length(); i < 25; i++) {
				message += Feedback.print(mode, ".");
			}
			SpreadsheetParsingResult result = generateTTL(mode, sheet, mapPreds.get(queryName));
			ttl = ttl + "\n# concept: " + queryName + result.getTurtle() + "\n";
			message += result.getMessage();
		}

		try {
			FileUtils.writeStringToFile(new File(filePath), ttl);
		} catch (IOException e) {
			message += e.getMessage();
			return message;
		}
		
		return message;
    }

    public static String generateTTL(int mode, String oper, RDFContext rdf, String labkey_site, 
    		String user_name, String password, String path, List<String> list_names) throws CommandException {

		String message = "";
		if (oper.equals("load")) {
			message += Feedback.println(mode, "   Triples before loading from LABKEY: " + rdf.totalTriples());
			message += Feedback.println(mode, " ");
		}
		
		LabkeyDataHandler loader = new LabkeyDataHandler(labkey_site, user_name, password, path);
		Map< String, Map< String, List<PlainTriple> > > mapSheets = 
				new HashMap< String, Map< String, List<PlainTriple> > >();
		Map< String, List<String> > mapPreds = 
				new HashMap< String, List<String> >();
		
		String ret = loadTriples(loader, list_names, mapSheets, mapPreds);
		if(!ret.equals("")) {
			return (message + ret);
		}
		
		String filePath = TTL_DIR + "labkey.ttl";
		message += parseTriplesToTTL(mode, filePath, mapSheets, mapPreds);
		message += verifyTTL(mode, filePath).getMessage();

		if (oper.equals("load")) {
		    message += Feedback.print(mode, "   Uploading generated file.");
		    rdf.loadLocalFile(mode, filePath, KB_FORMAT);
		    message += Feedback.println(mode, "");
		    message += Feedback.println(mode, " ");
		    message += Feedback.println(mode, "   Triples after [loading from LABKEY]: " + rdf.totalTriples());
		}

	    return message;
	}
    
    public static String processTTL(int mode, String oper, RDFContext rdf, String fileName) {
		String message = "";
		if (oper.equals("load")) {
		   message += Feedback.println(mode, "   Triples before loading from LABKEY: " + rdf.totalTriples());
		   message += Feedback.println(mode, " ");
		}
		
		String listing = "";
		try {
			listing = URLEncoder.encode(SpreadsheetProcessing.printFileWithLineNumber(mode, fileName), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		};

		message += Feedback.println(mode, " ");
		message += Feedback.println(mode, "   Generated " + fileName + " and stored locally.");
		try {
			RDFDataMgr.loadModel(fileName);
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "SUCCESS parsing the document!");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) FILE CONTENT ====");
			message += listing;
		} catch (Exception e) {
			message += Feedback.println(mode, " ");
			message += Feedback.print(mode, "ERROR parsing the document!");
			message += Feedback.println(mode, " ");
			message += e.getMessage();
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, " ");
			message += Feedback.println(mode, "==== TURTLE (TTL) FILE CONTENT ====");
			message += listing;
			return message;
		}

	    message += Feedback.print(mode, "   Uploading generated file.");
	    rdf.loadLocalFile(mode, fileName, KB_FORMAT);
	    message += Feedback.println(mode, "");
	    message += Feedback.println(mode, " ");
	    message += Feedback.println(mode, "   Triples after [loading]: " + rdf.totalTriples());
	    return message;
	}
}