package org.hadatac.console.models;

public class UserPreRegistrationForm {
    public String usr_uri = "";
    public String given_name = "";
    public String family_name = "";
    public String email = "";
    public String comment = "";
    public String homepage = "";
    public String group_uri = "";
    public String facet_study = "";
    public String facet_object = "";
    public String facet_entity_characteristic = "";
    public String facet_unit = "";
    public String facet_time = "";
    public String facet_space = "";
    public String facet_platform = "";

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
    public String getFacetStudy() { return facet_study; }
    public String getFacetObject() { return facet_object; }
    public String getFacetEntityCharacteristic() { return facet_entity_characteristic; }
    public String getFacetUnit() { return facet_unit; }
    public String getFacetTime() { return facet_time; }
    public String getFacetSpace() { return facet_space; }
    public String getFacetPlatform() { return facet_platform; }

}