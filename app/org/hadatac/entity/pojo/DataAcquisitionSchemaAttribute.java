package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;

public class DataAcquisitionSchemaAttribute {

	public static String INDENT1 = "     ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:DASchemaAttribute;  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	public static String PREFIX = "DASA-";

	private String uri;
	private String localName;
	private String label;
	private String partOfSchema;
	private String position;
	private int    positionInt;

	/* 
       tempPositionInt is set every time a new csv file is loaded. tempPositionIn = -1 indicates that the attribute is not valid for the given cvs
         - because an original position is out of range for the csv
         - because there is no original position and the given localName does not match any of the labels in the CSV
	 */

	private int    tempPositionInt;
	private String entity;
	private String entityLabel;
	private String attribute;
	private String attributeLabel;
	private String unit;
	private String unitLabel;
	private String daseUri;
	private String dasoUri;
	private boolean isMeta;
	private DataAcquisitionSchema das;

	public DataAcquisitionSchemaAttribute(String uri, String partOfSchema) {
		this.uri = uri;
		this.partOfSchema = partOfSchema;
		this.localName = "";
		this.label = "";
		this.position = "";
		this.positionInt = -1;
		this.setEntity("");
		this.setAttribute("");
		this.setUnit("");
		this.daseUri = "";
		this.dasoUri = "";
		this.isMeta = false;
	}

	public DataAcquisitionSchemaAttribute(String uri, 
			String localName, 
			String label,
			String partOfSchema,
			String position, 
			String entity, 
			String attribute, 
			String unit, 
			String daseUri, 
			String dasoUri) {
		this.uri = uri;
		this.localName = localName;
		this.label = label;
		this.partOfSchema = partOfSchema;
		this.position = position;
		try {
			if (position != null && !position.equals("")) {
				positionInt = Integer.parseInt(position);
			} else {
				positionInt = -1;
			}
		} catch (Exception e) {
			positionInt = -1;
		}
		this.setEntity(entity);
		this.setAttribute(attribute);
		this.setUnit(unit);
		this.daseUri = daseUri;
		this.dasoUri = dasoUri;
		System.out.println("dasoUri: " + dasoUri);
	}

	public String getUri() {
		if (uri == null) {
			return "";
		} else {
			return uri;
		}
	}

	public String getUriNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getLocalName() {
		return localName;
	}

	public void setLocalName(String localName) {
		this.localName = localName;
	}

	public void setDataAcquisitionSchema(DataAcquisitionSchema das) {
		this.das = das;
	}

	public String getLabel() {
		if (label == null) {
			return "";
		} else {
			return label;
		}
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPartOfSchema() {
		if (partOfSchema == null) {
			return "";
		} else {
			return partOfSchema;
		}
	}

	public void setPartOfSchema(String partOfSchema) {
		this.partOfSchema = partOfSchema;
	}

	public String getPosition() {
		return position;
	}

	public void setPosition(String position) {
		this.position = position;
	}

	public int getPositionInt() {
		return positionInt;
	}

	public int getTempPositionInt() {
		return tempPositionInt;
	}

	public void setTempPositionInt(int tempPositionInt) {
		this.tempPositionInt = tempPositionInt;
	}

	public String getEntity() {
		if (entity == null) {
			return "";
		} else {
			return entity;
		}
	}

	public String getEntityNamespace() {
		if (entity == "") {
			return "";
		}
		return ValueCellProcessing.replaceNameSpaceEx(entity.replace("<","").replace(">",""));
	}

	public void setEntity(String entity) {
		this.entity = entity;
		if (entity == null || entity.equals("")) {
			this.entityLabel = "";
		} else {
			this.entityLabel = FirstLabel.getLabel(entity);
		}
	}

	public String getEntityLabel() {
		if (entityLabel.equals("")) {
			return ValueCellProcessing.replaceNameSpaceEx(entity);
		}
		return entityLabel;
	}

	public String getEntityViewLabel() {
		if (isMeta) {
			return "";
		}
		if (dasoUri != null && !dasoUri.equals("") && getObject() != null) {
			return "[" + getObject().getEntityLabel() + "]";
		}
		if (dasoUri == null || dasoUri.equals("")) {
			if (das != null && (!das.getIdLabel().equals("") || !das.getOriginalIdLabel().equals(""))) {
				return "[inferred from DefaultObject]";
			}
			return "";
		} else {
			return getEntityLabel();
		}
	}

	public String getAnnotatedEntity() {
		String annotation;
		if (entityLabel.equals("")) {
			if (entity == null || entity.equals("")) {
				return "";
			}
			annotation = ValueCellProcessing.replaceNameSpaceEx(entity);
		} else {
			annotation = entityLabel;
		}
		if (!getEntityNamespace().equals("")) {
			annotation += " [" + getEntityNamespace() + "]";
		} 
		return annotation;
	}

	public String getAttribute() {
		if (attribute == null) {
			return "";
		} else {
			return attribute;
		}
	}

	public String getAttributeNamespace() {
		if (attribute == "") {
			return "";
		}
		return ValueCellProcessing.replaceNameSpaceEx(attribute.replace("<","").replace(">",""));
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
		if (attribute == null || attribute.equals("")) {
			this.attributeLabel =  "";
		} else {
			this.attributeLabel = FirstLabel.getLabel(attribute);
		}
		this.isMeta = (DataAcquisitionSchema.METADASA.contains(ValueCellProcessing.replaceNameSpaceEx(attribute)));
	}

	public String getAttributeLabel() {
		if (attributeLabel.equals("")) {
			return ValueCellProcessing.replaceNameSpaceEx(attribute);
		}
		return attributeLabel;
	}

	public String getAnnotatedAttribute() {
		String annotation;
		if (attributeLabel.equals("")) {
			if (attribute == null || attribute.equals("")) {
				return "";
			}
			annotation = ValueCellProcessing.replaceNameSpaceEx(attribute);
		} else {
			annotation = attributeLabel;
		}
		if (!getAttributeNamespace().equals("")) {
			annotation += " [" + getAttributeNamespace() + "]";
		} 
		return annotation;
	}

	public String getUnit() {
		if (unit == null) {
			return "";
		} else {
			return unit;
		}
	}

	public String getUnitNamespace() {
		if (unit == "") {
			return "";
		}
		return ValueCellProcessing.replaceNameSpaceEx(unit.replace("<","").replace(">",""));
	}

	public void setUnit(String unit) {
		this.unit = unit;
		if (unit == null || unit.equals("")) {
			this.unitLabel = "";
		} else {
			this.unitLabel = FirstLabel.getLabel(unit);
		}
	}

	public String getUnitLabel() {
		if (unitLabel.equals("")) {
			return ValueCellProcessing.replaceNameSpaceEx(unit);
		}
		return unitLabel;
	}

	public String getAnnotatedUnit() {
		String annotation;
		if (unitLabel.equals("")) {
			if (unit == null || unit.equals("")) {
				return "";
			}
			annotation = ValueCellProcessing.replaceNameSpaceEx(unit);
		} else {
			annotation = unitLabel;
		}
		if (!getUnitNamespace().equals("")) {
			annotation += " [" + getUnitNamespace() + "]";
		} 
		return annotation;
	}

	public String getObjectUri() {
		return dasoUri;
	}

	public void setObjectUri(String dasoUri) {
		this.dasoUri = dasoUri;
	}

	public DataAcquisitionSchemaObject getObject() {
		if (dasoUri == null || dasoUri.equals("")) {
			return null;
		}
		return DataAcquisitionSchemaObject.find(dasoUri);
	}

	public String getObjectNamespace() {
		if (dasoUri == null || dasoUri.equals("")) {
			return "";
		}
		return ValueCellProcessing.replaceNameSpaceEx(dasoUri.replace("<","").replace(">",""));
	}

	public String getObjectViewLabel() {
		if (attribute.equals(ValueCellProcessing.replaceNameSpaceEx("hasco:originalID"))) {
			return "[DefaultObject]";
		}
		if (isMeta) {
			return "";
		}
		if (dasoUri == null || dasoUri.equals("")) {
			if (das != null && (!das.getIdLabel().equals("") || !das.getOriginalIdLabel().equals(""))) {
				return "[DefaultObject]";
			}
			return "";
		} else {
			DataAcquisitionSchemaObject daso = DataAcquisitionSchemaObject.find(dasoUri);
			if (daso == null || daso.getLabel() == null || daso.getLabel().equals("")) {
				return dasoUri;
			}
			return daso.getLabel();
		}
	}

	public String getEventUri() {
		return daseUri;
	}

	public void setEventUri(String daseUri) {
		this.daseUri = daseUri;
	}

	public DataAcquisitionSchemaEvent getEvent() {
		if (daseUri == null || daseUri.equals("")) {
			return null;
		}
		return DataAcquisitionSchemaEvent.find(daseUri);
	}

	public String getEventNamespace() {
		if (daseUri == null || daseUri.equals("")) {
			return "";
		}
		return ValueCellProcessing.replaceNameSpaceEx(daseUri.replace("<","").replace(">",""));
	}

	public String getEventViewLabel() {
		if (isMeta) {
			return "";
		}
		if (daseUri == null || daseUri.equals("")) {
			if (das != null && !das.getTimestampLabel().equals("")) {
				return "[value at label " + das.getTimestampLabel() + "]";
			}
			return "";
		} else {
			DataAcquisitionSchemaEvent dase = DataAcquisitionSchemaEvent.find(daseUri);
			if (dase == null || dase.getLabel() == null || dase.getLabel().equals("")) {
				return daseUri;
			}
			return dase.getLabel();
		}
	}

	public static DataAcquisitionSchemaAttribute find (String dasa_uri) {
		DataAcquisitionSchemaAttribute dasa = null;
		System.out.println("Looking for data acquisition schema attribute with URI " + dasa_uri);
		if (dasa_uri.startsWith("http")) {
			dasa_uri = "<" + dasa_uri + ">";
		}
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT  ?hasPosition ?partOfSchema ?hasEntity ?hasAttribute " + 
				" ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
				"    " + dasa_uri + " a hasco:DASchemaAttribute . " + 
				"    " + dasa_uri + " hasco:partOfSchema ?partOfSchema .  " + 
				"    " + dasa_uri + " hasco:hasPosition ?hasPosition .  " + 
				"    OPTIONAL { " + dasa_uri + " hasco:hasEntity ?hasEntity } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:hasAttribute ?hasAttribute } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:hasUnit ?hasUnit } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:hasEvent ?hasDASE } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:isAttributeOf ?hasDASO } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:hasSource ?hasSource } . " + 
				"    OPTIONAL { " + dasa_uri + " hasco:isPIConfirmed ?isPIConfirmed } . " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find DASA with URI: " + dasa_uri);
			return dasa;
		}

		String localNameStr = "";
		String labelStr = "";
		String partOfSchemaStr = "";
		String positionStr = "";
		String entityStr = "";
		String attributeStr = "";
		String unitStr = "";
		String dasoUriStr = "";
		String daseUriStr = "";

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null) {

				labelStr = FirstLabel.getLabel(dasa_uri);

				try {
					if (soln.getLiteral("hasPosition") != null && soln.getLiteral("hasPosition").getString() != null) {
						positionStr = soln.getLiteral("hasPosition").getString();
					}
				} catch (Exception e1) {
					positionStr = "";
				}

				try {
					if (soln.getResource("partOfSchema") != null && soln.getResource("partOfSchema").getURI() != null) {
						partOfSchemaStr = soln.getResource("partOfSchema").getURI();
					}
				} catch (Exception e1) {
					partOfSchemaStr = "";
				}

				try {
					if (soln.getResource("hasEntity") != null && soln.getResource("hasEntity").getURI() != null) {
						entityStr = soln.getResource("hasEntity").getURI();
					}
				} catch (Exception e1) {
					entityStr = "";
				}

				try {
					if (soln.getResource("hasAttribute") != null && soln.getResource("hasAttribute").getURI() != null) {
						attributeStr = soln.getResource("hasAttribute").getURI();
					}
				} catch (Exception e1) {
					attributeStr = "";
				}

				try {
					if (soln.getResource("hasUnit") != null && soln.getResource("hasUnit").getURI() != null) {
						unitStr = soln.getResource("hasUnit").getURI();
					}
				} catch (Exception e1) {
					unitStr = "";
				}

				try {
					if (soln.getResource("hasDASO") != null && soln.getResource("hasDASO").getURI() != null) {
						dasoUriStr = soln.getResource("hasDASO").getURI();
					}
				} catch (Exception e1) {
					dasoUriStr = "";
				}

				try {
					if (soln.getResource("hasDASE") != null && soln.getResource("hasDASE").getURI() != null) {
						daseUriStr = soln.getResource("hasDASE").getURI();
					}
				} catch (Exception e1) {
					daseUriStr = "";
				}

				dasa = new DataAcquisitionSchemaAttribute(dasa_uri,
						localNameStr,
						labelStr,
						partOfSchemaStr,
						positionStr,
						entityStr,
						attributeStr,
						unitStr,
						daseUriStr,
						dasoUriStr);
			}

		}

		return dasa;
	}

	public static List<DataAcquisitionSchemaAttribute> findBySchema (String schemaUri) {
		System.out.println("Looking for data acquisition schema attributes for " + schemaUri);
		if (schemaUri.startsWith("http")) {
			schemaUri = "<" + schemaUri + ">";
		}
		List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute " + 
				" ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
				"    ?uri a hasco:DASchemaAttribute . " + 
				"    ?uri hasco:partOfSchema " + schemaUri + " .  " + 
				"} ";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find attributes for schema: " + schemaUri);
			return attributes;
		}

		String uriStr = "";

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null) {

				try {
					if (soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
						uriStr = soln.getResource("uri").getURI();
						DataAcquisitionSchemaAttribute attr = find(uriStr);
						attributes.add(attr);
					}
				} catch (Exception e1) {
					System.out.println("[ERROR] DataAcquisitionSchemaAttribute. URI: " + uriStr);
				}
			}
		}
		attributes.sort(Comparator.comparing(DataAcquisitionSchemaAttribute::getPositionInt));
		return attributes;
	}

	public void save() {
		delete();  // delete any existing triple for the current DASA
		//System.out.println("Saving <" + uri + ">");
		if (uri == null || uri.equals("")) {
			System.out.println("[ERROR] Trying to save DASA without assigning an URI");
			return;
		}
		if (partOfSchema == null || partOfSchema.equals("")) {
			System.out.println("[ERROR] Trying to save DASA without assigning DAS's URI");
			return;
		}
		String insert = "";

		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += this.getUri() + " a hasco:DASchemaAttribute . ";
		insert += this.getUri() + " rdfs:label  \"" + label + "\" . ";
		if (partOfSchema.startsWith("http")) {
			insert += this.getUri() + " hasco:partOfSchema <" + partOfSchema + "> .  "; 
		} else {
			insert += this.getUri() + " hasco:partOfSchema " + partOfSchema + " .  "; 
		} 
		insert += this.getUri() + " hasco:hasPosition  \"" + position + "\" . "; 
		if (!entity.equals("")) {
			insert += this.getUri() + " hasco:hasEntity "  + entity + " .  ";
		}   
		if (!attribute.equals("")) {
			insert += this.getUri() + " hasco:hasAttribute " + attribute + " .  ";
		}
		if (!unit.equals("")) {
			insert += this.getUri() + " hasco:hasUnit " + unit + " .  ";
		}
		if (daseUri != null && !daseUri.equals("")) {
			if (daseUri.startsWith("http")) {
				insert += this.getUri() + " hasco:hasEvent <" + daseUri + "> .  ";
			} else {
				insert += this.getUri() + " hasco:hasEvent " + daseUri + " .  ";
			}
		}
		if (dasoUri != null && !dasoUri.equals("")) {
			if (dasoUri.startsWith("http")) {
				insert += this.getUri() + " hasco:isAttributeOf <" + dasoUri + "> .  ";
			} else {
				insert += this.getUri() + " hasco:isAttributeOf " + dasoUri + " .  ";
			}
		} 
		//insert += this.getUri() + " hasco:hasSource " + " .  "; 
		//insert += this.getUri() + " hasco:isPIConfirmed " + " .  "; 
		insert += LINE_LAST;
		System.out.println("DASA insert query (pojo's save): <" + insert + ">");
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int saveToLabKey(String user_name, String password) {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
		row.put("a", "hasco:DASchemaAttribute");
		row.put("rdfs:label", getLabel());
		row.put("rdfs:comment", getLabel());
		row.put("hasco:partOfSchema", ValueCellProcessing.replaceNameSpaceEx(getPartOfSchema()));
		row.put("hasco:hasPosition", this.getPosition());
		row.put("hasco:hasEntity", this.getEntity());
		row.put("hasco:hasAttribute", this.getAttribute());
		row.put("hasco:hasUnit", this.getUnit());
		row.put("hasco:hasEvent", ValueCellProcessing.replaceNameSpaceEx(daseUri));
		row.put("hasco:hasSource", "");
		row.put("hasco:isAttributeOf", ValueCellProcessing.replaceNameSpaceEx(dasoUri));
		row.put("hasco:isVirtual", "");
		row.put("hasco:isPIConfirmed", "false");
		rows.add(row);
		int totalChanged = 0;
		try {
			totalChanged = loader.insertRows("DASchemaAttribute", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("DASchemaAttribute", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update DASA(s)");
			}
		}
		return totalChanged;
	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int deleteFromLabKey(String user_name, String password) throws CommandException {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
		rows.add(row);
		for (Map<String,Object> str : rows) {
			System.out.println("deleting DASA " + row.get("hasURI"));
		}
		return loader.deleteRows("DASchemaAttribute", rows);
	}

	public void delete() {
		String query = "";
		if (this.getUri() == null || this.getUri().equals("")) {
			return;
		}
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += DELETE_LINE1;
		if (this.getUri().startsWith("http")) {
			query += "<" + this.getUri() + ">";
		} else {
			query += this.getUri();
		}
		query += DELETE_LINE3;
		query += LINE_LAST;

		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}
}
