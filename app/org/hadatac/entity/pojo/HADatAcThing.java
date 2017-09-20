package org.hadatac.entity.pojo;

import java.util.List;
import java.util.Map;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;

public class HADatAcThing {

	String uri;
	String typeUri;
	String label;
	String comment;
	int count = 0;
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			List<String> preValues, FacetHandler facetHandler) {
		return null;
	}
	
	public long getNumberFromSolr(List<String> values, FacetHandler facetHandler) {
		return 0;
	}

	public String getUri() {
		return uri.replace("<","").replace(">","");
	}

	public String getUriNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
	}

	public void setUri(String uri) {
		if (uri == null || uri.equals("")) {
			this.uri = "";
			return;
		}
		this.uri = ValueCellProcessing.replacePrefixEx(uri);
	}

	public String getTypeUri() {
		return typeUri;
	}

	public String getTypeNamespace() {
		return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
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

}
