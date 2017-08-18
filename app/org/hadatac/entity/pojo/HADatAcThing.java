package org.hadatac.entity.pojo;

import org.hadatac.metadata.loader.ValueCellProcessing;

public class HADatAcThing {
	
    String uri;
    String typeUri;
    String label;
    String comment;

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
    
    public String getComment() {
	return comment;
    }
    
    public void setComment(String comment) {
	this.comment = comment;
    }	
    
}
