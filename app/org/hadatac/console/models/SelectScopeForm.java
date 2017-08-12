package org.hadatac.console.models;

public class SelectScopeForm {

    public String newGlobalScopeUri;
    public String newLocalScopeUri;
 
    public SelectScopeForm () {
    }

    public String getNewGlobalScopeUri() {
    	return newGlobalScopeUri;
    }
    
    public void setNewGlobalScopeUri(String newGlobalScopeUri) {
    	this.newGlobalScopeUri = newGlobalScopeUri;
    }

    public String getNewLocalScopeUri() {
    	return newLocalScopeUri;
    }
    
    public void setNewLocalScopeUri(String newLocalScopeUri) {
    	this.newLocalScopeUri = newLocalScopeUri;
    }

}
