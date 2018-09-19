package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;

public class DataAcquisitionSchemaObject extends HADatAcThing {

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
    private String roleLabel;
    private String inRelationTo;
    private String inRelationToLabel;
    private String relation;
    private String relationLabel;
    private String wasDerivedFrom;

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
            this.entityLabel = FirstLabel.getLabel(entity);
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
        System.out.println("New ROLE : " + role);
        if (role == null || role.equals("")) {
            this.roleLabel = "";
        } else {
            this.roleLabel = FirstLabel.getLabel(role);
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

    public String getInRelationToNamespace() {
        return URIUtils.replaceNameSpaceEx(inRelationTo);
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
            System.out.println("RELATION label -- just relation : <" + URIUtils.replaceNameSpaceEx(relation) + ">");
            return URIUtils.replaceNameSpaceEx(relation);
        }
        System.out.println("RELATION label : <" + relationLabel + ">");
        return relationLabel;
    }

    public static DataAcquisitionSchemaObject find(String uri) {
        System.out.println("Looking for data acquisition schema objects with uri: " + uri);

        DataAcquisitionSchemaObject object = null;
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?entity ?partOfSchema ?role ?inRelationTo ?relation ?inRelationToStr ?wasDerivedFrom WHERE { \n" + 
                "   <" + uri + "> a hasco:DASchemaObject . \n" + 
                "   <" + uri + "> hasco:partOfSchema ?partOfSchema . \n" + 
                "   OPTIONAL { <" + uri + "> hasco:hasEntity ?entity } . \n" + 
                "   OPTIONAL { <" + uri + "> hasco:hasRole ?role } .  \n" + 
                "   OPTIONAL { <" + uri + "> sio:inRelationTo ?inRelationTo } . \n" +
                "   OPTIONAL { <" + uri + "> sio:Relation ?relation } . \n" +
                "   OPTIONAL { <" + uri + "> ?relation ?inRelationTo } . \n" +
                "   OPTIONAL { <" + uri + "> hasco:inRelationToLabel ?inRelationToStr } . \n" +
                "   OPTIONAL { <" + uri + "> <http://hadatac.org/ont/hasco/wasDerivedFrom> ?wasDerivedFrom } . \n" +
                "}";

        //System.out.println("DataAcquisitionSchemaObject find(String uri) query: " + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

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
        String inRelationToLabelStr = "";
        String relationStr = "";
        String wasDerivedFromStr = "";

        try {
            if (soln != null) {

                labelStr = FirstLabel.getLabel(uri);

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
            }
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisitionSchemaObject.find() e.Message: " + e.getMessage());
        }
        
        return object;
    }

    public static List<DataAcquisitionSchemaObject> findBySchema(String schemaUri) {
        System.out.println("Looking for data acquisition schema objects for <" + schemaUri + ">");

        List<DataAcquisitionSchemaObject> objects = new ArrayList<DataAcquisitionSchemaObject>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri ?label ?hasEntity ?hasRole ?inRelationTo ?relation WHERE { \n" + 
                "   ?uri a hasco:DASchemaObject . \n" + 
                "   ?uri hasco:partOfSchema <" + schemaUri + "> . \n" + 
                "}";
        
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
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", "hasco:DASchemaObject");
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getLabel());
        row.put("hasco:partOfSchema", URIUtils.replaceNameSpaceEx(getPartOfSchema()));
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

    @Override
    public int deleteFromLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
        rows.add(row);

        try {
            return loader.deleteRows("DASchemaObject", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete DASO(s)");
            e.printStackTrace();
            return 0;
        }
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
                insert += this.getUri() + " sio:inRelationTo " +  inRelationToStr + " .  ";
            } else {
                insert += this.getUri() + " sio:inRelationTo <" + inRelationToStr + "> .  ";
            }
        }
        if (!relation.equals("")) {
            String relationStr =  URIUtils.replacePrefixEx(relation);
            if (relationStr.startsWith("<")) {
                insert += this.getUri() + " sio:relation " +  relationStr + " .  ";
            } else {
                insert += this.getUri() + " sio:relation <" + relationStr + "> .  ";
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
        String query = "";
        if (this.getUri() == null || this.getUri().equals("")) {
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

