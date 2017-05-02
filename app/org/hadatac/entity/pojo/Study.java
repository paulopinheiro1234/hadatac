package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetFormatter;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.sparql.resultset.RDFOutput;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.MetadataAcquisitionQueryResult;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class Study {
	private DateTime startedAt;
	private DateTime endedAt;
	
	@Field("studyUri")
	private String studyUri;
//	@Field("permission_uri")
//	private String permissionUri;
	@Field("studyLabel_i")
	private String label;
	@Field("proj_i")
	private String project;
	@Field("studyComment_i")
	private String comment;
	@Field("institutionName_i")
	private String institution;
	@Field("agentName_i")
	private String agent;
	@Field("studyTitle_i")
	private String title;
	
	private List<DataAcquisition> dataAcquisitions;
	
	
	// Constructer
	public Study() {
		startedAt = null;
		endedAt = null;
//		permissionUri = "";
		label = "";
		project= "";
		comment = "";
		institution = "";
		agent = "";
	}
	
	// get Methods
	public String getUri() {
		return studyUri;
	}
	
/*	public String getPermissionUri() {
		return permissionUri;
	}
	*/
	public String getLabel() {
		return label;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getComment() {
		return comment;
	}
	
/*	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumSubjects() {
		return numSubjects;
	}*/
	
	public String getInstitution() {
		return institution;
	}
	
	public String getAgent() {
		return agent;
	}
	
	public String getTitle() {
		return title;
	}
	
	public List<DataAcquisition> getDataAcquisitions() {
    	return dataAcquisitions;
    }
    
    public void setDataAcquisitions(List<DataAcquisition> dataAcquisition) {
    	this.dataAcquisitions = dataAcquisition;
    }
	
	// get Start Time Methods
	public String getStartedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	public String getStartedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	
	// get End Time Methods
	public String getEndedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	public String getEndedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	
	// set Methods
	public void setUri(String uri) {
		this.studyUri = uri;
	}
	
/*	public void setPermissionUri(String permissionUri) {
		this.permissionUri = permissionUri;
	}
	*/
	public void setLabel(String label) {
		this.label = label;
	}
		
	public void setProject(String project) {
		this.project = project;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
	
/*	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}
	
	public void setNumSubjects(int numSubjects) {
		this.numSubjects = numSubjects;
	}
*/
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	// set Start Time Methods
	@Field("started_at")
	public void setStartedAt(String startedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsd(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsdWithMillis(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	
	// set End Time Methods
	@Field("ended_at")
	public void setEndedAt(String endedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsd(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsdWithMillis(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	
	public static Study create(String uri) {
		Study study = new Study();
		
		study.setUri(uri);
		
		return study;
	}
	
	public static Study convertFromSolr(SolrDocument doc) {
		Iterator<Object> i;
		DateTime date;
		Study study = new Study();
		// URI
		study.setUri(doc.getFieldValue("studyUri").toString());
		// permissions
/*		if (doc.getFieldValues("permission_uri") != null) {
			study.setPermissionUri(doc.getFieldValue("permission_uri").toString());
		}*/
		// label
		if (doc.getFieldValues("studyLabel_i") != null) {
			study.setLabel(doc.getFieldValue("studyLabel_i").toString());
		}
		// projectTitle
		if (doc.getFieldValues("proj_i") != null) {
			study.setProject(doc.getFieldValue("proj_i").toString());
		}
		// comment
		if (doc.getFieldValues("comment_i") != null) {
			study.setLabel(doc.getFieldValue("comment_i").toString());
		}
		// description
		if (doc.getFieldValues("studyTitle_i") != null) {
			study.setTitle(doc.getFieldValue("studyTitle_i").toString());
		}
/*		// numSubjects
		if (doc.getFieldValues("numSubjects") != null) {
			study.setNumSubjects(Integer.parseInt(doc.getFieldValue("numSubjects").toString()));
		}
		// numSamples
		if (doc.getFieldValues("numSamples") != null) {
			study.setNumSamples(Integer.parseInt(doc.getFieldValue("numSamples").toString()));
		}*/
		// institution
		if (doc.getFieldValues("institutionName_i") != null) {
			study.setInstitution(doc.getFieldValue("institutionName_i").toString());
		}
		
		// agent
		if (doc.getFieldValues("agentName_i") != null) {
			study.setAgent(doc.getFieldValue("agentName_i").toString());
		}
		
		return study;
	}
	
	public static MetadataAcquisitionQueryResult find(int page, int qtd, List<String> permissions, FacetHandler handler) {
		MetadataAcquisitionQueryResult result = new MetadataAcquisitionQueryResult();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES);
		SolrQuery query = new SolrQuery();
		String permission_query = "";
		String facet_query = "";
		String q;
		
		permission_query += "permission_uri:\"" + "PUBLIC" + "\"";
		if (permissions != null) {
			Iterator<String> i = permissions.iterator();
			while (i.hasNext()) {
				permission_query += " OR ";
				permission_query += "permission_uri:\"" + i.next() + "\"";
			}
		}
		
		if (handler != null) {
		    facet_query = handler.toSolrQuery();
		    /* Iterator<String> i = handler.facetsAnd.keySet().iterator();
			while (i.hasNext()) {
				String field = i.next();
				String value = handler.facetsAnd.get(field);
				facet_query += field + ":\"" + value + "\"";
				if (i.hasNext()) {
					facet_query += " AND ";
				}
				} */
		}
		
		if (facet_query.trim().equals("")) {
			facet_query = "*:*";
		}
		
		q =  "(" + permission_query + ") AND (" + facet_query + ")";
		System.out.println("!!! QUERY: " + q);
		query.setQuery(q);
		query.setStart((page-1)*qtd);
		query.setRows(qtd);
		query.setFacet(true);
		query.addFacetField("demographics,acculturation,occupation,housingCharacteristics,ATIDU,socioEconomicStatus,assessment,BDN,anthropometry,laboratory,birthOutcomes");
		// See Measurement.java as an example if we wish to add the possible facet values as pivots
		try {
			QueryResponse queryResponse = solr.query(query);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
				result.documents.add(convertFromSolr(m.next()));
			}
			
			if (queryResponse.getFacetFields() != null) {
				Iterator<FacetField> f = queryResponse.getFacetFields().iterator();
				while (f.hasNext()) {
					FacetField field = f.next();
					result.field_facets.put(field.getName(), new HashMap<String, Long>());
					Iterator<Count> v = field.getValues().iterator();
					while (v.hasNext()) {
						Count count = v.next();
						Map<String, Long> map = result.field_facets.get(field.getName());
						map.put(count.getName(), count.getCount());
					}
				}
			}
			
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - Exception message: " + e.getMessage());
		}
		
		return result;
	}
	
	public static Study find(String study_uri) {
		Study returnStudy = new Study();
		String studyQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		"SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyComment (group_concat( ?agentName_ ; separator = ' & ') as ?agentName) ?institutionName " + 
		" WHERE {        ?subUri rdfs:subClassOf* hasco:Study . " + 
		"                       ?studyUri a ?subUri . " + 
		"           ?studyUri rdfs:label ?studyLabel  . " + 
		"			FILTER ( ?studyUri = " + DynamicFunctions.replaceURLWithPrefix(study_uri) + " ) . " +
		"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
		"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
		"             OPTIONAL{ ?studyUri hasco:hasAgent ?agent .  " +
		"                         ?agent foaf:name ?agentName_} . " +
		"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
		"                                 ?institution foaf:name ?institutionName} . " + 
		"                             }" +
		"GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
		
		try {
			Query studyQuery = QueryFactory.create(studyQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				//values = new HashMap<String, String>();
				returnStudy.setUri(soln.get("studyUri").toString());
				if (soln.contains("studyLabel"))
					returnStudy.setLabel(soln.get("studyLabel").toString());
				if (soln.contains("proj"))
					returnStudy.setProject(soln.get("proj").toString());
				if (soln.contains("studyComment"))
					returnStudy.setComment(soln.get("studyComment").toString());
				if (soln.contains("agentName"))
					returnStudy.setAgent(soln.get("agentName").toString());
				if (soln.contains("institutionName"))
					returnStudy.setInstitution(soln.get("institutionName").toString());
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return returnStudy;
	}
	
	public static Model findModel(String study) {
		String studyQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		"SELECT DISTINCT ?s ?p ?o " +
		"WHERE " +
		"{  " +
		"  { " +
		"	{  " +
		// Study 
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?s a ?subUri . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?s = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    { " +
		// Other Studies 
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?s a ?subUri . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?s != " + study + ") " +
		"    }  " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Data Acquisitions, Cohort
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?s hasco:isDataAcquisitionOf|hasco:isCohortOf ?study . " + 
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Data Acquisitions, Cohort
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?s hasco:isDataAcquisitionOf|hasco:isCohortOf ?study . " + 
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Cohort Subjects
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?cohort hasco:isCohortOf ?study . " +
		"	?s hasco:isSubjectOf ?cohort . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Cohort Subjects
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?cohort hasco:isCohortOf ?study . " +
		"	?s hasco:isSubjectOf ?cohort . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"	{  " +
		//  Data Acquisition Schema and Deployment
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?da hasco:hasSchema|hasneto:hasDeployment ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"  	} " +
		"    MINUS " +
		"    {  " +
		// Other Data Acquisition Schema and Deployment
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?da hasco:hasSchema|hasneto:hasDeployment ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"  	} " +
		"  } " +
		"  UNION " + 
		"  { " +
		"    { " +
		// Data Acquisition Samples
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?s hasco:isMeasuredObjectOf ?da .  " +
		"   ?s ?p ?o . " +
		"  FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other Data Acquisition Samples
		"  	?subUri rdfs:subClassOf* hasco:Study . " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"   ?s hasco:isMeasuredObjectOf ?da .  " +
		"   ?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"    } " +
		"  } "  +
		"  UNION " + 
		"  { " +
		"    { " +
		// Deployment - Platform, Instrument, detector
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"  	?da hasneto:hasDeployment ?deploy .  " +
		"	?deploy vstoi:hasPlatform|hasneto:hasInstrument|hasneto:hasDetector ?s . " +
		"  	?s ?p ?o . " +
		"  FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other Deployment - Platform, Instrument, detector
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"   ?da hasco:isDataAcquisitionOf ?study . " + 
		"  	?da hasneto:hasDeployment ?deploy .  " +
		"	?deploy vstoi:hasPlatform|hasneto:hasInstrument|hasneto:hasDetector ?s . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"  UNION " + 
		"  { " +
		"    { " +
		// DA Schema Attribute
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " +
		"   ?da hasco:hasSchema ?schema . " +
		"   ?s hasneto:partOfSchema ?schema . " +
		"  	?s ?p ?o . " +
		"  	FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    { " +
		// Other DA Schema Attribute
		"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
		"  	?study a ?subUri . " +
		"  	?da hasco:isDataAcquisitionOf ?study . " +
		"   ?da hasco:hasSchema ?schema . " +
		"   ?s hasneto:partOfSchema ?schema . " +
		"  	?s ?p ?o . " +
		"  FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"  UNION  " +
		"  { " +
		"  	 {  " +
		// Datasets
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"   ?study a ?subUri . " +
		"   ?s hasco:isDatasetOf ?study . " +
		"   ?s ?p ?o . " +
		"   FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    {  " +
		// Other Datasets
		"   ?subUri rdfs:subClassOf* hasco:Study . " + 
		"   ?study a ?subUri . " +
		"   ?s hasco:isDatasetOf ?study . " +
		"   ?s ?p ?o . " +
		"   FILTER (?study != " + study + ") " +
		"     } " +
		"   } " +
		"   UNION " + 
		"   { " +
		"  	  {  " +
		// Attribute References 
		"    ?subUri rdfs:subClassOf* hasco:Study . " + 
		"    ?study a ?subUri . " +
		"    ?data hasco:isDatasetOf ?study . " +
		"    ?s hasco:isAttributeReferenceOf ?data . " +
		"    ?s ?p ?o . " +
		"    FILTER (?study = " + study + ") " +
		"    } " +
		"    MINUS " +
		"    {  " +
		// Other Attribute References
		"    ?subUri rdfs:subClassOf* hasco:Study . " + 
		"    ?study a ?subUri . " +
		"    ?data hasco:isDatasetOf ?study . " +
		"     ?s hasco:isAttributeReferenceOf ?data . " +
		"    ?s ?p ?o . " +
		"    FILTER (?study != " + study + ") " +
		"    } " +
		"  } " +
		"} ";
		
		Model model = ModelFactory.createDefaultModel();
		try {
			Query studyQuery = QueryFactory.create(studyQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			
			System.out.println("resultsrw.size(): " + resultsrw.size());
	        while (resultsrw.hasNext()) {
	            QuerySolution soln = resultsrw.next();
	
	            Resource subject = soln.getResource("s");
	            Property property = model.createProperty(soln.getResource("p").toString());
	            RDFNode object = soln.get("o");
	            
	            model.add(subject, property, object);
	        }
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		
		return model;
	}
	
	public static List<Study> find(State state) {
		List<Study> studies = new ArrayList<Study>();
	    String queryString = "";
        if (state.getCurrent() == State.ACTIVE) { 
    	   queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
    			    "SELECT DISTINCT ?studyUri ?studyLabel ?proj ?studyComment (group_concat( ?agentName_ ; separator = ' & ') as ?agentName) ?institutionName " + 
    				" WHERE {        ?subUri rdfs:subClassOf* hasco:Study . " + 
    				"                       ?studyUri a ?subUri . " + 
    				"           ?studyUri rdfs:label ?studyLabel  . " + 
    				"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
    				"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
    				"             OPTIONAL{ ?studyUri hasco:hasAgent ?agent .  " +
    				"                         ?agent foaf:name ?agentName_} . " +
    				"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
    				"                   ?institution foaf:name ?institutionName} . " + 
    				"   FILTER NOT EXISTS { ?studyUri prov:endedAtTime ?enddatetime . } " + 
     			   	"                             }" +
    				"GROUP BY ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment ?agentName ?institutionName ";
        } else {
    	   if (state.getCurrent() == State.CLOSED) {
    		   queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
       			    "SELECT ?studyUri ?studyLabel ?proj ?studyComment (group_concat( ?agentName_ ; separator = ' & ') as ?agentName) ?institutionName " + 
       				" WHERE {        ?subUri rdfs:subClassOf* hasco:Study . " + 
       				"                       ?studyUri a ?subUri . " + 
       				"           ?studyUri rdfs:label ?studyLabel  . " +
       				//"   ?studyUri prov:startedAtTime ?startdatetime .  " + 
 				    //"   ?studyUri prov:endedAtTime ?enddatetime .  " + 
 				   	"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
       				"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
    				"             OPTIONAL{ ?studyUri hasco:hasAgent ?agent .  " +
    				"                         ?agent foaf:name ?agentName_} . " +
       				"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
       				"                     ?institution foaf:name ?institutionName} . " +
        			"                             }"+
    				"GROUP BY ?studyUri ?studyLabel ?proj ?studyComment ?agentName ?institutionName ";// +
       	   			//"ORDER BY DESC(?enddatetime) ";
    	   } else {
        	   if (state.getCurrent() == State.ALL) {
        		   queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
           			    "SELECT ?studyUri ?studyLabel ?proj ?studyComment (group_concat( ?agentName_ ; separator = ' & ') as ?agentName ) ?institutionName " + 
        				" WHERE {        ?subUri rdfs:subClassOf* hasco:Study . " + 
        				"                       ?studyUri a ?subUri . " + 
        				"           ?studyUri rdfs:label ?studyLabel  . " + 
        				"		 OPTIONAL {?studyUri hasco:hasProject ?proj} . " +
        				"        OPTIONAL { ?studyUri rdfs:comment ?studyComment } . " + 
        				"             OPTIONAL{ ?studyUri hasco:hasAgent ?agent .  " +
        				"                         ?agent foaf:name ?agentName_} . " +
        				"        OPTIONAL { ?studyUri hasco:hasInstitution ?institution . " + 
        				"                  ?institution foaf:name ?institutionName} . " +
         			   	"                             }" +
        				"GROUP BY ?studyUri ?studyLabel ?proj ?studyComment ?agentName ?institutionName ";
        	   } else {
        		   System.out.println("Study.java: no valid state specified.");
        		   return null;
        	   }
    	   }
        }
		Query query = QueryFactory.create(queryString);
		
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();
		
		Study study = null;
		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			if (soln != null && soln.getResource("studyUri").getURI()!= null) { 
				study = Study.find(soln.get("studyUri").toString());
				System.out.println("Study URI: " + soln.get("studyUri").toString());
			}
			studies.add(study);
		}
		
		return studies;
	}
	
	public int close(String endedAt) {
		this.setEndedAtXsd(endedAt);
		return this.save();
	}
	
	public void delete() {
		deleteStudy();
		deleteDataCollections();
		deleteMeasurements();
	}
	
	public int deleteStudy() {
		SolrClient study_solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES);
		try {
			UpdateResponse response = study_solr.deleteByQuery("studyUri:\"" + studyUri + "\"");
			study_solr.commit();
			study_solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public int deleteDataCollections() {
		SolrClient study_solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.DATA_COLLECTION);
		try {
			UpdateResponse response = study_solr.deleteByQuery("study_uri:\"" + studyUri + "\"");
			study_solr.commit();
			study_solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public int deleteMeasurements() {
		SolrClient study_solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.DATA_ACQUISITION);
		try {
			UpdateResponse response = study_solr.deleteByQuery("study_uri:\"" + DynamicFunctions.replaceURLWithPrefix(studyUri) + "\"");
			study_solr.commit();
			study_solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data")
					+ Collections.STUDIES);
			if (endedAt.toString().startsWith("9999")) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Study.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int save(SolrClient solr) {
		try {
			if (endedAt.toString().startsWith("9999")) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Study.save(SolrClient) - e.Message: " + e.getMessage());
			return -1;
		}
	}
}