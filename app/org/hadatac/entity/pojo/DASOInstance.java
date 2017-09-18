package org.hadatac.entity.pojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;

import org.apache.commons.text.StrSubstitutor;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.utils.Collections;

import play.Play;

public class DASOInstance {
	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	static final String URI_TEMPLATE = "${prefix}${study}/${id}/attr/${modifier}";
	
	private String uri;
	private String label;
	private String type;
	private String study;
	private HashMap<String,String> relations;

	//private String partOfSchema;
	//private String entity;
	//private String entityLabel;
	//private String role;
	//private String inRelationTo;
	//private String inRelationToLabel;
	//private String relation;
	//private String relationLabel;

	public void setUri(String uri){ this.uri = uri; }
	public void setLabel(String label){ this.label = label; }
	public void setType(String type){ this.uri = type; }
	public void setRelations(HashMap<String,String> relations){ this.relations = relations; }
	//public void setTemplateValues(HashMap<String,String> vals){ this.templateValues = vals; }

	public String getUri(){ return this.uri; }
	public String getLabel(){ return this.label; }
	public String getType(){ return this.type; }
	public HashMap<String,String> getRelations(){ return this.relations; }    
	public String getUriNamespace() { return ValueCellProcessing.replaceNameSpaceEx(uri); }
	//public HashMap<String,String> getTemplateValues(){ return this.templateValues; }

	public DASOInstance(String uri, String label, String type, HashMap<String,String> relations){
		this.setUri(uri);
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
	}// /constructor with uri given

	public DASOInstance(String label, String type, HashMap<String,String> relations, HashMap<String,String> templateVals){
		this.setUri(this.generateURIFromTemplate(templateVals));
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
	}// /constructor needing a uri

	public String generateURIFromTemplate(HashMap<String,String> templateValues){
		// TODO: double-check necessary keys have values (do not return null)
		StrSubstitutor sub = new StrSubstitutor(templateValues);
		String generatedURI = sub.replace(URI_TEMPLATE);
		System.out.println("[DASO Instance]: generated uri " + generatedURI);
		return generatedURI;
	}// /generateURIFromTemplate

	/*
		Study ID: default-study
		templateURI: hbgd-kb:DASO-time_cat_infosheet-summaryClass
		rdfs:subClassOf/hbgd-kb:DASO-time_cat_infosheet-id-key
		rdfs:label/summaryClass
		rdfs:type/owl:Class
*/


	// TODO: finish
	/*
	public static DASOInstance find(String URI){		
	}// /find
	*/

	// TODO: finish
	/*
	public static List<DASOInstance> findByStudy(String studyID){
		List<DASOInstance> instances = new ArrayList<DASOInstance>();
	}// /findByStudy
	*/

} // /class
