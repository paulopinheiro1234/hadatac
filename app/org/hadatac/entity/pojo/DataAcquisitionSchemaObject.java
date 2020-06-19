package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;

public class DataAcquisitionSchemaObject extends HADatAcThing {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:DASchemaObject;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "DASO-";

    private static Map<String, DataAcquisitionSchemaObject> DASOCache;

    private String uri;
    private String label;
    private String partOfSchema;
    private String position;
    private int positionInt;
    private int tempPositionInt;
    private String entity;
    private String entityLabel;
    private String role;
    private String roleLabel;
    private String inRelationTo;
    private String inRelationToLabel;
    private String relation;
    private String relationLabel;
    private String wasDerivedFrom;
    private String alternativeName = "";

    private static Map<String, DataAcquisitionSchemaObject> getCache() {
        if (DASOCache == null) {
            DASOCache = new HashMap<String, DataAcquisitionSchemaObject>(); 
        }
        return DASOCache;
    }

    public static void resetCache() {
        DASOCache = null;
    }

    public DataAcquisitionSchemaObject() {}

    public DataAcquisitionSchemaObject(String uri, 
            String label, 
            String partOfSchema,
            String position,
            String entity, 
            String role, 
            String inRelationTo, 
            String inRelationToLabel,
            String wasDerivedFrom,
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
        this.setEntity(entity);
        this.setRole(role);
        this.setInRelationTo(inRelationTo);
        this.setWasDerivedFrom(wasDerivedFrom);
        this.setRelation(relation);
        DataAcquisitionSchemaObject.getCache();
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
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
            this.entityLabel = FirstLabel.getPrettyLabel(entity);
        }
    }

    public String getEntityNamespace() {
        return URIUtils.replaceNameSpaceEx(entity);
    }

    public String getEntityLabel(Map<String, String> codeMappings) {
        if (entity == null || entityLabel.equals("")) {
            String newLabel = URIUtils.replaceNameSpaceEx(entity);
            if (newLabel.contains(":")) {
                if (codeMappings.containsKey(newLabel)){
                    return codeMappings.get(newLabel);
                } else {
                    return newLabel.split("\\:")[1];
                }
            } else {
                return newLabel;
            }
        } else {
            return entityLabel;
        }
    }

    public String getEntityLabel() {
        if (entity == null || entityLabel.equals("")) {
            return URIUtils.replaceNameSpaceEx(entity);
        }
        return entityLabel;
    }

    public String getRoleLabel() {
        if (role == null || roleLabel.equals("")) {
            return URIUtils.replaceNameSpaceEx(role);
        }
        return roleLabel;
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

    public String getRole() {
        return this.role;
    }

    public void setRole(String role) {
        //this.role = role;
        this.role = role;
        //System.out.println("New ROLE : " + role);
        if (role == null || role.equals("")) {
            this.roleLabel = "";
        } else {
            this.roleLabel = FirstLabel.getPrettyLabel(role);
        }
    }

    public String getInRelationTo() {
        return inRelationTo;
    }

    public String getWasDerivedFrom() {
        return wasDerivedFrom;
    }

    public void setWasDerivedFrom(String wasDerivedFrom) {
        this.wasDerivedFrom = wasDerivedFrom;
    }

    public String getAlternativeName() {
        return alternativeName;
    }

    public void setAlternativeName(String alternativeName) {
        this.alternativeName = alternativeName;
    }

    public String getInRelationToNamespace() {
        return URIUtils.replaceNameSpaceEx(inRelationTo);
    }

    public void setInRelationTo(String inRelationTo) {
        this.inRelationTo = inRelationTo;
        if (inRelationTo == null || inRelationTo.equals("")) {
            this.inRelationToLabel = "";
        } else {
            this.inRelationToLabel = FirstLabel.getPrettyLabel(inRelationTo);
        }
    }

    public String getInRelationToLabel() {
        return inRelationToLabel;
    }

    public String getRelation() {
        return relation;
    }

    public String getRelationNamespace() {
        return URIUtils.replaceNameSpaceEx(relation);
    }

    public void setRelation(String relation) {
        this.relation = relation;
        //System.out.println("New RELATION : " + relation);
        if (relation == null || relation.equals("")) {
            this.relationLabel = "";
        } else {
            this.relationLabel = FirstLabel.getPrettyLabel(relation);
        }
    }

    public String getRelationLabel() {
        if (relationLabel == null || relationLabel.equals("")) {
            //System.out.println("RELATION label -- just relation : <" + relation + ">");
            //System.out.println("RELATION label -- just relation : <" + URIUtils.replaceNameSpaceEx(relation) + ">");
            return URIUtils.replaceNameSpaceEx(relation);
        }
        //System.out.println("RELATION label : <" + relationLabel + ">");
        return relationLabel;
    }

    public static DataAcquisitionSchemaObject find(String uri) {
        if (DataAcquisitionSchemaObject.getCache().containsKey(uri)) {
            return DataAcquisitionSchemaObject.getCache().get(uri);
        }
        
        //System.out.println("Looking for data acquisition schema object with uri: " + uri);

        DataAcquisitionSchemaObject object = null;
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?entity ?partOfSchema ?role ?inRelationTo ?relation ?inRelationToStr ?wasDerivedFrom ?alternativeName WHERE { \n" + 
                "   <" + uri + "> a hasco:DASchemaObject . \n" + 
                "   <" + uri + "> hasco:partOfSchema ?partOfSchema . \n" + 
                "   OPTIONAL { <" + uri + "> hasco:hasEntity ?entity } . \n" + 
                "   OPTIONAL { <" + uri + "> hasco:hasRole ?role } .  \n" + 
                "   OPTIONAL { <" + uri + "> sio:SIO_000668 ?inRelationTo } . \n" +
                "   OPTIONAL { <" + uri + "> hasco:Relation ?relation } . \n" +
                "   OPTIONAL { <" + uri + "> ?relation ?inRelationTo } . \n" +
                "   OPTIONAL { <" + uri + "> hasco:inRelationToLabel ?inRelationToStr } . \n" +
                "   OPTIONAL { <" + uri + "> hasco:wasDerivedFrom ?wasDerivedFrom } . \n" +
                "   OPTIONAL { <" + uri + "> dcterms:alternativeName ?alternativeName } . \n" +
                "}";

        //System.out.println("DataAcquisitionSchemaObject find(String uri) query: " + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find object with uri: " + uri);
            DataAcquisitionSchemaObject.getCache().put(uri, null);
            return null;
        }

        QuerySolution soln = resultsrw.next();
        String labelStr = "";
        String partOfSchemaStr = "";
        String positionStr = "";
        String entityStr = "";
        String roleStr = "";
        String inRelationToStr = "";
        String inRelationToLabelStr = "";
        String relationStr = "";
        String wasDerivedFromStr = "";
        String alternativeName = "";

        try {
            if (soln != null) {

                labelStr = FirstLabel.getPrettyLabel(uri);

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
                    if (soln.getResource("role") != null && soln.getResource("role").getURI() != null) {
                        roleStr = soln.getResource("role").getURI();
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
                    if (soln.getResource("inRelationToStr") != null) {
                        inRelationToLabelStr = soln.getResource("inRelationToStr").toString();
                    }
                } catch (Exception e1) {
                    inRelationToLabelStr = "";
                }

                try {
                    if (soln.getLiteral("wasDerivedFrom") != null) {
                        wasDerivedFromStr = soln.getLiteral("wasDerivedFrom").toString();
                    }
                } catch (Exception e1) {
                    wasDerivedFromStr = "";
                }

                try {
                    if (soln.getLiteral("alternativeName") != null) {
                        alternativeName = soln.getLiteral("alternativeName").toString();
                    }
                } catch (Exception e1) {
                    alternativeName = "";
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
                        inRelationToLabelStr,
                        wasDerivedFromStr,
                        relationStr);
                object.setAlternativeName(alternativeName);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisitionSchemaObject.find() e.Message: " + e.getMessage());
        }

        DataAcquisitionSchemaObject.getCache().put(uri, object);
        return object;
    }

    public static List<String> findUriBySchema(String schemaUri) {
        //System.out.println("Looking for data acquisition schema objects for <" + schemaUri + ">");

        List<String> objectUris = new ArrayList<String>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { \n" + 
                "   ?uri a hasco:DASchemaObject . \n" + 
                "   ?uri hasco:partOfSchema <" + schemaUri + "> . \n" + 
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] DataAcquisitionSchemaObject. Could not find objects for schema: " + schemaUri);
            return objectUris;
        }

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            try {
                if (soln != null && soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
                    String uriStr = soln.getResource("uri").getURI();
                    if (uriStr != null) {
                        objectUris.add(uriStr);
                    }
                }
            }  catch (Exception e) {
                System.out.println("[ERROR] DataAcquisitionSchemaObject.findBySchema() e.Message: " + e.getMessage());
            }
        }

        return objectUris;
    }

    public static List<DataAcquisitionSchemaObject> findBySchema(String schemaUri) {
        //System.out.println("Looking for data acquisition schema objects for <" + schemaUri + ">");

        List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?label ?hasEntity ?hasRole ?inRelationTo ?relation WHERE { \n" + 
                "   ?uri a hasco:DASchemaObject . \n" + 
                "   ?uri hasco:partOfSchema <" + schemaUri + "> . \n" + 
                "   ?uri rdfs:label ?label . \n" + 
                "} " +
                "ORDER BY ?label";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

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
                System.out.println("[ERROR] DataAcquisitionSchemaObject.findBySchema() e.Message: " + e.getMessage());
            }
        }

        return objects;
    }

    public static DataAcquisitionSchemaObject findByLabelInSchema(String schemaUri, String label) {
        //System.out.println("DataAcquisitionSchemaObject: label = [" + label + "]");
        List<DataAcquisitionSchemaObject> schemaList = findBySchema(schemaUri);
        for (DataAcquisitionSchemaObject daso : schemaList) {
            //System.out.println("DataAcquisitionSchemaObject: label in daso = [" + daso.getLabel() + "]");
            if (daso.getLabel() != null && daso.getLabel().equals(label)) {
                return daso;
            }
        }
        return null;
    }

    public static String findUriFromRole(String newInRelationTo, List<DataAcquisitionSchemaObject> objects) {
        if (newInRelationTo == null) {
            return "";
        }
        if (newInRelationTo.equals("DefaultObject")) {
            return URIUtils.replacePrefixEx("hasco:DefaultObject");
        }
        for (DataAcquisitionSchemaObject daso : objects) {
            if (daso.getRole().equals(newInRelationTo)) {
                return URIUtils.replacePrefixEx(daso.getUri());
            }
        } 
        return "";
    }

    @Override
    public boolean saveToTripleStore() {
        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save DASO without assigning an URI");
            return false;
        }
        if (partOfSchema == null || partOfSchema.equals("")) {
            System.out.println("[ERROR] Trying to save DASO without assigning DAS's URI");
            return false;
        }

        deleteFromTripleStore();

        String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }

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
            String inRelationToStr =  URIUtils.replacePrefixEx(inRelationTo);
            if (inRelationToStr.startsWith("<")) {
                insert += this.getUri() + " sio:SIO_000668 " +  inRelationToStr + " .  ";
            } else {
                insert += this.getUri() + " sio:SIO_000668 <" + inRelationToStr + "> .  ";
            }
        }
        if (!relation.equals("")) {
            String relationStr =  URIUtils.replacePrefixEx(relation);
            if (relationStr.startsWith("<")) {
                insert += this.getUri() + " hasco:Relation " +  relationStr + " .  ";
            } else {
                insert += this.getUri() + " hasco:Relation <" + relationStr + "> .  ";
            }
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
        super.deleteFromTripleStore();
        DataAcquisitionSchemaObject.resetCache();
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

