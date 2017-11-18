package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
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
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.metadata.DynamicFunctions;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.ValueCellProcessing;
import org.hadatac.utils.Collections;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;
import org.labkey.remoteapi.CommandException;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import play.Play;

public class Indicator extends HADatAcThing implements Comparable<Indicator> {

	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";

	private String uri;
	private String label;
	private String comment;
	private String superUri;

	static String className = "hasco:Indicator";

	public Indicator() {
		setUri("");
		setSuperUri("hasco:Indicator");
		setLabel("");
		setComment("");
	}

	public Indicator(String uri) {
		setUri(uri);
		setSuperUri("hasco:Indicator");
		setLabel("");
		setComment("");
	}

	public Indicator(String uri, String label, String comment) {
		setUri(uri);
		setSuperUri("hasco:Indicator");
		setLabel(label);
		setComment(comment);
	}

	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}

	public String getSuperUri() {
		return superUri;
	}
	public void setSuperUri(String superUri) {
		this.superUri = superUri;
	}

	public String getLabel() {
		return label;
	}
	public void setLabel(String label) {
		this.label = label;
	}

	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}

	@Override
	public boolean equals(Object o) {
		if((o instanceof Indicator) && (((Indicator)o).getUri() == this.getUri())) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return getUri().hashCode();
	}

	public static List<Indicator> find() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf hasco:Indicator . " + 
				"} ";

		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Indicator indicator = find(soln.getResource("uri").getURI());
			indicators.add(indicator);
		}			

		java.util.Collections.sort((List<Indicator>) indicators);
		return indicators;		
	}

	public static Indicator find(String uri) {
		Indicator indicator = null;
		Model model;
		Statement statement;
		RDFNode object;

		String queryString = "DESCRIBE <" + uri + ">";
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.sparqlService(
				Play.application().configuration().getString("hadatac.solr.triplestore") 
				+ Collections.METADATA_SPARQL, query);
		model = qexec.execDescribe();

		indicator = new Indicator();
		StmtIterator stmtIterator = model.listStatements();

		while (stmtIterator.hasNext()) {
			statement = stmtIterator.next();
			object = statement.getObject();
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
				indicator.setLabel(object.asLiteral().getString());
			}
			if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
				indicator.setComment(object.asLiteral().getString());
			}
		}

		indicator.setUri(uri);

		return indicator;
	}

	public static List<Indicator> findRecursive() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				" SELECT ?uri WHERE { " +
				" ?uri rdfs:subClassOf hasco:Indicator+ . " + 
				"} ";

		Query query = QueryFactory.create(queryString);

		QueryExecution qexec = QueryExecutionFactory.sparqlService(Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet results = qexec.execSelect();
		ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
		qexec.close();

		while (resultsrw.hasNext()) {
			QuerySolution soln = resultsrw.next();
			Indicator indicator = find(soln.getResource("uri").getURI());
			indicators.add(indicator);
		}			

		java.util.Collections.sort((List<Indicator>) indicators);
		return indicators;		
	}

	public static List<Indicator> findStudyIndicators() {
		List<Indicator> indicators = new ArrayList<Indicator>();
		String query = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?indicator ?indicatorLabel ?indicatorComment WHERE { "
				+ " ?subTypeUri rdfs:subClassOf* hasco:Study . "
				+ " ?studyUri a ?subTypeUri . "
				+ " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
				+ " ?dataAcq hasco:hasSchema ?schemaUri ."
				+ " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ " ?schemaAttribute hasco:hasAttribute ?attribute . "
				+ " {  { ?indicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?indicator rdfs:subClassOf hasco:SampleIndicator } } . "
				+ " ?indicator rdfs:label ?indicatorLabel . " 
				+ " OPTIONAL { ?indicator rdfs:comment ?indicatorComment } . "
				+ " ?attribute rdfs:subClassOf+ ?indicator . " 
				+ " ?attribute rdfs:label ?attributeLabel . "
				+ " }";

		QueryExecution qexecStudy = QueryExecutionFactory.sparqlService(
				Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
		ResultSet resultSet = qexecStudy.execSelect();
		ResultSetRewindable resultsrwStudy = ResultSetFactory.copyResults(resultSet);
		qexecStudy.close();
		Indicator indicator = null;
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			indicator = new Indicator();
			indicator.setUri(soln.getResource("indicator").getURI());
			indicator.setLabel(soln.get("indicatorLabel").toString());
			if(soln.contains("indicatorComment")){
				indicator.setComment(soln.get("indicatorComment").toString());
			}
			indicators.add(indicator);
		}
		java.util.Collections.sort(indicators);
		return indicators; 
	}

	public Map<HADatAcThing, List<HADatAcThing>> getTargetFacets(
			List<String> preValues, FacetHandler facetHandler) {
		
		String query = "";
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += "SELECT ?studyIndicator ?indicatorLabel ?attributeUri ?attributeLabel WHERE { "
				+ "?subTypeUri rdfs:subClassOf* hasco:Study . "
				+ "?studyUri a ?subTypeUri . "
				+ "?dataAcq hasco:isDataAcquisitionOf ?studyUri . "
				+ "?dataAcq hasco:hasSchema ?schemaUri . "
				+ "?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ "?schemaAttribute hasco:hasAttribute ?attributeUri . " 
				+ "?attributeUri rdfs:subClassOf* ?studyIndicator . "
				+ "?attributeUri rdfs:label ?attributeLabel . "
				+ "?studyIndicator rdfs:subClassOf hasco:StudyIndicator . "
				+ "?studyIndicator rdfs:label ?indicatorLabel . "
				+ "}";

		Map<HADatAcThing, List<HADatAcThing>> mapIndicatorToCharList = new HashMap<HADatAcThing, List<HADatAcThing>>();
		try {
			QueryExecution qe = QueryExecutionFactory.sparqlService(
					Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
			ResultSet resultSet = qe.execSelect();
			ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
			qe.close();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				Indicator indicator = new Indicator();
				indicator.setUri(soln.get("studyIndicator").toString());
				indicator.setLabel(soln.get("indicatorLabel").toString());
				AttributeInstance attrib = new AttributeInstance();
				attrib.setUri(soln.get("attributeUri").toString());
				attrib.setLabel(soln.get("attributeLabel").toString());
				if (!mapIndicatorToCharList.containsKey(indicator)) {
					List<HADatAcThing> attributes = new ArrayList<HADatAcThing>();
					mapIndicatorToCharList.put(indicator, attributes);
				}
				if (!mapIndicatorToCharList.get(indicator).contains(attrib)) {
					mapIndicatorToCharList.get(indicator).add(attrib);
				}
			}
		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		return mapIndicatorToCharList;
	}

	public void save() {
		if (uri == null || uri.equals("")) {
			System.out.println("[ERROR] Trying to save Indicator without assigning a URI");
			return;
		}
		System.out.println("Indicator.save(): About to delete");
		delete();  // delete any existing triple for the current study

		String insert = "";
		String ind_uri = "";

		System.out.println("Indicator.save(): Checking URI");
		if (this.getUri().startsWith("<")) {
			ind_uri = this.getUri();
		} else {
			ind_uri = "<" + this.getUri() + ">";
		}
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		if (label != null && !label.equals("")) {
			insert += ind_uri + " rdfs:label \"" + label + "\" .  ";
		}
		if (comment != null && !comment.equals("")) {
			insert += ind_uri + " rdfs:comment \"" + comment + "\" .  ";
		}
		if (superUri != null && !superUri.equals("")) {
			insert += ind_uri + " rdfs:subClassOf <" + DynamicFunctions.replacePrefixWithURL(superUri) + "> .  ";
		}
		insert += LINE_LAST;
		System.out.println("Indicator (pojo's save): <" + insert + ">");
		UpdateRequest request = UpdateFactory.create(insert);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(
				request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		processor.execute();
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
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
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
		row.put("rdfs:subClassOf", "hasco:Indicator");
		row.put("rdfs:label", getLabel());
		row.put("rdfs:comment", getComment());
		rows.add(row);

		int totalChanged = 0;
		try {
			totalChanged = loader.insertRows("IndicatorType", rows);
		} catch (CommandException e) {
			try {
				totalChanged = loader.updateRows("IndicatorType", rows);
			} catch (CommandException e2) {
				System.out.println("[ERROR] Could not insert or update Indicator(s)");
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
			System.out.println("deleting Indicator " + str.get("hasURI"));
		}
		return loader.deleteRows("IndicatorType", rows);
	}

	@Override
	public int compareTo(Indicator another) {
		return this.getLabel().compareTo(another.getLabel());
	}

}
