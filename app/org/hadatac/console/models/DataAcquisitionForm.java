package org.hadatac.console.models;

public class DataAcquisitionForm {

    public String newPermission;
    public String newOwner;
    public String newParameter;
    public String newSchema;
 
    public String getNewPermission() {
		return newPermission;
	}
	public void setNewPermission(String newPermission) {
		this.newPermission = newPermission;
	}

    public String getNewOwner() {
		return newOwner;
	}
	public void setNewOwner(String newOwner) {
		this.newOwner = newOwner;
	}
	
    public String getNewParameter() {
		return newParameter;
	}
	public void setNewParameter(String newParameter) {
		this.newParameter = newParameter;
	}
	
	public String getNewSchema() {
		return newSchema;
	}
	public void setNewSchema(String newSchema) {
		this.newSchema = newSchema;
	}
}
