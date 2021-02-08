package org.hadatac.console.models;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserSearchActivity {

    private static final Logger log = LoggerFactory.getLogger(UserSearchActivity.class);
    private static final long serialVersionUID = 1L;

    @Field("id")
    private String id_s;

    @Field("user_email_str")
    private String user_email;

    @Field("submission_time_str")
    private String submission_time;

    @Field("json_query_str")
    private String json_query;

    public String getId_s() { return id_s; }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getSubmission_time() {
        return submission_time;
    }

    public void setSubmission_time(String submission_time) {
        this.submission_time = submission_time;
    }

    public String getJson_query() {
        return json_query;
    }

    public void setJson_query(String json_query) {
        this.json_query = json_query;
    }

    public void save() {

        SolrClient solrClient = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.SEARCH_ACTIVITIES)).build();

        if (this.id_s == null) {
            this.id_s = UUID.randomUUID().toString();
        }

        try {
            solrClient.addBean(this);
            solrClient.commit();
            solrClient.close();
        } catch (Exception e) {
            log.error("errors when saving to Solr: " + e.getMessage());
        }
    }

/*    private static UserPermission convertSolrDocumentToUserPermission(SolrDocument doc) {
        UserPermission permission = new UserPermission();
        permission.id_s = doc.getFieldValue("id").toString();
        permission.value = doc.getFieldValue("value_str").toString();

        return permission;
    }*/

}
