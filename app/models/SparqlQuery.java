package models;

import views.formdata.FacetFormData;

public class SparqlQuery extends Query {

    public String subject;
    public String predicate; 

    public SparqlQuery() {}

    public SparqlQuery(String subject, String predicate){ 
        this.subject = subject;
        this.predicate = predicate;
    }

    public static SparqlQuery makeInstance(FacetFormData formData) {
        SparqlQuery query = new SparqlQuery(formData.subject, formData.predicate); 
        return query;
    }
}
