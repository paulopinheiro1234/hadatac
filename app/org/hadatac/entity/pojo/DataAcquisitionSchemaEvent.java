package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.HashMap;

import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;


public class DataAcquisitionSchemaEvent extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:DASchemaEvent;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "DASE-";

    private String uri = "";
    private List<String> types = new ArrayList<String>();
    private String label = "";
    private String partOfSchema = "";
    private String inRelationToUri = "";
    private String relationUri = "";
    private String isVirtual = "";
    private String isPIConfirmed = "";
    private String entity = "";
    private String entityLabel = "";
    private String unit = "";
    private String unitLabel = "";

    public DataAcquisitionSchemaEvent() {
    }

    public DataAcquisitionSchemaEvent(
            String uri, 
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

    public List<String> getTypes() {
        return types;
    }

    public void setTypes(List<String> types) {
        this.types = types;
    }

    public void addType(String type) {
        this.types.add(type);
    }

    public String getInRelationToUri() {
        return inRelationToUri;
    }

    public void setInRelationToUri(String inRelationToUri) {
        this.inRelationToUri = inRelationToUri;
    }

    public String getRelationUri() {
        return relationUri;
    }

    public void setRelationUri(String relationUri) {
        this.relationUri = relationUri;
    }

    public String getIsVirtual() {
        return isVirtual;
    }

    public void setIsVirtual(String isVirtual) {
        this.isVirtual = isVirtual;
    }

    public String getIsPIConfirmed() {
        return isPIConfirmed;
    }

    public void setIsPIConfirmed(String isPIConfirmed) {
        this.isPIConfirmed = isPIConfirmed;
    }

    public String getUriNamespace() {
        return URIUtils.replaceNameSpaceEx(uri);
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
        return URIUtils.replaceNameSpaceEx(entity);
    }

    public String getEntityLabel() {
        if (entityLabel.equals("")) {
            return URIUtils.replaceNameSpaceEx(entity);
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
        return URIUtils.replaceNameSpaceEx(unit);
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
            return URIUtils.replaceNameSpaceEx(unit);
        }
        return unitLabel;
    }

    public String getAnnotatedUnit() {
        String annotation;
        if (unitLabel.equals("")) {
            if (unit == null || unit.equals("")) {
                return "";
            }
            annotation = URIUtils.replaceNameSpaceEx(unit);
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
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    public static List<DataAcquisitionSchemaEvent> findBySchema(String schemaUri) {
        List<DataAcquisitionSchemaEvent> events = new ArrayList<DataAcquisitionSchemaEvent>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { " + 
                "   ?uri a hasco:DASchemaEvent . " + 
                "   ?uri hasco:partOfSchema <" + schemaUri + "> .  " + 
                "}";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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

    @Override
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", String.join(", ", getTypes().stream()
                .map(uri -> URIUtils.replaceNameSpaceEx(uri))
                .collect(Collectors.toList())));
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getComment());
        row.put("hasco:partOfSchema", URIUtils.replaceNameSpaceEx(getPartOfSchema()));
        row.put("hasco:hasEntity", URIUtils.replaceNameSpaceEx(getEntity()));
        row.put("hasco:hasUnit", URIUtils.replaceNameSpaceEx(getUnit()));
        row.put("sio:inRelationTo", URIUtils.replaceNameSpaceEx(getInRelationToUri()));
        row.put("sio:Relation", URIUtils.replaceNameSpaceEx(getRelationUri()));
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
    @Override
    public int deleteFromLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
        rows.add(row);

        try {
            return loader.deleteRows("DASchemaEvent", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete DASE(s)");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean saveToTripleStore() {
        if (getUri() == null || getUri().equals("")) {
            System.out.println("[ERROR] Trying to save DASE without assigning an URI");
            return false;
        }
        if (getPartOfSchema() == null || getPartOfSchema().equals("")) {
            System.out.println("[ERROR] Trying to save DASE without assigning DAS's URI");
            return false;
        }

        deleteFromTripleStore();

        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
        insert += " <" + getUri() + "> a hasco:DASchemaEvent . ";
        for (String type : getTypes()) {
            insert += " <" + getUri() + "> a <" + type + "> . ";
        }
        insert += " <" + getUri() + "> rdfs:label \"" + getLabel() + "\" . ";
        if (!getPartOfSchema().equals("")) {
            insert += " <" + getUri() + "> hasco:partOfSchema <" + getPartOfSchema() + "> .  ";
        }
        if (getComment() != null && !getComment().equals("")) {
            insert += " <" + getUri() + "> rdfs:comment \""  + getComment() + "\" .  ";
        }
        if (!getEntity().equals("")) {
            insert += " <" + getUri() + "> hasco:hasEntity <"  + getEntity() + "> .  ";
        }
        if (!getUnit().equals("")) {
            insert += " <" + getUri() + "> hasco:hasUnit <" + getUnit() + "> .  ";
        }         
        if (!getInRelationToUri().equals("")) {
            insert += " <" + getUri() + "> sio:inRelationTo <" + getInRelationToUri() + "> .  ";
        }  
        if (!getRelationUri().equals("")) {
            insert += " <" + getUri() + "> sio:Relation <" + getRelationUri() + "> .  ";
        }
        if (!getIsVirtual().equals("")) {
            insert += " <" + getUri() + "> hasco:isVirtual \"" + getIsVirtual() + "\" .  ";
        }
        if (!getIsPIConfirmed().equals("")) {
            insert += " <" + getUri() + "> hasco:isPIConfirmed \"" + getIsPIConfirmed() + "\" . ";
        }
        
        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }

    @Override
    public void deleteFromTripleStore() {
        String query = "";
        if (getUri() == null || getUri().equals("")) {
            return;
        }
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += DELETE_LINE1;
        if (getUri().startsWith("http")) {
            query += "<" + getUri() + ">";
        } else {
            query += getUri();
        }
        query += DELETE_LINE3;
        query += LINE_LAST;                                        
        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        processor.execute();
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }

    @Override
    public int deleteFromSolr() {
        return 0;
    }
}
