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
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
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
import org.hadatac.metadata.loader.URIUtils;

public class Platform extends HADatAcThing implements Comparable<Platform> {

	public static String LAT = "http://semanticscience.org/resource/Latitude";
	public static String LONG = "http://semanticscience.org/resource/Longitude";
	
	public static String INSERT_LINE1 = "INSERT DATA {  ";
    public static String DELETE_LINE1 = "DELETE WHERE {  ";
    public static String DELETE_LINE3 = " ?p ?o . ";
    public static String LINE_LAST = "}  ";
    public static String PREFIX = "PLT-";

    private String location;
    private Float firstCoordinate;
    private String firstCoordinateUnit;
    private String firstCoordinateCharacteristic;
    private Float secondCoordinate;
    private String secondCoordinateUnit;
    private String secondCoordinateCharacteristic;
    private Float thirdCoordinate;
    private String thirdCoordinateUnit;
    private String thirdCoordinateCharacteristic;
    private String elevation;
    private String partOf;
    private String serialNumber;
    private String image;
    private String layout;
    private String referenceLayout;
    private String url;
    private Float width;
    private String widthUnit;
    private Float depth;
    private String depthUnit;
    private Float height;
    private String heightUnit;

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
        this.location = "";
        this.elevation = "";
        this.partOf = "";
        this.serialNumber = "";
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
    public Float getFirstCoordinate() {
        return firstCoordinate;
    }
    public void setFirstCoordinate(Float firstCoordinate) {
        this.firstCoordinate = firstCoordinate;
    }
    public String getFirstCoordinateUnit() {
        return firstCoordinateUnit;
    }
    public String getFirstCoordinateUnitLabel() {
        if (firstCoordinateUnit == null || firstCoordinateUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(firstCoordinateUnit);
    }

    public void setFirstCoordinateUnit(String firstCoordinateUnit) {
        this.firstCoordinateUnit = firstCoordinateUnit;
    }

    public String getFirstCoordinateCharacteristic() {
        return firstCoordinateCharacteristic;
    }

    public String getFirstCoordinateCharacteristicLabel() {
        if (firstCoordinateCharacteristic == null || firstCoordinateCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(firstCoordinateCharacteristic);
    }

    public void setFirstCoordinateCharacteristic(String firstCoordinateCharacteristic) {
        this.firstCoordinateCharacteristic = firstCoordinateCharacteristic;
    }

    public Float getSecondCoordinate() {
        return secondCoordinate;
    }

    public void setSecondCoordinate(Float secondCoordinate) {
        this.secondCoordinate = secondCoordinate;
    }

    public String getSecondCoordinateUnit() {
        return secondCoordinateUnit;
    }

    public String getSecondCoordinateUnitLabel() {
        if (secondCoordinateUnit == null || secondCoordinateUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(secondCoordinateUnit);
    }

    public void setSecondCoordinateUnit(String secondCoordinateUnit) {
        this.secondCoordinateUnit = secondCoordinateUnit;
    }

    public String getSecondCoordinateCharacteristic() {
        return secondCoordinateCharacteristic;
    }

    public String getSecondCoordinateCharacteristicLabel() {
        if (secondCoordinateCharacteristic == null || secondCoordinateCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(secondCoordinateCharacteristic);
    }

    public void setSecondCoordinateCharacteristic(String secondCoordinateCharacteristic) {
        this.secondCoordinateCharacteristic = secondCoordinateCharacteristic;
    }

    public Float getThirdCoordinate() {
        return thirdCoordinate;
    }

    public void setThirdCoordinate(Float thirdCoordinate) {
        this.thirdCoordinate = thirdCoordinate;
    }

    public String getThirdCoordinateUnit() {
        return thirdCoordinateUnit;
    }

    public String getThirdCoordinateUnitLabel() {
        if (thirdCoordinateUnit == null || thirdCoordinateUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(thirdCoordinateUnit);
    }

    public void setThirdCoordinateUnit(String thirdCoordinateUnit) {
        this.thirdCoordinateUnit = thirdCoordinateUnit;
    }

    public String getThirdCoordinateCharacteristic() {
        return thirdCoordinateCharacteristic;
    }

    public String getThirdCoordinateCharacteristicLabel() {
        if (thirdCoordinateCharacteristic == null || thirdCoordinateCharacteristic.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(thirdCoordinateCharacteristic);
    }

    public void setThirdCoordinateCharacteristic(String thirdCoordinateCharacteristic) {
        this.thirdCoordinateCharacteristic = thirdCoordinateCharacteristic;
    }

    public Float getWidth() {
        return width;
    }
    public void setWidth(Float width) {
        this.width = width;
    }

    public String getWidthUnit() {
        return widthUnit;
    }
    public String getWidthUnitLabel() {
        if (widthUnit == null || widthUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(widthUnit);
    }

    public void setWidthUnit(String widthUnit) {
        this.widthUnit = widthUnit;
    }

    public Float getDepth() {
        return depth;
    }

    public void setDepth(Float depth) {
        this.depth = depth;
    }

    public String getDepthUnit() {
        return depthUnit;
    }

    public String getDepthUnitLabel() {
        if (depthUnit == null || depthUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(depthUnit);
    }

    public void setDepthUnit(String depthUnit) {
        this.depthUnit = depthUnit;
    }

    public Float getHeight() {
        return height;
    }

    public void setHeight(Float height) {
        this.height = height;
    }

    public String getHeightUnit() {
        return heightUnit;
    }

    public String getHeightUnitLabel() {
        if (heightUnit == null || heightUnit.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(heightUnit);
    }

    public void setHeightUnit(String heightUnit) {
        this.heightUnit = heightUnit;
    }

    public String getURL() {
        return url;
    }
    public void setURL(String url) {
        this.url = url;
    }

    public String getSerialNumber() {
        return serialNumber;
    }
    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getPartOf() {
        return partOf;
    }

    public List<Platform> getImmediateSubPlatforms() {
        List<Platform> subPlatforms = new ArrayList<Platform>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?uri hasco:partOf <" + uri + "> . " + 
                "} ";

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            Platform platform = find(soln.getResource("uri").getURI());
            subPlatforms.add(platform);
        }			

        if (subPlatforms.size() > 1) {
        	java.util.Collections.sort((List<Platform>) subPlatforms);
        }
        
        return subPlatforms;
    }
    
    public String getPartOfLabel() {
        if (partOf == null || partOf.isEmpty()) {
        	return "";
        }
        return FirstLabel.getPrettyLabel(partOf);
    }

    public void setPartOf(String partOf) {
        this.partOf = partOf;
    }
    
    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
    
    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }
    
    public String getReferenceLayout() {
        return referenceLayout;
    }

    public void setReferenceLayout(String referenceLayout) {
        this.referenceLayout = referenceLayout;
    }
    
    public String getTypeLabel() {
    	PlatformType pltType = PlatformType.find(getTypeUri());
    	if (pltType == null || pltType.getLabel() == null) {
    		return "";
    	}
    	return pltType.getLabel();
    }

    public boolean hasGeoReference() {
    	return getFirstCoordinate() != null && getSecondCoordinate() != null &&
    		   getFirstCoordinateCharacteristic() != null && getSecondCoordinate() != null &&
    		   getFirstCoordinateCharacteristic().equals(LAT) &&
    		   getSecondCoordinateCharacteristic().equals(LONG);
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

                STR str = new STR();
                str.setUri(soln.get("dataAcquisitionUri").toString());
                str.setLabel(soln.get("dataAcquisitionLabel").toString());
                str.setField("acquisition_uri_str");
                if (!results.containsKey(platform)) {
                    List<Facetable> facets = new ArrayList<Facetable>();
                    results.put(platform, facets);
                }
                if (!results.get(platform).contains(str)) {
                    results.get(platform).add(str);
                }

                Facet subFacet = facet.getChildById(platform.getUri());
                subFacet.putFacet("platform_uri_str", platform.getUri());
                subFacet.putFacet("acquisition_uri_str", str.getUri());
            }
        } catch (QueryExceptionHTTP e) {
            e.printStackTrace();
        }

        return results;
    }

    public static Platform find(String uri) {
 
    	//System.out.println("Platform.find <" + uri + ">");
    	
    	Platform platform = null;
        Statement statement;
        RDFNode object;

        String queryString = "DESCRIBE <" + uri + ">";
        Model model = SPARQLUtils.describe(CollectionUtil.getCollectionPath(
                CollectionUtil.Collection.METADATA_SPARQL), queryString);

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
                platform.setFirstCoordinate(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstCoordinateUnit")) {
                platform.setFirstCoordinateUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasFirstCoordinateCharacteristic")) {
                platform.setFirstCoordinateCharacteristic(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondCoordinate")) {
                platform.setSecondCoordinate(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondCoordinateUnit")) {
                platform.setSecondCoordinateUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasSecondCoordinateCharacteristic")) {
                platform.setSecondCoordinateCharacteristic(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdCoordinate")) {
                platform.setThirdCoordinate(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdCoordinateUnit")) {
            	platform.setThirdCoordinateUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasThirdCoordinateCharacteristic")) {
            	platform.setThirdCoordinateCharacteristic(object.asResource().getURI());
            } else if (statement.getSubject().getURI().equals(uri) && statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/partOf")) {
            	platform.setPartOf(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasImage")) {
                platform.setImage(object.asLiteral().getString());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayout")) {
                platform.setLayout(object.asLiteral().getString());
            } else if (statement.getSubject().getURI().equals(uri) && statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasReferenceLayout")) {
                platform.setReferenceLayout(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutWidth")) {
                platform.setWidth(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutWidthUnit")) {
                platform.setWidthUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutDepth")) {
                platform.setDepth(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutDepthUnit")) {
                platform.setDepthUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutHeight")) {
                platform.setHeight(object.asLiteral().getFloat());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasLayoutHeightUnit")) {
                platform.setHeightUnit(object.asResource().getURI());
            } else if (statement.getPredicate().getURI().equals("http://hadatac.org/ont/hasco/hasURL")) {
                platform.setURL(object.asLiteral().getString());
            }
        }

        platform.setUri(uri);

    	//System.out.println("AFTER Platform.find <" + platform + ">");

        return platform;
    }

    public static int getNumberPlatforms() {
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += " select (count(?uri) as ?tot) where { " + 
                " ?platModel rdfs:subClassOf* vstoi:Platform . " + 
                " ?uri a ?platModel ." + 
                "}";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                return Integer.parseInt(soln.getLiteral("tot").getString());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static List<Platform> findWithPages(int pageSize, int offset) {
        List<Platform> platforms = new ArrayList<Platform>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
        		"SELECT ?uri WHERE { " + 
                " ?platModel rdfs:subClassOf* vstoi:Platform . " + 
                " ?uri a ?platModel . } " + 
                " LIMIT " + pageSize + 
                " OFFSET " + offset;

        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) {
                Platform platform = Platform.find(soln.getResource("uri").getURI());
                platforms.add(platform);
            }
        }
        return platforms;
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

    public static List<Platform> findWithGeoReferenceAndDeployment() {
        List<Platform> platforms = new ArrayList<Platform>();
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
                " SELECT ?uri WHERE { " +
                " ?platModel rdfs:subClassOf* vstoi:Platform . " + 
                " ?uri a ?platModel ." +
                " ?uri hasco:hasFirstCoordinate ?lat . " +
                " ?uri hasco:hasSecondCoordinate ?lon . " +
                " ?uri hasco:hasFirstCoordinateCharacteristic <" + LAT + "> . " +
                " ?uri hasco:hasSecondCoordinateCharacteristic <" + LONG + "> . " +
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

    /*
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
                platform.setFirstCoordinate(soln.getLiteral("lat").getFloat());
            }
            if(soln.getLiteral("lon") != null) {
                platform.setSecondCoordinate(soln.getLiteral("long").getFloat());
            }
            if(soln.getLiteral("ele") != null) {
                platform.setThirdCoordinate(soln.getLiteral("ele").getFloat());
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
    */

    
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

}
