package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

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
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.ConfigProp;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.labkey.remoteapi.CommandException;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;

public class Sample {

    public static String INDENT1 = "     ";
    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String LINE3 = INDENT1 + "a         hasco:Sample;  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "SP-";

    private String uri;
    private String sampleType;
    private String originalID;
    private String label;
    private String partOfCollection;
    private String comment;
    private String isSampleOf;
    
    public Sample(String uri, String partOfCollection) {
	this.setUri(uri);
	this.setTypeUri("");
	this.setOriginalID("");
	this.setLabel("");
	this.setPartOfCollection(partOfCollection);
	this.setComment("");
	this.setIsSampleOf("");
    }

    public Sample(String uri,
		  String sampleType,
		  String originalID,
		  String label,
		  String partOfCollection,
		  String comment,
		  String isSampleOf) { 
	this.setUri(uri);
        this.setTypeUri(sampleType);
	this.setOriginalID(originalID);
	this.setLabel(label);
	this.setPartOfCollection(partOfCollection);
	this.setComment(comment);
	this.setIsSampleOf(isSampleOf);
    }
    
    public String getUri() {
	if (uri == null) {
	    return "";
	} else {
	    return uri;
	}
    }
    
    public String getUriNamespace() {
	return ValueCellProcessing.replaceNameSpaceEx(uri.replace("<","").replace(">",""));
    }
    
    public void setUri(String uri) {
	this.uri = uri;
    }
    
    public String getTypeUri() {
	if (sampleType.equals("")) {
	    return ValueCellProcessing.replaceNameSpaceEx(sampleType);
	}
	return sampleType;
    }
    
    public SampleType getType() {
	if (sampleType == null || sampleType.equals("")) {
	    return null;
	}
	return SampleType.find(sampleType);
    }
    
    public void setTypeUri(String sampleType) {
	this.sampleType = sampleType;
    }
    
    public String getOriginalID() {
	if (originalID == null) {
	    return "";
	} else {
	    return originalID;
	}
    }
    
    public void setOriginalID(String originalID) {
	this.originalID = originalID;
    }
    
    public String getLabel() {
	if (label == null) {
	    return "";
	} else {
	    return label;
	}
    }
    
    public void setLabel(String label) {
	this.label = label;
    }
    
    public String getPartOfCollection() {
	if (partOfCollection == null) {
	    return "";
	} else {
	    return partOfCollection;
	}
    }
    
    public void setPartOfCollection(String partOfCollection) {
	this.partOfCollection = partOfCollection;
    }
    
    public String getComment() {
	if (comment == null) {
	    return "";
	} else {
	    return comment;
	}
    }
    
    public void setComment(String comment) {
	this.comment = comment;
    }
    
    public String getIsSampleOf() {
	if (isSampleOf == null) {
	    return "";
	} else {
	    return isSampleOf;
	}
    }
    
    public void setIsSampleOf(String isSampleOf) {
	this.isSampleOf = isSampleOf;
    }
    
    public static Sample find(String sp_uri) {
	Sample sp = null;
	System.out.println("Looking for sample with URI " + sp_uri);
	if (sp_uri.startsWith("http")) {
	    sp_uri = "<" + sp_uri + ">";
	}
	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT  ?spType ?originalID ?partOfCollection ?hasLabel " + 
	    " ?hasComment ?hasSource ?isPIConfirmed WHERE { " + 
	    "    " + sp_uri + " a ?spType . " + 
	    "    " + sp_uri + " hasco:partOfCollection ?partOfCollection .  " + 
	    "    OPTIONAL { " + sp_uri + " hasco:originalID ?originalID } . " + 
	    "    OPTIONAL { " + sp_uri + " rdfs:label ?hasLabel } . " + 
	    "    OPTIONAL { " + sp_uri + " rdfs:comment ?hasComment } . " + 
	    "    OPTIONAL { " + sp_uri + " hasco:isSampleOf ?isSampleOf } . " + 
	    "    OPTIONAL { " + sp_uri + " hasco:hasSource ?hasSource } . " + 
	    "    OPTIONAL { " + sp_uri + " hasco:isPIConfirmed ?isPIConfirmed } . " + 
	    "}";
	Query query = QueryFactory.create(queryString);
	
	QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	qexec.close();
	
	if (!resultsrw.hasNext()) {
	    System.out.println("[WARNING] Sample. Could not find SP with URI: " + sp_uri);
	    return sp;
	}
	
	String sampleTypeStr = "";
	String originalIDStr = "";
	String labelStr = "";
	String partOfCollectionStr = "";
	String commentStr = "";
	String isSampleOfStr = "";
	
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null) {
		
		try {
		    if (soln.getResource("spType") != null && soln.getResource("spType").getURI() != null) {
			sampleTypeStr = soln.getResource("spType").getURI();
		    }
		} catch (Exception e1) {
		    sampleTypeStr = "";
		}
		
		try {
		    if (soln.getLiteral("originalID") != null && soln.getLiteral("originalID").getString() != null) {
			originalIDStr = soln.getLiteral("originalID").getString();
		    }
		} catch (Exception e1) {
		    originalIDStr = "";
		}
		
		labelStr = FirstLabel.getLabel(sp_uri);

		try {
		    if (soln.getResource("partOfCollection") != null && soln.getResource("partOfCollection").getURI() != null) {
			partOfCollectionStr = soln.getResource("partOfCollection").getURI();
		    }
		} catch (Exception e1) {
		    partOfCollectionStr = "";
		}
		
		try {
		    if (soln.getLiteral("hasComment") != null && soln.getLiteral("hasComment").getString() != null) {
			commentStr = soln.getLiteral("hasComment").getString();
		    }
		} catch (Exception e1) {
		    commentStr = "";
		}
		
		try {
		    if (soln.getResource("isSampleOf") != null && soln.getResource("isSampleOf").getURI() != null) {
			isSampleOfStr = soln.getResource("isSampleOf").getURI();
		    }
		} catch (Exception e1) {
		    isSampleOfStr = "";
		}
		
		sp = new Sample(sp_uri,
				sampleTypeStr,
				originalIDStr,
				labelStr,
				partOfCollectionStr,
				commentStr,
				isSampleOfStr);
	    }
	}
	return sp;
    }
    
    public static List<Sample> findByCollection(SampleCollection sc) {
	if (sc == null) {
	    return null;
	}
    	List<Sample> samples = new ArrayList<Sample>();
    	
    	String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
	    "SELECT ?uri WHERE { " + 
	    "   ?uri hasco:partOfCollection  <" + sc.getUri() + "> . " +
	    " } ";
	System.out.println("Sample findByCollection: " + queryString);
    	Query query = QueryFactory.create(queryString);
    	QueryExecution qexec = QueryExecutionFactory.sparqlService(
			Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	ResultSet results = qexec.execSelect();
	ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
	while (resultsrw.hasNext()) {
	    QuerySolution soln = resultsrw.next();
	    if (soln != null && soln.getResource("uri").getURI() != null) { 
		System.out.println("URI: [" + soln.getResource("uri").getURI() + "]");
		Sample sample = Sample.find(soln.getResource("uri").getURI());
		samples.add(sample);
	    }
	}
	return samples;
    }

    public void save() {
	delete();  // delete any existing triple for the current SP
	System.out.println("Saving <" + uri + ">");
	if (uri == null || uri.equals("")) {
	    System.out.println("[ERROR] Trying to save SP without assigning an URI");
	    return;
	}
	if (partOfCollection == null || partOfCollection.equals("")) {
	    System.out.println("[ERROR] Trying to save SP without assigning DAS's URI");
	    return;
	}
	String insert = "";

	String sp_uri = "";
	if (this.getUri().startsWith("<")) {
	    sp_uri = this.getUri();
	} else {
	    sp_uri = "<" + this.getUri() + ">";
	}

	    
	insert += NameSpaces.getInstance().printSparqlNameSpaceList();
    	insert += INSERT_LINE1;
	if (sampleType.startsWith("http")) {
	    insert += sp_uri + " a <" + sampleType + "> . ";
	} else {
	    insert += sp_uri + " a " + sampleType + " . ";
	}
	if (!originalID.equals("")) {
	    insert += sp_uri + " hasco:originalID \""  + originalID + "\" .  ";
	}   
	if (!label.equals("")) {
	    insert += sp_uri + " rdfs:label  \"" + label + "\" . ";
	}
	if (!partOfCollection.equals("")) {
	    if (partOfCollection.startsWith("http")) {
		insert += sp_uri + " hasco:partOfCollection <" + partOfCollection + "> .  "; 
	    } else {
		insert += sp_uri + " hasco:partOfCollection " + partOfCollection + " .  "; 
	    } 
	}
	if (!comment.equals("")) {
	    insert += sp_uri + " hasco:hasComment \""  + comment + "\" .  ";
	}   
	if (!isSampleOf.equals("")) {
	    if (isSampleOf.startsWith("http")) {
		insert += sp_uri + " hasco:isSampleOf <" + isSampleOf + "> .  "; 
	    } else {
		insert += sp_uri + " hasco:isSampleOf " + isSampleOf + " .  "; 
	    } 
	}
	//insert += this.getUri() + " hasco:hasSource " + " .  "; 
	//insert += this.getUri() + " hasco:isPIConfirmed " + " .  "; 
    	insert += LINE_LAST;
	System.out.println("SP insert query (pojo's save): <" + insert + ">");
    	UpdateRequest request = UpdateFactory.create(insert);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				      request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) {
	String site = ConfigProp.getPropertyValue("labkey.config", "site");
        String path = "/" + ConfigProp.getPropertyValue("labkey.config", "folder");
    	LabkeyDataHandler loader = new LabkeyDataHandler(site, user_name, password, path);
    	List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
    	Map<String, Object> row = new HashMap<String, Object>();
    	row.put("hasURI", ValueCellProcessing.replaceNameSpaceEx(getUri()));
    	row.put("a", ValueCellProcessing.replaceNameSpaceEx(getTypeUri()));
    	row.put("hasco:originalID", getOriginalID());
    	row.put("rdfs:label", getLabel());
    	row.put("hasco:partOfCollection", ValueCellProcessing.replaceNameSpaceEx(getPartOfCollection()));
    	row.put("rdfs:comment", getComment());
    	row.put("hasco:isSampleOf", ValueCellProcessing.replaceNameSpaceEx(getIsSampleOf()));
	row.put("hasco:hasSource", "");
    	row.put("hasco:isVirtual", "");
    	row.put("hasco:isPIConfirmed", "false");
    	rows.add(row);
	int totalChanged = 0;
    	try {
	    totalChanged = loader.insertRows("Sample", rows);
	} catch (CommandException e) {
	    try {
		totalChanged = loader.updateRows("Sample", rows);
	    } catch (CommandException e2) {
		System.out.println("[ERROR] Could not insert or update Sample(s)");
	    }
	}
	return totalChanged;
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
	    System.out.println("deleting sample " + row.get("hasURI"));
	}
    	return loader.deleteRows("Sample", rows);
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
	//System.out.println("SPARQL query inside sp poho's delete: " + query);
    	UpdateRequest request = UpdateFactory.create(query);
        UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
        processor.execute();
    }
    
}
