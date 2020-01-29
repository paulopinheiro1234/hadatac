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
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class MessageTopic extends HADatAcThing implements Comparable<MessageTopic> {

    private String streamUri = "";
    private String streamSpecUri = "";
    private String namedGraphUri = "";
	private String status = "";
    private String deploymentUri = "";
    private String submissionTime = "";
    private String completionTime = "";
    private String lastProcessTime = "";
    private List<String> headers = new ArrayList<String>();
    
    private String log = "";
    private AnnotationLogger logger = null;

    public static final String ACTIVE = "PREP";
    public static final String INACTIVE = "UNPREP";
    public static final String FAIL = "FAILED PREP";
    
    public MessageTopic() {
    	super();
        logger = new AnnotationLogger(this);
    }
    
	public String getStreamUri() {
		return streamUri;
	}

	public MessageStream getStream() {
		if (streamUri == null || streamUri.equals("")) {
			return null;
		}
		MessageStream stream = MessageStream.find(streamUri);
		return stream;
	}

	public void setStreamUri(String streamUri) {
		this.streamUri = streamUri;
	}
	
	public String getStreamSpecUri() {
		return streamSpecUri;
	}
	
	public void setNamedGraphUri(String namedGraphUri) {
		this.namedGraphUri = namedGraphUri;
	}
	
	public String getNamedGraphUri() {
		return namedGraphUri;
	}
	
	public STR getStreamSpec() {
		if (streamSpecUri == null || streamSpecUri.equals("")) {
			return null;
		}
		STR str = STR.findByUri(streamSpecUri);
		return str;
	}

	public void setStreamSpecUri(String streamSpecUri) {
		this.streamSpecUri = streamSpecUri;
	}
	
    public String getStatus() {
    	if (status == null || status.isEmpty()) {
    		status = "off";
    	}
    	return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    
    public String getDeploymentUri() {
        return deploymentUri;
    }

    public Deployment getDeployment() {
    	if (deploymentUri == null || deploymentUri.equals("")) {
    		return null;
    	}
    	return Deployment.find(deploymentUri);
    }
    
    public void setDeploymentUri(String deploymentUri) {
        this.deploymentUri = deploymentUri;
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
    
    public List<String> getHeaders() {
        return headers;
    }
    public void setHeaders(List<String> headers) {
        this.headers = headers;
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
        
	@Override
	public boolean equals(Object o) {
		if((o instanceof MessageTopic) && (((MessageTopic)o).getUri().equals(this.getUri()))) {
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

	private static List<MessageTopic> execFindQuery(String query) {
		List<MessageTopic> topics = new ArrayList<MessageTopic>();
		ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
		while (resultsrw.hasNext()) {
		    QuerySolution soln = resultsrw.next();
		    MessageTopic topic = find(soln.getResource("uri").getURI());
		    topics.add(topic);
		}			
		if (topics != null && topics.size() > 1) {
			java.util.Collections.sort((List<MessageTopic>) topics);
		}
		return topics;
	}
	
	public static List<MessageTopic> find() {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
			    " SELECT ?uri WHERE { " +
			    " ?uri a hasco:MessageTopic ." + 
			    "} ";
		return execFindQuery(queryString);
	}
	
	public static List<MessageTopic> findByStream(String streamUri) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    " ?uri a hasco:MessageTopic ." + 
            " ?uri hasco:hasMessageStream <" + streamUri + "> . " +
		    "} ";
		return execFindQuery(queryString);
	}
	
	public static List<MessageTopic> findActiveByStream(String streamUri) {
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
		    " SELECT ?uri WHERE { " +
		    " ?uri a hasco:MessageTopic ." + 
            " ?uri hasco:hasMessageStream <" + streamUri + "> . " +
		    " ?uri hasco:hasStatus \"" + ACTIVE + "\" . " +
		    "} ";
		return execFindQuery(queryString);
	}
	
	public static MessageTopic find(String uri) {
	    MessageTopic stream = null;
	    Statement statement;
	    RDFNode object;
	    RDFNode subject;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		stream = new MessageTopic();
		StmtIterator stmtIterator = model.listStatements();
		
		if (!stmtIterator.hasNext()) {
			return null;
		}
		
		List<String> tmpHeaders = new ArrayList<String>();
		
		while (stmtIterator.hasNext()) {
		    statement = stmtIterator.next();
		    object = statement.getObject();
		    subject = statement.getSubject();
		    if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    	stream.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                stream.setTypeUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasMessageTopic")) {
		    	stream.setDeploymentUri(subject.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasMessageStream")) {
		    	stream.setStreamUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStreamSpec")) {
		    	stream.setStreamSpecUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStatus")) {
		    	stream.setStatus(object.asLiteral().getString());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLog")) {
		    	if (object.asLiteral().getString() != null) {
		    		stream.setLogger(new AnnotationLogger(stream, object.asLiteral().getString()));
		    	} 
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasHeader")) {
		    	tmpHeaders.add(object.asLiteral().getString());
		    }
		}		
		stream.setHeaders(tmpHeaders);
		stream.setUri(uri);
		
		return stream;
	}
	
    @Override
    public int compareTo(MessageTopic another) {
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

        String topic_uri = "";
        if (this.getUri().startsWith("<")) {
            topic_uri = this.getUri();
        } else {
            topic_uri = "<" + this.getUri() + ">";
        }

        insert += NameSpaces.getInstance().printSparqlNameSpaceList();
        insert += INSERT_LINE1;

        if (!getNamedGraph().isEmpty()) {
            insert += " GRAPH <" + getNamedGraph() + "> { ";
        }

        insert += topic_uri + " a <http://hadatac.org/ont/hasco/MessageTopic> . ";
        if (this.getLabel() != null && !this.getLabel().equals("")) {
        	insert += topic_uri + " rdfs:label  \"" + this.getLabel() + "\" . ";
        }
        if (this.getComment() != null && !this.getComment().equals("")) {
            insert += topic_uri + " rdfs:comment  \"" + this.getComment() + "\" . ";
        }
    	if (this.getStreamUri() != null && !this.getStreamUri().equals("")) {
        	insert += topic_uri + " hasco:hasMessageStream  <" + this.getStreamUri() + "> . ";
        }
    	if (this.getStreamSpecUri() != null && !this.getStreamSpecUri().equals("")) {
        	insert += topic_uri + " hasco:hasStreamSpec  <" + this.getStreamSpecUri() + "> . ";
        }
    	if (this.getDeploymentUri() != null && !this.getDeploymentUri().equals("")) {
        	insert += " <" + this.getDeploymentUri() + ">  hasco:hasMessageTopic  " + topic_uri+ " . ";
        }
        if (this.getStatus() != null && !this.getStatus().equals("")) {
            insert += topic_uri + " hasco:hasStatus  \"" + this.getStatus() + "\" . ";
        }
        if (this.getLog() != null && !this.getLog().equals("")) {
        	insert += topic_uri + " hasco:hasLog  \"" + this.getLog() + "\" . ";
        }
        if (this.getHeaders() != null && this.getHeaders().size() > 0) {
        	for (String header : this.getHeaders()) {
        		insert += topic_uri + " hasco:hasHeader  \"" + header + "\" . ";
        	}
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
