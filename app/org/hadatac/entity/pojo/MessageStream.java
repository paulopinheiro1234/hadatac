package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.solr.client.solrj.beans.Field;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.eclipse.paho.client.mqttv3.MqttClient;

public class MessageStream extends HADatAcThing implements Comparable<MessageStream> {

    private String ip = "";
	private String port = "";
	private String status = "";
	private String protocol = "";
    private String id;
    private String viewableId = "";
    private String editableId = "";
    private String name = "";
    private String ownerEmail = "";
    private List<String> viewerEmails;
    private List<String> editorEmails;
    private String studyUri = "";
    private String dataAcquisitionUri = "";
    private String datasetUri = "";
    private String submissionTime = "";
    private String completionTime = "";
    private String lastProcessTime = "";
    private String dataFileId = "";
    private DataFile archive = null;
    private String log = "";
    private AnnotationLogger logger = null;
    private boolean succeed = false;
    private long totalMessages = 0;
    private int ingestedMessages = 0;

    public static final String INITIATED = "INITIATED";
    public static final String ACTIVE = "ACTIVE";
    public static final String CLOSED = "CLOSED";
    
    public MessageStream() {
    	super();
        this.id = UUID.randomUUID().toString();
        logger = new AnnotationLogger(this);
        totalMessages = 0;
        ingestedMessages = 0;
    }
    
	public String getIP() {
		return ip;
	}

	public void setIP(String ip) {
		this.ip = ip;
	}
	
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
    
    public String getStatus() {
    	if (status == null || status.isEmpty()) {
    		status = CLOSED;
    	}
    	return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getViewableId() {
        return viewableId;
    }
    public void setViewableId(String viewableId) {
        this.viewableId = viewableId;
    }
    
    public String getEditableId() {
        return editableId;
    }
    public void setEditableId(String editableId) {
        this.editableId = editableId;
    }
    
    public String getName() {
    	if (name != null && !name.isEmpty()) {
    		return name;
    	}
    	return label + "_at_" + ip + "_" + port; 
    }
    public void setName(String name) {
        this.name = name;
    }
  
    public String getStudyUri() {
        return studyUri;
    }
    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    public String getDataAcquisitionUri() {
        return dataAcquisitionUri;
    }
    public void setDataAcquisitionUri(String dataAcquisitionUri) {
        this.dataAcquisitionUri = dataAcquisitionUri;
    }

    public String getDatasetUri() {
        return datasetUri;
    }
    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

    public String getSubmissionTime() {
        return submissionTime;
    }
    public void setSubmissionTime(String submissionTime) {
        this.submissionTime = submissionTime;
    }

    public String getCompletionTime() {
        return completionTime;
    }
    public void setCompletionTime(String completionTime) {
        this.completionTime = completionTime;
    }

    public String getLastProcessTime() {
        return lastProcessTime;
    }
    public void setLastProcessTime(String lastProcessTime) {
        this.lastProcessTime = lastProcessTime;
    }
    
    public String getDataFileId() {
        return dataFileId;
    }
    public void setDataFileId(String dataFileId) {
        this.dataFileId = dataFileId;
    }
    
    public DataFile getArchive() {
        return archive;
    }
    public void setArchive(DataFile archive) {
        this.archive = archive;
    }
    
    public String getLog() {
        return getLogger().getLog();
    }
    public void setLog(String log) {
        getLogger().setLog(log);
        this.log = log;
    }

    public AnnotationLogger getLogger() {
        return logger;
    }
    public void setLogger(AnnotationLogger logger) {
        this.logger = logger;
    }
    
    public long getTotalMessages() {
        return totalMessages;
    }
    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }
    
    public int getIngestedMessages() {
        return ingestedMessages;
    }
    public void setIngestedMessages(int ingestedMessages) {
        this.ingestedMessages = ingestedMessages;
    }
    
    public boolean hasSucceed() {
        return succeed;
    }
    public void setSucceed(boolean succeed) {
        this.succeed = succeed;
    }
    
	@Override
	public boolean equals(Object o) {
		if((o instanceof MessageStream) && (((MessageStream)o).getUri().equals(this.getUri()))) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}
	
	public static String INDENT1 = "   ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:ObjectCollection;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String DELETE_LINE4 = "  hasco:hasLastCounter ?o . ";
    public static String LINE_LAST = "}  ";

    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }
	
	public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
			Facet facet, FacetHandler facetHandler) {
		String valueConstraint = "";
		if (!facet.getFacetValuesByField("platform_uri_str").isEmpty()) {
			valueConstraint += " VALUES ?platformUri { " + stringify(
					facet.getFacetValuesByField("platform_uri_str")) + " } \n ";
		}
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?platformUri ?instrumentUri ?dataAcquisitionUri ?instrumentLabel ?dataAcquisitionLabel WHERE { \n"
				+ valueConstraint
				+ " ?dataAcquisitionUri hasco:hasDeployment ?deploymentUri . \n"
				+ " ?deploymentUri vstoi:hasPlatform ?platformUri . \n"
				+ " ?deploymentUri hasco:hasInstrument ?instrumentUri . \n"
				+ " ?instrumentUri rdfs:label ?instrumentLabel . \n"
				+ " ?dataAcquisitionUri rdfs:label ?dataAcquisitionLabel . \n"
				+ " } \n";

		//System.out.println("Instrument getTargetFacets query: " + query);
		
		facet.clearFieldValues("acquisition_uri_str");
		
		Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
		try {
		    ResultSetRewindable resultsrw = SPARQLUtils.select(
	                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		    
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				MessageStream instrument = new MessageStream();
				instrument.setUri(soln.get("instrumentUri").toString());
				instrument.setLabel(soln.get("instrumentLabel").toString());
				instrument.setQuery(query);
				instrument.setField("instrument_uri_str");
				
				ObjectAccessSpec da = new ObjectAccessSpec();
				da.setUri(soln.get("dataAcquisitionUri").toString());
				da.setLabel(soln.get("dataAcquisitionLabel").toString());
				da.setField("acquisition_uri_str");
				
				if (!results.containsKey(instrument)) {
					List<Facetable> facets = new ArrayList<Facetable>();
					results.put(instrument, facets);
				}
				if (!results.get(instrument).contains(da)) {
					results.get(instrument).add(da);
				}
				
				Facet subFacet = facet.getChildById(instrument.getUri());
				subFacet.putFacet("instrument_uri_str", instrument.getUri());
				subFacet.putFacet("acquisition_uri_str", da.getUri());
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		return results;
	}
	
	public static List<MessageStream> find() {
		List<MessageStream> streams = new ArrayList<MessageStream>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    " ?uri a hasco:MessageStream ." + 
		    "} ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
			
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    MessageStream stream = find(soln.getResource("uri").getURI());
		    streams.add(stream);
		}			
		
		if (streams != null && streams.size() > 1) {
			java.util.Collections.sort((List<MessageStream>) streams);
		}

		return streams;
	}
	
	public static List<Deployment> findDeployments() {
		List<Deployment> deployments = new ArrayList<Deployment>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?stream ?topic ?topicName ?dpl WHERE { " +
		    " ?stream a hasco:MessageStream ." + 
            " ?topic hasco:hasMessageStream ?stream . " +
		    " ?topic rdfs:label ?topicName . " +
		    " ?dpl hasco:hasMessageTopic ?topic . " +
		    "} ";
		
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
			
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    Deployment dpl = Deployment.find(soln.getResource("dpl").getURI());
		    deployments.add(dpl);
		}			
		
		return deployments;
	}
	
	public static MessageStream find(String uri) {
	    MessageStream stream = null;
	    Statement statement;
	    RDFNode object;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		stream = new MessageStream();
		StmtIterator stmtIterator = model.listStatements();
		
		if (!stmtIterator.hasNext()) {
			return null;
		}
		
		while (stmtIterator.hasNext()) {
		    statement = stmtIterator.next();
		    object = statement.getObject();
		    if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    	stream.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                stream.setTypeUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasId")) {
		    	stream.setId(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasIP")) {
		    	stream.setIP(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasPort")) {
                stream.setPort(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStatus")) {
		    	stream.setStatus(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasProtocol")) {
		    	stream.setProtocol(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasDataFileId")) {
		    	stream.setDataFileId(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasTotalMessages")) {
		    	stream.setTotalMessages(object.asLiteral().getLong());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasIngestedMessages")) {
		    	stream.setIngestedMessages(object.asLiteral().getInt());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLog")) {
		    	if (object.asLiteral().getString() != null) {
		    		stream.setLogger(new AnnotationLogger(stream, object.asLiteral().getString()));
		    	} 
		    }
		}
		
		stream.setUri(uri);
		
		return stream;
	}
	
    @Override
    public int compareTo(MessageStream another) {
        return this.getLabel().compareTo(another.getLabel());
    }

    @Override
    public void save() {
        saveToTripleStore();
    }

    @Override
    public boolean saveToTripleStore() {
    	deleteFromTripleStore();
        String insert = "";

        String stream_uri = "";
        if (this.getUri().startsWith("<")) {
            stream_uri = this.getUri();
        } else {
            stream_uri = "<" + this.getUri() + ">";
        }

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }

        insert += stream_uri + " a <http://hadatac.org/ont/hasco/MessageStream> . ";
        if (this.getLabel() != null && !this.getLabel().equals("")) {
        	insert += stream_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
        }
        if (this.getComment() != null && !this.getComment().equals("")) {
            insert += stream_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
        }
    	if (this.getId() != null && !this.getId().equals("")) {
        	insert += stream_uri + " hasco:hasId  \"" + this.getId() + "\" . ";
        }
    	if (this.getIP() != null && !this.getIP().equals("")) {
        	insert += stream_uri + " hasco:hasIP  \"" + this.getIP() + "\" . ";
        }
        if (this.getPort() != null && !this.getPort().equals("")) {
            insert += stream_uri + " hasco:hasPort  \"" + this.getPort() + "\" . ";
        }
        if (this.getStatus() != null && !this.getStatus().equals("")) {
            insert += stream_uri + " hasco:hasStatus  \"" + this.getStatus() + "\" . ";
        }
        if (this.getProtocol() != null && !this.getProtocol().equals("")) {
        	insert += stream_uri + " hasco:hasProtocol  \"" + this.getProtocol() + "\" . ";
        }
        if (this.getDataFileId() != null && !this.getDataFileId().equals("")) {
            insert += stream_uri + " hasco:hasDataFileId  \"" + this.getDataFileId() + "\" . ";
        }
        insert += stream_uri + " hasco:hasTotalMessages  \"" + this.getTotalMessages() + "\" . ";
        insert += stream_uri + " hasco:hasIngestedMessages  \"" + this.getIngestedMessages() + "\" . ";
        if (this.getLog() != null && !this.getLog().equals("")) {
        	insert += stream_uri + " hasco:hasLog  \"" + this.getLog() + "\" . ";
        }

        if (!getNamedGraph().isEmpty()) {
            insert += " } ";
        }

        insert += LINE_LAST;

        try {
            UpdateRequest request = UpdateFactory.create(insert);
            UpdateProcessor processor = UpdateExecutionFactory.createRemote(
                    request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
            processor.execute();
        } catch (QueryParseException e) {
            System.out.println("QueryParseException due to update query: " + insert);
            throw e;
        }

        return true;
    }
    
    @Override
    public void delete() {
        deleteFromTripleStore();
    }

    @Override
    public void deleteFromTripleStore() {
    	super.deleteFromTripleStore();
    }

    @Override
    public boolean saveToSolr() {
        return false;
    }
    
    @Override
    public int deleteFromSolr() {
        return 0;
    }
    
    @Override
    public int saveToLabKey(String userName, String password) {
        return 0;
    }
    
    @Override
    public int deleteFromLabKey(String userName, String password) {
        return 0;
    }

}
