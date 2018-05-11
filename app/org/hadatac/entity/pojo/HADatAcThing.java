package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.NotImplementedException;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.query.ResultSetFormatter;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.console.http.SPARQLUtils;

public abstract class HADatAcThing {
	String uri;
	String typeUri;
	String field;
	String label;
	String comment;
	int count = 0;
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			Facet facet, FacetHandler facetHandler) {
		return null;
	}
	
	public static String stringify(List<String> preValues, boolean isUri) {
		List<String> finalValues = new ArrayList<String>();
		if (isUri) {
			preValues.forEach((value) -> finalValues.add("<" + value + ">"));
		} else {
			preValues.forEach((value) -> finalValues.add("\"" + value + "\""));
		}
		
		return String.join(" ", finalValues);
	}
	
	public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
		return 0;
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

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
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
    
	public void save() { throw new NotImplementedException("Used unimplemented HADatAcThing.save() method"); }
	public void delete() { throw new NotImplementedException("Used unimplemented HADatAcThing.delete() method"); }
	
	public abstract boolean saveToTripleStore();
	public abstract void deleteFromTripleStore();
	
	public abstract boolean saveToSolr();
	public abstract int deleteFromSolr();
	
	public abstract int saveToLabKey(String userName, String password);
	public abstract int deleteFromLabKey(String userName, String password);
}
