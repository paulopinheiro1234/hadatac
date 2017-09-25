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

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

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
	public static String INDENT1 = "     ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:DASchemaAttribute;  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";

	final String kbPrefix = Play.application().configuration().getString("hadatac.community.ont_prefix") + "-kb:";
	
	static final String URI_TEMPLATE = "${study}/${id}/${modifier}";
	
	private String uri; // generated here in this class
	private String label; // provided by template+codebook
	private String studyId; // provided by DA file
	private String rowKey; // provided by row
	private String type; // provided by template
	private String origValue;
	private HashMap<String,String> relations;

	public void setUri(String uri){ this.uri = uri; }
	public void setLabel(String label){ this.label = label; }
	public void setStudyId(String id){ this.studyId = id; }
	public void setType(String type){ this.type = type; }
	public void setRowKey(String row){ this.rowKey = row; }
	public void setOrigValue(String ov){ this.origValue = ov; }
	public void setRelations(HashMap<String,String> relations){ this.relations = relations; }

	public String getUri(){ return this.uri; }
	public String getLabel(){ return this.label; }
	public String getStudyId(){ return this.studyId; }
	public String getType(){ return this.type; }
	public String getRowKey(){ return this.rowKey; }
	public String getOrigValue(){ return this.origValue; }
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

	public DASOInstance(String studyId, String rowKey, String label, String type, HashMap<String,String> relations){
		this.setStudyId(studyId);
		this.setRowKey(rowKey);
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
		this.setOrigValue("");
		this.setUri(generateURI());
	}// /constructor needing a uri

	public String generateURI(){
		HashMap<String,String> templateValues = new HashMap<String,String>();
		templateValues.put("study", this.studyId);
		templateValues.put("id", this.rowKey);
		// TODO: see what this needs to look like
		if(this.rowKey.equals("summaryClass")){
			if(this.relations.containsKey("rdfs:subClassOf")){
				String[] temp = this.relations.get("rdfs:subClassOf").split("_",2);
				String mod = temp[0];
				if(temp.length > 1) mod += "/val/"+temp[1];
				this.setOrigValue(mod);
				templateValues.put("modifier", mod.replaceAll("[ ';.,]",""));
			}
			else
				templateValues.put("modifier", "");
		} else {
			templateValues.put("modifier", "");
		}
		StrSubstitutor sub = new StrSubstitutor(templateValues);
		String generatedURI = sub.replace(URI_TEMPLATE);
		generatedURI = kbPrefix + generatedURI;
		return generatedURI;
	}// /generateURI


	//private String uri; // generated here in this class
	//private String label; // provided by template+codebook
	//private String studyId; // provided by DA file
	//private String rowKey; // provided by row
	//private String type; // provided by template
	//private HashMap<String,String> relations;

	/*public void save() {
		//delete();  // delete any existing triple for the current DASA
		if (uri == null || uri.equals("")) {
			System.out.println("[ERROR] Trying to save DASOInstance without assigning a URI");
			return;
		}
		if (type == null || type.equals("")) {
			System.out.println("[ERROR] Trying to save DASOInstance without assigning a type");
			return;
		}
		String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += this.getUri() + " a " + this.getType() + " . ";
		insert += this.getUri() + " rdfs:label  \"" + label + "\" . ";
		if (partOfSchema.startsWith("http")) {
			insert += this.getUri() + " hasco:partOfSchema <" + partOfSchema + "> .  "; 
		} else {
			insert += this.getUri() + " hasco:partOfSchema " + partOfSchema + " .  "; 
		} 

		insert += LINE_LAST;
		//System.out.println("[DASOInstance] insert query (pojo's save): <" + insert + ">");
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
			request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
	}// /save()
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
	
	public String toString(){
		String pretty = this.getUri() + "\n";
		for(Map.Entry rel : this.getRelations().entrySet()) {
			pretty += rel.getKey() + " : " + rel.getValue();
		}
		return pretty;
	}// /toString

} // /class
