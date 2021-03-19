package org.hadatac.console.models;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.hadatac.utils.CollectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class UserDownloadActivity {

    private static final Logger log = LoggerFactory.getLogger(UserDownloadActivity.class);
    private static final long serialVersionUID = 1L;

    @Field("id")
    private String id_s;

    @Field("user_search_ref_str")
    private String user_search_ref;

    @Field("user_email_str")
    private String user_email;

    @Field("submission_time_str")
    private String submission_time;

    public String getId_s() { return id_s; }

    public String getUser_search_ref() {
        return user_search_ref;
    }

    public void setUser_search_ref(String user_search_ref) {
        this.user_search_ref = user_search_ref;
    }

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

    public void save() {

        SolrClient solrClient = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DOWNLOAD_ACTIVITIES)).build();

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

}
