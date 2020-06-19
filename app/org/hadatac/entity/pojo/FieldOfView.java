package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.NameSpaces;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;

public class FieldOfView extends HADatAcThing implements Comparable<FieldOfView> {

	private String geometry;
	private String isFOVOf;
    private Float firstParameter;
    private String firstParameterUnit;
    private String firstParameterCharacteristic;
    private Float secondParameter;
    private String secondParameterUnit;
    private String secondParameterCharacteristic;
    private Float thirdParameter;
    private String thirdParameterUnit;
    private String thirdParameterCharacteristic;

    public FieldOfView(String uri,
            String typeUri,
            String label,
            String comment) {
        this.uri = uri;
        this.typeUri = typeUri;
        this.label = label;
        this.comment = comment;
    }

    public FieldOfView() {
        this.uri = "";
        this.typeUri = "";
        this.label = "";
        this.comment = "";
        this.geometry = "";
        this.isFOVOf = "";
    }

    public String getGeometry() {
        return geometry;
    }
    public void setGeometry(String geometry) {
        this.geometry = geometry;
    }
    public String getIsFOVOf() {
        return isFOVOf;
    }
    public void setIsFOVOf(String isFOVOf) {
        this.isFOVOf = isFOVOf;
    }
    public Float getFirstParameter() {
        return firstParameter;
    }
    public void setFirstParameter(Float firstParameter) {
        this.firstParameter = firstParameter;
    }
    public String getFirstParameterUnit() {
        return firstParameterUnit;
    }
    public String getFirstParameterUnitLabel() {
        if (firstParameterUnit == null || firstParameterUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(firstParameterUnit);
    }

    public void setFirstParameterUnit(String firstParameterUnit) {
        this.firstParameterUnit = firstParameterUnit;
    }

    public String getFirstParameterCharacteristic() {
        return firstParameterCharacteristic;
    }

    public String getFirstParameterCharacteristicLabel() {
        if (firstParameterCharacteristic == null || firstParameterCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(firstParameterCharacteristic);
    }

    public void setFirstParameterCharacteristic(String firstParameterCharacteristic) {
        this.firstParameterCharacteristic = firstParameterCharacteristic;
    }

    public Float getSecondParameter() {
        return secondParameter;
    }

    public void setSecondParameter(Float secondParameter) {
        this.secondParameter = secondParameter;
    }

    public String getSecondParameterUnit() {
        return secondParameterUnit;
    }

    public String getSecondParameterUnitLabel() {
        if (secondParameterUnit == null || secondParameterUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(secondParameterUnit);
    }

    public void setSecondParameterUnit(String secondParameterUnit) {
        this.secondParameterUnit = secondParameterUnit;
    }

    public String getSecondParameterCharacteristic() {
        return secondParameterCharacteristic;
    }

    public String getSecondParameterCharacteristicLabel() {
        if (secondParameterCharacteristic == null || secondParameterCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(secondParameterCharacteristic);
    }

    public void setSecondParameterCharacteristic(String secondParameterCharacteristic) {
        this.secondParameterCharacteristic = secondParameterCharacteristic;
    }

    public Float getThirdParameter() {
        return thirdParameter;
    }

    public void setThirdParameter(Float thirdParameter) {
        this.thirdParameter = thirdParameter;
    }

    public String getThirdParameterUnit() {
        return thirdParameterUnit;
    }

    public String getThirdParameterUnitLabel() {
        if (thirdParameterUnit == null || thirdParameterUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(thirdParameterUnit);
    }

    public void setThirdParameterUnit(String thirdParameterUnit) {
        this.thirdParameterUnit = thirdParameterUnit;
    }

    public String getThirdParameterCharacteristic() {
        return thirdParameterCharacteristic;
    }

    public String getThirdParameterCharacteristicLabel() {
        if (thirdParameterCharacteristic == null || thirdParameterCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(thirdParameterCharacteristic);
    }

    public void setThirdParameterCharacteristic(String thirdParameterCharacteristic) {
        this.thirdParameterCharacteristic = thirdParameterCharacteristic;
    }

    @Override
    public boolean equals(Object o) {
        if((o instanceof FieldOfView) && (((FieldOfView)o).getUri().equals(this.getUri()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromTripleStore(facet, facetHandler);
    }
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromTripleStore(
            Facet facet, FacetHandler facetHandler) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        return results;
    }

    public static FieldOfView find(String uri) {
        FieldOfView fov = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        fov = new FieldOfView();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                fov.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                fov.setTypeUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                fov.setComment(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasGeometry")) {
                fov.setGeometry(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/isFieldOfViewOf")) {
                fov.setIsFOVOf(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstParameter")) {
                fov.setFirstParameter(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstParameterUnit")) {
                fov.setFirstParameterUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstParameterCharacteristic")) {
                fov.setFirstParameterCharacteristic(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondParameter")) {
                fov.setSecondParameter(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondParameterUnit")) {
                fov.setSecondParameterUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondParameterCharacteristic")) {
                fov.setSecondParameterCharacteristic(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdParameter")) {
                fov.setThirdParameter(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdParameterUnit")) {
            	fov.setThirdParameterUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdParameterCharacteristic")) {
            	fov.setThirdParameterCharacteristic(object.asResource().getURI());
            } 
        }

        fov.setUri(uri);

        return fov;
    }

    public static List<FieldOfView> find() {
        List<FieldOfView> fovs = new ArrayList<FieldOfView>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri a hasco:FieldOfView ." + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            FieldOfView fov = find(soln.getResource("uri").getURI());
            fovs.add(fov);
        }			

        java.util.Collections.sort((List<FieldOfView>) fovs);

        return fovs;
    }

    @Override
    public int compareTo(FieldOfView another) {
        return this.getLabel().compareTo(another.getLabel());
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
