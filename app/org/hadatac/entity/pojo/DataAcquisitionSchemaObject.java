package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;

public class DataAcquisitionSchemaObject {

	public static String INDENT1 = "     ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:DASchemaObject;  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	public static String PREFIX = "DASO-";

	private String uri;
	private String label;
	private String partOfSchema;
	private String position;
	private int positionInt;
	private int tempPositionInt;
	private String entity;
	private String entityLabel;
	private String role;
	private String inRelationTo;
	private String inRelationToLabel;
	private String relation;
	private String relationLabel;

	public DataAcquisitionSchemaObject(String uri, 
			String label, 
			String partOfSchema,
			String position,
			String entity, 
			String role, 
			String inRelationTo, 
			String relation) {
		this.uri = uri;
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
		System.out.println("positionInt: " + positionInt);
		this.setEntity(entity);
		this.role = role;
		this.setInRelationTo(inRelationTo);
		this.setRelation(relation);
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getUriNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(uri);
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getPartOfSchema() {
		return partOfSchema;
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
		return entity;
	}

	public void setEntity(String entity) {
		this.entity = entity;
		if (entity == null || entity.equals("")) {
			this.entityLabel = "";
		} else {
			this.entityLabel = FirstLabel.getLabel(entity);
		}
	}

	public String getEntityNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(entity);
	}

	public String getEntityLabel() {
		if (entity == null || entityLabel.equals("")) {
			return ValueCellProcessing.replaceNameSpaceEx(entity);
		}
		return entityLabel;
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

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public String getInRelationTo() {
		return inRelationTo;
	}

	public String getInRelationToNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(inRelationTo);
	}

	public void setInRelationTo(String inRelationTo) {
		this.inRelationTo = inRelationTo;
		if (inRelationTo == null || inRelationTo.equals("")) {
			this.inRelationToLabel = "";
		} else {
			this.inRelationToLabel = FirstLabel.getLabel(inRelationTo);
		}
	}

	public String getInRelationToLabel() {
		if (inRelationTo == null || inRelationToLabel.equals("")) {
			String str = ValueCellProcessing.replaceNameSpaceEx(inRelationTo);
			return str.substring(str.indexOf(":") + 1);
		}
		return inRelationToLabel;
	}

	public String getRelation() {
		return relation;
	}

	public String getRelationNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(relation);
	}

	public void setRelation(String relation) {
		this.relation = relation;
		System.out.println("New RELATION : " + relation);
		if (relation == null || relation.equals("")) {
			this.relationLabel = "";
		} else {
			this.relationLabel = FirstLabel.getLabel(relation);
		}
	}

	public String getRelationLabel() {
		if (relationLabel == null || relationLabel.equals("")) {
			System.out.println("RELATION label -- just relation : <" + relation + ">");
			System.out.println("RELATION label -- just relation : <" + ValueCellProcessing.replaceNameSpaceEx(relation) + ">");
			return ValueCellProcessing.replaceNameSpaceEx(relation);
		}
		System.out.println("RELATION label : <" + relationLabel + ">");
		return relationLabel;
	}

	public static DataAcquisitionSchemaObject find(String uri) {
		System.out.println("Looking for data acquisition schema objects with uri: " + uri);
		DataAcquisitionSchemaObject object = null;
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?position ?entity ?partOfSchema ?role ?inRelationTo ?relation WHERE { " + 
				"   <" + uri + "> a hasco:DASchemaObject . " + 
				"   <" + uri + "> hasco:partOfSchema ?partOfSchema . " + 
				"   OPTIONAL { <" + uri + "> hasco:hasPosition ?position } . " + 
				"   OPTIONAL { <" + uri + "> hasco:hasEntity ?entity } . " + 
				"   OPTIONAL { <" + uri + "> hasco:hasRole ?role } .  " + 
				"   OPTIONAL { <" + uri + "> sio:inRelationTo ?inRelationTo } . " + 
				"   OPTIONAL { <" + uri + "> sio:relation ?relation } . " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find object with uri: " + uri);
			return null;
		}

		QuerySolution soln = resultsrw.next();
		String labelStr = "";
		String partOfSchemaStr = "";
		String positionStr = "";
		String entityStr = "";
		String roleStr = "";
		String inRelationToStr = "";
		String relationStr = "";

		try {
			if (soln != null) {

				labelStr = FirstLabel.getLabel(uri);

				try {
					if (soln.getLiteral("position") != null && soln.getLiteral("position").getString() != null) {
						positionStr = soln.getLiteral("position").getString();
					} 
				} catch (Exception e1) {
					positionStr = "";
				}
				System.out.println("positionStr: " + positionStr);
				
				try {
					if (soln.getResource("entity") != null && soln.getResource("entity").getURI() != null) {
						entityStr = soln.getResource("entity").getURI();
					} 
				} catch (Exception e1) {
					entityStr = "";
				}

				try {
					if (soln.getResource("partOfSchema") != null && soln.getResource("partOfSchema").getURI() != null) {
						partOfSchemaStr = soln.getResource("partOfSchema").getURI();
					} 
				} catch (Exception e1) {
					partOfSchemaStr = "";
				}

				try {
					if (soln.getLiteral("role") != null && soln.getLiteral("role").getString() != null) {
						roleStr = soln.getLiteral("role").getString();
					} 
				} catch (Exception e1) {
					roleStr = "";
				}

				try {
					if (soln.getResource("inRelationTo") != null && soln.getResource("inRelationTo").getURI() != null) {
						inRelationToStr = soln.getResource("inRelationTo").getURI();
					}
				} catch (Exception e1) {
					inRelationToStr = "";
				}

				try {
					if (soln.getResource("relation") != null && soln.getResource("relation").getURI() != null) {
						relationStr = soln.getResource("relation").getURI();
					}
				} catch (Exception e1) {
					relationStr = "";
				}

				object = new DataAcquisitionSchemaObject(uri,
						labelStr,
						partOfSchemaStr,
						positionStr,
						entityStr,
						roleStr,
						inRelationToStr,
						relationStr);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] DataAcquisitionSchemaObject. uri: e.Message: " + e.getMessage());
		}
		return object;
	}

	public static List<DataAcquisitionSchemaObject> findBySchema (String schemaUri) {
		//System.out.println("Looking for data acquisition schema objectss for " + schemaUri);
		List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
				"SELECT ?uri ?label ?hasEntity ?hasRole ?inRelationTo ?relation WHERE { " + 
				"   ?uri a hasco:DASchemaObject . " + 
				"   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
				"}";
		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		if (!resultsrw.hasNext()) {
			System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find objects for schema: " + schemaUri);
			return objects;
		}

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			try {
				if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {

					DataAcquisitionSchemaObject obj = DataAcquisitionSchemaObject.find(soln.getResource("uri").getURI());
					if (obj != null) {
						objects.add(obj);
					}
				}
			}  catch (Exception e) {
				System.out.println("[ERROR] DataAcquisitionSchemaObject. uri: e.Message: " + e.getMessage());
			}

		}
		return objects;
	}

	public static String findUriFromRole(String newInRelationTo, List<DataAcquisitionSchemaObject> objects) {
		if (newInRelationTo == null) {
			return "";
		}
		if (newInRelationTo.equals("DefaultObject")) {
			return ValueCellProcessing.replacePrefixEx("hasco:DefaultObject");
		}
		for (DataAcquisitionSchemaObject daso : objects) {
			if (daso.getRole().equals(newInRelationTo)) {
				return ValueCellProcessing.replacePrefixEx(daso.getUri());
			}
		} 
		return "";
	}

	public void save() {
		delete();  // delete any existing triple for the current DASO
		//System.out.println("Saving <" + uri + ">");
		if (uri == null || uri.equals("")) {
			System.out.println("[ERROR] Trying to save DASO without assigning an URI");
			return;
		}
		if (partOfSchema == null || partOfSchema.equals("")) {
			System.out.println("[ERROR] Trying to save DASO without assigning DAS's URI");
			return;
		}
		String insert = "";

		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += this.getUri() + " a hasco:DASchemaObject . ";
		insert += this.getUri() + " rdfs:label  \"" + label + "\" . ";
		if (partOfSchema.startsWith("http")) {
			insert += this.getUri() + " hasco:partOfSchema <" + partOfSchema + "> .  "; 
		} else {
			insert += this.getUri() + " hasco:partOfSchema " + partOfSchema + " .  "; 
		} 
		if (!role.equals("")) {
			insert += this.getUri() + " hasco:hasRole  \"" + role + "\" . "; 
		}
		if (!entity.equals("")) {
			insert += this.getUri() + " hasco:hasEntity "  + entity + " .  ";
		}   
		if (!inRelationTo.equals("")) {
			String inRelationToStr =  ValueCellProcessing.replacePrefixEx(inRelationTo);
			if (inRelationToStr.startsWith("<")) {
				insert += this.getUri() + " sio:inRelationTo " +  inRelationToStr + " .  ";
			} else {
				insert += this.getUri() + " sio:inRelationTo <" + inRelationToStr + "> .  ";
			}
		}
		if (!relation.equals("")) {
			String relationStr =  ValueCellProcessing.replacePrefixEx(relation);
			if (relationStr.startsWith("<")) {
				insert += this.getUri() + " sio:relation " +  relationStr + " .  ";
			} else {
				insert += this.getUri() + " sio:relation <" + relationStr + "> .  ";
			}
		}
		//insert += this.getUri() + " hasco:hasSource " + " .  "; 
		//insert += this.getUri() + " hasco:isPIConfirmed " + " .  "; 
		insert += LINE_LAST;
		System.out.println("DASO insert query (pojo's save): <" + insert + ">");
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
		row.put("a", "hasco:DASchemaObject");
		row.put("rdfs:label", getLabel());
		row.put("rdfs:comment", getLabel());
		row.put("hasco:partOfSchema", ValueCellProcessing.replaceNameSpaceEx(getPartOfSchema()));
		row.put("hasco:hasEntity", this.getEntity());
		row.put("hasco:hasRole", this.getRole());
		row.put("sio:inRelationTo", this.getInRelationTo());
		row.put("sio:relation", this.getRelation());
		row.put("hasco:isVirtual", "");
		row.put("hasco:isPIConfirmed", "false");
		rows.add(row);
		int totalChanged = 0;
		try {
			totalChanged = loader.insertRows("DASchemaObject", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("DASchemaObject", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update DASO(s)");
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
			System.out.println("deleting DASO " + row.get("hasURI"));
		}
		return loader.deleteRows("DASchemaObject", rows);
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
		//System.out.println("SPARQL query inside dasa poho's delete: " + query);
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}
}

