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
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;

public class DataAcquisitionSchemaAttribute extends HADatAcThing {

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
    private Map<String, String> relations = new HashMap<String, String>();
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
    }

    public String getUri() {
        if (uri == null) {
            return "";
        } else {
            return uri;
        }
    }

    public String getUriNamespace() {
        return URIUtils.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
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
        return URIUtils.replaceNameSpaceEx(entity.replace("<","").replace(">",""));
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
            return URIUtils.replaceNameSpaceEx(entity);
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
            annotation = URIUtils.replaceNameSpaceEx(entity);
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
        return URIUtils.replaceNameSpaceEx(attribute.replace("<","").replace(">",""));
    }

    public void setAttribute(String attribute) {
        this.attribute = attribute;
        if (attribute == null || attribute.equals("")) {
            this.attributeLabel =  "";
        } else {
            this.attributeLabel = FirstLabel.getLabel(attribute);
        }
        this.isMeta = (DataAcquisitionSchema.METADASA.contains(URIUtils.replaceNameSpaceEx(attribute)));
    }

    public String getAttributeLabel() {
        if (attributeLabel.equals("")) {
            return URIUtils.replaceNameSpaceEx(attribute);
        }
        return attributeLabel;
    }

    public String getAnnotatedAttribute() {
        String annotation;
        if (attributeLabel.equals("")) {
            if (attribute == null || attribute.equals("")) {
                return "";
            }
            annotation = URIUtils.replaceNameSpaceEx(attribute);
        } else {
            annotation = attributeLabel;
        }
        if (!getAttributeNamespace().equals("")) {
            annotation += " [" + getAttributeNamespace() + "]";
        } 
        return annotation;
    }

    public String getInRelationToUri() {
        String inRelationToUri = "";
        for (String key : relations.keySet()) {
            inRelationToUri = relations.get(key);
            break;
        }
        return inRelationToUri;
    }

    public String getInRelationToLabel() {
	String inRelationTo = getInRelationToUri();
        if (inRelationTo == null || inRelationTo.equals("")) {
            return "";
        } else {
            return FirstLabel.getLabel(inRelationTo);
        }
    }

    public String getInRelationToUri(String relationUri) {
        if (relations.containsKey(relationUri)) {
            return relations.get(relationUri);
        }

        return "";
    }

    public void addRelation(String relationUri, String inRelationToUri) {
        relations.put(relationUri, inRelationToUri);
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
        return URIUtils.replaceNameSpaceEx(unit.replace("<","").replace(">",""));
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
        return URIUtils.replaceNameSpaceEx(dasoUri.replace("<","").replace(">",""));
    }

    public String getObjectViewLabel() {
        if (attribute.equals(URIUtils.replaceNameSpaceEx("hasco:originalID"))) {
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
        return URIUtils.replaceNameSpaceEx(daseUri.replace("<","").replace(">",""));
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

    public static int getNumberDASAs() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "select distinct (COUNT(?x) AS ?tot) where {" + 
	    " ?x a <http://hadatac.org/ont/hasco/DASchemaAttribute> } ";

        //System.out.println("Study query: " + query);

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), query);
            
            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
	    }
	} catch (Exception e) {
	    e.printStackTrace();
	}
	return -1;
    }

    public static DataAcquisitionSchemaAttribute find(String dasa_uri) {
        DataAcquisitionSchemaAttribute dasa = null;
        System.out.println("Looking for data acquisition schema attribute with URI <" + dasa_uri + ">");

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?partOfSchema ?hasEntity ?hasAttribute " + 
                " ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed ?relation ?inRelationTo ?label WHERE { \n" + 
                "    <" + dasa_uri + "> a hasco:DASchemaAttribute . \n" + 
                "    <" + dasa_uri + "> hasco:partOfSchema ?partOfSchema . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:hasEntity ?hasEntity } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:hasAttribute ?hasAttribute } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:hasUnit ?hasUnit } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:hasEvent ?hasDASE } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:isAttributeOf ?hasDASO } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:hasSource ?hasSource } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> hasco:isPIConfirmed ?isPIConfirmed } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> sio:Relation ?relation . <" + dasa_uri + "> ?relation ?inRelationTo . } . \n" + 
                "    OPTIONAL { <" + dasa_uri + "> rdfs:label ?label } . \n" +
                "}";

        //System.out.println("DataAcquisitionSchemaAttribute find() queryString: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find DASA with URI: <" + dasa_uri + ">");
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
        String inRelationToUri = "";
        String relationUri = "";

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            labelStr = FirstLabel.getLabel(dasa_uri);

            if (soln.get("partOfSchema") != null) {
                partOfSchemaStr = soln.get("partOfSchema").toString();
            }
            if (soln.get("hasEntity") != null) {
                entityStr = soln.get("hasEntity").toString();
            }
            if (soln.get("hasAttribute") != null) {
                attributeStr = soln.get("hasAttribute").toString();
            }
            if (soln.get("hasUnit") != null) {
                unitStr = soln.get("hasUnit").toString();
            }
            if (soln.get("hasDASO") != null) {
                dasoUriStr = soln.get("hasDASO").toString();
            }
            if (soln.get("hasDASE") != null) {
                daseUriStr = soln.get("hasDASE").toString();
            }
            if (soln.get("inRelationTo") != null) {
                inRelationToUri = soln.get("inRelationTo").toString();
            }
            if (soln.get("relation") != null) {
                relationUri = soln.get("relation").toString();
            }

            if (dasa == null) {
                dasa = new DataAcquisitionSchemaAttribute(
                        dasa_uri,
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

            dasa.addRelation(relationUri, inRelationToUri);
        }

        return dasa;
    }

    public static List<DataAcquisitionSchemaAttribute> findByAttribute(String attributeUri) {
        System.out.println("Looking for data acquisition schema attributes with hasco:hasAttribute " + attributeUri);
        if (attributeUri.startsWith("http")) {
            attributeUri = "<" + attributeUri + ">";
        }
        List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?hasEntity ?schemaUri " + 
                " ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
                "    ?uri a hasco:DASchemaAttribute . " + 
                "    ?uri hasco:hasAttribute " + attributeUri + ". " +
                "    ?uri hasco:partOfSchema ?schemaUri .  " + 
                "} ";
        System.out.println("[DASA] query string = \n" + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find DASA's with attribute: " + attributeUri);
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

    // Given a study URI, 
    // returns a list of DASA's
    // (we need to go study -> data acqusition(s) -> data acqusition schema(s) -> data acquisition schema attributes)
    public static List<DataAcquisitionSchemaAttribute> findByStudy(String studyUri){
        System.out.println("Looking for data acquisition schema attributes from study " + studyUri);
        if (studyUri.startsWith("http")) {
            studyUri = "<" + studyUri + ">";
        }
        List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?hasEntity ?schemaUri ?attrUri" + 
                " ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
                "    ?da hasco:isDataAcquisitionOf " + studyUri + " .  " +
                "    ?da hasco:hasSchema ?schemaUri .  "+
                "    ?uri hasco:partOfSchema ?schemaUri .  " +
                "    ?uri a hasco:DASchemaAttribute . " + 
                "    ?uri hasco:hasAttribute ?attrUri . " +
                "} ";
        System.out.println("[DASA] query string = \n" + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find DASA's with attribute: " + studyUri);
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

    public static List<DataAcquisitionSchemaAttribute> findBySchema(String schemaUri) {
        System.out.println("Looking for data acquisition schema attributes for <" + schemaUri + ">");

        List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?hasEntity ?hasAttribute " + 
                " ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { \n" + 
                " ?uri a hasco:DASchemaAttribute . \n" + 
                " ?uri hasco:partOfSchema <" + schemaUri + "> . \n" + 
                "} ";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find attributes for schema: <" + schemaUri + ">");
            return attributes;
        }

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            try {
                if (soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
                    String uri = soln.getResource("uri").getURI();
                    DataAcquisitionSchemaAttribute attr = find(uri);
                    attributes.add(attr);
                }
            } catch (Exception e1) {
                System.out.println("[ERROR] DataAcquisitionSchemaAttribute.findBySchema() URI: <" + schemaUri + ">");
                e1.printStackTrace();
            }
        }
        attributes.sort(Comparator.comparing(DataAcquisitionSchemaAttribute::getPositionInt));

        return attributes;
    }

    @Override
    public void save() {
        saveToTripleStore();
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", "hasco:DASchemaAttribute");
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getLabel());
        row.put("hasco:partOfSchema", URIUtils.replaceNameSpaceEx(getPartOfSchema()));
        row.put("hasco:hasEntity", this.getEntity());
        row.put("hasco:hasAttribute", this.getAttribute());
        row.put("hasco:hasUnit", this.getUnit());
        row.put("hasco:hasEvent", URIUtils.replaceNameSpaceEx(daseUri));
        row.put("hasco:hasSource", "");
        row.put("hasco:isAttributeOf", URIUtils.replaceNameSpaceEx(dasoUri));
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
    @Override
    public int deleteFromLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
        rows.add(row);

        try {
            return loader.deleteRows("DASchemaAttribute", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete DASA(s)");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean saveToTripleStore() {
        deleteFromTripleStore();

        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save DASA without assigning an URI");
            return false;
        }
        if (partOfSchema == null || partOfSchema.equals("")) {
            System.out.println("[ERROR] Trying to save DASA without assigning DAS's URI");
            return false;
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

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionsName(CollectionUtil.METADATA_UPDATE));
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
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionsName(CollectionUtil.METADATA_UPDATE));
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
