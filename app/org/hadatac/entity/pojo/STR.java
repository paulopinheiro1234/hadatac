package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

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
import org.hadatac.console.controllers.annotator.AnnotationLogger;
import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Facetable;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.RecordFile;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.State;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import io.ebeaninternal.server.lib.util.Str;

public class STR extends HADatAcThing implements Comparable<STR> {
    private static final String className = "hasco:DataAcquisition";

    @Field("uri")
    private String uri;
    @Field("label_str")
    private String label;
    @Field("comment_str")
    private String comment;
    //@Field("used_uri_str_multi")
    //private String used_uri;

    private DateTime startedAt;
    private DateTime endedAt;

    @Field("owner_uri_str")
    private String ownerUri;
    @Field("version_str")
    private String version;
    @Field("permission_uri_str")
    private String permissionUri;
    @Field("parameter_str")
    private String parameter;
    @Field("triggering_event_int")
    private int triggeringEvent;
    @Field("nr_data_points_long")
    private long numberDataPoints;
    
    @Field("total_messages_long")
    private long totalMessages;
    @Field("ingested_messages_long")
    private long ingestedMessages;
    @Field("message_protocol_str")
    private String messageProtocol;
    @Field("message_ip_str")
    private String messageIP;
    @Field("message_port_str")
    private String messagePort;
    @Field("message_headers_str")
    private String messageHeaders;
    @Field("message_archive_id_str")
    private String messageArchiveId;

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
    @Field("localscope_uri_str_multi")
    private List<String> cellScopeUri;
    @Field("localscope_name_str_multi")
    private List<String> cellScopeName;

    /*
     * Possible values for message status:
     * ACTIVE:     It is not closed and it is collecting data
     * SUSPENDED:  It is not closed but it is not collecting data
     * CLOSED:     It is not collecting data. It is no longer available 
     *             for data collection 
     */
    @Field("message_status_str")
    private String messageStatus;

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
    @Field("status_int")
    private int status;
    
    private boolean isComplete;
    private String localName;
    private Study study;
    private Deployment deployment;
    private DataAcquisitionSchema sdd;
    private Map<String,MessageTopic> topicsMap;
	private List<String> headers;

    private DataFile archive = null;
    private String log;
    private AnnotationLogger logger = null;

    public static final String ACTIVE = "ACTIVE";
    public static final String SUSPENDED = "SUSPENDED";
    public static final String CLOSED = "CLOSED";

    public static final String MQTT = "mqtt";
    public static final String HTTP = "http";

    public STR() {
        startedAt = null;
        endedAt = null;
        numberDataPoints = 0;
        isComplete = false;
        datasetURIs = new ArrayList<String>();
        totalMessages = 0;
        ingestedMessages = 0;
        messageProtocol = null;
        messageIP = null;
        messagePort = null;
        topicsMap = null;
        study = null;
        sdd = null;
        deployment = null;
        headers = new ArrayList<String>();
        cellScopeUri = new ArrayList<String>();
        cellScopeName = new ArrayList<String>();
        logger = new AnnotationLogger(this);
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

    @Override
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

    public long getNumberDataPoints() {
        return numberDataPoints;
    }

    public void setNumberDataPoints(long numberDataPoints) {
        this.numberDataPoints = numberDataPoints;
    }

    public long getTotalMessages() {
        return totalMessages;
    }

    public void setTotalMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public long getIngestedMessages() {
        return totalMessages;
    }

    public void setIngestedMessages(long totalMessages) {
        this.totalMessages = totalMessages;
    }

    public String getMessageProtocol() {
        return messageProtocol;
    }

    public void setMessageProtocol(String messageProtocol) {
        this.messageProtocol = messageProtocol;
    }

    public String getMessageIP() {
        return messageIP;
    }

    public void setMessageIP(String messageIP) {
        this.messageIP = messageIP;
    }

    public String getMessagePort() {
        return messagePort;
    }

    public void setMessagePort(String messagePort) {
        this.messagePort = messagePort;
    }

    public String getMessageName() {
    	if (label == null && label.isEmpty()) {
    		return "";
    	}
    	if (messagePort == null || messagePort.isEmpty()) {
        	return label + "_at_" + messageIP; 
    	}
    	return label + "_at_" + messageIP + "_" + messagePort; 
    }

    public String getMessageArchiveId() {
        return messageArchiveId;
    }

    public void setMessageArchiveId(String messageArchiveId) {
        this.messageArchiveId = messageArchiveId;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
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

    public String getLog() {
        return getMessageLogger().getLog();
    }
    public void setLog(String log) {
        getMessageLogger().setLog(log);
        this.log = log;
    }

    public AnnotationLogger getMessageLogger() {
        return logger;
    }
    public void setMessageLogger(AnnotationLogger logger) {
        this.logger = logger;
    }
    
    /*
    public String getUsedUri() {
        return used_uri;
    }

    public void setUsedUri(String used_uri) {
        this.used_uri = used_uri;
    }
	*/

    public String getStudyUri() {
        return studyUri;
    }

    public Study getStudy() {
        if (studyUri == null || studyUri.equals(""))
            return null;
        study = Study.find(studyUri);
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
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
    	if (sdd != null) {
    		return sdd;
    	}
    	if (schemaUri == null || schemaUri.equals("")) {
    		return null;
    	}
    	DataAcquisitionSchema schema = DataAcquisitionSchema.find(schemaUri);
        headers = new ArrayList<String>();
        if (schema != null && schema.getAttributes() != null) {
        	for (DataAcquisitionSchemaAttribute attr : schema.getAttributes()) {
        		headers.add(attr.getLabel());
        	}
        }
        setHeaders(headers.toString());
    	return schema;
    }
    
    public void setSchemaUri(String schemaUri) {
        this.schemaUri = schemaUri;
        getSchema();
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

    private void loadTopicsMap() {
		List<MessageTopic> topics = MessageTopic.findByStream(uri);
		if (topics != null) {
			topicsMap = new HashMap<String, MessageTopic>(); 
			for (MessageTopic topic : topics) {
				topic.cacheTopic();
				topicsMap.put(topic.getLabel(), topic);
			}
		}    	
    }
    
    public Map<String,MessageTopic> getTopicsMap() {
		if (topicsMap != null) {
			return topicsMap;
		}
		loadTopicsMap();
		return topicsMap; 
	};
	
	public List<MessageTopic> getTopicsList() {
		if (topicsMap != null) {
			return new ArrayList<MessageTopic>(topicsMap.values());
		}
		loadTopicsMap();
		if (topicsMap != null) {
			return new ArrayList<MessageTopic>(topicsMap.values());			
		}
		return new ArrayList<MessageTopic>();
	}
	
	public void resetTopicsMap() {
		topicsMap = null;
	}

    public List<String> getHeaders() {
    	if (headers != null) {
    		return headers;
    	}
    	List<String> headers = new ArrayList<String>();
    	if (messageHeaders == null || messageHeaders.isEmpty()) {
    		return headers;
    	}
    	String auxstr = messageHeaders.replace("[","").replace("]","");
    	StringTokenizer str = new StringTokenizer(auxstr,","); 
        while (str.hasMoreTokens()) {
        	headers.add(str.nextToken().trim()); 
        }
        return headers;
    }
    
    private void setHeaders(String headersStr) {
        this.messageHeaders = headersStr;
        getHeaders();
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
    }

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

    public void addNumberDataPoints(long number) {
        numberDataPoints += number;
    }

    public List<String> getDOIs() {
    	List<String> resp = new ArrayList<String>();
    	List<DataFile> dfs = DataFile.findByDataAcquisition(uri);
    	System.out.println("STR da's uri is [" + uri + "]  and dfs's size is [" + dfs.size() + "]");
    	for (DataFile df : dfs) {
        	System.out.println("STR df's wasDerivedFrom size is [" + df.getWasDerivedFrom().size() + "]");
    		for (String doi : df.getWasDerivedFrom()) {
    			resp.add(doi);
    		}
    	}
    	return resp;
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
            if (doc.getFieldValue("version_str") != null) {
                dataAcquisition.setVersion(doc.getFieldValue("version_str").toString());
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
            if (doc.getFieldValue("total_messages_long") != null) {
                dataAcquisition
                        .setTotalMessages(Long.parseLong(doc.getFieldValue("total_messages_long").toString()));
            }
            if (doc.getFieldValue("ingested_messages_long") != null) {
                dataAcquisition
                        .setIngestedMessages(Long.parseLong(doc.getFieldValue("ingested_messages_long").toString()));
            }
            if (doc.getFieldValue("message_protocol_str") != null) {
                dataAcquisition.setMessageProtocol(doc.getFieldValue("message_protocol_str").toString());
            }
            if (doc.getFieldValue("message_ip_str") != null) {
                dataAcquisition.setMessageIP(doc.getFieldValue("message_ip_str").toString());
            }
            if (doc.getFieldValue("message_port_str") != null) {
                dataAcquisition.setMessagePort(doc.getFieldValue("message_port_str").toString());
            }
            if (doc.getFieldValue("message_headers_str") != null) {
                dataAcquisition.setHeaders(doc.getFieldValue("message_headers_str").toString());
            }
            if (doc.getFieldValue("message_status_str") != null) {
                dataAcquisition.setMessageStatus(doc.getFieldValue("message_status_str").toString());
            }
            if (doc.getFieldValue("message_archive_id_str") != null) {
                dataAcquisition.setMessageArchiveId(doc.getFieldValue("message_archive_id_str").toString());
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
            if (doc.getFieldValue("location_latlong") != null) {
                dataAcquisition.setLocation(doc.getFieldValue("location_latlong").toString());
            }
            if (doc.getFieldValue("elevation_str") != null) {
                dataAcquisition.setElevation(doc.getFieldValue("elevation_str").toString());
            }
            if (doc.getFieldValues("dataset_uri_str_multi") != null) {
                i = doc.getFieldValues("dataset_uri_str_multi").iterator();
                while (i.hasNext()) {
                    dataAcquisition.addDatasetUri(i.next().toString());
                }
            }
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

        for (STR str : findAll()) {
        	if (str == null || str.getUri() == null) {
        		continue;
        	}
        	if (str.getPermissionUri() == null) {
        		System.out.println("[ERROR] PermissionUri for STR " + str.getUri() + "is missing");
        		continue;
        	}
        	if (str.getOwnerUri() == null) {
        		System.out.println("[ERROR] OwnerUri for STR " + str.getUri() + "is missing");
        		continue;
        	}
            if (str.getPermissionUri().equals("Public") || str.getPermissionUri().equals(user_uri)
                    || str.getOwnerUri().equals(user_uri)) {
                results.add(str.getUri());
                continue;
            }

            for (String level : accessLevels) {
                if (str.getPermissionUri().equals(level)) {
                    results.add(str.getUri());
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

    public static List<STR> findStreams() {
        SolrQuery query = new SolrQuery();
        query.set("q", "message_ip_str:*");
        query.set("rows", "10000000");

        return findByQuery(query);
    }

    /* Open streams are those with ended_at_date =  9999-12-31T23:59:59.999Z */
    public static List<STR> findOpenStreams() {
        SolrQuery query = new SolrQuery();
        query.set("q", "message_ip_str:* AND ended_at_date:\"9999-12-31T23:59:59.999Z\" AND -message_status_str:\"CLOSED\"");
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
        builder.append("deployment_uri: " + this.deploymentUri + "\n");
        builder.append("instrument_model: " + this.instrumentModel + "\n");
        builder.append("instrument_uri: " + this.instrumentUri + "\n");
        builder.append("platform_name: " + this.platformName + "\n");
        builder.append("platform_uri: " + this.platformUri + "\n");
        builder.append("location: " + this.location + "\n");
        builder.append("elevation: " + this.elevation + "\n");
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
    public boolean saveToTripleStore() {
        return false;
    }

    @Override
    public void deleteFromTripleStore() {
    }
        
}
