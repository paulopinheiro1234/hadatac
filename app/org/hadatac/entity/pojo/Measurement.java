package org.hadatac.entity.pojo;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
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
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class Measurement extends HADatAcThing {
    @Field("uri")
    private String uri;
    @Field("owner_uri_str")
    private String ownerUri;
    @Field("acquisition_uri_str")
    private String acquisitionUri;
    @Field("study_uri_str")
    private String studyUri;
    @Field("object_uri_str")
    private String objectUri;
    @Field("timestamp_date")
    private Date timestamp;
    @Field("named_time_str")
    private String abstractTime;
    @Field("value_str")
    private String value;
    @Field("original_value_str")
    private String originalValue;
    @Field("pid_str")
    private String pid;
    @Field("sid_str")
    private String sid;	
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
    @Field("characteristic_uri_str")
    private String characteristicUri;	
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

    public String getObjectUri() {
        return objectUri;
    }

    public void setPID(String objectUri) {
        this.pid = objectUri;
    }

    public void setSID(String objectUri) {
        this.sid = objectUri;
    }

    public String getObjectPID() {
        return this.pid;
    }

    public String getObjectSID() {
        return this.sid;
    }

    public void setObjectUri(String objectUri) {
        this.objectUri = objectUri;
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

    public String getCharacteristicUri() {
        return characteristicUri;
    }

    public void setCharacteristicUri(String characteristicUri) {
        this.characteristicUri = characteristicUri;
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
                ConfigFactory.load().getString("hadatac.solr.data") 
                + Collections.DATA_ACQUISITION).build();
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
                ConfigFactory.load().getString("hadatac.solr.data") 
                + Collections.DATA_ACQUISITION).build();
        try {
            UpdateResponse response = solr.deleteByQuery("dataset_uri_str:\"" + datasetUri + "\"");
            solr.commit();
            solr.close();

            List<DataAcquisition> dataAcquisitions = DataAcquisition.findAll();
            for (DataAcquisition da : dataAcquisitions) {
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

        List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
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
            facet_query += "characteristic_uri_str" + ":\"" + char_uri + "\"";
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
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + Collections.DATA_ACQUISITION).build();
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

        List<String> ownedDAs = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
        if (ownedDAs.isEmpty()) {
            /*
             * an empty query happens when current user is not allowed to see any
             * data acquisition
             */
            System.out.println("Not allowed to access any Data Acquisition!");
            return result;
        }

        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacets(facets);

        FacetHandler retFacetHandler = new FacetHandler();
        retFacetHandler.loadFacets(facets);

        // System.out.println("\nfacetHandler before: " + facetHandler.toSolrQuery());

        // Run one time
        getAllFacetStats(facetHandler, retFacetHandler, result, false);

        // Get facet statistics
        getAllFacetStats(retFacetHandler, retFacetHandler, result, true);
        //getAllFacetStats(facetHandler, retFacetHandler, result, true);

        // Get documents
        // System.out.println("\n\n\nfacetHandler after: " + retFacetHandler.bottommostFacetsToSolrQuery());
        long docSize = 0;

        String q = buildQuery(ownedDAs, retFacetHandler);

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

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    ConfigFactory.load().getString("hadatac.solr.data") 
                    + Collections.DATA_ACQUISITION).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList docs = queryResponse.getResults();
            docSize = docs.getNumFound();
            System.out.println("Num of results: " + docSize);

            Set<String> uri_set = new HashSet<String>();
            Iterator<SolrDocument> iterDoc = docs.iterator();
            Map<String, DataAcquisition> cachedDA = new HashMap<String, DataAcquisition>();
            Map<String, String> mapClassLabel = generateCodeClassLabel();
            while (iterDoc.hasNext()) {
                Measurement measurement = convertFromSolr(iterDoc.next(), cachedDA, mapClassLabel);
                result.addDocument(measurement);
                uri_set.add(measurement.getEntityUri());
                uri_set.add(measurement.getCharacteristicUri());
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
        fTreeS.setTargetFacet(DataAcquisition.class);
        fTreeS.addUpperFacet(Study.class);
        Pivot pivotS = getFacetStats(fTreeS, 
                retFacetHandler.getFacetByName(FacetHandler.STUDY_FACET), 
                facetHandler);

        FacetTree fTreeEC = new FacetTree();
        fTreeEC.setTargetFacet(AttributeInstance.class);
        fTreeEC.addUpperFacet(Indicator.class);
        fTreeEC.addUpperFacet(EntityRole.class);
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
        Pivot pivotT = getFacetStats(fTreeT, 
                retFacetHandler.getFacetByName(FacetHandler.TIME_FACET),
                facetHandler);

        FacetTree fTreePI = new FacetTree();
        fTreePI.setTargetFacet(DataAcquisition.class);
        fTreePI.addUpperFacet(Platform.class);
        fTreePI.addUpperFacet(Instrument.class);
        Pivot pivotPI = getFacetStats(fTreePI, 
                retFacetHandler.getFacetByName(FacetHandler.PLATFORM_INSTRUMENT_FACET),
                facetHandler);

        if (bAddToResults) {
            result.extra_facets.put(FacetHandler.STUDY_FACET, pivotS);
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
        pivot.setNullParent();

        return pivot;
    }

    public static long getNumByDataAcquisition(DataAcquisition dataAcquisition) {
        SolrClient solr = new HttpSolrClient.Builder(
                ConfigFactory.load().getString("hadatac.solr.data") 
                + Collections.DATA_ACQUISITION).build();
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
                ConfigFactory.load().getString("hadatac.solr.data") 
                + Collections.DATA_ACQUISITION).build();
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
                results.put(uri, uri.substring(Math.max(uri.lastIndexOf("#"), uri.lastIndexOf("/")) + 1));
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
        query += "SELECT ?uri ?label WHERE { "
                + valueConstraint
                + " ?uri rdfs:label ?label . "
                + "}";

        try {
            QueryExecution qe = QueryExecutionFactory.sparqlService(
                    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
            ResultSet resultSet = qe.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
            qe.close();

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
            QueryExecution qe = QueryExecutionFactory.sparqlService(
                    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
            ResultSet resultSet = qe.execSelect();
            ResultSetRewindable resultsrw = ResultSetFactory.copyResults(resultSet);
            qe.close();

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
        if (cache.containsKey(getCharacteristicUri())) {
            setCharacteristic(cache.get(getCharacteristicUri()));
        }
        if (cache.containsKey(getUnitUri())) {
            setUnit(cache.get(getUnitUri()));
        }
    }

    public static Measurement convertFromSolr(SolrDocument doc, 
            Map<String, DataAcquisition> cachedDA, Map<String, String> cachedURILabels) {
        Measurement m = new Measurement();
        m.setUri(SolrUtils.getFieldValue(doc, "uri"));
        m.setOwnerUri(SolrUtils.getFieldValue(doc, "owner_uri_str"));
        m.setDatasetUri(SolrUtils.getFieldValue(doc, "dataset_uri_str"));
        m.setAcquisitionUri(SolrUtils.getFieldValue(doc, "acquisition_uri_str"));
        m.setStudyUri(SolrUtils.getFieldValue(doc, "study_uri_str"));
        m.setDasoUri(SolrUtils.getFieldValue(doc, "daso_uri_str"));
        m.setDasaUri(SolrUtils.getFieldValue(doc, "dasa_uri_str"));
        m.setObjectUri(SolrUtils.getFieldValue(doc, "object_uri_str"));
        m.setInRelationToUri(SolrUtils.getFieldValue(doc, "in_relation_to_uri_str"));
        m.setPID(SolrUtils.getFieldValue(doc, "pid_str"));
        m.setSID(SolrUtils.getFieldValue(doc, "sid_str"));
        m.setAbstractTime(SolrUtils.getFieldValue(doc, "named_time_str"));
        m.setOriginalValue(SolrUtils.getFieldValue(doc, "original_value_str"));

        String value = SolrUtils.getFieldValue(doc, "value_str");
        if (cachedURILabels.containsKey(value)) {
            m.setValue(cachedURILabels.get(value));
        } else {
            m.setValue(value);
        }

        m.setEntityUri(SolrUtils.getFieldValue(doc, "entity_uri_str"));
        m.setCharacteristicUri(SolrUtils.getFieldValue(doc, "characteristic_uri_str"));
        m.setUnitUri(SolrUtils.getFieldValue(doc, "unit_uri_str"));

        DataAcquisition da = null;
        if (cachedDA == null) {
            da = DataAcquisition.findByUri(m.getAcquisitionUri());
        } else {
            // Use cached DA
            if (cachedDA.containsKey(m.getAcquisitionUri())) {
                da = cachedDA.get(m.getAcquisitionUri());
            } else {
                da = DataAcquisition.findByUri(m.getAcquisitionUri());
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
            List<String> fieldNames, File file, DataFile dataFile) {		
        try {
            // Create headers
            FileUtils.writeStringToFile(file, String.join(",", fieldNames) + "\n", "utf-8", true);

            // Create rows
            int i = 1;
            int total = measurements.size();
            for (Measurement m : measurements) {
                FileUtils.writeStringToFile(file, m.toCSVRow(fieldNames) + "\n", "utf-8", true);
                int prev_ratio = 0;
                double ratio = (double)i / total * 100;
                if (((int)ratio) != prev_ratio) {
                    prev_ratio = (int)ratio;
                    dataFile.setCompletionPercentage((int)ratio);
                    dataFile.save();
                }
                i++;
            }
            dataFile.setCompletionPercentage(100);
            dataFile.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            dataFile.setStatus(DataFile.CREATED);
            dataFile.save();
        } catch (IOException e) {
            e.printStackTrace();
        }
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
}
