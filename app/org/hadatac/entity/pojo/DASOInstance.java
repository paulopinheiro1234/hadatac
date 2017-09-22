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

	static final String URI_TEMPLATE = "${study}/${id}/${modifier}";
	
	private String uri; // generated here in this class
	private String label; // provided by template+codebook
	private String studyId; // provided by DA file
	private String rowKey; // provided by row
	private String type; // provided by template
	private HashMap<String,String> relations;

	public void setUri(String uri){ this.uri = uri; }
	public void setLabel(String label){ this.label = label; }
	public void setStudyId(String id){ this.studyId = id; }
	public void setType(String type){ this.type = type; }
	public void setRowKey(String row){ this.rowKey = row; }
	public void setRelations(HashMap<String,String> relations){ this.relations = relations; }

	public String getUri(){ return this.uri; }
	public String getLabel(){ return this.label; }
	public String getStudyId(){ return this.studyId; }
	public String getType(){ return this.type; }
	public String getRowKey(){ return this.rowKey; }
	public HashMap<String,String> getRelations(){ return this.relations; }    
	public String getUriNamespace() { return ValueCellProcessing.replaceNameSpaceEx(uri); }
	//public HashMap<String,String> getTemplateValues(){ return this.templateValues; }

	/*
	public DASOInstance(String uri, String label, String type, HashMap<String,String> relations){
		this.setUri(uri);
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
	}// /constructor with uri given
	*/

	public DASOInstance(String studyId, String label, String type, String rowKey, HashMap<String,String> relations){
		this.setStudyId(studyId);
		this.setLabel(label);
		this.setType(type);
		this.setRowKey(rowKey);
		this.setUri(generateURI());
		this.setRelations(relations);
	}// /constructor needing a uri

	public String generateURI(){
		HashMap<String,String> templateValues = new HashMap<String,String>();
		templateValues.put("study", this.studyId);
		templateValues.put("id", this.rowKey);
		// TODO: see what this needs to look like
		templateValues.put("modifier", "");

		StrSubstitutor sub = new StrSubstitutor(templateValues);
		String generatedURI = kbPrefix + sub.replace(URI_TEMPLATE);
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
