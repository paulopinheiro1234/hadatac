package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.labkey.remoteapi.CommandException;
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
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.data.model.MetadataAcquisitionQueryResult;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.hadatac.console.controllers.AuthApplication;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class Study extends HADatAcThing {
	private static String className = "hasco:Study";
	
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";
	public static String PREFIX = "STD-";

	@Field("studyUri")
	private String studyUri;

	private String studyType;

	@Field("studyLabel_i")
	private String label;

	@Field("studyTitle_i")
	private String title;

	@Field("proj_i")
	private String project;

	@Field("studyComment_i")
	private String comment;

	private String externalSource;

	@Field("institutionName_i")
	private String institutionUri;

	@Field("agentName_i")
	private String agentUri;

	private DateTime startedAt;

	private DateTime endedAt;

	private List<String> dataAcquisitionUris;

	private List<String> objectCollectionUris;

	private Agent agent;

	private Agent institution;

	private String lastId;

	public Study(String studyUri,
			String studyType,
			String label,
			String title,
			String project,
			String comment,
			String externalSource,
			String institutionUri,
			String agentUri,
			String startDateTime,
			String endDateTime) {
		this.studyUri = studyUri;
		this.studyType = studyType;
		this.label = label;
		this.title = title;
		this.project = project;
		this.comment = comment;
		this.externalSource = externalSource;
		this.institutionUri = institutionUri;
		this.agentUri = agentUri;
		this.setStartedAt(startDateTime);
		this.setEndedAt(endDateTime);
		this.dataAcquisitionUris = new ArrayList<String>();
		this.objectCollectionUris = new ArrayList<String>();
		this.lastId= "0";
	}

	public Study() {
		this.studyUri = "";
		this.studyType = "";
		this.label = "";
		this.title = "";
		this.project = "";
		this.comment = "";
		this.externalSource = "";
		this.institutionUri = "";
		this.agentUri = "";
		this.setStartedAt("");
		this.setEndedAt("");
		this.dataAcquisitionUris = new ArrayList<String>();
		this.objectCollectionUris = new ArrayList<String>();
		this.lastId = "0";
	}

	public String getUri() {
		return studyUri;
	}

	public String getLabel() {
		return label;
	}

	public String getProject() {
		return project;
	}

	public String getComment() {
		return comment;
	}

	public String getExternalSource() {
		return externalSource;
	}

	public String getInstitutionUri() {
		return institutionUri;
	}

	public Agent getInstitution() {
		if (institutionUri == null || institutionUri.equals("")) {
			return null;
		}
		if (institution != null && institution.getUri().equals(institutionUri)) {
			return institution;
		}
		return Agent.find(institutionUri);
	}

	public String getAgentUri() {
		return agentUri;
	}

	public Agent getAgent() {
		if (agentUri == null || agentUri.equals("")) {
			return null;
		}
		if (agent != null && agent.getUri().equals(agentUri)) {
			return agent;
		}
		return Agent.find(agentUri);
	}

	public String getTitle() {
		return title;
	}

	public String getType() {
		return studyType;
	}

	public long getLastId() {
		if (this.lastId == null) {
			return 0;
		}
		return Long.parseLong(this.lastId);
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

	public void setType(String studyType) {
		this.studyType = studyType;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public void setProject(String project) {
		this.project = project;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public void setExternalSource(String externalSource) {
		this.externalSource = externalSource;
	}

	public void setInstitutionUri(String institutionUri) {
		if (institutionUri != null && !institutionUri.equals("")) {
			if (institutionUri.indexOf("http") > -1) {
				this.institutionUri = institutionUri;
			}
		}
	}

	public void setAgentUri(String agentUri) {
		if (agentUri != null && !agentUri.equals("")) {
			if (agentUri.indexOf("http") > -1) {
				this.agentUri = agentUri;
			}
		}
	}

	public void setTitle(String title) {
		this.title = title;
	}

	private void setLastId(String lastId) {
		this.lastId = lastId;
	}

	public void increaseLastId(long quantity) {
		if (quantity > 0) {
			long l = Long.parseLong(this.lastId);
			long newL = l + quantity; 
			this.lastId = Long.toString(newL);
			save();
		}
	}

	// set Start Time Methods
	public void setStartedAt(String startedAt) {
		if (startedAt == null || startedAt.equals("")) {
			this.startedAt = null;
		} else {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
			this.startedAt = formatter.parseDateTime(startedAt);
		}
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
	public void setEndedAt(String endedAt) {
		if (startedAt == null || startedAt.equals("")) {
			this.startedAt = null;
		} else {
			DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
			this.endedAt = formatter.parseDateTime(endedAt);
		}
	}

	public void setEndedAtXsd(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.endedAt = formatter.parseDateTime(endedAt);
	}

	public void setEndedAtXsdWithMillis(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.endedAt = formatter.parseDateTime(endedAt);
	}

	public void setDataAcquisitionUris(List<String> dataAcquisitionUris) {
		this.dataAcquisitionUris = dataAcquisitionUris;
	}

	public List<String> getDataAcquisitionUris() {
		return this.dataAcquisitionUris;
	}

	public void addDataAcquisitionUri(String da_uri) {
		this.dataAcquisitionUris.add(da_uri);
	}

	public void setObjectCollectionUris(List<String> objectCollectionUris) {
		this.objectCollectionUris = objectCollectionUris;
	}

	public List<String>  getObjectCollectionUris() {
		return this.objectCollectionUris;
	}

	public void addObjectCollectionUri(String oc_uri) {
		this.objectCollectionUris.add(oc_uri);
	}
	
	@Override
	public boolean equals(Object o) {
		if((o instanceof Study) && (((Study)o).getUri() == this.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
	
	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			List<String> preValues, FacetHandler facetHandler) {
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?studyUri ?dataAcquisitionUri ?studyLabel ?dataAcquisitionLabel WHERE { "
				+ "?dataAcquisitionUri hasco:isDataAcquisitionOf ?studyUri . "
				+ "?studyUri rdfs:label ?studyLabel . "
				+ "?dataAcquisitionUri rdfs:label ?dataAcquisitionLabel . "
				+ "}";

		Map<HADatAcThing, List<HADatAcThing>> results = new HashMap<HADatAcThing, List<HADatAcThing>>();
		try {
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet resultSet = qe.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
			qe.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				Study study = new Study();
				study.setUri(soln.get("studyUri").toString());
				study.setLabel(soln.get("studyLabel").toString());
				
				DataAcquisition da = new DataAcquisition();
				da.setUri(soln.get("dataAcquisitionUri").toString());
				da.setLabel(soln.get("dataAcquisitionLabel").toString());
				if (!results.containsKey(study)) {
					List<HADatAcThing> facets = new ArrayList<HADatAcThing>();
					results.put(study, facets);
				}
				if (!results.get(study).contains(da)) {
					results.get(study).add(da);
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		return results;
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
		// institutionUri
		if (doc.getFieldValues("institutionName_i") != null) {
			study.setInstitutionUri(doc.getFieldValue("institutionName_i").toString());
		}

		// agentUri
		if (doc.getFieldValues("agentName_i") != null) {
			study.setAgentUri(doc.getFieldValue("agentName_i").toString());
		}

		return study;
	}

	public static MetadataAcquisitionQueryResult find(int page, int qtd, List<String> permissions, FacetHandler handler) {
		MetadataAcquisitionQueryResult result = new MetadataAcquisitionQueryResult();

		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES).build();
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
		}

		if (facet_query.trim().equals("")) {
			facet_query = "*:*";
		}

		q =  "(" + permission_query + ") AND (" + facet_query + ")";
		//System.out.println("!!! QUERY: " + q);
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

	private static List<String> findObjectCollectionUris(String study_uri) {
		List<String> ocList = new ArrayList<String>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?oc_uri  WHERE {  " + 
				"      ?oc_uri hasco:isMemberOf " + study_uri + " . " + 
				" } ";
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				if (soln.contains("oc_uri")) {
					ocList.add(soln.get("oc_uri").toString());
					System.out.println("STUDY: [" + study_uri + "]   OC: [" + soln.get("oc_uri").toString() + "]");
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return ocList;
	}

	private static List<String> findDataAcquisitionUris(String study_uri) {
		List<String> daList = new ArrayList<String>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?da_uri  WHERE {  " + 
				"      ?da_uri hasco:isDataAcquisitionOf " + study_uri + " . " + 
				" } ";
		try {
			Query query = QueryFactory.create(queryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				if (soln.contains("da_uri")) {
					daList.add(soln.get("da_uri").toString());
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}
		return daList;
	}

	public static Study find(String study_uri) {
		if (study_uri == null || study_uri.equals("")) {
			System.out.println("[ERROR] No valid STUDY_URI provided to retrieve Study object: " + study_uri);
			return null;
		}
		Study returnStudy = new Study();
		String prefixedUri = ValueCellProcessing.replacePrefixEx(study_uri);
		String adjustedUri = prefixedUri;
		if (adjustedUri.startsWith("http")) {
			adjustedUri = "<" + adjustedUri + ">";
		}
		String studyQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT DISTINCT ?studyType ?studyLabel ?title ?proj ?studyComment ?external ?agentUri ?institutionUri ?lastId" + 
				" WHERE {  " + 
				"      ?studyType rdfs:subClassOf* hasco:Study . " + 
				"      " + adjustedUri + " a ?studyType . " + 
				"      OPTIONAL { " + adjustedUri + " rdfs:label ?studyLabel } . " + 
				"	   OPTIONAL { " + adjustedUri + " hasco:hasTitle ?title } . " +
				"	   OPTIONAL { " + adjustedUri + " hasco:hasProject ?proj } . " +
				"      OPTIONAL { " + adjustedUri + " rdfs:comment ?studyComment } . " + 
				"      OPTIONAL { " + adjustedUri + " hasco:hasExternalSource ?external } . " + 
				"      OPTIONAL { " + adjustedUri + " hasco:hasAgent ?agentUri } .  " +
				"      OPTIONAL { " + adjustedUri + " hasco:hasInstitution ?institutionUri } . " + 
				"      OPTIONAL { " + adjustedUri + " hasco:hasLastId ?lastId } . " + 
				" } " + 
				" GROUP BY ?studyType ?studyLabel ?title ?proj ?studyComment ?external ?agentUri ?institutionUri ?lastId ";

		try {
			//System.out.println("Study's find() query: " + studyQueryString);
			Query studyQuery = QueryFactory.create(studyQueryString);
			QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), studyQuery);
			ResultSet results = qexec.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
			qexec.close();
			if (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				returnStudy.setUri(prefixedUri);
				if (soln.contains("studyLabel")) {
					returnStudy.setLabel(soln.get("studyLabel").toString());
				}
				if (soln.contains("studyType")) {
					returnStudy.setType(soln.get("studyType").toString());
				}
				if (soln.contains("title")) {
					returnStudy.setTitle(soln.get("title").toString());
				}
				if (soln.contains("proj")) {
					returnStudy.setProject(soln.get("proj").toString());
				}
				if (soln.contains("studyComment")) {
					returnStudy.setComment(soln.get("studyComment").toString());
				} 
				if (soln.contains("external")) {
					returnStudy.setExternalSource(soln.get("external").toString());
				} 
				if (soln.contains("agentUri")) {
					returnStudy.setAgentUri(soln.get("agentUri").toString());
				}
				if (soln.contains("institutionUri")) {
					returnStudy.setInstitutionUri(soln.get("institutionUri").toString());
				}
				if (soln.contains("lastId")) {
					returnStudy.setLastId(soln.get("lastId").toString());
				}
			}
			returnStudy.setDataAcquisitionUris(Study.findDataAcquisitionUris(adjustedUri));
			returnStudy.setObjectCollectionUris(Study.findObjectCollectionUris(adjustedUri));
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
				"   ?da hasco:hasSchema|hasco:hasDeployment ?s . " +
				"  	?s ?p ?o . " +
				"  	FILTER (?study = " + study + ") " +
				"  	} " +
				"    MINUS " +
				"    {  " +
				// Other Data Acquisition Schema and Deployment
				"  	?subUri rdfs:subClassOf* hasco:Study . " + 
				"  	?study a ?subUri . " +
				"  	?da hasco:isDataAcquisitionOf ?study . " + 
				"   ?da hasco:hasSchema|hasco:hasDeployment ?s . " +
				"  	?s ?p ?o . " +
				"  	FILTER (?study != " + study + ") " +
				"  	} " +
				"  } " +
				"  UNION " + 
				"  { " +
				"    { " +
				// Sample Collections
				"  	?subUri rdfs:subClassOf* hasco:Study . " + 
				"  	?study a ?subUri . " +
				"   ?s hasco:isSampleCollectionOf ?study . " + 
				"   ?s ?p ?o . " +
				"  FILTER (?study = " + study + ") " +
				"    } " +
				"    MINUS " +
				"    { " +
				// Other Sample Collections
				"  	?subUri rdfs:subClassOf* hasco:Study . " + 
				"  	?study a ?subUri . " +
				"   ?s hasco:isSampleCollectionOf ?study . " + 
				"   ?s ?p ?o . " +
				"  	FILTER (?study != " + study + ") " +
				"    } " +
				"  } "  +
				"  UNION " + 
				"  { " +
				"    { " +
				// Sample Collection Samples
				"  	?subUri rdfs:subClassOf* hasco:Study . " + 
				"  	?study a ?subUri . " +
				"   ?sc hasco:isSampleCollectionOf ?study . " + 
				"   ?s hasco:isObjectOf ?sc .  " +
				"   ?s ?p ?o . " +
				"  FILTER (?study = " + study + ") " +
				"    } " +
				"    MINUS " +
				"    { " +
				// Other Sample Collection Samples
				"  	?subUri rdfs:subClassOf* hasco:Study . " + 
				"  	?study a ?subUri . " +
				"   ?sc hasco:isSampleCollectionOf ?study . " + 
				"   ?s hasco:isObjectOf ?sc .  " +
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
				"  	?da hasco:hasDeployment ?deploy .  " +
				"	?deploy vstoi:hasPlatform|hasco:hasInstrument|hasco:hasDetector ?s . " +
				"  	?s ?p ?o . " +
				"  FILTER (?study = " + study + ") " +
				"    } " +
				"    MINUS " +
				"    { " +
				// Other Deployment - Platform, Instrument, detector
				"  	?subUri rdfs:subClassOf* hasco:Study .  " + 
				"  	?study a ?subUri . " +
				"   ?da hasco:isDataAcquisitionOf ?study . " + 
				"  	?da hasco:hasDeployment ?deploy .  " +
				"	?deploy vstoi:hasPlatform|hasco:hasInstrument|hasco:hasDetector ?s . " +
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
				"   ?s hasco:partOfSchema ?schema . " +
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
				"   ?s hasco:partOfSchema ?schema . " +
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

			//System.out.println("resultsrw.size(): " + resultsrw.size());
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

	public static List<Study> find() {
		List<Study> studies = new ArrayList<Study>();
		String queryString = "";
		queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT ?studyUri ?subUri " + 
				" WHERE {  ?subUri rdfs:subClassOf* hasco:Study . " + 
				"          ?studyUri a ?subUri . " +  
				" }";

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
				//System.out.println("Study URI: " + soln.get("studyUri").toString());
			}
			studies.add(study);
		}

		return studies;
	}

	public int deleteDataAcquisitions() {
		SolrClient study_solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.DATA_COLLECTION).build();
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
		SolrClient study_solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.DATA_ACQUISITION).build();
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

	public void save() {
		//System.out.println("Saving <" + uri + ">");
		if (studyUri == null || studyUri.equals("")) {
			System.out.println("[ERROR] Trying to save Study without assigning an URI");
			return;
		}

		delete();  // delete any existing triple for the current study

		String insert = "";
		String std_uri = "";

		if (this.getUri().startsWith("<")) {
			std_uri = this.getUri();
		} else {
			std_uri = "<" + this.getUri() + ">";
		}
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		if (studyType.startsWith("<")) {
			insert += std_uri + " a " + studyType + " . ";
		} else {
			insert += std_uri + " a <" + studyType + "> . ";
		}
		insert += std_uri + " rdfs:label  \"" + label + "\" . ";
		if (title != null && !title.equals("")) {
			insert += std_uri + " hasco:hasTitle \"" + title + "\" .  "; 
		} 
		if (project != null && !project.equals("")) {
			insert += std_uri + " hasco:hasProject \""  + project + "\" .  ";
		}   
		if (comment != null && !comment.equals("")) {
			insert += std_uri + " rdfs:comment \"" + comment + "\" .  ";
		}
		if (externalSource != null && !externalSource.equals("")) {
			insert += std_uri + " hasco:hasExternalSource \"" + externalSource + "\" .  ";
		}
		if (agentUri != null && !agentUri.equals("")) {
			if (agentUri.startsWith("<")) {
				insert += std_uri + " hasco:hasAgent " + agentUri + " .  ";
			} else {
				insert += std_uri + " hasco:hasAgent <" + agentUri + "> .  ";
			}
		}
		if (institutionUri != null && !institutionUri.equals("")) {
			if (institutionUri.startsWith("<")) {
				insert += std_uri + " hasco:hasInstitution " + institutionUri + " .  ";
			} else {
				insert += std_uri + " hasco:hasInstitution <" + institutionUri + "> .  ";
			}
		}
		if (lastId != null) {
			insert += std_uri + " hasco:hasLastId  \"" + lastId + "\" .  ";
		}
		insert += LINE_LAST;
		//System.out.println("Study insert query (pojo's save): <" + insert + ">");
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();

	}

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int saveSolr() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") +
					Collections.STUDIES).build();
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

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int saveToLabKey(String user_name, String password) {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
		row.put("a", ValueCellProcessing.replaceNameSpaceEx(studyType));
		row.put("rdfs:label", getLabel());
		row.put("hasco:hasTitle", getTitle());
		row.put("hasco:hasProject", ValueCellProcessing.replaceNameSpaceEx(getProject()));
		row.put("rdfs:comment", getComment());
		row.put("hasco:hasExternalSource", getExternalSource());
		row.put("skos:definition", "");
		row.put("hasco:hasAgent", ValueCellProcessing.replaceNameSpaceEx(this.getAgentUri()));
		row.put("hasco:hasLastId", getLastId());
		row.put("hasco:hasInstitution", ValueCellProcessing.replaceNameSpaceEx(this.getInstitutionUri()));
		rows.add(row);

		int totalChanged = 0;
		try {
			totalChanged = loader.insertRows("Study", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("Study", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update Study(ies)");
			}
		}
		return totalChanged;
	}

	public void delete() {
		String query = "";
		if (this.getUri() == null || this.getUri().equals("")) {
			return;
		}
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += DELETE_LINE1;
		if (this.getUri().startsWith("http")) {
			query += "<" + this.getUri() + ">";
		} else {
			query += this.getUri();
		}
		query += DELETE_LINE3;
		query += LINE_LAST;
		//System.out.println("SPARQL query inside dasa poho's delete: " + query);
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
		//deleteDataAcquisitions();
		//deleteMeasurements();
	}

	public int deleteFromSolr() {
		SolrClient study_solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") +
				Collections.STUDIES).build();
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

	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
	public int deleteFromLabKey(String user_name, String password) throws CommandException {
		String site = ConfigProp.getPropertyValue("labkey.config", "site");
		String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
		LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
		List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
		Map<String, Object> row = new HashMap<String, Object>();
		row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
		rows.add(row);
		for (Map<String,Object> str : rows) {
			System.out.println("deleting Study " + str.get("hasURI"));
		}

		return loader.deleteRows("Study", rows);
	}
}

