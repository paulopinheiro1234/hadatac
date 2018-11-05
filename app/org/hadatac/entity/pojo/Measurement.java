package org.hadatac.entity.pojo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.controllers.dataacquisitionsearch.FacetTree;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;


public class Measurement extends HADatAcThing implements Runnable {
    @Field("uri")
    private String uri;
    @Field("owner_uri_str")
    private String ownerUri;
    @Field("acquisition_uri_str")
    private String acquisitionUri;
    @Field("study_uri_str")
    private String studyUri;
    @Field("object_collection_type_str")
    private String objectCollectionType;
    @Field("object_uri_str")
    private String objectUri;
    @Field("study_object_uri_str")
    private String studyObjectUri;
    @Field("timestamp_date")
    private Date timestamp;
    @Field("named_time_str")
    private String abstractTime;
    @Field("time_value_double")
    private String timeValue;
    @Field("time_value_unit_uri_str")
    private String timeValueUnitUri;
    @Field("value_str")
    private String value;
    @Field("original_value_str")
    private String originalValue;
    @Field("lod_str")
    private String levelOfDetection;
    @Field("pid_str")
    private String pid;
    @Field("sid_str")
    private String sid;
    @Field("role_str")
    private String role;
    @Field("unit_uri_str")
    private String unitUri;
    @Field("daso_uri_str")
    private String dasoUri;
    @Field("dasa_uri_str")
    private String dasaUri;
    @Field("in_relation_to_uri_str")
    private String inRelationToUri;
    @Field("entity_uri_str")
    private String entityUri;
    //@Field("characteristic_uri_str")
    //private String characteristicUri;
    @Field("characteristic_uri_str_multi")
    private List<String> characteristicUris;
    @Field("location_latlong")
    private String location;
    @Field("elevation_double")
    private double elevation;
    @Field("dataset_uri_str")
    private String datasetUri;

    // Variables that are not stored in Solr
    private String entity;
    private String characteristic;
    private String unit;
    private String platformName;
    private String platformUri;
    private String instrumentModel;
    private String instrumentUri;
    private String strTimestamp;

    public Measurement() {
    	characteristicUris = new ArrayList<String>();
    }

    public String getOwnerUri() {
        return ownerUri;
    }

    public void setOwnerUri(String ownerUri) {
        this.ownerUri = ownerUri;
    }

    public String getAcquisitionUri() {
        return acquisitionUri;
    }

    public void setAcquisitionUri(String acquisitionUri) {
        this.acquisitionUri = acquisitionUri;
    }

    public String getStudyUri() {
        return studyUri;
    }

    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    public String getObjectCollectionType() {
        return objectCollectionType;
    }

    public void setObjectCollectionType(String objectCollectionType) {
        this.objectCollectionType = objectCollectionType;
    }

    public String getObjectUri() {
        return objectUri;
    }

    public void setObjectUri(String objectUri) {
        this.objectUri = objectUri;
    }

    public String getStudyObjectUri() {
        return studyObjectUri;
    }

    public void setStudyObjectUri(String studyObjectUri) {
        this.studyObjectUri = studyObjectUri;
    }

    public void setPID(String objectUri) {
        this.pid = objectUri;
    }

    public void setSID(String objectUri) {
        this.sid = objectUri;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getObjectPID() {
        return this.pid;
    }

    public String getObjectSID() {
        return this.sid;
    }

    public String getInstrumentModel() {
        return instrumentModel;
    }

    public void setInstrumentModel(String instrumentModel) {
        this.instrumentModel = instrumentModel;
    }

    public String getInstrumentUri() {
        return instrumentUri;
    }

    public void setInstrumentUri(String instrumentUri) {
        this.instrumentUri = instrumentUri;
    }

    public String getPlatformName() {
        return platformName;
    }

    public void setPlatformName(String platformName) {
        this.platformName = platformName;
    }

    public String getPlatformUri() {
        return platformUri;
    }

    public void setPlatformUri(String platformUri) {
        this.platformUri = platformUri;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        if (timestamp != null) {
            this.strTimestamp = timestamp.toString();
        }
    }

    public String getTimestampString() {
        return strTimestamp;
    }

    public void setTimestampString(String strTimestamp) {
        this.strTimestamp = strTimestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = Date.from(timestamp);
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = Date.from(Instant.parse(timestamp));
    }

    public String getAbstractTime() {
        return abstractTime;
    }

    public void setAbstractTime(String abstractTime) {
        this.abstractTime = abstractTime;
    }

    public String getTimeValue() {
        return this.timeValue;
    }

    public void setTimeValue(String timeValue) {
        this.timeValue = timeValue;
    }

    public String getTimeValueUnitUri() {
        return platformUri;
    }

    public void setTimeValueUnitUri(String uri) {
        this.timeValueUnitUri = uri;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getOriginalValue() {
        return originalValue;
    }

    public void setOriginalValue(String originalValue) {
        this.originalValue = originalValue;
    }
    
    public String getLevelOfDetection() {
        return levelOfDetection;
    }

    public void setLevelOfDetection(String levelOfDetection) {
        this.levelOfDetection = levelOfDetection;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getUnitUri() {
        return unitUri;
    }

    public void setUnitUri(String unitUri) {
        this.unitUri = unitUri;
    }

    public String getDasoUri() {
        return dasoUri;
    }

    public void setDasoUri(String dasoUri) {
        this.dasoUri = dasoUri;
    }

    public String getDasaUri() {
        return dasaUri;
    }

    public void setDasaUri(String dasaUri) {
        this.dasaUri = dasaUri;
    }

    public String getInRelationToUri() {
        return inRelationToUri;
    }

    public void setInRelationToUri(String inRelationToUri) {
        this.inRelationToUri = inRelationToUri;
    }

    public String getEntity() {
        return entity;
    }

    public void setEntity(String entity) {
        this.entity = entity;
    }

    public String getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(String entityUri) {
        this.entityUri = entityUri;
    }

    public String getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(String characteristic) {
        this.characteristic = characteristic;
    }

    /*public String getCharacteristicUri() {
        return characteristicUri;
    }

    public void setCharacteristicUri(String characteristicUri) {
        this.characteristicUri = characteristicUri;
    }*/

    public List<String> getCharacteristicUris() {
        return characteristicUris;
    }

    public void addCharacteristicUris(String characteristicUri) {
        this.characteristicUris.add(characteristicUri);
    }
    
    public void setCharacteristicUris(List<String> characteristicUris) {
        this.characteristicUris = characteristicUris;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public double getElevation() {
        return elevation;
    }

    public void setElevation(double elevation) {
        this.elevation = elevation;
    }

    public String getDatasetUri() {
        return datasetUri;
    }

    public void setDatasetUri(String datasetUri) {
        this.datasetUri = datasetUri;
    }

    @Override
    public boolean saveToSolr() {
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        try {
            solr.addBean(this).getStatus();
            solr.commit();
            solr.close();

            return true;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] Measurement.save - e.Message: " + e.getMessage());
            return false;
        }
    }

    public static int delete(String datasetUri) {
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        try {
            UpdateResponse response = solr.deleteByQuery("dataset_uri_str:\"" + datasetUri + "\"");
            solr.commit();
            solr.close();

            List<ObjectAccessSpec> dataAcquisitions = ObjectAccessSpec.findAll();
            for (ObjectAccessSpec da : dataAcquisitions) {
                if (da.containsDataset(datasetUri)) {
                    da.setNumberDataPoints(Measurement.getNumByDataAcquisition(da));
                    da.saveToSolr();
                }
            }
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] Measurement.delete - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] Measurement.delete - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.delete - Exception message: " + e.getMessage());
        }

        return -1;
    }

    public static String buildQuery(String user_uri, String study_uri, String subject_uri, String char_uri) {
        String acquisition_query = "";
        String facet_query = "";
        String q = "";

        List<String> listURI = ObjectAccessSpec.findAllAccessibleDataAcquisition(user_uri);
        Iterator<String> iter_uri = listURI.iterator();
        while (iter_uri.hasNext()) {
            String uri = iter_uri.next();
            acquisition_query += "acquisition_uri_str" + ":\"" + uri + "\"";
            if (iter_uri.hasNext()) {
                acquisition_query += " OR ";
            }
        }

        if (acquisition_query.equals("")) {
            return "";
        }

        if (!study_uri.equals("")) {
            facet_query += "study_uri_str" + ":\"" + study_uri + "\"";
        }
        if (!subject_uri.equals("")) {
            if (!study_uri.equals("")) {
                facet_query += " AND ";
            }
            facet_query += "object_uri_str" + ":\"" + subject_uri + "\"";
        }

        if (!char_uri.equals("")) {
            if (!study_uri.equals("") || !subject_uri.equals("")) {
                facet_query += " AND ";
            }
            facet_query += "characteristic_uri_str_multi" + ":\"" + char_uri + "\"";
        }

        if (facet_query.trim().equals("")) {
            q = acquisition_query;
        } else {
            q = "(" + acquisition_query + ") AND (" + facet_query + ")";
        }

        return q;
    }

    public static String buildQuery(List<String> ownedDataAcquisitions, FacetHandler handler) {
        String acquisition_query = String.join(" OR ", ownedDataAcquisitions.stream()
                .map(p -> "acquisition_uri_str:\"" + p + "\"")
                .collect(Collectors.toList()));

        if (acquisition_query.equals("")) {
            return "";
        }

        String facet_query = "";
        String q = "";
        if (handler != null) {
            facet_query = handler.bottommostFacetsToSolrQuery();
        }

        if (facet_query.trim().equals("") || facet_query.trim().equals("*:*")) {
            q = acquisition_query;
        } else {
            q = "(" + acquisition_query + ") AND (" + facet_query + ")";
        }

        return q;
    }

    public static AcquisitionQueryResult findForViews(String user_uri, String study_uri, 
            String subject_uri, String char_uri, boolean bNumberOfResultsOnly) {
        AcquisitionQueryResult result = new AcquisitionQueryResult();

        String q = buildQuery(user_uri, study_uri, subject_uri, char_uri);
        /*
         * an empty query happens when current user is not allowed to see any
         * data acquisition
         */
        if (q.equals("")) {
            return result;
        }

        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        if (bNumberOfResultsOnly) {
            query.setRows(0);
        }
        else {
            query.setRows(10000000);
        }
        query.setFacet(false);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList results = queryResponse.getResults();
            if (bNumberOfResultsOnly) {
                result.setDocumentSize(results.getNumFound());
            } else {
                Iterator<SolrDocument> m = results.iterator();
                while (m.hasNext()) {
                    result.documents.add(convertFromSolr(m.next(), null, new HashMap<>()));
                }
            }
        } catch (SolrServerException e) {
            System.out.println("[ERROR] Measurement.findForViews() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] Measurement.findForViews() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.findForViews() - Exception message: " + e.getMessage());
        }

        return result;
    }

    public static AcquisitionQueryResult find(String user_uri, int page, int qtd, String facets) {
        AcquisitionQueryResult result = new AcquisitionQueryResult();

        List<String> ownedDAs = ObjectAccessSpec.findAllAccessibleDataAcquisition(user_uri);
        if (ownedDAs.isEmpty()) {
            /*
             * an empty query happens when current user is not allowed to see any
             * data acquisition
             */
            System.out.println("Not allowed to access any Data Acquisition!");
            return result;
        }

        System.out.println("facets for measurement's find(); " + facets);
        
        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacets(facets);

        FacetHandler retFacetHandler = new FacetHandler();
        retFacetHandler.loadFacets(facets);

        //System.out.println("\nfacetHandler before: " + facetHandler.toSolrQuery());
        //System.out.println("\nfacetHandler before: " + facetHandler.toJSON());

        // Run one time
        // getAllFacetStats(facetHandler, retFacetHandler, result, false);

        // Get facet statistics
        // getAllFacetStats(retFacetHandler, retFacetHandler, result, true);
        getAllFacetStats(facetHandler, retFacetHandler, result, true);

        //System.out.println("\n\n\nfacetHandler after: " + retFacetHandler.bottommostFacetsToSolrQuery());
        //System.out.println("\n\n\nfacetHandler after: " + retFacetHandler.toJSON());

        // Get documents
        long docSize = 0;

        //String q = buildQuery(ownedDAs, retFacetHandler);
        String q = buildQuery(ownedDAs, facetHandler);

        //System.out.println("measurement solr query: " + q);
        
        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        if (page != -1) {
            query.setStart((page - 1) * qtd + 1);
            query.setRows(qtd);
        } else {
            query.setRows(99999999);
        }
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet", "{ "
                + "object_uri_str: { "
                + "type: terms, "
                + "field: object_uri_str, "
                + "limit: 100000000 }, "
                + "characteristic_uri_str_multi: { "
                + "type: terms, "
                + "field: characteristic_uri_str_multi, "
                + "limit: 10000 } }");
        //query.set("group.field", "study_uri_str");
        //query.set("group", "true");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList docs = queryResponse.getResults();
            docSize = docs.getNumFound();
            System.out.println("Num of results: " + docSize);

            //System.out.println("\n\n\nqueryResponse: " + queryResponse);
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            
        	//System.out.println("PRINTING PIVOT");
            //for (Pivot p : pivot.children) {
            //	System.out.println("Field: " + p.getField() + "   Value: " + p.getValue());
            //}
            
            result.extra_facets.put(FacetHandler.SUBJECT_CHARACTERISTIC_FACET, pivot);

            Set<String> uri_set = new HashSet<String>();
            Iterator<SolrDocument> iterDoc = docs.iterator();
            Map<String, ObjectAccessSpec> cachedDA = new HashMap<String, ObjectAccessSpec>();
            Map<String, String> mapClassLabel = generateCodeClassLabel();
            while (iterDoc.hasNext()) {
                Measurement measurement = convertFromSolr(iterDoc.next(), cachedDA, mapClassLabel);
                result.addDocument(measurement);
                uri_set.add(measurement.getEntityUri());
                uri_set.addAll(measurement.getCharacteristicUris());
                uri_set.add(measurement.getUnitUri());
            }

            // Assign labels of entity, characteristic, and units collectively
            Map<String, String> cachedLabels = Measurement.generateCachedLabel(new ArrayList<String>(uri_set));
            for (Measurement measurement : result.getDocuments()) {
                measurement.setLabels(cachedLabels);
            }
        } catch (SolrServerException e) {
            System.out.println("[ERROR] Measurement.find() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] Measurement.find() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.find() - Exception message: " + e.getMessage());
            e.printStackTrace();
        }

        result.setDocumentSize(docSize);

        return result;
    }

    private static void getAllFacetStats(
            FacetHandler facetHandler, 
            FacetHandler retFacetHandler,
            AcquisitionQueryResult result,
            boolean bAddToResults) {
        FacetTree fTreeS = new FacetTree();
        fTreeS.setTargetFacet(ObjectAccessSpec.class);
        fTreeS.addUpperFacet(Study.class);
        Pivot pivotS = getFacetStats(fTreeS, 
                retFacetHandler.getFacetByName(FacetHandler.STUDY_FACET), 
                facetHandler);

        FacetTree fTreeOC = new FacetTree();
        fTreeOC.setTargetFacet(StudyObjectRole.class);
        fTreeOC.addUpperFacet(ObjectCollection.class);
        // fTreeOC.addUpperFacet(StudyObjectType.class);
        Pivot pivotOC = getFacetStats(fTreeOC, 
                retFacetHandler.getFacetByName(FacetHandler.OBJECT_COLLECTION_FACET), 
                facetHandler);

        FacetTree fTreeEC = new FacetTree();
        fTreeEC.setTargetFacet(AttributeInstance.class);
        fTreeEC.addUpperFacet(Indicator.class);
        fTreeEC.addUpperFacet(EntityRole.class);
        //fTreeEC.addUpperFacet(StudyObjectRole.class);
        fTreeEC.addUpperFacet(EntityInstance.class);
        fTreeEC.addUpperFacet(InRelationToInstance.class);
        Pivot pivotEC = getFacetStats(fTreeEC, 
                retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET), 
                facetHandler);

        FacetTree fTreeU = new FacetTree();
        fTreeU.setTargetFacet(UnitInstance.class);
        Pivot pivotU = getFacetStats(fTreeU, 
                retFacetHandler.getFacetByName(FacetHandler.UNIT_FACET),
                facetHandler);

        FacetTree fTreeT = new FacetTree();
        fTreeT.setTargetFacet(TimeInstance.class);
        fTreeT.addUpperFacet(DASEType.class);
        Pivot pivotT = getFacetStats(fTreeT, 
                retFacetHandler.getFacetByName(FacetHandler.TIME_FACET),
                facetHandler);

        FacetTree fTreePI = new FacetTree();
        fTreePI.setTargetFacet(ObjectAccessSpec.class);
        fTreePI.addUpperFacet(Platform.class);
        fTreePI.addUpperFacet(Instrument.class);
        Pivot pivotPI = getFacetStats(fTreePI, 
                retFacetHandler.getFacetByName(FacetHandler.PLATFORM_INSTRUMENT_FACET),
                facetHandler);

        if (bAddToResults) {
            result.extra_facets.put(FacetHandler.STUDY_FACET, pivotS);
            result.extra_facets.put(FacetHandler.OBJECT_COLLECTION_FACET, pivotOC);
            result.extra_facets.put(FacetHandler.ENTITY_CHARACTERISTIC_FACET, pivotEC);
            result.extra_facets.put(FacetHandler.UNIT_FACET, pivotU);
            result.extra_facets.put(FacetHandler.TIME_FACET, pivotT);
            result.extra_facets.put(FacetHandler.PLATFORM_INSTRUMENT_FACET, pivotPI);
        }
    }

    private static Pivot getFacetStats(
            FacetTree fTree, 
            Facet facet,
            FacetHandler facetHandler) {
        Pivot pivot = new Pivot();
        fTree.retrieveFacetData(0, facet, facetHandler, pivot);
        pivot.recomputeStats();

        return pivot;
    }

    public static long getNumByDataAcquisition(ObjectAccessSpec dataAcquisition) {
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        SolrQuery query = new SolrQuery();
        query.set("q", "acquisition_uri_str:\"" + dataAcquisition.getUri() + "\"");
        query.set("rows", "10000000");

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            // Update the data set URI list
            dataAcquisition.deleteAllDatasetURIs();
            Iterator<SolrDocument> iter = results.iterator();
            while (iter.hasNext()) {
                SolrDocument doc = iter.next();
                dataAcquisition.addDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
            }
            return results.getNumFound();
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
                    + e.getMessage());
        }

        return 0;
    }

    public static List<Measurement> findByDataAcquisitionUri(String acquisition_uri) {
        List<Measurement> listMeasurement = new ArrayList<Measurement>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        SolrQuery query = new SolrQuery();
        query.set("q", "acquisition_uri_str:\"" + acquisition_uri + "\"");
        query.set("rows", "10000000");

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            Iterator<SolrDocument> i = results.iterator();
            while (i.hasNext()) {
                Measurement measurement = convertFromSolr(i.next(), null, new HashMap<>());
                listMeasurement.add(measurement);
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
                    + e.getMessage());
        }

        return listMeasurement;
    }

    public static Map<String, String> generateCachedLabel(List<String> uris) {
        Map<String, String> results = new HashMap<String, String>();

        List<String> validURIs = new ArrayList<String>();
        // Set default label as local name
        for (String uri : uris) {
            if (URIUtils.isValidURI(uri)) {
                results.put(uri, URIUtils.getBaseName(uri));
                validURIs.add(uri);
            } else {
                results.put(uri, uri);
            }
        }

        String valueConstraint = "";
        if (uris.isEmpty()) {
            return results;
        } else {
            valueConstraint = " VALUES ?uri { " + HADatAcThing.stringify(validURIs, true) + " } ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?uri ?label WHERE { \n"
                + valueConstraint + " \n"
                + " ?uri rdfs:label ?label . \n"
                + "}";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if (soln.get("label") != null && !soln.get("label").toString().isEmpty()) {
                    results.put(soln.get("uri").toString(), soln.get("label").toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public static Map<String, String> generateCodeClassLabel() {
        Map<String, String> results = new HashMap<String, String>();

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?possibleValue ?class ?codeLabel ?label WHERE { \n"
                + "?possibleValue a hasco:PossibleValue . \n"
                + "?possibleValue hasco:hasClass ?class . \n"
                + "OPTIONAL { ?possibleValue hasco:hasCodeLabel ?codeLabel } . \n"
                + "OPTIONAL { ?class rdfs:label ?label } . \n"
                + "}";

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), query);

            while (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if (soln.get("label") != null && !soln.get("label").toString().isEmpty()) {
                    results.put(soln.get("class").toString(), soln.get("label").toString());
                } else if (soln.get("codeLabel") != null && !soln.get("codeLabel").toString().isEmpty()) {
                    results.put(soln.get("class").toString(), soln.get("codeLabel").toString());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return results;
    }

    public void setLabels(Map<String, String> cache) {
        if (cache.containsKey(getEntityUri())) {
            setEntity(cache.get(getEntityUri()));
        }
        
        List<String> attributes = new ArrayList<String>();
        for (String attributeUri : getCharacteristicUris()) {
            if (cache.containsKey(attributeUri)) {
                attributes.add(cache.get(attributeUri));
            } else {
                attributes.add(attributeUri);
            }
        }
        if (attributes.size() > 0) {
            setCharacteristic(String.join("; ", attributes));
        }
        
        if (cache.containsKey(getUnitUri())) {
            setUnit(cache.get(getUnitUri()));
        }
    }

    public static Measurement convertFromSolr(SolrDocument doc, 
            Map<String, ObjectAccessSpec> cachedDA, Map<String, String> cachedURILabels) {
        Measurement m = new Measurement();
        m.setUri(SolrUtils.getFieldValue(doc, "uri"));
        m.setOwnerUri(SolrUtils.getFieldValue(doc, "owner_uri_str"));
        m.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
        m.setAcquisitionUri(SolrUtils.getFieldValue(doc, "acquisition_uri_str"));
        m.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str"));
        m.setDasoUri(SolrUtils.getFieldValue(doc, "daso_uri_str"));
        m.setDasaUri(SolrUtils.getFieldValue(doc, "dasa_uri_str"));
        m.setObjectUri(SolrUtils.getFieldValue(doc, "object_uri_str"));
        m.setRole(SolrUtils.getFieldValue(doc, "role_str"));
        m.setInRelationToUri(SolrUtils.getFieldValue(doc, "in_relation_to_uri_str"));
        m.setPID(SolrUtils.getFieldValue(doc, "pid_str"));
        m.setSID(SolrUtils.getFieldValue(doc, "sid_str"));
        m.setAbstractTime(SolrUtils.getFieldValue(doc, "named_time_str"));
        m.setTimeValue(SolrUtils.getFieldValue(doc, "time_value_double"));
        m.setTimeValueUnitUri(SolrUtils.getFieldValue(doc, "time_value_unit_uri_str"));
        m.setOriginalValue(SolrUtils.getFieldValue(doc, "original_value_str"));
        m.setEntityUri(SolrUtils.getFieldValue(doc, "entity_uri_str"));
        //m.setCharacteristicUri(SolrUtils.getFieldValue(doc, "characteristic_uri_str"));
        m.setCharacteristicUris(SolrUtils.getFieldValues(doc, "characteristic_uri_str_multi"));
        m.setUnitUri(SolrUtils.getFieldValue(doc, "unit_uri_str"));

        String value = SolrUtils.getFieldValue(doc, "value_str");
        if (cachedURILabels.containsKey(value)) {
            m.setValue(cachedURILabels.get(value));
        } else {
            m.setValue(value);
        }

        ObjectAccessSpec da = null;
        if (cachedDA == null) {
            da = ObjectAccessSpec.findByUri(m.getAcquisitionUri());
        } else {
            // Use cached DA
            if (cachedDA.containsKey(m.getAcquisitionUri())) {
                da = cachedDA.get(m.getAcquisitionUri());
            } else {
                da = ObjectAccessSpec.findByUri(m.getAcquisitionUri());
                cachedDA.put(m.getAcquisitionUri(), da);
            }
        }
        if (da != null) {
            m.setPlatformUri(da.getPlatformUri());
            m.setPlatformName(da.getPlatformName());
            m.setInstrumentUri(da.getInstrumentUri());
            m.setInstrumentModel(da.getInstrumentModel());
            m.setLocation(da.getLocation());
            //m.setElevation(Double.parseDouble(da.getElevation()));
        }

        if (doc.getFieldValue("timestamp_date") != null) {
            if (((Date)doc.getFieldValue("timestamp_date")).equals(new Date(0))) {
                m.setTimestamp((Date)null);
            } else {
                m.setTimestamp((Date)doc.getFieldValue("timestamp_date"));
            }
        }

        return m;
    }

    public static List<String> getFieldNames() {
        List<String> results = new ArrayList<String>();
        java.lang.reflect.Field[] fields = Measurement.class.getDeclaredFields();
        for (java.lang.reflect.Field field : fields) {
            // Not include dates
            if (field.getType() != Date.class) {
                results.add(field.getName());
            }
        }

        return results;
    }

    public static void outputAsCSV(List<Measurement> measurements, 
            List<String> fieldNames, File file) {
        try {
            // Create headers
            FileUtils.writeStringToFile(file, String.join(",", fieldNames) + "\n", "utf-8", true);

            // Create rows
            int i = 1;
            int total = measurements.size();
            DataFile dataFile = null;
            for (Measurement m : measurements) {
                if (file.exists()) {
                    FileUtils.writeStringToFile(file, m.toCSVRow(fieldNames) + "\n", "utf-8", true);
                }
                int prev_ratio = 0;
                double ratio = (double)i / total * 100;
                if (((int)ratio) != prev_ratio) {
                    prev_ratio = (int)ratio;

                    dataFile = DataFile.findByName(file.getName());
                    if (dataFile != null) {
                        if (dataFile.getStatus() == DataFile.DELETED) {
                            dataFile.delete();
                            return;
                        }
                        dataFile.setCompletionPercentage((int)ratio);
                        dataFile.save();
                    } else {
                        return;
                    }
                }
                i++;
            }

            dataFile = DataFile.findByName(file.getName());
            if (dataFile != null) {
                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputAsCSVByAlignment(List<Measurement> measurements, File file) {        
        try {
            // Write empty string to create the file
            FileUtils.writeStringToFile(file, "", "utf-8", true);
            
            // Initiate Alignment and Results
            Alignment alignment = new Alignment();
            Map<String, Map<String, String>> results = new HashMap<String, Map<String, String>>();

            // Prepare rows: Measurements are compared already collected alignment attributes (role/entity/attribute/inrelationto/unit/time)
            //               New alignment attributes are created for measurements with no corresponging alignment attributes.
            int i = 1;
            int prev_ratio = 0;
            int total = measurements.size();
            DataFile dataFile = null;
            for (Measurement m : measurements) {
                StudyObject obj = null;
                String replacementUri = null;
                StudyObject replacementObj = null;
                if (m.getObjectUri() != null && !m.getObjectUri().equals("")) {
                    if (!results.containsKey(m.getObjectUri())) {
                        obj = StudyObject.find(m.getObjectUri());
                        if (obj == null) {
                            System.out.println("[ERROR] could not find object with uri " + m.getObjectUri());
                        } else {
                            Entity objEntity = Entity.find(obj.getTypeUri());
                            List<String> scope = StudyObject.retrieveScopeUris(obj.getUri());
                            if (scope != null && scope.size() > 0) {
                                //for (String str : scope) {
                                //    System.out.println("Scope for " + obj.getUri() + " is [" + str + "]");
                                //}
                                replacementUri = scope.get(0);
                                if (alignment.containsObject(replacementUri)) {
                                    replacementObj = alignment.getObject(replacementUri);
                                } else {
                                    replacementObj = StudyObject.find(replacementUri);
                                }
                            }
                            AlignmentEntityRole entRole = new AlignmentEntityRole(objEntity,"");
                            if (!alignment.containsRole(entRole.getKey())) {
                                alignment.addRole(entRole);
                            }
                            if (replacementObj != null && replacementObj.getUri() != null && !replacementObj.getUri().equals("")) {
                                //System.out.println("Replacing " + obj.getUri() + " [" + entRole + "] with " + replacementObj.getUri());
                                if (!alignment.containsReplacementUri(obj.getUri())) {
                                    alignment.addReplacementUri(obj.getUri(),replacementObj.getUri());
                                }
                                obj = replacementObj;
                            }
                            if (!alignment.containsObject(obj.getUri())) {
                                alignment.addObject(obj);
                                results.put(obj.getUri(), new HashMap<String, String>());
                                results.get(m.getObjectUri()).put(alignment.objectKey(entRole), m.getObjectPID());
                            }
                            if (replacementUri != null) {
                                //System.out.println("SID: " + m.getObjectSID());
                                //results.get(m.getObjectUri()).put(alignment.objectKey(entRole), m.getObjectSID());
                            }
                            //System.out.println("Processing Object " + totObj++ + " with PID " + m.getObjectPID());
                        }
                    }

                    String key = alignment.measurementKey(m);
                    if (key != null) {
			String finalValue = "";
			/*if (alignment.containsCode(m.getCharacteristicUri())) {
			    finalValue = alignment.getCode(m.getCharacteristicUri()); */
			if (alignment.containsCode(m.getCharacteristicUris().get(0))) {
				finalValue = alignment.getCode(m.getCharacteristicUris().get(0));
			} else {
			    finalValue = m.getValue();
			}
                        results.get(alignment.replaceUri(m.getObjectUri())).put(key, finalValue);
                    } else {
                        System.out.println("[ERROR] the following measurement could not match any alignment attribute (and no alignment " + 
                                "attribute could be created for this measurement): " + 
                                //m.getEntityUri() + " " + m.getCharacteristicUri());
                                m.getEntityUri() + " " + m.getCharacteristicUris().get(0));
                    }
                }

                double ratio = (double)i / total * 100;
                int current_ratio = (int)ratio;
                if (current_ratio > prev_ratio) {
                    prev_ratio = current_ratio;
                    System.out.println("Progress: " + current_ratio + "%");
                    
                    dataFile = DataFile.findByName(file.getName());
                    if (dataFile != null) {
                        if (dataFile.getStatus() == DataFile.DELETED) {
                            dataFile.delete();
                            return;
                        }
                        dataFile.setCompletionPercentage(current_ratio);
                        dataFile.save();
                    } else {
                        return;
                    }
                }
                i++;
            }

            // Write headers: Labels are derived from collected alignment attributes
            List<AlignmentAttribute> aaList = alignment.getAlignmentAttributes();
            aaList.sort(new Comparator<AlignmentAttribute>() {
                @Override
                public int compare(AlignmentAttribute o1, AlignmentAttribute o2) {
                    return o1.toString().compareTo(o2.toString());
                }
            });
            //System.out.println("aligned attributes size: " + aaList.size());
            boolean first = true;
            for (AlignmentAttribute aa : aaList) {
                if (first) {
                    FileUtils.writeStringToFile(file, "\"" + aa + "\"", "utf-8", true);
                    first = false;
                } else {
                    FileUtils.writeStringToFile(file, ",\"" + aa + "\"", "utf-8", true);
                }
            }
            FileUtils.writeStringToFile(file, "\n", "utf-8", true);

            // Sort collected objects by their original ID
            List<StudyObject> objects = alignment.getObjects();
            objects.sort(new Comparator<StudyObject>() {
                @Override
                public int compare(StudyObject o1, StudyObject o2) {
                    return o1.getOriginalId().compareTo(o2.getOriginalId());
                }
            });
            //System.out.println("objects size: " + objects.size());

            // Write rows: traverse collected object. From these objects, traverse alignment objects
            for (StudyObject obj : objects) {
                if (results.containsKey(obj.getUri())) {
                    Map<String, String> row = results.get(obj.getUri());
                    first = true;
                    for (AlignmentAttribute aa : aaList) {
                        if (first) {
                            FileUtils.writeStringToFile(file, "\"" + row.get(aa.toString()) + "\"", "utf-8", true);
                            first = false;
                        } else {
                            FileUtils.writeStringToFile(file, ",\"" + row.get(aa.toString()) + "\"", "utf-8", true);
                        }
                    }
                    FileUtils.writeStringToFile(file, "\n", "utf-8", true);
                }
            }

            //System.out.println("Finished writing!");
            
	    // Write harmonized code book
	    outputHarmonizedCodebook(alignment, null);

            dataFile = DataFile.findByName(file.getName());
            if (dataFile != null) {
                if (dataFile.getStatus() == DataFile.DELETED) {
                    dataFile.delete();
                    return;
                }
                dataFile.setCompletionPercentage(100);
                dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
                dataFile.setStatus(DataFile.CREATED);
                dataFile.save();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void outputHarmonizedCodebook(Alignment alignment, File file) {        
	// Write empty string to create the file
	//FileUtils.writeStringToFile(file, "", "utf-8", true);

	//System.out.println("Harmonized code book");
	//System.out.println("Class, code, description");

	// Wrte code book
	/*for (String key : alignment.getCodeBook().keySet()) {
	    Attribute attr = Attribute.find(key);
	    if (attr != null && attr.getDasaUri() != null && !attr.getDasaUri().equals("")) {
		System.out.print(key);
		String code = attr.findHarmonizedCode(attr.getDasaUri());
		if (code != null) {
		    System.out.print(", " + code);
		} else {
		    System.out.print(",");
		}
		if (attr.getLabel() != null) {
		    System.out.print(", " + attr.getLabel());
		} else {
		    System.out.print(",");
		}
		System.out.println();
	    }
	    }*/
    }

    public String toCSVRow(List<String> fieldNames) {
        List<String> values = new ArrayList<String>();
        for (String name : fieldNames) {
            Object obj = null;
            try {
                obj = Measurement.class.getDeclaredField(name).get(this);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            }
            if (obj != null) {
                values.add(obj.toString());
            } else {
                values.add("");
            }
        }

        return String.join(",", values);
    }

    @Override
    public boolean saveToTripleStore() {
        return false;
    }

    @Override
    public void deleteFromTripleStore() {  
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

    @Override
    public void run() {

    }
}
