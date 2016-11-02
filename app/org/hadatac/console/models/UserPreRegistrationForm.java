package org.hadatac.console.models;

public class UserPreRegistrationForm {
	public String usr_uri = "";
    public String given_name = "";
    public String family_name = "";
    public String email = "";
    public String comment = "";
    public String homepage = "";
    public String group_uri = "";
    
    public String getUserUri() {
    	return usr_uri;
    }
    public String getGivenName() {
    	return given_name;
    }
    public String getFamilyName() {
    	return family_name;
    }
    public String getEmail() {
    	return email;
    }
    public String getComment() {
    	return comment;
    }
    public String getHomepage() {
    	return homepage;
    }
    public String getGroupUri() {
    	return group_uri;
    }
}
