package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.function.library.print;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.labkey.remoteapi.CommandException;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.metadata.loader.LabkeyDataHandler;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionSchema {
	public static String INDENT1 = "     ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:DASchema;  ";
	public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	public static String PREFIX = "DAS-";
	public static List<String> METADASA = Arrays.asList(
			"sio:TimeStamp", 
			"sio:TimeInstant", 
			"hasco:namedTime", 
			"hasco:originalID", 
			"hasco:uriId", 
			"hasco:hasMetaEntity", 
			"hasco:hasMetaEntityURI", 
			"hasco:hasMetaAttribute", 
			"hasco:hasMetaAttributeURI", 
			"hasco:hasMetaUnit", 
			"hasco:hasMetaUnitURI", 
			"sio:InRelationTo",
			"hasco:hasLOD",
			"hasco:hasCalibration",
			"hasco:hasElevation",
			"hasco:hasLocation");
	private String uri = "";
	private String label = "";
	private List<DataAcquisitionSchemaAttribute> attributes = null;
	private List<DataAcquisitionSchemaObject> objects = null;
	private List<DataAcquisitionSchemaEvent> events = null;
	private String timestampLabel;
	private String timeInstantLabel;
	private String namedTimeLabel;
	private String idLabel;
	private String originalIdLabel;
	private String elevationLabel;
	private String entityLabel;
	private String unitLabel;
	private String inRelationToLabel;

	public DataAcquisitionSchema() {
		this.timestampLabel = "";
		this.timeInstantLabel = "";
		this.namedTimeLabel = "";
		this.elevationLabel = "";
		this.idLabel = "";
		this.originalIdLabel = "";
		this.entityLabel = "";
		this.unitLabel = "";
		this.inRelationToLabel = "";
		this.attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
		this.objects = new ArrayList<DataAcquisitionSchemaObject>();
		this.events = new ArrayList<DataAcquisitionSchemaEvent>();
	}

	public DataAcquisitionSchema(String uri, String label) {
		this();
		this.uri = uri;
		this.label = label;
	}

	public String getUri() {
		return uri.replace("<","").replace(">","");
	}

	public String getUriNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getTimestampLabel() {
		return timestampLabel;
	}

	public void setTimestampLabel(String timestampLabel) {
		this.timestampLabel = timestampLabel;
	}

	public String getTimeInstantLabel() {
		return timeInstantLabel;
	}

	public void setTimeInstantLabel(String timeInstantLabel) {
		this.timeInstantLabel = timeInstantLabel;
	}

	public String getNamedTimeLabel() {
		return namedTimeLabel;
	}

	public void setNamedTimeLabel(String namedTimeLabel) {
		this.namedTimeLabel = namedTimeLabel;
	}

	public String getIdLabel() {
		return idLabel;
	}

	public void setIdLabel(String idLabel) {
		this.idLabel = idLabel;
	}

	public String getOriginalIdLabel() {
		return originalIdLabel;
	}

	public void setOriginalIdLabel(String originalIdLabel) {
		this.originalIdLabel = originalIdLabel;
	}

	public String getElevationLabel() {
		return elevationLabel;
	}

	public void setElevationLabel(String elevationLabel) {
		this.elevationLabel = elevationLabel;
	}

	public String getEntityLabel() {
		return entityLabel;
	}

	public void setEntityLabel(String entityLabel) {
		this.entityLabel = entityLabel;
	}

	public String getUnitLabel() {
		return unitLabel;
	}

	public void setUnitLabel(String unitLabel) {
		this.unitLabel = unitLabel;
	}

	public String getInRelationToLabel() {
		return inRelationToLabel;
	}

	public void setInRelationToLabel(String inRelationToLabel) {
		this.inRelationToLabel = inRelationToLabel;
	}

	public int getTotalDASA() {
		if (attributes == null) {
			return -1;
		}
		return attributes.size();
	}

	public int getTotalDASE() {
		if (events == null) {
			return -1;
		}
		return events.size();
	}

	public int getTotalDASO() {
		if (objects == null) {
			return -1;
		}
		return objects.size();
	}

	public List<DataAcquisitionSchemaAttribute> getAttributes() {
		return attributes;
	}

	public void setAttributes(List<DataAcquisitionSchemaAttribute> attributes) {
		System.out.println("setAttributes is called!");
		if (attributes == null) {
			System.out.println("[ERROR] No DataAcquisitionSchemaAttribute for " + uri + " is defined in the knowledge base. ");
		} else {
			this.attributes = attributes;
			for (DataAcquisitionSchemaAttribute dasa : attributes) {
				dasa.setDataAcquisitionSchema(this);
				System.out.println("dasa.getAttribute(): " + dasa.getAttribute());
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeStamp"))) {
					setTimestampLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema TimeStampLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:TimeInstant"))) {
					setTimeInstantLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema TimeInstantLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:namedTime"))) {
					setNamedTimeLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema NamedTimeLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:uriId"))) {
					setIdLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema IdLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:originalID"))) { 
					setOriginalIdLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema IdLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:hasEntity"))) {
					setEntityLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema EntityLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("hasco:hasUnit"))) {
					setUnitLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema UnitLabel: " + dasa.getLabel());
				}
				if (dasa.getAttribute().equals(ValueCellProcessing.replacePrefixEx("sio:InRelationTo"))) {
					setInRelationToLabel(dasa.getLabel());
					System.out.println("[OK] DataAcquisitionSchema InRelationToLabel: " + dasa.getLabel());
				}
				System.out.println("[OK] DataAcquisitionSchemaAttribute <" + dasa.getUri() + "> is defined in the knowledge base. " + 
						"Entity: \""    + dasa.getEntityLabel()     + "\"; " + 
						"Attribute: \"" + dasa.getAttributeLabel() + "\"; " + 
						"Unit: \""      + dasa.getUnitLabel()       + "\""); 
				//System.out.println("     DataAcquisitionSchemaAttribute DASO URI: \"" + dasa.getObjectUri() + "\"");
				//System.out.println("     DataAcquisitionSchemaAttribute DASE URI: \"" + dasa.getEventUri() + "\"");
			}
		}
	}

	public List<DataAcquisitionSchemaObject> getObjects() {
		return objects;
	}

	public void setObjects(List<DataAcquisitionSchemaObject> objects) {
		if (objects == null) {
			System.out.println("[WARNING] No DataAcquisitionSchemaObject for " + uri + " is defined in the knowledge base. ");
		} else {
			this.objects = objects;
			for (DataAcquisitionSchemaObject daso : objects) {
				System.out.println("[OK] DataAcquisitionSchemaObject <" + daso.getUri() + "> is defined in the knowledge base. " + 
						"Role: \""  + daso.getRole() + "\"");
			}
		}
	}

	public DataAcquisitionSchemaObject getObject(String dasoUri) {
		for (DataAcquisitionSchemaObject daso : objects) {
			if (daso.getUri().equals(dasoUri)) {
				return daso;
			}
		}
		return null;
	}

	public List<DataAcquisitionSchemaEvent> getEvents() {
		return events;
	}

	public void setEvents(List<DataAcquisitionSchemaEvent> events) {
		if (events == null) {
			System.out.println("[WARNING] No DataAcquisitionSchemaEvent for " + uri + " is defined in the knowledge base. ");
		} else {
			this.events = events;
			for (DataAcquisitionSchemaEvent dase : events) {
				System.out.println("[OK] DataAcquisitionSchemaEvent <" + dase.getUri() + "> is defined in the knowledge base. " + 
						"Label: \""  + dase.getLabel() + "\"");
			}
		}
	}

	public DataAcquisitionSchemaEvent getEvent(String daseUri) {
		for (DataAcquisitionSchemaEvent dase : events) {
			if (dase.getUri().equals(daseUri)) {
				return dase;
			}
		}
		return null;
	}

	public static DataAcquisitionSchema find(String schemaUri) {
		System.out.println("Looking for data acquisition schema " + schemaUri);
		
		if (schemaUri == null || schemaUri.equals("")) {
			System.out.println("[ERROR] DataAcquisitionSchema URI blank or null.");
			return null;
		}
		if (schemaUri.startsWith("http")) {
			schemaUri = "<" + schemaUri + ">";
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				" ASK { " + schemaUri + " a hasco:DASchema . } ";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		boolean uriExist = qexec.execAsk();
		qexec.close();

		if (!uriExist) {
			System.out.println("[WARNING] DataAcquisitionSchema. Could not find schema for uri: " + schemaUri);
			return null;
		}

		DataAcquisitionSchema schema = new DataAcquisitionSchema();
		schema.setUri(schemaUri);
		schema.setLabel(FirstLabel.getLabel(schemaUri));
		schema.setAttributes(DataAcquisitionSchemaAttribute.findBySchema(schemaUri));
		schema.setObjects(DataAcquisitionSchemaObject.findBySchema(schemaUri));
		schema.setEvents(DataAcquisitionSchemaEvent.findBySchema(schemaUri));
		System.out.println("[OK] DataAcquisitionSchema " + schemaUri + " exists. " + 
				"It has " + schema.getAttributes().size() + " attributes, " + 
				schema.getObjects().size() + " objects, and " + 
				schema.getEvents().size() + " events.");
		return schema;
	}

	public static List<DataAcquisitionSchema> findAll() {
		List<DataAcquisitionSchema> schemas = new ArrayList<DataAcquisitionSchema>();

		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri WHERE { " + 
				"   ?uri a hasco:DASchema . } ";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("uri").getURI() != null) { 
				DataAcquisitionSchema schema = DataAcquisitionSchema.find(soln.getResource("uri").getURI());
				schemas.add(schema);
			}
		}
		return schemas;
	}
	
	public static Map<String, Map<String, String>> findPossibleValues(String schemaUri) {
		System.out.println("findPossibleValues is called!");
		Map<String, Map<String, String>> mapPossibleValues = new HashMap<String, Map<String, String>>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ " SELECT ?daso_or_dasa ?codeClass ?code ?resource WHERE { "
				+ " ?possibleValue a hasco:PossibleValue . "
				+ " ?possibleValue hasco:isPossibleValueOf ?daso_or_dasa . "
				+ " ?possibleValue hasco:hasCode ?code . "
				+ " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . " 
				+ " OPTIONAL { ?possibleValue hasco:hasClass ?codeClass } . "
				+ " OPTIONAL { ?possibleValue hasco:hasResource ?resource } . "
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		try {
			while (resultsrw.hasNext()) {				
				String classUri = "";
				QuerySolution soln = resultsrw.next();
				if (soln.get("codeClass").toString().length() > 0) {
					classUri = soln.getResource("codeClass").toString();
				} else if (soln.get("resource").toString().length() > 0) {
					classUri = soln.getResource("resource").toString();
				} 
				
				String daso_or_dasa = soln.getResource("daso_or_dasa").toString();
				String code = soln.getLiteral("code").toString();
				if (mapPossibleValues.containsKey(daso_or_dasa)) {
					mapPossibleValues.get(daso_or_dasa).put(code.toLowerCase(), classUri);
				} else {
					Map<String, String> indvMapPossibleValues = new HashMap<String, String>();
					indvMapPossibleValues.put(code.toLowerCase(), classUri);
					mapPossibleValues.put(daso_or_dasa, indvMapPossibleValues);
				}
			}
		} catch (Exception e) {
			System.out.println("My Error: " + e.getMessage());
		}

		return mapPossibleValues;
	}
	
	public static String findByPosIndex(String schemaUri, String pos) {
		System.out.println("findByPosIndex is called!");
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ " SELECT ?daso_or_dasa WHERE { "
				+ " ?daso_or_dasa hasco:hasPosition \"" + pos + "\" . "
				+ " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . "
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		try {
			if (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				return soln.getResource("daso_or_dasa").toString();
			}
		} catch (Exception e) {
			System.out.println("findByPosIndex() Error: " + e.getMessage());
		}

		return "";
	}
	
	public static Map<String, List<String>> findIdUriMappings(String studyUri) {
		System.out.println("findIdUriMappings is called!");
		Map<String, List<String>> mapIdUriMappings = new HashMap<String, List<String>>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ " SELECT ?subj_or_sample ?id ?obj ?subj_id WHERE { "
				+ " { "
				+ "		?subj_or_sample a sio:Human . "
				+ " 	?subj_or_sample hasco:originalID ?id . "
				+ " 	?subj_or_sample hasco:isMemberOf* <" + studyUri + "> . "						
				+ " } UNION { "
				+ "     ?subj_or_sample a sio:Sample . "
				+ " 	?subj_or_sample hasco:originalID ?id . "
				+ " 	?subj_or_sample hasco:isMemberOf* <" + studyUri + "> . " 
				+ " 	?subj_or_sample hasco:hasObjectScope ?obj . "
				+ " 	?obj hasco:originalID ?subj_id . "
				+ " }"
				+ " }";

		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		try {
			while (resultsrw.hasNext()) {			
				QuerySolution soln = resultsrw.next();
				List<String> details = new ArrayList<String>();
				if (soln.get("subj_or_sample") != null) {
					details.add(soln.get("subj_or_sample").toString());
				} else {
					details.add("");
				}
				if (soln.get("subj_id") != null) {
					details.add(soln.get("subj_id").toString());
				} else {
					details.add("");
				}
				if (soln.get("obj") != null) {
					details.add(soln.get("obj").toString());
				} else {
					details.add("");
				}
				mapIdUriMappings.put(soln.get("id").toString(), details);
			}
		} catch (Exception e) {
			System.out.println("Error in findIdUriMappings(): " + e.getMessage());
		}

		System.out.println("mapIdUriMappings: " + mapIdUriMappings.keySet().size());
		return mapIdUriMappings;
	}

	public static DataAcquisitionSchema create(String uri) {
		DataAcquisitionSchema das = new DataAcquisitionSchema();
		das.setUri(uri);
		return das;
	}

	public void save() {
		// SAVING DAS's DASAs
		for (DataAcquisitionSchemaAttribute dasa : attributes) {
			dasa.save();
		}

		// SAVING DAS ITSELF
		String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += this.getUri() + " a hasco:DASchema . ";
		insert += this.getUri() + " rdfs:label  \"" + this.getLabel() + "\" . ";
		insert += LINE_LAST;
		//System.out.println(insert);
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int saveToLabKey(String user_name, String password) throws CommandException {
		// SAVING DAS's DASAs
		for (DataAcquisitionSchemaAttribute dasa : attributes) {
			//System.out.println("Saving DASA " + dasa.getUri() + " into LabKey");
			dasa.saveToLabKey(user_name, password);
		}

		// SAVING DAS ITSELF
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
		row.put("a", "hasco:DataAcquisitionSchema");
		row.put("rdfs:label", getLabel());
		rows.add(row);
		return loader.insertRows("DASchema", rows);
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int deleteFromLabKey(String user_name, String password) throws CommandException {
		// DELETING DAS's DASAs
		for (DataAcquisitionSchemaAttribute dasa : attributes) {
			//System.out.println("Deleting DASA " + dasa.getUri() + " from LabKey");
			dasa.deleteFromLabKey(user_name, password);
		}

		// DELETING DAS ITSELF
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
		rows.add(row);
		return loader.deleteRows("DASchema", rows);
	}

	public void delete() {
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += DELETE_LINE1;
		query += "<" + this.getUri() + ">  ";
		query += DELETE_LINE3;
		query += LINE_LAST;
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}
}
