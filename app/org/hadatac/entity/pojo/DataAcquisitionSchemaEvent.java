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

public class DataAcquisitionSchemaEvent {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:DASchemaEvent;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "DASE-";

    private String uri;
    private String label;
    private String partOfSchema;
    private String entity;
    private String entityLabel;
    private String unit;
    private String unitLabel;
    
    public DataAcquisitionSchemaEvent(String uri, 
				      String label, 
				      String partOfSchema, 
				      String entity, 
				      String unit) {
	this.uri = uri;
	this.label = label;
	this.partOfSchema = partOfSchema;
	this.setEntity(entity);
	this.setUnit(unit);
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
	if (entityLabel.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(entity);
	}
	return entityLabel;
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
    
    
    public static DataAcquisitionSchemaEvent find(String uri) {
	DataAcquisitionSchemaEvent event = null;
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?partOfSchema ?entity ?unit WHERE { " + 
	    "   <" + uri + "> a hasco:DASchemaEvent . " + 
            "   <" + uri + "> hasco:partOfSchema ?partOfSchema. " +
	    "   OPTIONAL { <" + uri + ">  hasco:hasEntity ?entity } ." + 
	    "   OPTIONAL { <" + uri + "> hasco:hasUnit ?unit } ." + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaEvent. Could not find event for uri: " + uri);
	    return event;
	}
	
	QuerySolution soln = resultsrw.next();
	String labelStr = "";
	String partOfSchemaStr = "";
	String entityStr = "";
	String unitStr = "";
	try {
	    if (soln != null) {

		labelStr = FirstLabel.getLabel(uri);
		
                try {
                    if (soln.getResource("partOfSchema") != null && soln.getResource("partOfSchema").getURI() != null) {
                        partOfSchemaStr = soln.getResource("partOfSchema").getURI();
                    }
                } catch (Exception e1) {
                    partOfSchemaStr = "";
                }

 		try {
		    if (soln.getResource("entity") != null && soln.getResource("entity").getURI() != null) {
			entityStr = soln.getResource("entity").getURI();
		    }
		} catch (Exception e1) {
		    entityStr = "";
		}
		
		try {
		    if (soln.getResource("unit") != null && soln.getResource("unit").getURI() != null) {
			unitStr = soln.getResource("unit").getURI();
		    }
		} catch (Exception e1) {
		    unitStr = "";
		}
		
		event = new DataAcquisitionSchemaEvent(uri,
						       labelStr,
						       partOfSchemaStr,
						       entityStr,
						       unitStr);
	    }
	}  catch (Exception e) {
	    System.out.println("[ERROR] DataAcquisitionSchemaEvent. uri: e.Message: " + e.getMessage());
	}
	
	return event;
    }
    
    public static List<DataAcquisitionSchemaEvent> findBySchema (String schemaUri) {
	//System.out.println("Looking for data acuisition schema events for " + schemaUri);
	List<DataAcquisitionSchemaEvent> events = new ArrayList<DataAcquisitionSchemaEvent>();
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?uri a hasco:DASchemaEvent . " + 
	    "   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] DataAcquisitionSchemaEvent. Could not find events for schema: " + schemaUri);
	    return events;
	}
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    try {
		if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
		    
		    DataAcquisitionSchemaEvent obj = DataAcquisitionSchemaEvent.find(soln.getResource("uri").getURI());
		    if (obj != null) {
			events.add(obj);
		    }
		}
	    }  catch (Exception e) {
		System.out.println("[ERROR] DataAcquisitionSchemaEvent. uri: e.Message: " + e.getMessage());
	    }
	    
	}
	return events;
    }
    
    public void save() {
        delete();  // delete any existing triple for the current DASE                                                        
        //System.out.println("Saving <" + uri + ">");                                                                        
        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save DASE without assigning an URI");
            return;
        }
        if (partOfSchema == null || partOfSchema.equals("")) {
            System.out.println("[ERROR] Trying to save DASE without assigning DAS's URI");
            return;
        }
        String insert = "";

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        insert += this.getUri() + " a hasco:DASchemaEvent . ";
        insert += this.getUri() + " rdfs:label  \"" + label + "\" . ";
        if (partOfSchema.startsWith("http")) {
            insert += this.getUri() + " hasco:partOfSchema <" + partOfSchema + "> .  ";
        } else {
            insert += this.getUri() + " hasco:partOfSchema " + partOfSchema + " .  ";
        }
        if (!entity.equals("")) {
            insert += this.getUri() + " hasco:hasEntity "  + entity + " .  ";
        }
        if (!unit.equals("")) {
            insert += this.getUri() + " hasco:hasUnit " + unit + " .  ";
        }
        //insert += this.getUri() + " hasco:hasSource " + " .  ";                                                            
        //insert += this.getUri() + " hasco:isPIConfirmed " + " .  ";                                                        
        insert += LINE_LAST;
        System.out.println("DASE insert query (pojo's save): <" + insert + ">");
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
        row.put("a", "hasco:DASchemaEvent");
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getLabel());
        row.put("hasco:partOfSchema", ValueCellProcessing.replaceNameSpaceEx(getPartOfSchema()));
        row.put("hasco:hasEntity", this.getEntity());
        row.put("hasco:hasUnit", this.getUnit());
        row.put("hasco:isVirtual", "");
        row.put("hasco:isPIConfirmed", "false");
	rows.add(row);
        int totalChanged = 0;
        try {
            totalChanged = loader.insertRows("DASchemaEvent", rows);
        } catch (CommandException e) {
            try {
                totalChanged = loader.updateRows("DASchemaEvent", rows);
            } catch (CommandException e2) {
                System.out.println("[ERROR] Could not insert or update DASE(s)");
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
            System.out.println("deleting DASE " + row.get("hasURI"));
        }
        return loader.deleteRows("DASchemaEvent", rows);
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
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.
														METADATA_UPDATE));
        processor.execute();
    }

}
