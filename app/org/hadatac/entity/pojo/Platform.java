package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import org.apache.commons.text.WordUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QueryParseException;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;


public class Platform extends HADatAcThing implements Comparable<Platform> {

    public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "PLT-";

    private String location;
    private String firstCoordinate;
    private String secondCoordinate;
    private String thirdCoordinate;
    private String elevation;
    private String serialNumber;

    public Platform(String uri,
            String typeUri,
            String label,
            String comment) {
        this.uri = uri;
        this.typeUri = typeUri;
        this.label = label;
        this.comment = comment;
    }

    public Platform() {
        this.uri = "";
        this.typeUri = "";
        this.label = "";
        this.comment = "";
    }

    public String getLocation() {
        return location;
    }
    public void setLocation(String location) {
        this.location = location;
    }
    public String getElevation() {
        return elevation;
    }
    public void setElevation(String elevation) {
        this.elevation = elevation;
    }
    public String getFirstCoordinate() {
        return firstCoordinate;
    }
    public void setFirstCoordinate(String firstCoordinate) {
        this.firstCoordinate = firstCoordinate;
    }

    public String getSecondCoordinate() {
        return secondCoordinate;
    }

    public void setSecondCoordinate(String secondCoordinate) {
        this.secondCoordinate = secondCoordinate;
    }

    public String getThirdCoordinate() {
        return thirdCoordinate;
    }

    public void setThirdCoordinate(String thirdCoordinate) {
        this.thirdCoordinate = thirdCoordinate;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    @Override
    public boolean equals(Object o) {
        if((o instanceof Platform) && (((Platform)o).getUri().equals(this.getUri()))) {
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
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?platformUri ?dataAcquisitionUri ?platformLabel ?dataAcquisitionLabel WHERE { \n"
                + " ?dataAcquisitionUri hasco:hasDeployment ?deploymentUri . \n"
                + " ?deploymentUri vstoi:hasPlatform ?platformUri . \n"
                + " ?platformUri rdfs:label ?platformLabel . \n"
                + " ?dataAcquisitionUri rdfs:label ?dataAcquisitionLabel . \n"
                + " } \n";

        // System.out.println("Platform getTargetFacets() query: " + query);

        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
            
            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                Platform platform = new Platform();
                String platformUri = soln.get("platformUri").toString();
                String platformLabel = soln.get("platformLabel").toString();
                platform.setUri(platformUri);
                platform.setQuery(query);
                platform.setField("platform_uri_str");
                if (platformLabel.isEmpty()) {
                    platform.setLabel(WordUtils.capitalize(URIUtils.getBaseName(platformUri)));
                } else {
                    platform.setLabel(platformLabel);
                }

                ObjectAccessSpec oas = new ObjectAccessSpec();
                oas.setUri(soln.get("dataAcquisitionUri").toString());
                oas.setLabel(soln.get("dataAcquisitionLabel").toString());
                oas.setField("acquisition_uri_str");
                if (!results.containsKey(platform)) {
                    List<Facetable> facets = new ArrayList<Facetable>();
                    results.put(platform, facets);
                }
                if (!results.get(platform).contains(oas)) {
                    results.get(platform).add(oas);
                }

                Facet subFacet = facet.getChildById(platform.getUri());
                subFacet.putFacet("platform_uri_str", platform.getUri());
                subFacet.putFacet("acquisition_uri_str", oas.getUri());
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return results;
    }

    public static Platform find(String uri) {
        Platform platform = null;
        Model model;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);
        model = qexec.execDescribe();

        platform = new Platform();
        StmtIterator stmtIterator = model.listStatements();

        while (stmtIterator.hasNext()) {
            statement = stmtIterator.next();
            object = statement.getObject();
            if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#label")) {
                platform.setLabel(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")) {
                platform.setTypeUri(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://www.w3.org/2000/01/rdf-schema#comment")) {
                platform.setComment(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/vstoi#hasSerialNumber")) {
                platform.setSerialNumber(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstCoordinate")) {
                platform.setFirstCoordinate(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondCoordinate")) {
                platform.setSecondCoordinate(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdCoordinate")) {
                platform.setThirdCoordinate(object.asLiteral().getString());
            }
        }

        platform.setUri(uri);

        return platform;
    }

    public static List<Platform> find() {
        List<Platform> platforms = new ArrayList<Platform>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?platModel rdfs:subClassOf* vstoi:Platform . " + 
                " ?uri a ?platModel ." + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Platform platform = find(soln.getResource("uri").getURI());
            platforms.add(platform);
        }			

        java.util.Collections.sort((List<Platform>) platforms);

        return platforms;
    }

    public static Platform find(HADataC hadatac) {
        Platform platform = null;

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + "SELECT ?platform ?label ?lat ?lon ?ele WHERE {\n"
                + "  <" + hadatac.getDeploymentUri() + "> vstoi:hasPlatform ?platform .\n"
                + "  OPTIONAL { ?platform rdfs:label ?label . }\n"
                + "  OPTIONAL { ?platform rdfs:comment ?comment . }\n"
                + "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasFirstCoordinate> ?lat . }\n"
                + "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasSecondCoordinate> ?lon . }\n"
                + "  OPTIONAL { ?platform <http://hadatac.org/ont/hasco/hasThirdCoordinate> ?ele . }\n"
                + "}";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(hadatac.getStaticMetadataSparqlURL(), queryString);

        if (resultsrw.size() >= 1) {
            QuerySolution soln = resultsrw.next();
            platform = new Platform();
            platform.setUri(soln.getResource("platform").getURI());
            if (soln.getLiteral("label") != null) {
                platform.setLabel(soln.getLiteral("label").getString());
            }
            if(soln.getLiteral("comment") != null) {
                platform.setComment(soln.getLiteral("comment").getString());
            }
            if(soln.getLiteral("lat") != null) {
                platform.setFirstCoordinate(soln.getLiteral("lat").getString());
            }
            if(soln.getLiteral("lon") != null) {
                platform.setSecondCoordinate(soln.getLiteral("long").getString());
            }
            if(soln.getLiteral("ele") != null) {
                platform.setThirdCoordinate(soln.getLiteral("ele").getString());
                platform.setLocation("(" + platform.getFirstCoordinate() + ", " 
                        + platform.getSecondCoordinate() + ", "
                        + platform.getThirdCoordinate() + ")");
            }
            if (soln.getLiteral("ele") != null) {
                platform.setElevation(soln.getLiteral("ele").getString());
            }
        }

        return platform;
    }

    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", URIUtils.replaceNameSpaceEx(typeUri));
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getComment());
        rows.add(row);

        int totalChanged = 0;
        try {
            totalChanged = loader.insertRows("Platform", rows);
        } catch (CommandException e) {
            try {
                totalChanged = loader.updateRows("Platform", rows);
            } catch (CommandException e2) {
                System.out.println("[ERROR] Could not insert or update Platform(ies)");
            }
        }
        return totalChanged;
    }

    @Override
    public int compareTo(Platform another) {
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

    @Override
    public int deleteFromLabKey(String userName, String password) {
        return 0;
    }
}
