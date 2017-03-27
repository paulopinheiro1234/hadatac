package org.hadatac.console.models;

import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

public class CSVAnnotationHandler {
	
	private String datasetUri = "";

    /* After step 1 */
	public String deploymentUri;
    public String deploymentPlatform;
    public String deploymentInstrument;
    /* After step 1 -- at controller time */
    public Map<String,String> deploymentCharacteristics;
    public String dataCollectionUri;
    
    /* After step 2 */
    public String datasetName;
    public String datasetUploadDatetime;
    /* After step 2 -- at controller time */
    public String[] fields;
    
    /* After step 3 */
    //private String[] matching_characteristics;
    //private String[] matching_units;
    /* After step 3 -- at controller time */
    //private String dataset_uri;
    
    public CSVAnnotationHandler() {}
    
    public CSVAnnotationHandler(String d_uri, String d_plat, String d_instr) {
    	deploymentUri = d_uri;
    	deploymentPlatform = d_plat;
    	deploymentInstrument = d_instr;
    }
    
    public String getDatasetUri() {
        return datasetUri;
    }
    
    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }
 
    public void setDeploymentUri(String deploymentUri) {
        this.deploymentUri = deploymentUri;
    }	    
        
    public String getDeploymentUri() {
        return deploymentUri;
    }	    
        
    public String setDeploymentPlatform(String deploymentPlatform) {
        return deploymentPlatform;
    }	    
        
    public String getDeploymentPlatform() {
        return deploymentPlatform;
    }	    
        
    public String getDeploymentInstrument() {
        return deploymentInstrument;
    }	    
        
    public void setDeploymentCharacteristics(Map<String, String> chars) {
        deploymentCharacteristics = chars;
    }	    
        
    public Map<String, String> getDeploymentCharacteristics() {
        return deploymentCharacteristics;
    }	    
        
    public String getDataAcquisitionUri() {
        return dataCollectionUri;
    }	    
        
    public void setDataAcquisitionUri(String dcURI) {
        dataCollectionUri = dcURI;
    }	    
        
    public String getDatasetName() {
        return datasetName;
    }	    
        
    public void setDatasetName(String dsName) {
        datasetName = dsName;
    }	          
 
    public String getDatasetUploadDatetime() {
        return datasetUploadDatetime;
    }	    
        
    public void setDatasetUploadDatetime(String datasetUploadDatetime) {
        this.datasetUploadDatetime = datasetUploadDatetime;
    }	          
 
    public String[] getFields() {
        return fields;
    }	    
        
    public void setFields(String[] fields) {
        this.fields = fields;
    }	          
 
    public String toJSON() {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
            return mapper.writeValueAsString(this);
    	} catch (Exception e) {
    		e.printStackTrace();
    	} 
    	
    	return "";
    }
}
