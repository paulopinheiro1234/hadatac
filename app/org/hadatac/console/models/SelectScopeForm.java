package org.hadatac.console.models;

public class SelectScopeForm {

    public String newRowScopeUri;
    public String newCellScopeUri;
 
    public SelectScopeForm () {
    }

    public String getNewRowScopeUri() {
    	return newRowScopeUri;
    }
    
    public void setNewRowScopeUri(String newRowScopeUri) {
    	this.newRowScopeUri = newRowScopeUri;
    }

    public String getNewCellScopeUri() {
    	return newCellScopeUri;
    }
    
    public void setNewCellScopeUri(String newCellScopeUri) {
    	this.newCellScopeUri = newCellScopeUri;
    }

}
