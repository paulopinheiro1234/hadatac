package org.hadatac.entity.pojo;

import java.lang.reflect.Field;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.NotImplementedException;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.metadata.api.MetadataFactory;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.eclipse.rdf4j.model.Model;
import org.hadatac.utils.NameSpaces;

import org.hadatac.utils.CollectionUtil;
import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.ReversedPropertyField;
import org.hadatac.annotations.Subject;
import org.hadatac.console.http.SPARQLUtils;


public abstract class HADatAcThing implements Facetable {

	public static String OWL_THING = "http://www.w3.org/2002/07/owl#Thing";
	
    @Subject
    String uri = "";
    
    @PropertyField(uri="rdf:type")
    String typeUri = "";

    @PropertyField(uri="rdfs:label")
    String label = "";
    
    @PropertyField(uri="rdfs:comment")
    String comment = "";
    
    String field = "";
    String query = "";
    int count = 0;

    String namedGraph = "";
    
    // delete the object or not when deleting the data file it was generated from
    boolean deletable = true;

    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return null;
    }

    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(
            Facet facet, FacetHandler facetHandler) {
        return null;
    }

    public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {
        return null;
    }

    public long getNumber(Facet facet, FacetHandler facetHandler) {
        return 0;
    }

    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        return 0;
    }

    public long getNumberFromTripleStore(Facet facet, FacetHandler facetHandler) {
        return 0;
    }

    public static String stringify(List<String> preValues) {
        List<String> finalValues = new ArrayList<String>();
        preValues.forEach((value) -> {
            if (value.startsWith("http")) {
                if (value.contains("; ")) {
                    finalValues.addAll(Arrays.asList(value.split("; ")).stream()
                            .map(s -> "<" + s + ">")
                            .collect(Collectors.toList()));
                } else {
                    finalValues.add("<" + value + ">");
                }
            } else {
                finalValues.add("\"" + value + "\"");
            }
        });

        return String.join(" ", finalValues);
    }

    public String getUri() {
        return uri.replace("<","").replace(">","");
    }

    public String getUriNamespace() {
        if(uri == "" || uri == null || uri.equals("")){
            return "";
        } else{
            return URIUtils.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
        }
    }

    public void setUri(String uri) {
        if (uri == null || uri.equals("")) {
            this.uri = "";
            return;
        }
		this.uri = URIUtils.replacePrefixEx(uri);
    }

    public String getTypeUri() {
        return typeUri;
    }

    public String getTypeLabel() {
    	if (typeUri == null) {
    		return "";
    	}
    	Entity ent = Entity.find(typeUri);
    	if (ent == null || ent.getLabel() == null) {
    		return "";
    	}
    	return ent.getLabel();
    }
    
    public void setTypeUri(String typeUri) {
        this.typeUri = typeUri;
    }

    public String getTypeNamespace() {
        if (uri == "" || uri == null || uri.equals("")){
            return "";
        } else {
            return URIUtils.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
        }
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getNamedGraph() {
        return namedGraph;
    }

    public void setNamedGraph(String namedGraph) {
        this.namedGraph = namedGraph;
    }
    
    public boolean getDeletable() {
        return deletable;
    }

    public void setDeletable(boolean deletable) {
        this.deletable = deletable;
    }

    public static List<String> getLabels(String uri) {
    	List<String> results = new ArrayList<String>();
    	if (uri == null || uri.equals("")) {
    		return results;
    	}
        if (uri.startsWith("http")) {
            uri = "<" + uri.trim() + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?label WHERE { \n" + 
                "  " + uri + " rdfs:label ?label . \n" + 
                "}";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln.get("label") != null && !soln.get("label").toString().isEmpty()) {
                results.add(soln.get("label").toString().replace("@en", ""));
            }
        }

        return results;
    }

    public static String getLabel(String uri) {
        List<String> labels = getLabels(uri);
        if (labels.size() > 0) {
            return labels.get(0);
        }

        return "";
    }

    public static String getShortestLabel(String uri) {
        List<String> labels = getLabels(uri);
        if (labels.size() > 0) {
            labels.sort(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    return Integer.compare(o1.length(), o2.length());
                }
            });

            return labels.get(0);
        }

        return "";
    }

    public static int getNumberInstances() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "select (COUNT(?categ) as ?tot) where " +  
                " { SELECT ?i (COUNT(?i) as ?categ) " +
                "     WHERE {" + 
                "             ?i a ?c . " +
                "     } " +
                " GROUP BY ?i " + 
                " }";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return -1;
    }

    public void save() { throw new NotImplementedException("Used unimplemented HADatAcThing.save() method"); }
    public void delete() { throw new NotImplementedException("Used unimplemented HADatAcThing.delete() method"); }

    @SuppressWarnings("unchecked")
    public boolean saveToTripleStore() {
        deleteFromTripleStore();

        Map<String, Object> row = new HashMap<String, Object>();
        List<Map<String, Object>> reversed_rows = new ArrayList<Map<String, Object>>();

        try {
            Class<?> currentClass = getClass();
            while(currentClass != null) {                
                for (Field field: currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    if (field.isAnnotationPresent(Subject.class)) {
                        String uri = (String)field.get(this);
                        if (URIUtils.isValidURI(uri)) {
                            row.put("hasURI", uri);
                        } else {
                            return false;
                        }
                    }
                    
                    if (field.isAnnotationPresent(ReversedPropertyField.class)) {
                        ReversedPropertyField reversedPropertyField = field.getAnnotation(ReversedPropertyField.class);
                        String propertyUri = reversedPropertyField.uri();
                        
                        if (field.getType().equals(String.class)) {
                            String value = (String)field.get(this);
                            if (!value.isEmpty()) {
                                Map<String, Object> rvs_row = new HashMap<String, Object>();
                                rvs_row.put(propertyUri, value);
                                reversed_rows.add(rvs_row);
                            }
                        }
                    }

                    if (field.isAnnotationPresent(PropertyField.class)) {
                        PropertyField propertyField = field.getAnnotation(PropertyField.class);
                        String propertyUri = propertyField.uri();

                        if (field.getType().equals(String.class)) {
                            String value = (String)field.get(this);
                            if (!value.isEmpty()) {
                                row.put(propertyUri, value);
                            }
                        }

                        if (field.getType().equals(List.class)) {
                            List<?> list = (List<?>)field.get(this);
                            if (!list.isEmpty() && list.get(0) instanceof String) {
                                for (String element : (List<String>)list) {
                                    if (!element.isEmpty()) {
                                        row.put(propertyUri, element);
                                    }
                                }
                            }
                        }

                        if (field.getType().equals(Integer.class)) {
                            row.put(propertyUri, ((Integer)field.get(this)).toString());
                        }

                        if (field.getType().equals(Double.class)) {
                            row.put(propertyUri, ((Double)field.get(this)).toString());
                        }

                        if (field.getType().equals(Long.class)) {
                            row.put(propertyUri, ((Long)field.get(this)).toString());
                        }
                    }
                }
                
                currentClass = currentClass.getSuperclass();
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        if (!row.containsKey("hasURI")) {
            return false;
        }
        
        String objUri = (String)row.get("hasURI");
        for (Map<String, Object> rvs_row : reversed_rows) {
            for (String key : rvs_row.keySet()) {
                String value = (String)row.get(key);
                if (URIUtils.isValidURI(value)) {
                    rvs_row.put("hasURI", value);
                    rvs_row.remove(key);
                    rvs_row.put(key, objUri);
                } else {
                    continue;
                }
            }
        }
        reversed_rows.add(row);

        Model model = MetadataFactory.createModel(reversed_rows, getNamedGraph());
        int numCommitted = MetadataFactory.commitModelToTripleStore(
                model, CollectionUtil.getCollectionPath(
                        CollectionUtil.Collection.METADATA_GRAPH));

        return numCommitted >= 0;
    }
    
    public void fromStatement(Statement statement) {
        String predicate = statement.getPredicate().getURI();
        String object = statement.getObject().toString();
        
        fromPredicateObject(predicate, object);
    }
    
    public void fromQuerySolution(QuerySolution solnFromDescribe) {
        // build object from results of DESCRIBE query
        String predicate = solnFromDescribe.get("predicate").toString();
        String object = solnFromDescribe.get("object").toString();
        
        fromPredicateObject(predicate, object);
    }
    
    @SuppressWarnings("unchecked")
    public void fromPredicateObject(String predicate, String object) {
        try {
            Class<?> currentClass = getClass();
            while(currentClass != null) {                
                for (Field field: currentClass.getDeclaredFields()) {
                    field.setAccessible(true);
                    
                    if (field.isAnnotationPresent(PropertyField.class)) {
                        PropertyField propertyField = field.getAnnotation(PropertyField.class);
                        String propertyUri = URIUtils.replacePrefixEx(propertyField.uri());
                        
                        if (predicate.equals(propertyUri)) {
                            if (field.getType().equals(String.class)) {
                                field.set(this, object);
                            }

                            if (field.getType().equals(List.class)) {
                                List<String> list = (List<String>)field.get(this);
                                if (!list.contains(object)) {
                                    list.add(object);
                                }
                            }

                            if (field.getType().equals(Integer.class)) {
                                field.set(this, Integer.parseInt(object));
                            }

                            if (field.getType().equals(Double.class)) {
                                field.set(this, Double.parseDouble(object));
                            }

                            if (field.getType().equals(Long.class)) {
                                field.set(this, Long.parseLong(object));
                            }
                        }
                    }
                }
                
                currentClass = currentClass.getSuperclass();
            }
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void deleteFromTripleStore() {       
        String query = "";
        if (getUri() == null || getUri().equals("")) {
            return;
        }

        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " DELETE WHERE { \n";
        if (getUri().startsWith("http")) {
            query += "<" + this.getUri() + ">";
        } else {
            query += this.getUri();
        }
        query += " ?p ?o . \n";
        query += " } ";

        //System.out.println("Delete from triplestore query: " + query);
        
        UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
        processor.execute();

        //System.out.println("Deleted <" + getUri() + "> from triple store");
    }

    public abstract boolean saveToSolr();
    public abstract int deleteFromSolr();

}
