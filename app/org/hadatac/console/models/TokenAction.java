package org.hadatac.console.models;

import java.io.IOException;
import java.util.Date;
import java.util.UUID;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;
import play.data.format.Formats;

public class TokenAction {

	public enum Type {
		EMAIL_VERIFICATION,
		PASSWORD_RESET
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Verification time frame (until the user clicks on the link in the email)
	 * in seconds
	 * Defaults to one week
	 */
	private final static long VERIFICATION_TIME = 7 * 24 * 3600;

	public Long id;
	
	@Field("id")
	public String id_s;

	@Field("token")
	public String token;

	public User targetUser;

	public Type type;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date created;
	
	public DateTime created_j;

	@Formats.DateTime(pattern = "yyyy-MM-dd HH:mm:ss")
	public Date expires;
	
	public DateTime expires_j;
	
	public String getTargetUserId() {
		return targetUser.id_s;
	}
	
	@Field("target_user_id")
	public void setTargetUserId(String id_s) {
		targetUser.id_s = id_s;
	}
	
	public String getType() {
		return type.name();
	}
	
	@Field("type")
	public void setType(String name) {
		if (name.equals("EMAIL_VERIFICATION")) {
			type = Type.EMAIL_VERIFICATION;
		} else {
			type = Type.PASSWORD_RESET;
		}
	}
	
	public String getCreated() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(this.created_j);
	}
	
	@Field("created")
	public void setCreated(String created) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		created_j = formatter.parseDateTime(created);
	}
	
	public String getExpires() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(this.expires_j);
	}
	
	@Field("expires")
	public void setExpires(String expires) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		expires_j = formatter.parseDateTime(expires);
	}

	public static TokenAction findByToken(final String token, final Type type) {
		return findByTokenSolr(token, type);
	}
	
	public static TokenAction findByTokenSolr(final String token, final Type type) {
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/token_action");
    	SolrQuery solrQuery = new SolrQuery("token:" + token + " AND type:" + type.name());
    	TokenAction tokenAction = null;
		
    	try {
			QueryResponse queryResponse = solrClient.query(solrQuery);
			solrClient.close();
			SolrDocumentList list = queryResponse.getResults();
			if (list.size() == 1) {
				DateTime date;
				tokenAction = new TokenAction();
				SolrDocument doc = list.get(0);
				tokenAction.id_s = doc.getFieldValue("id").toString();
				tokenAction.token = doc.getFieldValue("token").toString();
				tokenAction.setType(doc.getFieldValue("type").toString());
				date = new DateTime((Date)doc.getFieldValue("created"));
				tokenAction.setCreated(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
				date = new DateTime((Date)doc.getFieldValue("expires"));
				tokenAction.setExpires(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
				tokenAction.targetUser = User.findByIdSolr(doc.getFieldValue("target_user_id").toString());
			}
		} catch (Exception e) {
			System.out.println("[ERROR] TokenAction.findByTokenSolr - Exception message: " + e.getMessage());
		}
    	
    	return tokenAction;
	}

	public static void deleteByUser(final User u, final Type type) {
		deleteByUserSolr(u, type);
	}
	
	public static void deleteByUserSolr(final User u, final Type type) {
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/token_action");
		try {
			solrClient.deleteByQuery("target_user_id:" + u.id_s + " AND type:" + type.name());
			solrClient.commit();
			solrClient.close();
		} catch (SolrServerException | IOException e) {
			System.out.println("[ERROR] TokenAction.deleteByUserSolr - Exception message: " + e.getMessage());
		}
	}

	public boolean isValid() {
		return this.expires_j.isAfterNow();
		//return this.expires.after(new Date());
	}

	public static TokenAction create(final Type type, final String token,
			final User targetUser) {
		final TokenAction ua = new TokenAction();
		ua.id_s = UUID.randomUUID().toString();
		ua.targetUser = targetUser;
		ua.token = token;
		ua.type = type;
		final Date created = new Date();
		final DateTime created_j = new DateTime();
		ua.created = created;
		ua.created_j = created_j;
		ua.expires = new Date(created.getTime() + VERIFICATION_TIME * 1000);
		ua.expires_j = new DateTime(created_j.getMillis() + VERIFICATION_TIME * 1000);
		ua.save();
		return ua;
	}
	
	public void save() {
		SolrClient solrClient = new HttpSolrClient(Play.application().configuration().getString("hadatac.solr.users") + "/token_action");
        
        try {
        	solrClient.addBean(this);
			solrClient.commit();
			solrClient.close();
		} catch (Exception e) {
			System.out.println("[ERROR] TokenAction.save - Exception message: " + e.getMessage());
		}
	}
}
