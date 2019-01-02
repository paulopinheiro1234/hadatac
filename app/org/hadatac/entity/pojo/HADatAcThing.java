package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.views.dataacquisitionsearch.Facetable;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.NameSpaces;

import org.hadatac.utils.CollectionUtil;
import org.hadatac.console.http.SPARQLUtils;


public abstract class HADatAcThing implements Facetable {
    String uri = "";
    String typeUri = "";
    String field = "";
    String label = "";
    String comment = "";
    String query = "";
    int count = 0;

    String namedGraph = "";
    
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
                finalValues.add("<" + value + ">");
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
    
    public static List<String> getLabels(String uri) {
        List<String> results = new ArrayList<String>();
        
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

    public abstract boolean saveToTripleStore();
    public abstract void deleteFromTripleStore();

    public abstract boolean saveToSolr();
    public abstract int deleteFromSolr();

    public abstract int saveToLabKey(String userName, String password);
    public abstract int deleteFromLabKey(String userName, String password);
}
