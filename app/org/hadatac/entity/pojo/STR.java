package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.text.WordUtils;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;
import org.labkey.remoteapi.CommandException;

import io.ebeaninternal.server.lib.util.Str;

public class STR extends HADatAcThing {
    private static final String className = "hasco:DataAcquisition";

    @Field("uri")
    private String uri;
    @Field("label_str")
    private String label;
    @Field("comment_str")
    private String comment;
    @Field("used_uri_str_multi")
    private String used_uri;

    private DateTime startedAt;
    private DateTime endedAt;

    @Field("owner_uri_str")
    private String ownerUri;
    @Field("permission_uri_str")
    private String permissionUri;
    @Field("parameter_str")
    private String parameter;
    @Field("triggering_event_int")
    private int triggeringEvent;
    @Field("nr_data_points_long")
    private long numberDataPoints;
    @Field("unit_str_multi")
    private List<String> unit;
    @Field("unit_uri_str_multi")
    private List<String> unitUri;
    @Field("entity_str_multi")
    private List<String> entity;
    @Field("entity_uri_str_multi")
    private List<String> entityUri;
    @Field("type_uri_str_multi")
    private List<String> typeURIs;
    @Field("associated_uri_str_multi")
    private List<String> associatedURIs;
    @Field("characteristic_str_multi")
    private List<String> characteristic;
    @Field("characteristic_uri_str_multi")
    private List<String> characteristicUri;
    @Field("study_uri_str")
    private String studyUri;
    @Field("method_uri_str")
    private String methodUri;
    @Field("schema_uri_str")
    private String schemaUri;
    @Field("deployment_uri_str")
    private String deploymentUri;
    @Field("instrument_model_str")
    private String instrumentModel;
    @Field("instrument_uri_str")
    private String instrumentUri;
    @Field("platform_name_str")
    private String platformName;
    @Field("platform_uri_str")
    private String platformUri;
    @Field("location_latlong")
    private String location;
    @Field("elevation_str")
    private String elevation;
    @Field("dataset_uri_str_multi")
    private List<String> datasetURIs;
    // @Field("globalscope_uri_str")
    // private String rowScopeUri;
    // @Field("globalscope_name_str")
    // private String rowScopeName;
    @Field("localscope_uri_str_multi")
    private List<String> cellScopeUri;
    @Field("localscope_name_str_multi")
    private List<String> cellScopeName;
    @Field("status_int")
    private int status;
    /*
     * 0 - DataAcquisition is a new one, its details on the preamble It should
     * not exist inside the KB Preamble must contain deployment link and
     * deployment must exists on the KB 1 - DataAcquisition already exists, only
     * a reference present on the preamble It should exist inside the KB as not
     * finished yet 2 - DataAcquisition already exists, the preamble states its
     * termination with endedAtTime information It should exist inside the KB as
     * not finished yet
     *
     * 9999 - Stream Specification is complete (anything else diferent
     * than 9999 is considered incomplete
     *
     */

    private boolean isComplete;
    private String ccsvUri;
    private String localName;
    private Deployment deployment;

    public STR() {
        startedAt = null;
        endedAt = null;
        numberDataPoints = 0;
        isComplete = false;
        datasetURIs = new ArrayList<String>();
        unit = new ArrayList<String>();
        unitUri = new ArrayList<String>();
        characteristic = new ArrayList<String>();
        characteristicUri = new ArrayList<String>();
        entity = new ArrayList<String>();
        entityUri = new ArrayList<String>();
        typeURIs = new ArrayList<String>();
        associatedURIs = new ArrayList<String>();
        deployment = null;
        // rowScopeUri = null;
        // rowScopeName = null;
        cellScopeUri = new ArrayList<String>();
        cellScopeName = new ArrayList<String>();
    }

    @Override
    public boolean equals(Object o) {
        if ((o instanceof STR) && (((STR) o).getUri().equals(this.getUri()))) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return getUri().hashCode();
    }

    public int compareTo(STR another) {
        if (this.getLabel() != null && another.getLabel() != null) {
            return this.getLabel().compareTo(another.getLabel());
        }
        return this.getUri().compareTo(another.getUri());
    }

    public String getElevation() {
        return elevation;
    }

    public void setElevation(String elevation) {
        this.elevation = elevation;
    }

    public String getCcsvUri() {
        return ccsvUri;
    }

    public void setCcsvUri(String ccsvUri) {
        this.ccsvUri = ccsvUri;
    }

    public long getNumberDataPoints() {
        return numberDataPoints;
    }

    public void setNumberDataPoints(long numberDataPoints) {
        this.numberDataPoints = numberDataPoints;
    }

    public String getLocalName() {
        return localName;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isComplete() {
        return status == 9999;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        if (uri == null || uri.equals("")) {
            this.uri = "";
            return;
        }
        this.uri = URIUtils.replacePrefixEx(uri);
    }

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
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

    public String getUsedUri() {
        return used_uri;
    }

    public void setUsedUri(String used_uri) {
        this.used_uri = used_uri;
    }

    public String getStudyUri() {
        return studyUri;
    }

    public Study getStudy() {
        if (studyUri == null || studyUri.equals(""))
            return null;
        Study study = Study.find(studyUri);
        return study;
    }

    public void setStudyUri(String study_uri) {
        this.studyUri = study_uri;
    }

    public String getOwnerUri() {
        return ownerUri;
    }

    public void setOwnerUri(String ownerUri) {
        this.ownerUri = ownerUri;
    }

    public String getPermissionUri() {
        return permissionUri;
    }

    public void setPermissionUri(String permissionUri) {
        this.permissionUri = permissionUri;
    }

    public boolean getIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

    public int getTriggeringEvent() {
        return triggeringEvent;
    }

    public void setTriggeringEvent(int triggeringEvent) {
        this.triggeringEvent = triggeringEvent;
    }

    public String getTriggeringEventName() {
        switch (triggeringEvent) {
        case TriggeringEvent.INITIAL_DEPLOYMENT:
            return TriggeringEvent.INITIAL_DEPLOYMENT_NAME;
        case TriggeringEvent.LEGACY_DEPLOYMENT:
            return TriggeringEvent.LEGACY_DEPLOYMENT_NAME;
        case TriggeringEvent.CHANGED_CONFIGURATION:
            return TriggeringEvent.CHANGED_CONFIGURATION_NAME;
        case TriggeringEvent.CHANGED_OWNERSHIP:
            return TriggeringEvent.CHANGED_OWNERSHIP_NAME;
        case TriggeringEvent.AUTO_CALIBRATION:
            return TriggeringEvent.AUTO_CALIBRATION_NAME;
        case TriggeringEvent.SUSPEND_DATA_ACQUISITION:
            return TriggeringEvent.SUSPEND_DATA_ACQUISITION_NAME;
        case TriggeringEvent.RESUME_DATA_ACQUISITION:
            return TriggeringEvent.RESUME_DATA_ACQUISITION_NAME;
        }
        return "";
    }

    public int getTriggeringEventByName(String name) {
        switch (name) {
        case TriggeringEvent.INITIAL_DEPLOYMENT_NAME:
            return TriggeringEvent.INITIAL_DEPLOYMENT;
        case TriggeringEvent.LEGACY_DEPLOYMENT_NAME:
            return TriggeringEvent.LEGACY_DEPLOYMENT;
        case TriggeringEvent.CHANGED_CONFIGURATION_NAME:
            return TriggeringEvent.CHANGED_CONFIGURATION;
        case TriggeringEvent.CHANGED_OWNERSHIP_NAME:
            return TriggeringEvent.CHANGED_OWNERSHIP;
        case TriggeringEvent.AUTO_CALIBRATION_NAME:
            return TriggeringEvent.AUTO_CALIBRATION;
        case TriggeringEvent.SUSPEND_DATA_ACQUISITION_NAME:
            return TriggeringEvent.SUSPEND_DATA_ACQUISITION;
        case TriggeringEvent.RESUME_DATA_ACQUISITION_NAME:
            return TriggeringEvent.RESUME_DATA_ACQUISITION;
        }

        return -1;
    }

    public String getStartedAt() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        return formatter.withZone(DateTimeZone.UTC).print(startedAt);
    }

    public String getStartedAtXsd() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        return formatter.withZone(DateTimeZone.UTC).print(startedAt);
    }

    @Field("started_at_date")
    public void setStartedAt(DateTime startedAt) {
        this.startedAt = startedAt;
    }

    public void setStartedAtXsd(String startedAt) {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        this.startedAt = formatter.parseDateTime(startedAt);
    }

    public void setStartedAtXsdWithMillis(String startedAt) {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        this.startedAt = formatter.parseDateTime(startedAt);
    }

    public String getEndedAt() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        return formatter.withZone(DateTimeZone.UTC).print(endedAt);
    }

    public String getEndedAtXsd() {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        return formatter.withZone(DateTimeZone.UTC).print(endedAt);
    }

    @Field("ended_at_date")
    public void setEndedAt(DateTime endedAt) {
        this.endedAt = endedAt;
    }

    public void setEndedAtXsd(String endedAt) {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
        this.endedAt = formatter.parseDateTime(endedAt);
    }

    public void setEndedAtXsdWithMillis(String endedAt) {
        DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
        this.endedAt = formatter.parseDateTime(endedAt);
    }

    public List<String> getUnit() {
        return unit;
    }

    public void setUnit(List<String> unit) {
        this.unit = unit;
    }

    public void addUnit(String unit) {
        if (this.unit.contains(unit) == false) {
            this.unit.add(unit);
        }
    }

    public List<String> getUnitUri() {
        return unitUri;
    }

    public void setUnitUri(List<String> unitUri) {
        this.unitUri = unitUri;
    }

    public void addUnitUri(String unitUri) {
        if (this.unitUri.contains(unitUri) == false) {
            this.unitUri.add(unitUri);
        }
    }

    public List<String> getEntity() {
        return entity;
    }

    public void setEntity(List<String> entity) {
        this.entity = entity;
    }

    public void addEntity(String entity) {
        if (this.entity.contains(entity) == false) {
            this.entity.add(entity);
        }
    }

    public List<String> getEntityUri() {
        return entityUri;
    }

    public void setEntityUri(List<String> entityUri) {
        this.entityUri = entityUri;
    }

    public void addEntityUri(String entityUri) {
        if (this.entityUri.contains(entityUri) == false) {
            this.entityUri.add(entityUri);
        }
    }

    public List<String> getCharacteristic() {
        return characteristic;
    }

    public void setCharacteristic(List<String> characteristic) {
        this.characteristic = characteristic;
    }

    public void addCharacteristic(String characteristic) {
        if (this.characteristic.contains(characteristic) == false) {
            this.characteristic.add(characteristic);
        }
    }

    public List<String> getCharacteristicUri() {
        return characteristicUri;
    }

    public void setCharacteristicUri(List<String> characteristicUri) {
        this.characteristicUri = characteristicUri;
    }

    public void addCharacteristicUri(String characteristicUri) {
        if (this.characteristicUri.contains(characteristicUri) == false) {
            this.characteristicUri.add(characteristicUri);
        }
    }

    public String getMethodUri() {
        return methodUri;
    }

    public void setMethodUri(String methodUri) {
        this.methodUri = methodUri;
    }

    public String getSchemaUri() {
        return schemaUri;
    }

    public DataAcquisitionSchema getSchema() {
    	if (schemaUri == null || schemaUri.equals(""))
    		return null;
    	DataAcquisitionSchema schema = DataAcquisitionSchema.find(schemaUri);
    	return schema;
    }
    
    public void setSchemaUri(String schemaUri) {
        this.schemaUri = schemaUri;
    }

    public String getDeploymentUri() {
        return deploymentUri;
    }

    public Deployment getDeployment() {
        if (deploymentUri == null || deploymentUri.equals("")) {
            return null;
        }
        if (deployment != null) {
            if (deployment.getUri().equals(deploymentUri)) {
                return deployment;
            }
        }
        return deployment = Deployment.find(deploymentUri);
    }

    public void setDeploymentUri(String deploymentUri) {
        this.deploymentUri = deploymentUri;
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

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public boolean hasScope() {
        return (hasCellScope());
        // return (hasRowScope() || hasCellScope());
    }

    // public boolean hasRowScope() {
    // return (rowScopeUri != null && !rowScopeUri.equals(""));
    // }

    public boolean hasCellScope() {
        if (cellScopeUri != null && cellScopeUri.size() > 0) {
            for (String tmpUri : cellScopeUri) {
                if (tmpUri != null && !tmpUri.equals("")) {
                    return true;
                }
            }
        }
        return false;
    }

    /*
     * public String getRowScopeUri() { return rowScopeUri; } public void
     * setRowScopeUri(String rowScopeUri) { this.rowScopeUri = rowScopeUri; if
     * (rowScopeUri == null || rowScopeUri.equals("")) { return; }
     * ObjectCollection oc = ObjectCollection.find(rowScopeUri); if (oc != null)
     * { if (oc.getUri().equals(rowScopeUri)) { rowScopeName = oc.getLabel();
     * return; } } else { StudyObject obj = StudyObject.find(rowScopeUri); if
     * (obj != null && obj.getUri().equals(rowScopeUri)) { rowScopeName =
     * obj.getLabel(); return; } } } public String getRowScopeName() { return
     * rowScopeName; } public void setRowScopeName(String rowScopeName) {
     * this.rowScopeName = rowScopeName; }
     */

    public List<String> getCellScopeUri() {
        return cellScopeUri;
    }

    public void setCellScopeUri(List<String> cellScopeUri) {
        this.cellScopeUri = cellScopeUri;
        if (cellScopeUri == null || cellScopeUri.size() == 0) {
            return;
        }
        cellScopeName = new ArrayList<String>();
        for (String objUri : cellScopeUri) {
            StudyObject obj = StudyObject.find(objUri);
            if (obj != null && obj.getUri().equals(objUri)) {
                cellScopeName.add(obj.getLabel());
            } else {
                cellScopeName.add("");
            }
        }
    }

    public void addCellScopeUri(String cellScopeUri) {
        this.cellScopeUri.add(cellScopeUri);
    }

    public List<String> getCellScopeName() {
        return cellScopeName;
    }

    public void setCellScopeName(List<String> cellScopeName) {
        this.cellScopeName = cellScopeName;
    }

    public void addCellScopeName(String cellScopeName) {
        this.cellScopeName.add(cellScopeName);
    }

    public List<String> getDatasetUri() {
        return datasetURIs;
    }

    public void setDatasetUri(List<String> datasetURIs) {
        this.datasetURIs = datasetURIs;
    }

    public void addDatasetUri(String dataset_uri) {
        if (!datasetURIs.contains(dataset_uri)) {
            datasetURIs.add(dataset_uri);
        }
    }

    public void deleteDatasetUri(String dataset_uri) {
        Iterator<String> iter = datasetURIs.iterator();
        while (iter.hasNext()) {
            if (iter.next().equals(dataset_uri)) {
                iter.remove();
            }
        }
    }

    public void deleteAllDatasetURIs() {
        datasetURIs.clear();
    }

    public boolean containsDataset(String uri) {
        return datasetURIs.contains(uri);
    }

    public List<String> getTypeURIs() {
        return typeURIs;
    }

    public void setTypeURIs(List<String> typeURIs) {
        this.typeURIs = typeURIs;
    }

    public void addTypeUri(String type_uri) {
        if (!typeURIs.contains(type_uri)) {
            typeURIs.add(type_uri);
        }
    }

    public List<String> getAssociatedURIs() {
        return associatedURIs;
    }

    public void setAssociatedURIs(List<String> associatedURIs) {
        this.associatedURIs = associatedURIs;
    }

    public void addAssociatedUri(String associated_uri) {
        if (!associatedURIs.contains(associated_uri)) {
            associatedURIs.add(associated_uri);
        }
    }

    public void addNumberDataPoints(long number) {
        numberDataPoints += number;
    }

    public boolean isFinished() {
        if (endedAt == null) {
            return false;
        } else {
            return endedAt.isBeforeNow();
        }
    }

    @Override
    public void save() {
        saveToTripleStore();
        saveToSolr();
    }

    @Override
    public boolean saveToSolr() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)).build();
            if (null == endedAt) {
                endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
            } else if (endedAt.toString().startsWith("9999")) {
                endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
            }
            client.addBean(this).getStatus();
            client.commit();
            client.close();

            return true;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] DataAcquisition.save() - e.Message: " + e.getMessage());
            return false;
        }
    }

    @Override
    public int deleteFromSolr() {
        try {
            deleteMeasurementData();

            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)).build();
            UpdateResponse response = solr.deleteById(this.uri);
            solr.commit();
            solr.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] STR.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] STR.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] STR.delete() - Exception message: " + e.getMessage());
        }

        return -1;
    }
    
    @Override
    public long getNumber(Facet facet, FacetHandler facetHandler) {
        return getNumberFromSolr(facet, facetHandler);
    }

    @Override
    public long getNumberFromSolr(Facet facet, FacetHandler facetHandler) {
        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(false);

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            SolrDocumentList results = queryResponse.getResults();
            return results.getNumFound();
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisition.getNumberFromSolr() - Exception message: " + e.getMessage());
        }

        return -1;
    }
    
    @Override
    public Map<Facetable, List<Facetable>> getTargetFacets(
            Facet facet, FacetHandler facetHandler) {
        return getTargetFacetsFromSolr(facet, facetHandler);
    }

    @Override
    public Map<Facetable, List<Facetable>> getTargetFacetsFromSolr(Facet facet, FacetHandler facetHandler) {

        SolrQuery query = new SolrQuery();
        String strQuery = facetHandler.getTempSolrQuery(facet);
        // System.out.println("strQuery: " + strQuery);
        query.setQuery(strQuery);
        query.setRows(0);
        query.setFacet(true);
        query.setFacetLimit(-1);
        query.setParam("json.facet",
                "{ " + "acquisition_uri_str:{ " + "type: terms, " + "field: acquisition_uri_str, " + "limit: 1000}}");

        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();
            Pivot pivot = Pivot.parseQueryResponse(queryResponse);
            return parsePivot(pivot, facet, query.toString());
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisition.getTargetFacetsFromSolr() - Exception message: " + e.getMessage());
        }

        return null;
    }

    private Map<Facetable, List<Facetable>> parsePivot(Pivot pivot, Facet facet, String query) {
        Map<Facetable, List<Facetable>> results = new HashMap<Facetable, List<Facetable>>();
        
        for (Pivot pivot_ent : pivot.children) {
            STR da = new STR();
            da.setUri(pivot_ent.getValue());
            da.setLabel(WordUtils.capitalize(STR.findByUri(pivot_ent.getValue()).getLabel()));
            da.setCount(pivot_ent.getCount());
            da.setQuery(query);
            da.setField("acquisition_uri_str");

            if (!results.containsKey(da)) {
                List<Facetable> children = new ArrayList<Facetable>();
                results.put(da, children);
            }

            Facet subFacet = facet.getChildById(da.getUri());
            subFacet.putFacet("acquisition_uri_str", da.getUri());
        }

        return results;
    }

    public static STR convertFromSolr(SolrDocument doc) {
        Iterator<Object> i;
        DateTime date;

        STR dataAcquisition = new STR();
        try {
            if (doc.getFieldValue("uri") != null) {
                dataAcquisition.setUri(doc.getFieldValue("uri").toString());
            }
            if (doc.getFieldValue("owner_uri_str") != null) {
                dataAcquisition.setOwnerUri(doc.getFieldValue("owner_uri_str").toString());
            }
            if (doc.getFieldValue("permission_uri_str") != null) {
                dataAcquisition.setPermissionUri(doc.getFieldValue("permission_uri_str").toString());
            }
            if (doc.getFieldValue("parameter_str") != null) {
                dataAcquisition.setParameter(doc.getFieldValue("parameter_str").toString());
            }
            if (doc.getFieldValue("study_uri_str") != null) {
                dataAcquisition.setStudyUri(doc.getFieldValue("study_uri_str").toString());
            }
            if (doc.getFieldValue("triggering_event_int") != null) {
                dataAcquisition
                        .setTriggeringEvent(Integer.parseInt(doc.getFieldValue("triggering_event_int").toString()));
            }
            if (doc.getFieldValue("nr_data_points_long") != null) {
                dataAcquisition
                        .setNumberDataPoints(Long.parseLong(doc.getFieldValue("nr_data_points_long").toString()));
            }
            if (doc.getFieldValue("started_at_date") != null) {
                date = new DateTime((Date) doc.getFieldValue("started_at_date"));
                dataAcquisition.setStartedAt(date.withZone(DateTimeZone.UTC));
            }
            if (doc.getFieldValue("ended_at_date") != null) {
                date = new DateTime((Date) doc.getFieldValue("ended_at_date"));
                dataAcquisition.setEndedAt(date.withZone(DateTimeZone.UTC));
            }
            if (doc.getFieldValues("method_uri_str") != null) {
                dataAcquisition.setMethodUri(doc.getFieldValue("method_uri_str").toString());
            }
            if (doc.getFieldValues("schema_uri_str") != null) {
                dataAcquisition.setSchemaUri(doc.getFieldValue("schema_uri_str").toString());
            }
            if (doc.getFieldValue("label_str") != null) {
                dataAcquisition.setLabel(doc.getFieldValue("label_str").toString());
            }
            if (doc.getFieldValue("comment_str") != null) {
                dataAcquisition.setComment(doc.getFieldValue("comment_str").toString());
            }
            if (doc.getFieldValues("associated_uri_str_multi") != null) {
                i = doc.getFieldValues("associated_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addAssociatedUri(i.next().toString());
                }
            }
            if (doc.getFieldValues("type_uri_str_multi") != null) {
                i = doc.getFieldValues("type_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addTypeUri(i.next().toString());
                }
            }
            if (doc.getFieldValues("unit_str_multi") != null) {
                i = doc.getFieldValues("unit_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addUnit(i.next().toString());
                }
            }
            if (doc.getFieldValues("unit_uri_str_multi") != null) {
                i = doc.getFieldValues("unit_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addUnitUri(i.next().toString());
                }
            }
            if (doc.getFieldValues("entity_str_multi") != null) {
                i = doc.getFieldValues("entity_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addEntity(i.next().toString());
                }
            }
            if (doc.getFieldValues("entity_uri_str_multi") != null) {
                i = doc.getFieldValues("entity_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addEntityUri(i.next().toString());
                }
            }
            if (doc.getFieldValues("characteristic_str_multi") != null) {
                i = doc.getFieldValues("characteristic_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addCharacteristic(i.next().toString());
                }
            }
            if (doc.getFieldValues("characteristic_uri_str_multi") != null) {
                i = doc.getFieldValues("characteristic_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addCharacteristicUri(i.next().toString());
                }
            }
            if (doc.getFieldValue("deployment_uri_str") != null) {
                dataAcquisition.setDeploymentUri(doc.getFieldValue("deployment_uri_str").toString());
            }
            if (doc.getFieldValue("instrument_model_str") != null) {
                dataAcquisition.setInstrumentModel(doc.getFieldValue("instrument_model_str").toString());
            }
            if (doc.getFieldValue("instrument_uri_str") != null) {
                dataAcquisition.setInstrumentUri(doc.getFieldValue("instrument_uri_str").toString());
            }
            if (doc.getFieldValue("platform_name_str") != null) {
                dataAcquisition.setPlatformName(doc.getFieldValue("platform_name_str").toString());
            }
            if (doc.getFieldValue("platform_uri_str") != null) {
                dataAcquisition.setPlatformUri(doc.getFieldValue("platform_uri_str").toString());
            }
            if (doc.getFieldValues("dataset_uri_str_multi") != null) {
                i = doc.getFieldValues("dataset_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addDatasetUri(i.next().toString());
                }
            }
            // if (doc.getFieldValue("globalscope_uri_str") != null) {
            // dataAcquisition.setRowScopeUri(doc.getFieldValue("globalscope_uri_str").toString());
            // }
            // if (doc.getFieldValue("globalscope_name_str") != null) {
            // dataAcquisition.setRowScopeName(doc.getFieldValue("globalscope_name_str").toString());
            // }
            if (doc.getFieldValues("localscope_uri_str_multi") != null) {
                i = doc.getFieldValues("localscope_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addCellScopeUri(i.next().toString());
                }
            }
            if (doc.getFieldValues("localscope_name_str_multi") != null) {
                i = doc.getFieldValues("localscope_name_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addCellScopeName(i.next().toString());
                }
            }
            if (doc.getFieldValue("status_int") != null) {
                dataAcquisition.setStatus(Integer.parseInt(doc.getFieldValue("status_int").toString()));
            }
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisition.convertFromSolr(SolrDocument) - e.Message: " + e.getMessage());
            e.printStackTrace();
        }

        return dataAcquisition;
    }

    public static List<STR> find(String ownerUri, State state) {
        SolrQuery query = new SolrQuery();
        if (state.getCurrent() == State.ALL) {
            if (null == ownerUri) {
                query.set("q", "owner_uri_str:*");
            } else {
                query.set("q", "owner_uri_str:\"" + ownerUri + "\"");
            }
        } else if (state.getCurrent() == State.ACTIVE) {
            if (null == ownerUri) {
                query.set("q", "owner_uri_str:* AND ended_at_date:\"9999-12-31T23:59:59.999Z\"");
            } else {
                query.set("q", "owner_uri_str:\"" + ownerUri + "\" AND ended_at_date:\"9999-12-31T23:59:59.999Z\"");
            }
        } else { // it is assumed that state is CLOSED
            if (null == ownerUri) {
                query.set("q", "owner_uri_str:* AND -ended_at_date:\"9999-12-31T23:59:59.999Z\"");
            } else {
                query.set("q", "owner_uri_str:\"" + ownerUri + "\" AND -ended_at_date:\"9999-12-31T23:59:59.999Z\"");
            }
        }
        query.set("sort", "started_at_date asc");
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    public static List<String> findAllAccessibleDataAcquisition(String user_uri) {
        List<String> results = new ArrayList<String>();
        List<String> accessLevels = new ArrayList<String>();

        User user = User.find(user_uri);
        if (null != user) {
            user.getGroupNames(accessLevels);
        }

        for (STR acquisition : findAll()) {
            if (acquisition.getPermissionUri().equals("Public") || acquisition.getPermissionUri().equals(user_uri)
                    || acquisition.getOwnerUri().equals(user_uri)) {
                results.add(acquisition.getUri());
                continue;
            }

            for (String level : accessLevels) {
                if (acquisition.getPermissionUri().equals(level)) {
                    results.add(acquisition.getUri());
                }
            }
        }

        return results;
    }

    public static List<STR> findAll() {
        SolrQuery query = new SolrQuery();
        query.set("q", "owner_uri_str:*");
        query.set("sort", "started_at_date asc");
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    public static List<STR> findAll(State state) {
        return find(null, state);
    }

    public static List<STR> find(String ownerUri) {
        SolrQuery query = new SolrQuery();
        query.set("q", "owner_uri_str:\"" + ownerUri + "\"");
        query.set("sort", "started_at_date asc");
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    public static STR findDataAcquisition(SolrQuery query) {
        STR dataAcquisition = null;
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)).build();

        try {
            QueryResponse queryResponse = solr.query(query);
            solr.close();
            SolrDocumentList list = queryResponse.getResults();
            if (list.size() == 1) {
                dataAcquisition = convertFromSolr(list.get(0));
            }
        } catch (Exception e) {
            System.out.println("[ERROR] DataAcquisition.find(SolrQuery) - Exception message: " + e.getMessage());
        }

        return dataAcquisition;
    }

    public static STR findByUri(String dataAcquisitionUri) {
        SolrQuery query = new SolrQuery();
        query.set("q", "uri:\"" + dataAcquisitionUri + "\"");
        query.set("sort", "started_at_date asc");
        query.set("rows", "10000000");

        List<STR> results = findByQuery(query);
        if (!results.isEmpty()) {
            return results.get(0);
        }

        return null;
    }

    public void close(String endedAt) {
        setEndedAtXsd(endedAt);
        saveToSolr();
    }

    public boolean deleteMeasurementData() {
        Iterator<String> iter = datasetURIs.iterator();
        while (iter.hasNext()) {
            if (Measurement.deleteFromSolr(iter.next()) == 0) {
                iter.remove();
            }
        }

        return datasetURIs.isEmpty();
    }

    public static List<STR> findByQuery(SolrQuery query) {
        List<STR> results = new ArrayList<STR>();

        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)).build();

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList docs = response.getResults();
            Iterator<SolrDocument> i = docs.iterator();
            while (i.hasNext()) {
                results.add(convertFromSolr(i.next()));
            }
        } catch (Exception e) {
            results.clear();
            System.out.println("[ERROR] DataAcquisition.findByQuery(SolrQuery) - Exception message: " + e.getMessage());
            e.printStackTrace();
        }

        return results;
    }

    public static List<STR> find(Deployment deployment, boolean active) {
        SolrQuery query = new SolrQuery();
        query.set("q", "deployment_uri_str:\"" + deployment.getUri() + "\"");
        query.set("sort", "started_at_date desc");
        query.set("rows", "10000000");
        List<STR> listDA = findByQuery(query);

        if (active == true) {
            // Filter out inactive data acquisition
            Iterator<STR> iterDA = listDA.iterator();
            while (iterDA.hasNext()) {
                STR dataAcquisition = iterDA.next();
                if (dataAcquisition.isFinished() != false) {
                    iterDA.remove();
                }
            }
        }

        return listDA;
    }

    public static STR find(HADataC hadatac) {
        SolrQuery query = new SolrQuery("uri:\"" + hadatac.getDataAcquisitionKbUri() + "\"");
        return findDataAcquisition(query);
    }

    public static STR create(HADataC hadatacCcsv, HADataC hadatacKb) {
        STR dataAcquisition = new STR();
        DataAcquisitionSchema schema = DataAcquisitionSchema.find(hadatacKb.getDataAcquisitionKbUri());

        dataAcquisition.setLocalName(hadatacCcsv.getDataAcquisition().getLocalName());
        dataAcquisition.setUri(hadatacCcsv.getDataAcquisitionKbUri());
        dataAcquisition.setStudyUri(hadatacCcsv.getDataAcquisition().getStudyUri());
        dataAcquisition.setStartedAtXsd(hadatacCcsv.getDataAcquisition().getStartedAtXsd());
        dataAcquisition.setEndedAtXsd(hadatacCcsv.getDataAcquisition().getEndedAtXsd());
        if (schema != null && schema.getAttributes() != null) {
            Iterator<DataAcquisitionSchemaAttribute> i = schema.getAttributes().iterator();
            while (i.hasNext()) {
                DataAcquisitionSchemaAttribute dasa = i.next();
                for (String attr : dasa.getAttributeLabels()) {
                    dataAcquisition.addCharacteristic(attr);
                }
                for (String attr : dasa.getAttributes()) {
                    dataAcquisition.addCharacteristicUri(attr);
                }
                dataAcquisition.addEntity(dasa.getEntityLabel());
                dataAcquisition.addEntityUri(dasa.getEntity());
                dataAcquisition.addUnit(dasa.getUnitLabel());
                dataAcquisition.addUnitUri(dasa.getUnit());
            }
        }
        dataAcquisition.setDeploymentUri(hadatacKb.getDeploymentUri());
        dataAcquisition.setInstrumentModel(hadatacKb.getDeployment().getInstrument().getLabel());
        dataAcquisition.setInstrumentUri(hadatacKb.getDeployment().getInstrument().getUri());
        dataAcquisition.setPlatformName(hadatacKb.getDeployment().getPlatform().getLabel());
        dataAcquisition.setPlatformUri(hadatacKb.getDeployment().getPlatform().getUri());
        dataAcquisition.setLocation(hadatacKb.getDeployment().getPlatform().getLocation());
        dataAcquisition.setElevation(hadatacKb.getDeployment().getPlatform().getElevation());
        dataAcquisition.addDatasetUri(hadatacCcsv.getDatasetKbUri());

        return dataAcquisition;
    }

    public static String getProperDataAcquisitionUri(String fileName) {
        String base_name = FilenameUtils.getBaseName(fileName);
        List<STR> da_list = findAll();

        // Use the longest match
        String daUri = "";
        int matchedQNameLength = 0;
        for (STR da : da_list) {
            String abbrevUri = URIUtils.replaceNameSpaceEx(da.getUri());
            String qname = abbrevUri.split(":")[1];
            if (base_name.startsWith(qname)) {
                if (qname.length() > matchedQNameLength) {
                    matchedQNameLength = qname.length();
                    daUri = da.getUri();
                }
            }
        }

        if (!daUri.isEmpty()) {
            return daUri;
        }

        return null;
    }

    public void merge(STR dataCollection) {
        Iterator<String> i;

        i = dataCollection.unit.iterator();
        while (i.hasNext()) {
            addUnit(i.next());
        }
        i = dataCollection.unitUri.iterator();
        while (i.hasNext()) {
            addUnitUri(i.next());
        }
        i = dataCollection.entity.iterator();
        while (i.hasNext()) {
            addEntity(i.next());
        }
        i = dataCollection.entityUri.iterator();
        while (i.hasNext()) {
            addEntityUri(i.next());
        }
        i = dataCollection.characteristic.iterator();
        while (i.hasNext()) {
            addCharacteristic(i.next());
        }
        i = dataCollection.characteristicUri.iterator();
        while (i.hasNext()) {
            addCharacteristicUri(i.next());
        }
        i = dataCollection.datasetURIs.iterator();
        while (i.hasNext()) {
            addDatasetUri(i.next());
        }
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        Iterator<String> i;
        builder.append("localName: " + this.localName + "\n");
        builder.append("uri: " + this.getUri() + "\n");
        builder.append("started_at: " + this.getStartedAt() + "\n");
        builder.append("ended_at: " + this.getEndedAt() + "\n");
        i = unit.iterator();
        while (i.hasNext()) {
            builder.append("unit: " + i.next() + "\n");
        }
        i = unitUri.iterator();
        while (i.hasNext()) {
            builder.append("unit_uri: " + i.next() + "\n");
        }
        i = characteristic.iterator();
        while (i.hasNext()) {
            builder.append("characteristic: " + i.next() + "\n");
        }
        i = characteristicUri.iterator();
        while (i.hasNext()) {
            builder.append("characteristic_uri: " + i.next() + "\n");
        }
        i = entity.iterator();
        while (i.hasNext()) {
            builder.append("entity: " + i.next() + "\n");
        }
        i = entityUri.iterator();
        while (i.hasNext()) {
            builder.append("entity_uri: " + i.next() + "\n");
        }
        builder.append("deployment_uri: " + this.deploymentUri + "\n");
        builder.append("instrument_model: " + this.instrumentModel + "\n");
        builder.append("instrument_uri: " + this.instrumentUri + "\n");
        builder.append("platform_name: " + this.platformName + "\n");
        builder.append("platform_uri: " + this.platformUri + "\n");
        builder.append("location: " + this.location + "\n");
        builder.append("elevation: " + this.elevation + "\n");
        // builder.append("rowScopeUri: " + this.rowScopeUri + "\n");
        // builder.append("rowScopeName: " + this.rowScopeName + "\n");
        for (String cellUri : cellScopeUri) {
            builder.append("cellScopeUri: " + cellUri + "\n");
        }
        for (String cellName : cellScopeName) {
            builder.append("cellScopeName: " + cellName + "\n");
        }
        i = datasetURIs.iterator();
        while (i.hasNext()) {
            builder.append("dataset_uri: " + i.next() + "\n");
        }

        return builder.toString();
    }

    @Override
    public int saveToLabKey(String userName, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(userName, password);

        List<String> abbrevTypeURIs = new ArrayList<String>();
        for (String uri : getTypeURIs()) {
            abbrevTypeURIs.add(URIUtils.replaceNameSpaceEx(uri));
        }
        List<String> abbrevAssociatedURIs = new ArrayList<String>();
        for (String uri : getAssociatedURIs()) {
            abbrevAssociatedURIs.add(URIUtils.replaceNameSpaceEx(uri));
        }

        String cellUri = "";
        int totalChanged = 0;
        Iterator<String> i = getCellScopeUri().iterator();
        while (i.hasNext()) {
            cellUri += URIUtils.replaceNameSpaceEx(i.next());
            if (i.hasNext()) {
                cellUri += " , ";
            }
        }
        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("a", String.join(", ", abbrevTypeURIs));
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("rdfs:label", getLabel());
        row.put("rdfs:comment", getComment());
        row.put("prov:startedAtTime", getStartedAt());
        row.put("prov:used", getParameter());
        row.put("prov:wasAssociatedWith", String.join(", ", abbrevAssociatedURIs));
        row.put("hasco:hasDeployment", URIUtils.replaceNameSpaceEx(getDeploymentUri()));
        row.put("hasco:isDataAcquisitionOf", URIUtils.replaceNameSpaceEx(getStudyUri()));
        row.put("hasco:hasSchema", URIUtils.replaceNameSpaceEx(getSchemaUri()));
        // row.put("hasco:hasRowScope",
        // URIUtils.replaceNameSpaceEx(getRowScopeUri()));
        row.put("hasco:hasCellScope", cellUri);
        row.put("hasco:hasTriggeringEvent", getTriggeringEventName());
        row.put("prov:endedAtTime", getEndedAt().startsWith("9999") ? "" : getEndedAt());
        rows.add(row);

        try {
            totalChanged = loader.insertRows("DataAcquisition", rows);
        } catch (CommandException e) {
            try {
                totalChanged = loader.updateRows("DataAcquisition", rows);
            } catch (CommandException e2) {
                System.out.println("[ERROR] Could not insert or update Stream Specification");
            }
        }

        return totalChanged;
    }

    @Override
    public int deleteFromLabKey(String userName, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(userName, password);

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        rows.add(row);

        try {
            return loader.deleteRows("DataAcquisition", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete Stream Specification(s)");
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public boolean saveToTripleStore() {
        return false;
    }

    @Override
    public void deleteFromTripleStore() {
    }
}
