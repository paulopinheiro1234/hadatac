package org.hadatac.console.models;

public class GroupRegistrationForm {
	public String group_uri = "";
    public String group_name = "";
    public String comment = "";
    public String homepage = "";
    public String parent_group_uri = "";
    
    public String getGroupUri() {
    	return group_uri;
    }
    public String getGroupName() {
    	return group_name;
    }
    public String getComment() {
    	return comment;
    }
    public String getHomepage() {
    	return homepage;
    }
    public String getParentGroupUri() {
    	return parent_group_uri;
    }
}
