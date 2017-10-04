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
import org.hadatac.console.controllers.annotator.AutoAnnotator;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import play.Play;

public class DASVirtualObject {
	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	private Map<String,Object> origRow;

	private String templateUri;
	private String originalLabel;
	private HashMap<String,String> objRelations;

	final HashMap<String,String> codeMap = AutoAnnotator.codeMappings;
	final HashMap<String, Map<String,String>> codebook = AutoAnnotator.codebook;
    
	// takes the row created in DASchemaObjectGenerator
	// iff that row is virtual
	public DASVirtualObject(String ogLabel, Map<String,Object> dasoRow) {
		this.origRow = dasoRow;
		this.objRelations = new HashMap<String,String>();

		if(dasoRow.get("hasURI") == null || dasoRow.get("hasURI").equals("")){
			//handle an error
		} else {
			this.templateUri = dasoRow.get("hasURI").toString();
		}
		if(dasoRow.get("rdfs:label") == null || dasoRow.get("rdfs:label").equals("")){
			//handle an error
		} else {
			this.objRelations.put("rdfs:label", dasoRow.get("rdfs:label").toString());
		}
		if(dasoRow.get("hasco:hasEntity") == null || dasoRow.get("hasco:hasEntity").equals("")){
			//handle an error
		} else {
			this.objRelations.put("rdfs:type", dasoRow.get("hasco:hasEntity").toString());
		}
		if(dasoRow.get("hasco:hasRole") == null || dasoRow.get("hasco:hasRole").equals("")){
			//handle an error
		} else {
			this.objRelations.put("hasco:hasRole", dasoRow.get("hasco:hasRole").toString());
		}
		if(dasoRow.get("sio:inRelationTo") == null || dasoRow.get("sio:inRelationTo").equals("")){
			//handle an error
		} else {
			if(dasoRow.get("sio:Relation") == null || dasoRow.get("sio:Relation").equals("")){
				this.objRelations.put("sio:isRelatedTo", dasoRow.get("sio:inRelationTo").toString());
			} else {
				this.objRelations.put(dasoRow.get("sio:Relation").toString(), dasoRow.get("sio:inRelationTo").toString());
			}
		}
		if(dasoRow.get("dcterms:alternativeName") == null || dasoRow.get("dcterms:alternativeName").equals("")){
			//handle an error
		} else {
			this.originalLabel = dasoRow.get("dcterms:alternativeName").toString();
		}

	}// DASOVirtualObject()

	public HashMap<String,String> getObjRelations(){
		return this.objRelations;
	}

	public String getTemplateUri(){
		return this.templateUri;
	}

	public String getOriginalLabel(){
		return this.originalLabel;
	}
	public String toString(){
		String result = "";
		result += "templateURI: " + this.templateUri + "\n";
		result += "column name: " + this.originalLabel + "\n";
		for (Map.Entry<String, String> entry : this.objRelations.entrySet()) {
			result += entry.getKey() + " " + entry.getValue() + "\n";
		}	
		return result;
	}// /toString()

	/*
	public int save() {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Measurement.save - e.Message: " + e.getMessage());
			return -1;
		}
	}// /save()
	*/

	/*
	// if we delete an SDD, we want to delete all DASVO associated with it
	public static int delete(String sddUri) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			UpdateResponse response = solr.deleteByQuery("dataset_uri:\"" + datasetUri + "\"");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.delete - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.delete - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.delete - Exception message: " + e.getMessage());
		}

		return -1;
	}// /delete()
	*/

	/*
	// this should mirror Measurement.java's method
	// since we're querying solr
	public static DASVirtualObject find(String dasvoUri) {
		DASVirtualObject dasvo = null;
		System.out.println("[DASVO] Looking for data acquisition virtual object with URI " + dasvoUri);
		if (dasvoUri.startsWith("http")) {
			dasvoUri = "<" + dasvoUri + ">";
		}

	}// /find()
	*/

}// /class
