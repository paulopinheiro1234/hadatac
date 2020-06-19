package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
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
import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.PropertyValueType;
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.data.loader.GeneratorChain;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

public class MessageTopic extends HADatAcThing implements Comparable<MessageTopic> {

    @PropertyField(uri="hasco:isMemberOf", valueType=PropertyValueType.URI)
    private String streamUri = "";

    @PropertyField(uri="hasco:hasDeployment", valueType=PropertyValueType.URI)
    private String deploymentUri = "";
    
    @PropertyField(uri="hasco:hasStudyObject", valueType=PropertyValueType.URI)
    private String objUri = "";
    
    @PropertyField(uri="hasco:hasSOC", valueType=PropertyValueType.URI)
    private String socUri = "";
    
    @PropertyField(uri="hasco:hasCellScope")
    private String cellScope = "";
    
    private StudyObject obj = null;
 
    private ObjectCollection soc = null;
    
    private Deployment dpl = null;
    
    public MessageTopic() {
    	super();
    }
    
	public String getStreamUri() {
		return streamUri;
	}

	public STR getStream() {
		if (streamUri == null || streamUri.equals("")) {
			return null;
		}
		STR stream = STR.findByUri(streamUri);
		return stream;
	}

	public void setStreamUri(String streamUri) {
		this.streamUri = streamUri;
	}
	
    public String getDeploymentUri() {
        return deploymentUri;
    }

    public Deployment getDeployment() {
    	if (dpl != null) {
    		return dpl;
    	}
    	if (deploymentUri == null || deploymentUri.equals("")) {
    		return null;
    	}
    	dpl = Deployment.find(deploymentUri);
    	return dpl;
    }
    
    public void setDeploymentUri(String deploymentUri) {
        this.deploymentUri = deploymentUri;
        getDeployment();
    }

    public String getStudyObjectUri() {
        return objUri;
    }

    public StudyObject getStudyObject() {
    	if (obj != null) {
    		return obj;
    	}
    	if (objUri == null || objUri.equals("")) {
    		return null;
    	}
    	obj = StudyObject.find(objUri);
    	return obj;
    }
    
    public void setStudyObjectUri(String objUri) {
    	if (objUri == null || objUri.isEmpty()) {
    		this.obj = null;
    		this.objUri = null;
    		this.soc = null;
    		this.socUri = null;
    		return;
    	}
    	obj = StudyObject.find(URIUtils.replacePrefixEx(objUri.trim()));
    	if (obj == null) {
    		System.out.println("No scope object");
    		this.obj = null;
    		this.objUri = null;
    		this.soc = null;
    		this.socUri = null;
    		return;
    	} 
    	this.objUri = objUri;
    	setSOC(obj.getIsMemberOf());
    }

    public ObjectCollection getSOC() {
    	return soc;
    }
    
    public String getSOCUri() {
        return socUri;
    }
    public void setSOC(String socUri) {
    	if (socUri == null || socUri.isEmpty()) {
    		this.soc = null;
    		this.socUri = null;
    		return;
    	}
    	soc = ObjectCollection.find(socUri);
    	if (soc != null) {
    		this.socUri = socUri;
    	} else {
    		this.socUri = null;
    	}
    }

    public String getCellScope() {
        return cellScope;
    }
    public void setCellScope(String cellScope) {
        this.cellScope = cellScope;
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
    public static String LINE3 = INDENT1 + "a         hasco:MessageTopic;  ";
    public static String DELETE_LINE3 = INDENT1 + " ?p ?o . ";
    public static String DELETE_LINE4 = "  hasco:hasLastCounter ?o . ";
    public static String LINE_LAST = "}  ";

    public void cacheTopic() {
    	this.getSOC();
    	this.getDeployment();
    	this.getStudyObject();
    }
    
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
            " ?uri hasco:isMemberOf <" + streamUri + "> . " +
		    "} ";
		return execFindQuery(queryString);
	}
	
	public static MessageTopic find(String uri) {
	    MessageTopic topic = null;
	    Statement statement;
	    RDFNode object;
	    RDFNode subject;
	    
	    String queryString = "DESCRIBE <" + uri + ">";
	    Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);
		
		topic = new MessageTopic();
		StmtIterator stmtIterator = model.listStatements();
		
		if (!stmtIterator.hasNext()) {
			return null;
		}
		
		while (stmtIterator.hasNext()) {
		    statement = stmtIterator.next();
		    object = statement.getObject();
		    subject = statement.getSubject();
		    if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
		    	topic.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                topic.setTypeUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasDeployment")) {
                topic.setDeploymentUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasStudyObject")) {
                topic.setStudyObjectUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSOC")) {
                topic.setSOC(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/isMemberOf")) {
		    	topic.setStreamUri(object.asResource().getURI());
		    } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasCellScope")) {
		    	topic.setCellScope(object.asLiteral().getString());
		    } 
		    
		}		
		topic.setUri(uri);
		
		return topic;
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
    public boolean saveToSolr() {
        return false;
    }
    
    @Override
    public int deleteFromSolr() {
        return 0;
    }
    
}
