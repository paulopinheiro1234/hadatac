package org.hadatac.entity.pojo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import module.DatabaseExecutionContext;
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
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.FacetTree;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.vocabularies.HASCO;
import org.hadatac.utils.NameSpaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Measurement extends HADatAcThing implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(Measurement.class);

    @Field("uri")
    private String uri;
    @Field("type_uri_str")
    private String typeUri;
    @Field("hasco_type_uri_str")
    private String hascoTypeUri;
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
    @Field("entry_object_uri_str")
    private String entryObjectUri;
    @Field("study_object_type_uri_str")
    private String studyObjectTypeUri;
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
    private String valueClass;
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
    @Field("characteristic_uri_str_multi")
    private List<String> characteristicUris;
    @Field("categorical_class_uri_str")
    private String categoricalClassUri;
    @Field("location_latlong")
    private String location;
    @Field("elevation_double")
    private double elevation;
    @Field("dataset_uri_str")
    private String datasetUri;
    @Field("original_id_str")
    private String originalId;

    // Variables that are not stored in Solr
    private String entity;
    //private String characteristic;
    private String unit;
    private String platformName;
    private String platformUri;
    private String instrumentModel;
    private String instrumentUri;
    private String strTimestamp;

    public Measurement() {
        typeUri = HASCO.VALUE;
        hascoTypeUri = HASCO.VALUE;
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

    public String getEntryObjectUri() {
        return entryObjectUri;
    }

    public void setEntryObjectUri(String entryObjectUri) {
        this.entryObjectUri = entryObjectUri;
    }

    public String getStudyObjectTypeUri() {
        return studyObjectTypeUri;
    }

    public void setStudyObjectTypeUri(String studyObjectTypeUri) {
        this.studyObjectTypeUri = studyObjectTypeUri;
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
    	//System.out.println("Inside Measurement.setTimestamp()");
    	this.timestamp = timestamp;
        if (timestamp != null) {


        	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            this.strTimestamp = sdf.format(timestamp);

            //this.strTimestamp = timestamp.toString();
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

    public String getValueClass() {
        return valueClass;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setValueClass(String valueClass) {
        this.valueClass = valueClass;
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

    public List<String> getCharacteristicUris() {
        return characteristicUris;
    }

    public void addCharacteristicUris(String characteristicUri) {
        this.characteristicUris.add(characteristicUri);
    }

    public void setCharacteristicUris(List<String> characteristicUris) {
        this.characteristicUris = characteristicUris;
    }

    public void setCategoricalClassUri(String categoricalClassUri) {
        this.categoricalClassUri = categoricalClassUri;
    }

    public String getCategoricalClassUri() {
        return categoricalClassUri;
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

    public String getOriginalId() {
        return originalId;
    }

    public void setOriginalId(String originalId) {
        this.originalId = originalId;
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

    public static int deleteFromSolr(String datasetUri) {
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        try {
            UpdateResponse response = solr.deleteByQuery("dataset_uri_str:\"" + datasetUri + "\"");
            solr.commit();
            solr.close();

            List<STR> dataAcquisitions = STR.findAll();
            for (STR da : dataAcquisitions) {
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

        List<String> listURI = STR.findAllAccessibleDataAcquisition(user_uri);
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
            facet_query = handler.toSolrQuery();
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

        long startTime = System.currentTimeMillis();
        List<String> ownedDAs = STR.findAllAccessibleDataAcquisition(user_uri);
        log.debug("STR.findAllAccessibleDataAcquisition(user_uri) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");
        if (ownedDAs.isEmpty()) {
            /*
             * an empty query happens when current user is not allowed to see any
             * data acquisition
             */
            System.out.println("User with this URL: " + user_uri + ": Not allowed to access any Data Acquisition!");
            return result;
        }

        startTime = System.currentTimeMillis();
        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);
        log.debug("facetHandler.loadFacetsFromString(facets) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        startTime = System.currentTimeMillis();
        FacetHandler retFacetHandler = new FacetHandler();
        retFacetHandler.loadFacetsFromString(facets);
        log.debug("retFacetHandler.loadFacetsFromString(facets) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        // System.out.println("\nfacetHandler before: " + facetHandler.toSolrQuery());
        // System.out.println("\nfacetHandler before: " + facetHandler.toJSON());

        // Run one time
        // getAllFacetStats(facetHandler, retFacetHandler, result, false);

        // Get facet statistics
        // getAllFacetStats(retFacetHandler, retFacetHandler, result, true);
        startTime = System.currentTimeMillis();
        getAllFacetStats(facetHandler, retFacetHandler, result, true);
        log.debug("getAllFacetStats() takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

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
            query.setStart((page - 1) * qtd);
            query.setRows(qtd);
        } else {
            query.setRows(99999999);
        }
        query.setFacet(true);
        query.setFacetLimit(-1);

        try {
            startTime = System.currentTimeMillis();
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            log.debug("solr.query takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

            SolrDocumentList docs = queryResponse.getResults();
            docSize = docs.getNumFound();
            System.out.println("Num of results: " + docSize);

            startTime = System.currentTimeMillis();
            Set<String> uri_set = new HashSet<String>();
            Map<String, STR> cachedDA = new HashMap<String, STR>();
            Map<String, String> mapClassLabel = generateCodeClassLabel();
            log.debug("generateCodeClassLabel() takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

            Iterator<SolrDocument> iterDoc = docs.iterator();
            while (iterDoc.hasNext()) {

                startTime = System.currentTimeMillis();
                Measurement measurement = convertFromSolr(iterDoc.next(), cachedDA, mapClassLabel);
                log.debug("convertFromSolr() takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

                result.addDocument(measurement);
                uri_set.add(measurement.getEntityUri());
                uri_set.addAll(measurement.getCharacteristicUris());
                uri_set.add(measurement.getUnitUri());
            }

            // Assign labels of entity, characteristic, and units collectively
            startTime = System.currentTimeMillis();
            Map<String, String> cachedLabels = Measurement.generateCachedLabel(new ArrayList<String>(uri_set));
            for (Measurement measurement : result.getDocuments()) {
                measurement.setLabels(cachedLabels);
            }
            log.debug("generateCachedLabel() takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

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


    public static AcquisitionQueryResult findAsync(String user_uri, int page, int qtd, String facets, DatabaseExecutionContext databaseExecutionContext)  {

        AcquisitionQueryResult resultAsync = new AcquisitionQueryResult();

        CompletableFuture<List<String>> promiseOfOwnedDAs = CompletableFuture.supplyAsync((
                () -> { return STR.findAllAccessibleDataAcquisition(user_uri); }
        ), databaseExecutionContext);

        CompletableFuture<AcquisitionQueryResult> promiseOfFacetStats = CompletableFuture.supplyAsync((
                () -> { return getAllFacetStatsWrapper(resultAsync, facets, databaseExecutionContext); }
        ), databaseExecutionContext);

        List<String> ownedDAs = null;
        try {
            ownedDAs = promiseOfOwnedDAs.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        AcquisitionQueryResult result = null;
        try {
            result = promiseOfFacetStats.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        // CompletableFuture<AcquisitionQueryResult> ans = promiseOfOwnedDAs.thenCombine(promiseOfFacetStats, (ownedDAs, facetResult) -> {

        long startTime = System.currentTimeMillis();
        if (ownedDAs.isEmpty()) {
            System.out.println("User with this URL: " + user_uri + ": Not allowed to access any Data Acquisition!");
            return result;
        }

        long docSize = 0;

        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);
        String q = buildQuery(ownedDAs, facetHandler);

        SolrQuery query = new SolrQuery();
        query.setQuery(q);
        if (page != -1) {
            query.setStart((page - 1) * qtd);
            query.setRows(qtd);
        } else {
            query.setRows(99999999);
        }
        query.setFacet(true);
        query.setFacetLimit(-1);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();

            SolrDocumentList docs = queryResponse.getResults();
            docSize = docs.getNumFound();
            System.out.println("Num of results: " + docSize);

            Set<String> uri_set = new HashSet<String>();
            Map<String, STR> cachedDA = new HashMap<String, STR>();
            Map<String, String> mapClassLabel = generateCodeClassLabelFacetSearch();

            Iterator<SolrDocument> iterDoc = docs.iterator();
            while (iterDoc.hasNext()) {
                Measurement measurement = convertFromSolr(iterDoc.next(), cachedDA, mapClassLabel);
                result.addDocument(measurement);
                uri_set.add(measurement.getEntityUri());
                uri_set.addAll(measurement.getCharacteristicUris());
                uri_set.add(measurement.getUnitUri());
            }

            // Assign labels of entity, characteristic, and units collectively
            Map<String, String> cachedLabels = Measurement.generateCachedLabelFacetSearch(new ArrayList<String>(uri_set));
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

        log.debug("findAsync takes: " + (System.currentTimeMillis()-startTime));

        return result;
        //});

        //return result;

    }

    public static AcquisitionQueryResult getAllFacetStatsWrapper(AcquisitionQueryResult result, String facets, DatabaseExecutionContext databaseExecutionContext) {

        long startTime = System.currentTimeMillis();

        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);

        FacetHandler retFacetHandler = new FacetHandler();
        retFacetHandler.loadFacetsFromString(facets);

        log.debug("findAsync -> getAllFacetStatsWrapper-1: " + (System.currentTimeMillis()-startTime));

        startTime = System.currentTimeMillis();
        getAllFacetStatsAsync(facetHandler, retFacetHandler, result, true, databaseExecutionContext);
        log.debug("findAsync -> getAllFacetStatsWrapper-2: " + (System.currentTimeMillis()-startTime));

        return result;

    }

    public static void getAllFacetStats(
            FacetHandler facetHandler, 
            FacetHandler retFacetHandler,
            AcquisitionQueryResult result,
            boolean bAddToResults) {

        FacetTree fTreeS = new FacetTree();
        fTreeS.setTargetFacet(STR.class);
        fTreeS.addUpperFacet(Study.class);

        long startTime = System.currentTimeMillis();
        Pivot pivotS = getFacetStats(fTreeS, 
                retFacetHandler.getFacetByName(FacetHandler.STUDY_FACET), 
                facetHandler);
        log.debug("getFacetStats(fTreeS = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        FacetTree fTreeOC = new FacetTree();
        fTreeOC.setTargetFacet(StudyObjectType.class);
        //fTreeOC.addUpperFacet(ObjectCollectionType.class);
        fTreeOC.addUpperFacet(StudyObjectRole.class);
        startTime = System.currentTimeMillis();
        Pivot pivotOC = getFacetStats(fTreeOC, 
                retFacetHandler.getFacetByName(FacetHandler.OBJECT_COLLECTION_FACET), 
                facetHandler);
        log.debug("getFacetStats(fTreeOC = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        /*
         *  The facet tree EC computes the entity-attribute indicators for indicators based on property's main attribute 
         */
        FacetTree fTreeEC = new FacetTree();
        fTreeEC.setTargetFacet(AttributeInstance.class);
        fTreeEC.addUpperFacet(Indicator.class);
        fTreeEC.addUpperFacet(EntityRole.class);
        //fTreeEC.addUpperFacet(Category.class);
        fTreeEC.addUpperFacet(InRelationToInstance.class);
        fTreeEC.addUpperFacet(EntityInstance.class);
        startTime = System.currentTimeMillis();
        Pivot pivotEC = getFacetStats(fTreeEC, 
                retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET), 
                facetHandler);
        log.debug("getFacetStats(fTreeEC = " + (System.currentTimeMillis()-startTime) + " sms to finish");


        /*
         *  The facet tree EC computes the entity-attribute indicators for indicators based on property's in-relation-to attribute 
         */
        FacetTree fTreeEC2 = new FacetTree();
        fTreeEC2.setTargetFacet(AttributeInstance.class);
        fTreeEC2.addUpperFacet(Indicator.class);
        fTreeEC2.addUpperFacet(EntityRole.class);
        //fTreeEC2.addUpperFacet(Category.class);
        fTreeEC2.addUpperFacet(InRelationToInstance.class);
        fTreeEC2.addUpperFacet(EntityInstance.class);
        startTime = System.currentTimeMillis();
        Pivot pivotEC2 = getFacetStats(fTreeEC2, 
                retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET2), 
                facetHandler);
        log.debug("getFacetStats(fTreeEC2 = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        /*
         *  Merging the computation result of pivotEC2 into pivotEC
         */
        pivotEC.addChildrenFromPivot(pivotEC2);
        pivotEC.normalizeCategoricalVariableLabels(retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET), facetHandler);
        
    	/*
        System.out.println("measurement - >>>>>>>>>>> EC Content");
        for (Pivot pivot1 : pivotEC.children) {
        	System.out.println("measurement - EC_1: field: " + pivot1.getField() + "  value: " + pivot1.getValue() + "   count: " + pivot1.getCount() + "    tooltip: " + pivot1.getTooltip());
            for (Pivot pivot2 : pivot1.children) {
            	System.out.println("  measurement - EC_2: field: " + pivot2.getField() + "   value: " + pivot2.getValue() + "  count: " + pivot2.getCount() + "    tooltip: " + pivot2.getTooltip());
                for (Pivot pivot3 : pivot2.children) {
                	System.out.println("    measurement - EC_3: field: " + pivot2.getField() + "   value: " + pivot3.getValue() + "  count: " + pivot3.getCount() + "    tooltip: " + pivot3.getTooltip());
                	for (Pivot pivot4 : pivot3.children) {
                    	System.out.println("      measurement - EC_4: field: " + pivot4.getField() + "   value: " + pivot4.getValue() + "  count: " + pivot4.getCount() + "    tooltip: " + pivot4.getTooltip());
                        for (Pivot pivot5 : pivot4.children) {
                        	System.out.println("        measurement - EC_5: field: " + pivot5.getField() + "   value: " + pivot5.getValue() + "  count: " + pivot5.getCount() + "    tooltip: " + pivot5.getTooltip());
                            for (Pivot pivot6 : pivot5.children) {
                            	System.out.println("          measurement - EC_6: field: " + pivot6.getField() + "   value: " + pivot6.getValue() + "  count: " + pivot6.getCount() + "    tooltip: " + pivot6.getTooltip());
                            }
                        }
                    }
                }
            }
        }
    	System.out.println("measurement - <<<<<<<<<<<< EC Content");
		*/
    	
        FacetTree fTreeU = new FacetTree();
        fTreeU.setTargetFacet(UnitInstance.class);
        startTime = System.currentTimeMillis();
        Pivot pivotU = getFacetStats(fTreeU, 
                retFacetHandler.getFacetByName(FacetHandler.UNIT_FACET),
                facetHandler);
        log.debug("getFacetStats(fTreeU = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        FacetTree fTreeT = new FacetTree();
        fTreeT.setTargetFacet(TimeInstance.class);
        //fTreeT.addUpperFacet(DASEType.class);
        startTime = System.currentTimeMillis();
        Pivot pivotT = getFacetStats(fTreeT, 
                retFacetHandler.getFacetByName(FacetHandler.TIME_FACET),
                facetHandler);
        log.debug("getFacetStats(fTreeT = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        FacetTree fTreePI = new FacetTree();
        fTreePI.setTargetFacet(STR.class);
        fTreePI.addUpperFacet(Platform.class);
        fTreePI.addUpperFacet(Instrument.class);
        startTime = System.currentTimeMillis();
        Pivot pivotPI = getFacetStats(fTreePI, 
                retFacetHandler.getFacetByName(FacetHandler.PLATFORM_INSTRUMENT_FACET),
                facetHandler);
        log.debug("getFacetStats(fTreePI = " + (System.currentTimeMillis()-startTime) + " sms to finish");

        if (bAddToResults) {
            result.extra_facets.put(FacetHandler.STUDY_FACET, pivotS);
            result.extra_facets.put(FacetHandler.OBJECT_COLLECTION_FACET, pivotOC);
            result.extra_facets.put(FacetHandler.ENTITY_CHARACTERISTIC_FACET, pivotEC);
            result.extra_facets.put(FacetHandler.UNIT_FACET, pivotU);
            result.extra_facets.put(FacetHandler.TIME_FACET, pivotT);
            result.extra_facets.put(FacetHandler.PLATFORM_INSTRUMENT_FACET, pivotPI);
        }
               
    }

    private static void getAllFacetStatsAsync(
            FacetHandler facetHandler,
            FacetHandler retFacetHandler,
            AcquisitionQueryResult result,
            boolean bAddToResults, DatabaseExecutionContext databaseExecutionContext) {

        AtomicReference<Pivot> pEC = new AtomicReference<>();
        AtomicReference<Pivot> pEC2 = new AtomicReference<>();

        CompletableFuture<FacetTree> promiseOfTreeS = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeS = new FacetTree();
                    fTreeS.setTargetFacet(STR.class);
                    fTreeS.addUpperFacet(Study.class);
                    Pivot pivotS = getFacetStats(fTreeS,
                            retFacetHandler.getFacetByName(FacetHandler.STUDY_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.STUDY_FACET, pivotS);
                    }
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeS = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeS;
                }
        ), databaseExecutionContext);

        CompletableFuture<FacetTree> promiseOfTreeOC = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeOC = new FacetTree();
                    fTreeOC.setTargetFacet(StudyObjectType.class);
                    //fTreeOC.addUpperFacet(ObjectCollectionType.class);
                    fTreeOC.addUpperFacet(StudyObjectRole.class);
                    Pivot pivotOC = getFacetStats(fTreeOC,
                            retFacetHandler.getFacetByName(FacetHandler.OBJECT_COLLECTION_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.OBJECT_COLLECTION_FACET, pivotOC);
                    }
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeOC = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeOC;
                }
        ), databaseExecutionContext);

        CompletableFuture<FacetTree> promiseOfTreeEC = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeEC = new FacetTree();
                    fTreeEC.setTargetFacet(AttributeInstance.class);
                    fTreeEC.addUpperFacet(Indicator.class);
                    fTreeEC.addUpperFacet(EntityRole.class);
                    //fTreeEC.addUpperFacet(Category.class);
                    fTreeEC.addUpperFacet(InRelationToInstance.class);
                    fTreeEC.addUpperFacet(EntityInstance.class);
                    Pivot pivotEC = getFacetStats(fTreeEC,
                            retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.ENTITY_CHARACTERISTIC_FACET, pivotEC);
                    }
                    pEC.set(pivotEC);
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeEC = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeEC;
                }
        ), databaseExecutionContext);

        CompletableFuture<FacetTree> promiseOfTreeEC2 = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeEC2 = new FacetTree();
                    fTreeEC2.setTargetFacet(AttributeInstance.class);
                    fTreeEC2.addUpperFacet(Indicator.class);
                    fTreeEC2.addUpperFacet(EntityRole.class);
                    //fTreeEC2.addUpperFacet(Category.class);
                    fTreeEC2.addUpperFacet(InRelationToInstance.class);
                    fTreeEC2.addUpperFacet(EntityInstance.class);
                    Pivot pivotEC2 = getFacetStats(fTreeEC2,
                            retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET2),
                            facetHandler);
                    pEC2.set(pivotEC2);
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeEC2 = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeEC2;
                }
        ), databaseExecutionContext);

        /*
         *  Merging the computation result of pivotEC2 into pivotEC
         */

        CompletableFuture<FacetTree> promiseOfTreeU = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeU = new FacetTree();
                    fTreeU.setTargetFacet(UnitInstance.class);
                    Pivot pivotU = getFacetStats(fTreeU,
                            retFacetHandler.getFacetByName(FacetHandler.UNIT_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.UNIT_FACET, pivotU);
                    }
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeU = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeU;
                }
        ), databaseExecutionContext);

        CompletableFuture<FacetTree> promiseOfTreeT = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreeT = new FacetTree();
                    fTreeT.setTargetFacet(TimeInstance.class);
                    //fTreeT.addUpperFacet(DASEType.class);
                    Pivot pivotT = getFacetStats(fTreeT,
                            retFacetHandler.getFacetByName(FacetHandler.TIME_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.TIME_FACET, pivotT);
                    }
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreeT = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreeT;
                }
        ), databaseExecutionContext);

        CompletableFuture<FacetTree> promiseOfTreePI = CompletableFuture.supplyAsync((
                () -> {
                    long startTime = System.currentTimeMillis();
                    FacetTree fTreePI = new FacetTree();
                    fTreePI.setTargetFacet(STR.class);
                    fTreePI.addUpperFacet(Platform.class);
                    fTreePI.addUpperFacet(Instrument.class);
                    Pivot pivotPI = getFacetStats(fTreePI,
                            retFacetHandler.getFacetByName(FacetHandler.PLATFORM_INSTRUMENT_FACET),
                            facetHandler);
                    if (bAddToResults) {
                        result.extra_facets.put(FacetHandler.PLATFORM_INSTRUMENT_FACET, pivotPI);
                    }
                    log.debug("getAllFacetStatsAsync - getFacetStats(fTreePI = " + (System.currentTimeMillis() - startTime) + " sms to finish");
                    return fTreePI;
                }
        ),databaseExecutionContext);

        try {

            long currentTime = System.currentTimeMillis();
            FacetTree fTreeS = promiseOfTreeS.get();
            FacetTree fTreeOC = promiseOfTreeOC.get();
            FacetTree fTreeEC = promiseOfTreeEC.get();
            FacetTree fTreeEC2 = promiseOfTreeEC2.get();
            FacetTree fTreeU = promiseOfTreeU.get();
            FacetTree fTreeT = promiseOfTreeT.get();
            FacetTree fTreePI = promiseOfTreePI.get();

            pEC.get().addChildrenFromPivot(pEC2.get());
            pEC.get().normalizeCategoricalVariableLabelsFacetSearch(retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET), facetHandler);
            //pivotEC.addChildrenFromPivot(pivotEC2);
            //pivotEC.normalizeCategoricalVariableLabels(retFacetHandler.getFacetByName(FacetHandler.ENTITY_CHARACTERISTIC_FACET), facetHandler);

            log.debug("getAllFacetStatsAsync - final stage: " + (System.currentTimeMillis()-currentTime));

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }

    private static Pivot getFacetStats(
            FacetTree fTree, 
            Facet facet,
            FacetHandler facetHandler) {
        long startTime = System.currentTimeMillis();
        Pivot pivot = new Pivot();
        fTree.retrieveFacetData(0, facet, facetHandler, pivot);
        pivot.recomputeStats();
        log.debug("***** bottom getFacetStats:" + (System.currentTimeMillis()-startTime));
        return pivot;
    }

    public static long getNumByDataAcquisition(STR dataAcquisition) {
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

    public static Measurement find(String uri) {
        List<Measurement> listMeasurement = new ArrayList<Measurement>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        SolrQuery query = new SolrQuery();
        query.set("q", "uri:\"" + uri + "\"");
        query.set("rows", "1");

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            Iterator<SolrDocument> i = results.iterator();
            if (i.hasNext()) {
                return convertFromSolr(i.next(), null, new HashMap<>());
            }
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
                    + e.getMessage());
        }

        return null;
    }

    /* Possible concepts:
         - HASCO.DATA_ACQUISITION
         - HASCO.STUDY_OBJECT
         - HASCO.DA_SCHEMA_ATTRIBUTE
         - HASCO.DA_SCHEMA_OBJECT
         - HASCO.DATA_FILE
     */
    public static List<Measurement> findByConceptAndUri(String concept_uri, String uri) {
        List<Measurement> listMeasurement = new ArrayList<Measurement>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        SolrQuery query = new SolrQuery();
        if (concept_uri.equals(HASCO.DATA_ACQUISITION)) {
            query.set("q", "acquisition_uri_str:\"" + uri + "\"");
        } else if (concept_uri.equals(HASCO.STUDY_OBJECT)) {
            query.set("q", "object_uri_str:\"" + uri + "\"");
        } else if (concept_uri.equals(HASCO.DA_SCHEMA_ATTRIBUTE)) {
            query.set("q", "dasa_uri_str:\"" + uri + "\"");
        } else if (concept_uri.equals(HASCO.DA_SCHEMA_OBJECT)) {
            query.set("q", "daso_uri_str:\"" + uri + "\"");
        } else if (concept_uri.equals(HASCO.DATA_FILE)) {
            query.set("q", "dataset_uri_str:\"" + uri + "\"");
        }
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

    /*
    public static List<Measurement> findByObjectUri(String obj_uri) {

    	List<Measurement> listMeasurement = new ArrayList<Measurement>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
        SolrQuery query = new SolrQuery();
        query.set("q", "object_uri_str:\"" + obj_uri + "\"");
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
     */

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
            valueConstraint = " VALUES ?uri { " + HADatAcThing.stringify(validURIs) + " } ";
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

    public static Map<String, String> generateCachedLabelFacetSearch(List<String> uris) {
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
            valueConstraint = " VALUES ?uri { " + HADatAcThing.stringify(validURIs) + " } ";
        }

        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?uri ?label WHERE { \n"
                + valueConstraint + " \n"
                + " ?uri rdfs:label ?label . \n"
                + "}";

        try {
            ResultSetRewindable resultsrw = SPARQLUtilsFacetSearch.select(
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

    public static Map<String, String> generateCodeClassLabelFacetSearch() {
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
            ResultSetRewindable resultsrw = SPARQLUtilsFacetSearch.select(
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
        //if (attributes.size() > 0) {
        //    setCharacteristic(String.join("; ", attributes));
        //}

        if (cache.containsKey(getUnitUri())) {
            setUnit(cache.get(getUnitUri()));
        }
    }

    public static Measurement convertFromSolr(SolrDocument doc, 
            Map<String, STR> cachedDA, Map<String, String> cachedURILabels) {
        Measurement m = new Measurement();
        m.setUri(SolrUtils.getFieldValue(doc, "uri"));
        if (SolrUtils.getFieldValue(doc, "type_uri_str") != null) {
            m.setTypeUri(SolrUtils.getFieldValue(doc, "type_uri_str"));
        }
        if (SolrUtils.getFieldValue(doc, "hasco_type_uri_str") != null) {
            m.setHascoTypeUri(SolrUtils.getFieldValue(doc, "hasco_type_uri_str"));
        }
        m.setUri(SolrUtils.getFieldValue(doc, "uri"));
        m.setOwnerUri(SolrUtils.getFieldValue(doc, "owner_uri_str"));
        m.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
        m.setAcquisitionUri(SolrUtils.getFieldValue(doc, "acquisition_uri_str"));
        m.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str"));
        m.setDasoUri(SolrUtils.getFieldValue(doc, "daso_uri_str"));
        m.setDasaUri(SolrUtils.getFieldValue(doc, "dasa_uri_str"));
        m.setStudyObjectUri(SolrUtils.getFieldValue(doc, "study_object_uri_str"));
        m.setEntryObjectUri(SolrUtils.getFieldValue(doc, "entry_object_uri_str"));
        m.setStudyObjectTypeUri(SolrUtils.getFieldValue(doc, "study_object_type_uri_str"));
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
        List<String> uris = Measurement.tokenizeSolr(SolrUtils.getFieldValues(doc, "characteristic_uri_str_multi"));
        m.setCharacteristicUris(uris);
        m.setCategoricalClassUri(SolrUtils.getFieldValue(doc, "categorical_class_uri_str"));
        m.setUnitUri(SolrUtils.getFieldValue(doc, "unit_uri_str"));
        m.setOriginalId(SolrUtils.getFieldValue(doc, "original_id_str"));

        m.setValueClass(SolrUtils.getFieldValue(doc, "value_str"));
        m.setLabel(SolrUtils.getFieldValue(doc, "value_str"));
        if (cachedURILabels.containsKey(m.getValueClass())) {
            m.setValue(cachedURILabels.get(m.getValueClass()));
        } else {
            m.setValue(m.getValueClass());
        }

        STR da = null;
        if (cachedDA == null) {
            da = STR.findByUri(m.getAcquisitionUri());
        } else {
            // Use cached DA
            if (cachedDA.containsKey(m.getAcquisitionUri())) {
                da = cachedDA.get(m.getAcquisitionUri());
            } else {
                da = STR.findByUri(m.getAcquisitionUri());
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

    public static List<String> tokenizeSolr(List<String> solrInput) {
	List<String> response = new ArrayList<String>();
	for (String str : solrInput) {
	    StringTokenizer st = new StringTokenizer(str, ";"); 
	    while (st.hasMoreTokens()) 
		response.add(st.nextToken().trim()); 
	}
	return response;
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

    // helper function to check if a give string has all digital and ","
    public boolean isAllNumerical(String strValue) {
        if ( strValue == null || strValue.length() == 0 ) return false;
        for ( char c : strValue.toCharArray() ) {
            if ( !Character.isDigit(c) && c != ',' && c != '.' ) return false;
        }
        return true;
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
    public void run() {

    }
}
