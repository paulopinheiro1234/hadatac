package org.hadatac.entity.pojo;

import java.util.ArrayList;
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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.hadatac.annotations.PropertyField;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class PossibleValue extends HADatAcClass implements Comparable<PossibleValue> {

    static String className = "hasco:PossibleValue";

    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = " a    sio:SIO_000614;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";

    public PossibleValue () {
        super(className);
    }

    @PropertyField(uri="hasco:isPossibleValueOf")
    private String hasDASAUri;

    @PropertyField(uri="hasco:hasVariable")
    private String hasVariable;

    @PropertyField(uri="hasco:hasCode")
    private String hasCode;

    @PropertyField(uri="hasco:hasCodeLabel")
    private String hasCodeLabel;

    @PropertyField(uri="hasco:hasClass")
    private String hasClass;

    @PropertyField(uri="hasco:hasResource")
    private String hasResource;
   
    @PropertyField(uri="hasco:otherFor")
    private String hasOtherFor;
   
    public String getHasDASAUri() {
        return hasDASAUri;
    }

    public void setHasDASAUri(String hasDASAUri) {
        this.hasDASAUri = hasDASAUri;
    }

    public String getHasVariable() {
        return hasVariable;
    }

    public void setHasVariable(String hasVariable) {
        this.hasVariable = hasVariable;
    }

    public String getHasCode() {
        return hasCode;
    }

    public void setHasCode(String hasCode) {
        this.hasCode = hasCode;
    }

    public String getHasCodeLabel() {
    	if (hasClass == null || hasClass.isEmpty()) {
    			return hasCodeLabel;
    	}
    	if (hasCodeLabel != null && !hasCodeLabel.isEmpty()) {
    		return hasCodeLabel;
    	}
    	Attribute attr = Attribute.find(this.hasClass);
    	if (attr != null && attr.getLabel() != null) {
    		return "[" + attr.getLabel() + "]";
    	}
    	return "";
    }

    public void setHasCodeLabel(String hasCodeLabel) {
        this.hasCodeLabel = hasCodeLabel;
    }

    public String getHasClass() {
        return hasClass;
    }
    
    public String getPrettyHasClass() {
    	return URIUtils.replaceNameSpaceEx(hasClass);
    }
    
    public void setHasClass(String hasClass) {
        this.hasClass = hasClass;
    }

    public String getHasResource() {
        return hasResource;
    }

    public void setHasResource(String hasResource) {
        this.hasResource = hasResource;
    }
    
    public String getHasOtherFor() {
    	return hasOtherFor;
    }
    
    public String getPrettyHasOtherFor() {
    	return URIUtils.replaceNameSpaceEx(hasOtherFor);
    }
    
    public void setHasOtherFor(String hasOtherFor) {
    	this.hasOtherFor = hasOtherFor;
    }
    
    public String getHarmonizedCode() {
    	if (this.hasClass == null || this.hasClass.isEmpty()) {
    		return "";
    	}
    	return Attribute.findHarmonizedCode(this.hasClass);
    }
    
    public static List<PossibleValue> find() {
        List<PossibleValue> codebook = new ArrayList<PossibleValue>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri rdfs:subClassOf* " + className + " . " +
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            PossibleValue code = find(soln.getResource("uri").getURI());
            codebook.add(code);
        }

        java.util.Collections.sort((List<PossibleValue>) codebook);
        return codebook;
    }

    public static List<PossibleValue> findBySchema(String schemaUri) {

        System.out.println("SchemaUri: " + schemaUri);

    	//System.out.println("PossibleValue.findBySchema: SchemaUri=" + schemaUri);
    	
    	List<PossibleValue> possibleValues = new ArrayList<PossibleValue>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?uri WHERE { \n"
                + " ?uri a hasco:PossibleValue . \n"
                + " ?uri hasco:isPossibleValueOf ?daso_or_dasa . \n"
                + " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . \n"
                + " OPTIONAL { ?uri hasco:hasVariable ?variable . } \n"
                + " OPTIONAL { ?uri hasco:hasCode ?code . } \n"
                + " } \n"
                + " ORDER BY ?variable ?code ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                String pvUri = "";
                QuerySolution soln = resultsrw.next();
                if (soln.get("uri") != null && !soln.get("uri").toString().isEmpty()) {
                    pvUri = soln.get("uri").toString();
                    if (pvUri != null) {
                        PossibleValue pv = find(pvUri);
                        if (pv != null) {
                            possibleValues.add(pv);
                        }
                    }
                }

            }
        } catch (Exception e) {
            System.out.println("[ERROR] PossibleValue.findBySchema(): " + e.getMessage());
            e.printStackTrace();
        }

        return possibleValues;
    }

    public static List<PossibleValue> findByVariable(String variableUri) {    	

    	//System.out.println("      VariableUri: " + variableUri);
    	
    	List<PossibleValue> possibleValues = new ArrayList<PossibleValue>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?uri WHERE { \n"
                + " ?uri a hasco:PossibleValue . \n"
                + " ?uri hasco:isPossibleValueOf <" + variableUri + "> .  \n"
                + " } \n "
                + " ORDER BY ?uri ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                String pvUri = "";
                QuerySolution soln = resultsrw.next();
                if (soln.get("uri") != null && !soln.get("uri").toString().isEmpty()) {
                    pvUri = soln.get("uri").toString();
                    if (pvUri != null) {
                    	PossibleValue pv = find(pvUri);
                    	if (pv != null) {
                    		possibleValues.add(pv);
                    	}
                    }
                } 

            }
        } catch (Exception e) {
            System.out.println("[ERROR] PossibleValue.findByVariable(): " + e.getMessage());
            e.printStackTrace();
        }

        return possibleValues;
    }

    public static String findCodeValue(String dasa_uri, String code) {
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?codeClass ?codeResource WHERE {"
                + " ?possibleValue a hasco:PossibleValue . "
                + " ?possibleValue hasco:isPossibleValueOf <" + dasa_uri + "> . "
                + " ?possibleValue hasco:hasCode ?code . "
                + " ?possibleValue hasco:hasClass ?codeClass . "
                + " FILTER (lcase(str(?code)) = \"" + code + "\") "
                + " }";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            try {
                if (null != soln.getResource("codeClass")) {
                    String classUri = soln.getResource("codeClass").toString();
                    if (classUri.length() != 0) {
                        return URIUtils.replacePrefixEx(classUri);
                    }
                }
            } catch (Exception e1) {
                return null;
            }
        }

        return null;
    }

    public static Map<String, Map<String, String>> findPossibleValues(String schemaUri) {
        Map<String, Map<String, String>> mapPossibleValues = new HashMap<String, Map<String, String>>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?daso_or_dasa ?codeClass ?code ?codeLabel ?resource ?other WHERE { \n"
                + " ?possibleValue a hasco:PossibleValue . \n"
                + " ?possibleValue hasco:isPossibleValueOf ?daso_or_dasa . \n"
                + " ?possibleValue hasco:hasCode ?code . \n"
                + " ?daso_or_dasa hasco:partOfSchema <" + schemaUri + "> . \n"
                + " OPTIONAL { ?possibleValue hasco:hasClass ?codeClass } . \n"
                + " OPTIONAL { ?possibleValue hasco:hasResource ?resource } . \n"
                + " OPTIONAL { ?possibleValue hasco:hasCodeLabel ?codeLabel } . \n"
                + " }";

        //System.out.println("findPossibleValues query: \n" + queryString);

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        try {
            while (resultsrw.hasNext()) {
                String classUri = "";
                QuerySolution soln = resultsrw.next();
                if (soln.get("codeClass") != null && !soln.get("codeClass").toString().isEmpty()) {
                    classUri = soln.get("codeClass").toString();
                } else if (soln.get("resource") != null && !soln.get("resource").toString().isEmpty()) {
                    classUri = soln.get("resource").toString();
                } else if (soln.get("codeLabel") != null && !soln.get("codeLabel").toString().isEmpty()) {
                    // No code class is given, use code label instead
                    classUri = soln.get("codeLabel").toString();
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
            System.out.println("[ERROR] PossibleValue.findPossibleValues(): " + e.getMessage());
            e.printStackTrace();
        }

        return mapPossibleValues;
    }

    public static PossibleValue find(String uri) {
        PossibleValue code = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

        code = new PossibleValue();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                code.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/isPossibleValueOf")) {
                code.setHasDASAUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasCode")) {
                code.setHasCode(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasVariable")) {
                code.setHasVariable(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasCodeLabel")) {
                code.setHasCodeLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#subClassOf")) {
                code.setSuperUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasClass")) {
            	try {
            		code.setHasClass(object.asResource().getURI());
            	} catch (Exception e) {
            	}
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/otherFor")) {
            	try {
            	   code.setHasOtherFor(object.asResource().getURI());
            	} catch (Exception e) {
            	}
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasResource")) {
                code.setHasResource(object.asResource().getURI());
            }
        }

        code.setUri(uri);
        code.setLocalName(uri.substring(uri.indexOf('#') + 1));
        if (code.getLabel() == null || code.getLabel().equals("")) {
            code.setLabel(code.getLocalName());
        }

        return code;
    }

    public boolean deleteHasClass() {
        String query = "";
        String uri = "";
        if (getUri() == null || getUri().equals("")) {
            return false;
        }

        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " DELETE WHERE { \n";

        if (getUri().startsWith("http")) {
            uri += "<" + this.getUri() + ">";
        } else {
            uri += this.getUri();
        }

        query += uri + " <http://hadatac.org/ont/hasco/hasClass> ?o . \n";
        query += " } ";

        try {
	        UpdateRequest request = UpdateFactory.create(query);
	        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
	                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
	        processor.execute();
        } catch (Exception e) {
            System.out.println("[ERROR] Possiblevalue.java: QueryParseException due to update query: " + query);
        	return false;
        }

        //System.out.println("Deleted hasco:hasClass property of <" + getUri() + "> from triple store");
    	
        return true;
        
    }
    
    public boolean saveHasClass() {
    	if (!deleteHasClass()) {
    		return false;
    	};
    	
    	String insert = "";
        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;
        
        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }
        
        insert += "<" + this.getUri() + ">  ";
        insert += LINE3;

        if (this.getHasClass() != null && !this.getHasClass().isEmpty()) {
        	insert += " <http://hadatac.org/ont/hasco/hasClass> <" + this.getHasClass() + "> ;   ";
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
        } catch (Exception e) {
            System.out.println("[ERROR] PossibleValue.java: QueryParseException due to update query: " + insert);
            return false;
        }

        //System.out.println("Added hasco:hasClass property of <" + getUri() + "> from triple store");
    	

        return true;
    	
    }
    
    @Override
    public void save() {
        saveToTripleStore();
    }
    
    @Override
    public int compareTo(PossibleValue another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getLocalName().compareTo(another.getLocalName());
    }
}
