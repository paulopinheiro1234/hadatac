package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.URIUtils;

public class HADatAcThing {

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

	public String getTypeNamespace() {
        if (uri == "" || uri == null || uri.equals("")){
            return "";
        } else {
    		return URIUtils.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
        }
	}

	public void setTypeUri(String typeUri) {
		this.typeUri = typeUri;
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
	
	public void save() {}
	public void delete() {}
	
	public void saveToTripleStore() {}
	public void deleteFromTripleStore() {}
	
	public int saveToSolr() { return 0; }
	public int deleteFromSolr() { return 0; }
	
	public int saveToLabKey(String userName, String password) { return 0; }
	public int deleteFromLabKey(String userName, String password) { return 0; }
}
