package org.hadatac.entity.pojo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.RangeFacet;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.util.NamedList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.utils.Collections;

import play.Play;

public class Measurement {
	@Field("uri")
	private String uri;
	@Field("owner_uri")
	private String ownerUri;
	@Field("acquisition_uri")
	private String acquisitionUri;
	@Field("study_uri")
	private String studyUri;
	@Field("object_uri")
	private String objectUri;
	private Instant timestamp;
	@Field("named_time")
	private String abstractTime;
	@Field("value")
	private String value;
	@Field("pid")
	private String pid;
	@Field("sid")
	private String sid;
	@Field("unit")
	private String unit;
	@Field("unit_uri")
	private String unitUri;
	@Field("entity")
	private String entity;
	@Field("entity_uri")
	private String entityUri;
	@Field("characteristic")
	private String characteristic;
	@Field("characteristic_uri")
	private String characteristicUri;
	/*
	@Field("indicator")
	private String indicator;
	@Field("indicator_uri")
	private String indicatorUri;
	*/
	@Field("instrument_model")
	private String instrumentModel;
	@Field("instrument_uri")
	private String instrumentUri;
	@Field("platform_name")
	private String platformName;
	@Field("platform_uri")
	private String platformUri;
	@Field("location")
	private String location;
	@Field("elevation")
	private double elevation;
	@Field("dataset_uri")
	private String datasetUri;

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

    /*	public String getObjectPID() {
		
//		this.objectUri = objectUri;
		
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?pid WHERE {"
                + " <" + objectUri + "> rdf:type <http://semanticscience.org/resource/Human> . "
                + " <" + objectUri + "> <http://hadatac.org/ont/hasco/originalID> ?pid . "       
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            try{
		            if (null != soln.getLiteral("pid")) {
		            	String pid = soln.getLiteral("pid").toString();
		            	if (!pid.equals("")) {
		            		return pid;
		            	}
		            }
		        } catch (Exception e1) {
		        	return "";
		        }
            } else {
        	String queryString2 = NameSpaces.getInstance().printSparqlNameSpaceList()
                    + " SELECT ?pid WHERE {"
                    + " <" + objectUri + "> <http://hadatac.org/ont/hasco/isSampleOf> ?sub . "
                    + " ?sub <http://hadatac.org/ont/hasco/originalID> ?pid . "       
                    + " }";
            
            Query query2 = QueryFactory.create(queryString2);
            QueryExecution qexec2 = QueryExecutionFactory.sparqlService(
                    Collections.getCollectionsName(Collections.METADATA_SPARQL), query2);
            ResultSet results2 = qexec2.execSelect();
            ResultSetRewindable resultsrw2 = ResultSetFactory.copyResults(results2);
            qexec2.close();
            
            if (resultsrw2.size() > 0) {
                QuerySolution soln2 = resultsrw2.next();
                try{
	                if (null != soln2.getLiteral("pid")) {
	                	String pid = soln2.getLiteral("pid").toString();
	                	if (!pid.equals("")) {
	                		return pid;
	                	}
	                }
                } catch (Exception e1) {
                	return "";
                }
            }
            }
            return "";
	}
	
    	public String getObjectSID() {
		
//		this.objectUri = objectUri;
		
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT ?sid WHERE {"
                + " <" + objectUri + "> <http://hadatac.org/ont/hasco/originalID> ?sid . "  
                + " <" + objectUri + "> <http://hadatac.org/ont/hasco/isObjectOf> ?sc . "
                + " ?sc	rdf:type <http://hadatac.org/ont/hasco/SampleCollection> . "
                + " }";
        
        Query query = QueryFactory.create(queryString);
        QueryExecution qexec = QueryExecutionFactory.sparqlService(
                Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
        ResultSet results = qexec.execSelect();
        ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
        qexec.close();
        
        if (resultsrw.size() > 0) {
            QuerySolution soln = resultsrw.next();
            try{
	            if (null != soln.getLiteral("sid")) {
	            	String sid = soln.getLiteral("sid").toString();
	            	if (!sid.equals("")) {
	            		return sid;
	            	}
	            }
            } catch (Exception e1) {
            	return "";
            }
        }
        
        return "";
	}*/

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

	public String getTimestamp() {
		return timestamp.toString();
	}

	@Field("timestamp")
	public void setTimestamp(String timestamp) {
		this.timestamp = Instant.parse(timestamp);
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

	/*
	public String getIndicator() {
		return indicator;
	}

	public void setIndicator(String indicator) {
		this.indicator = indicator;
	}

	public String getIndicatorUri() {
		return indicatorUri;
	}

	public void setIndicatorUri(String indicatorUri) {
		this.indicatorUri = indicatorUri;
	}
	*/

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

	public int save() {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Measurement.save - e.Message: " + e.getMessage());
			return -1;
		}
	}

	public static int delete(String datasetUri) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		try {
			UpdateResponse response = solr.deleteByQuery("dataset_uri:\"" + datasetUri + "\"");
			solr.commit();
			solr.close();
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
			acquisition_query += "acquisition_uri" + ":\"" + uri + "\"";
			if (iter_uri.hasNext()) {
				acquisition_query += " OR ";
			}
		}

		if (acquisition_query.equals("")) {
			return "";
		}

		if (!study_uri.equals("")) {
			facet_query += "study_uri" + ":\"" + study_uri + "\"";
		}

		if (!subject_uri.equals("")) {
			if (!study_uri.equals("")) {
				facet_query += " AND ";
			}
			facet_query += "object_uri" + ":\"" + subject_uri + "\"";
		}

		if (!char_uri.equals("")) {
			if (!study_uri.equals("") || !subject_uri.equals("")) {
				facet_query += " AND ";
			}
			facet_query += "characteristic_uri" + ":\"" + char_uri + "\"";
		}

		if (facet_query.trim().equals("")) {
			q = acquisition_query;
		} else {
			q = "(" + acquisition_query + ") AND (" + facet_query + ")";
		}

		return q;
	}

	public static String buildQuery(String user_uri, int page, int qtd, FacetHandler handler) {
		String acquisition_query = "";
		String facet_query = "";
		String q = "";

		List<String> listURI = DataAcquisition.findAllAccessibleDataAcquisition(user_uri);
		Iterator<String> iter_uri = listURI.iterator();
		while (iter_uri.hasNext()) {
			acquisition_query += "acquisition_uri:\"" + iter_uri.next() + "\"";
			if (iter_uri.hasNext()) {
				acquisition_query += " OR ";
			}
		}

		// System.out.println("User URI: " + user_uri + " acquistion_qeury: <<"
		// + acquisition_query + ">>");
		if (acquisition_query.equals("")) {
			return "";
		}

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
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			if (bNumberOfResultsOnly) {
				result.setDocumentSize(results.getNumFound());
			} else {
				Iterator<SolrDocument> m = results.iterator();
				while (m.hasNext()) {
					result.documents.add(convertFromSolr(m.next()));
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

	public static Instant findMinTime(String field, String q) {
		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		query.setRows(1);
		query.addSort(field, SolrQuery.ORDER.asc);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();

			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();

			SolrDocumentList results = queryResponse.getResults();
			if (results.size() == 1) {
				Measurement m = convertFromSolr(results.get(0));
				return Instant.parse(m.getTimestamp());
			}
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - IOException message: " + e.getMessage());
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - SolrServerException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - Exception message: " + e.getMessage());
		}

		return null;
	}

	public static Instant findMaxTime(String field, String q) {
		SolrQuery query = new SolrQuery();
		query.setQuery(q);
		query.setRows(1);
		query.set(field, "[* TO NOW]");
		query.addSort(field, SolrQuery.ORDER.desc);

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();

			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();

			SolrDocumentList results = queryResponse.getResults();
			if (results.size() == 1) {
				Measurement m = convertFromSolr(results.get(0));
				return Instant.parse(m.getTimestamp());
			}
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - IOException message: " + e.getMessage());
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - SolrServerException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findMinTime(String, String) - Exception message: " + e.getMessage());
		}

		return null;
	}

	public static String calculateTimeGap(Instant min, Instant max) {

	    if (min == null || max == null) {
		return "+1MINUTE";
	    }

		Duration duration = Duration.between(min, max);

		long days = duration.toDays();
		long weeks = days / 7;
		long months = days / 30;
		long years = days / 365;

		if (years > 2) {
			return "+1YEAR";
		}
		if (months > 4) {
			return "+1MONTH";
		}
		if (weeks > 4) {
			return "+1WEEK";
		}
		if (days > 4) {
			return "+1DAY";
		}

		long hours = duration.toHours();

		if (hours > 4) {
			return "+1HOUR";
		}

		long minutes = duration.toMinutes();

		if (minutes > 4) {
			return "+1MINUTE";
		}

		return "+1MINUTE";
	}

	public static AcquisitionQueryResult find(String user_uri, int page, int qtd, FacetHandler handler) {
		AcquisitionQueryResult result = new AcquisitionQueryResult();

		String q = buildQuery(user_uri, page, qtd, handler);
		/*
		 * an empty query happens when current user is not allowed to see any
		 * data acquisition
		 */
		if (q.equals("")) {
			return result;
		}

		Instant minTime = findMinTime("timestamp", q);
		Instant maxTime = findMaxTime("timestamp", q);

		String gap = calculateTimeGap(minTime, maxTime);

		int docSize = 0;
		SolrQuery query = new SolrQuery();
		//System.out.println("q: " + q);
		query.setQuery(q);
		query.setStart((page - 1) * qtd + 1);
		//System.out.println("Starting at: " + ((page - 1) * qtd + 1) + "    page: " + page + "     qtd: " + qtd);
		query.setRows(qtd);
		query.setFacet(true);
		query.setFacetLimit(-1);
		query.addFacetField("unit");
		if (minTime != null && maxTime != null && gap == null) {
		    query.addDateRangeFacet("timestamp", Date.from(minTime), Date.from(maxTime), gap); 
		}
		query.addFacetField("named_time");
		query.addFacetPivotField("study_uri,acquisition_uri");
		query.addFacetPivotField("entity,characteristic");
		query.addFacetPivotField("platform_name,instrument_model");
		/*
		query.setParam("wt", "json");
		query.setParam("json.facet", "{ "
				+ "entity:{ "
				+ "type: terms, "
				+ "field: entity,"
				+ "limit: 1000, "
				+ "facet:{ "
				+ "indicator: { type : terms,"
				+ "field: indicator,"
				+ "limit: 1000,"
				+ "facet:{"
				+ "characteristic: {"
				+ "type : terms,"
				+ "field: characteristic,"
				+ "limit: 1000 }}}}}}");
		*/
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					Play.application().configuration().getString("hadatac.solr.data") 
					+ Collections.DATA_ACQUISITION).build();
			System.out.println("!!!! QUERY: " + query.toQueryString());
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			SolrDocumentList results = queryResponse.getResults();
			Iterator<SolrDocument> m = results.iterator();
			while (m.hasNext()) {
				result.documents.add(convertFromSolr(m.next()));
			}

			if (queryResponse.getFacetFields() != null) {
				Iterator<FacetField> f = queryResponse.getFacetFields().iterator();
				while (f.hasNext()) {
					FacetField field = f.next();
					result.field_facets.put(field.getName(), new HashMap<String, Long>());
					Iterator<Count> v = field.getValues().iterator();
					while (v.hasNext()) {
						Count count = v.next();
						Map<String, Long> map = result.field_facets.get(field.getName());
						map.put(count.getName(), count.getCount());
					}
				}
			}

			DateTimeFormatter formatter =
					DateTimeFormatter.ofLocalizedDateTime( FormatStyle.SHORT )
					.withLocale( Locale.US )
					.withZone( ZoneId.systemDefault() );

			if (queryResponse.getFacetRanges() != null) {
				Iterator<RangeFacet> f = queryResponse.getFacetRanges().iterator();
				while (f.hasNext()) {
					RangeFacet field = f.next();
					result.date_facets.put(field.getName(), new HashMap<String, Map<String, String>>());

					RangeFacet.Count v1;
					RangeFacet.Count v2;
					Instant v1inst;
					Instant v2inst;
					String rangeQuery;
					String label;
					int i = 0;
					int j = field.getCounts().size();

					while (i < j) {
						v1 = (RangeFacet.Count)field.getCounts().get(i);
						v1inst = Instant.parse(v1.getValue());

						if (i == j-1) {
							v2 = null;
							v2inst = null;
							rangeQuery = "[" + v1.getValue() + " TO *]";
							label = "From " + formatter.format(v1inst) + " onwards";
						} else {
							v2 = (RangeFacet.Count)field.getCounts().get(i+1);
							v2inst = Instant.parse(v2.getValue());
							rangeQuery = "[" + v1.getValue() + " TO " + v2.getValue() + "]";
							label = "From " + formatter.format(v1inst) + " to " + formatter.format(v2inst);
						}

						Map<String, Map<String, String>> map = result.date_facets.get(field.getName());
						map.put(label, new HashMap<String, String>());
						Map<String, String> map2 = map.get(label);
						map2.put("count", Integer.toString(v1.getCount()));
						map2.put("gap", field.getGap().toString());
						map2.put("query", rangeQuery);
						//map2.put("label", label);

						i++;
					}

					/*
					Iterator<RangeFacet.Count> v = field.getCounts().iterator();
					while (v.hasNext()) {
						RangeFacet.Count count = v.next();
						Map<String, Map<String, String>> map = result.date_facets.get(field.getName());
						map.put(count.getValue(), new HashMap<String, String>());
						Map<String, String> map2 = map.get(count.getValue());
						System.out.println("!!!!!!! 2: " + count.getValue() + "   3: " + count.getCount());
						map2.put("count", Integer.toString(count.getCount()));
						map2.put("gap", field.getGap().toString());
						map2.put("text", "text");
					}*/
				}
			}

			if (queryResponse.getFacetPivot() != null) {
				Iterator<Entry<String, List<PivotField>>> iter = queryResponse.getFacetPivot().iterator();

				while (iter.hasNext()) {
					Entry<String, List<PivotField>> entry = iter.next();

					List<Pivot> parents = new ArrayList<Pivot>();
					result.pivot_facets.put(entry.getKey(), parents);
					// System.out.println("PIVOT: " + entry.getKey());

					List<PivotField> listPivotField = entry.getValue();
					// System.out.println("List<PivotField> size: " +
					// listPivotField.size());
					Iterator<PivotField> iterParents = listPivotField.iterator();

					while (iterParents.hasNext()) {
						PivotField pivot = iterParents.next();

						Pivot parent = new Pivot();
						if (pivot.getField().equals("study_uri")) {
							docSize += pivot.getCount();
						}
						parent.field = pivot.getField();
						parent.value = pivot.getValue().toString();
						parent.count = pivot.getCount();
						parents.add(parent);
						// System.out.println("PIVOT FIELD: " +
						// pivot.getField());
						// System.out.println("PIVOT VALUE: " +
						// pivot.getValue().toString());
						// System.out.println("PIVOT COUNT: " +
						// pivot.getCount());

						List<PivotField> subPivotFiled = pivot.getPivot();
						if (null != subPivotFiled) {
							Iterator<PivotField> iterChildren = subPivotFiled.iterator();
							while (iterChildren.hasNext()) {
								pivot = iterChildren.next();
								Pivot child = new Pivot();
								child.field = pivot.getField();
								child.value = pivot.getValue().toString();
								child.count = pivot.getCount();
								parent.children.add(child);
								// System.out.println("PIVOT FIELD: " +
								// pivot.getField());
								// System.out.println("PIVOT VALUE: " +
								// pivot.getValue().toString());
								// System.out.println("PIVOT COUNT: " +
								// pivot.getCount());
							}
						}
					}
				}
			}

			//result.extra_facets = parseFacetResults(queryResponse);

		} catch (SolrServerException e) {
			System.out.println("[ERROR] Measurement.find() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Measurement.find() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println("[ERROR] Measurement.find() - Exception message: " + e.getMessage());
		}

		result.setDocumentSize((long) docSize);

		return result;
	}
	
	@SuppressWarnings("unchecked")
	private static Pivot parseFacetResults(QueryResponse response) {
		if (response.getResponse() != null) {
			if (response.getResponse().get("facets") instanceof NamedList) {
				return parsePivot(((NamedList<Object>)response.getResponse().get("facets")));
			}
		}
		
		return null;
	}
	
	private static Pivot parsePivot(NamedList<Object> objects) {
		Pivot pivot = new Pivot();
		objects.forEach(new BiConsumer<String, Object>() {
			
			@SuppressWarnings("unchecked")
			@Override
			public void accept(String t, Object u) {
				//System.out.println("current t: " + t);
				//System.out.println("current u: " + u.getClass().getName());
				if (t.equals("val")) {
					pivot.value = (String)u;
				} else if (t.equals("count")) {
					pivot.count = (int)u;
				} else {
					if (u instanceof ArrayList<?>) {
						for (NamedList<Object> nl : (ArrayList<NamedList<Object>>) u) {
							pivot.children.add(parsePivot((NamedList<Object>)nl));
						}
					} else if (u instanceof NamedList<?>) {
						pivot.field = (String)t;
						for (NamedList<Object> nl : ((ArrayList<NamedList<Object>>)((NamedList<Object>)u).get("buckets"))) {
							pivot.children.add(parsePivot((NamedList<Object>)nl));
						}
					}
 				}
			}
		});
		
		return pivot;
	}

	public static long getNumByDataAcquisition(DataAcquisition dataAcquisition) {
		SolrClient solr = new HttpSolrClient.Builder(
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri:\"" + dataAcquisition.getUri() + "\"");
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
				if (doc.getFieldValue("dataset_uri") != null) {
					dataAcquisition.addDatasetUri(doc.getFieldValue("dataset_uri").toString());
				}
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
				Play.application().configuration().getString("hadatac.solr.data") 
				+ Collections.DATA_ACQUISITION).build();
		SolrQuery query = new SolrQuery();
		query.set("q", "acquisition_uri:\"" + acquisition_uri + "\"");
		query.set("rows", "10000000");

		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				Measurement measurement = convertFromSolr(i.next());
				listMeasurement.add(measurement);
			}
		} catch (Exception e) {
			System.out.println("[ERROR] Measurement.findByDataAcquisitionUri(acquisition_uri) - Exception message: "
					+ e.getMessage());
		}

		return listMeasurement;
	}

	public static Measurement convertFromSolr(SolrDocument doc) {
		Measurement m = new Measurement();
		m.setUri(doc.getFieldValue("uri").toString());
		m.setOwnerUri(doc.getFieldValue("owner_uri").toString());
		m.setAcquisitionUri(doc.getFieldValue("acquisition_uri").toString());
		m.setStudyUri(doc.getFieldValue("study_uri").toString());
		if (doc.getFieldValue("object_uri") != null) {
			m.setObjectUri(doc.getFieldValue("object_uri").toString());
			m.setPID(doc.getFieldValue("object_uri").toString());
			m.setSID(doc.getFieldValue("object_uri").toString());
		}
		if (doc.getFieldValue("timestamp") != null) {
			m.setTimestamp(((Date)doc.getFieldValue("timestamp")).toInstant().toString());
		}
		m.setAbstractTime(doc.getFieldValue("named_time").toString());
		m.setValue(doc.getFieldValue("value").toString());
		m.setUnit(doc.getFieldValue("unit").toString());
		m.setUnitUri(doc.getFieldValue("unit_uri").toString());
		m.setEntity(doc.getFieldValue("entity").toString());
		m.setEntityUri(doc.getFieldValue("entity_uri").toString());
		m.setCharacteristic(doc.getFieldValue("characteristic").toString());
		m.setCharacteristicUri(doc.getFieldValue("characteristic_uri").toString());
		//m.setIndicator(doc.getFieldValue("indicator").toString());
		//m.setIndicatorUri(doc.getFieldValue("indicator_uri").toString());
		m.setInstrumentModel(doc.getFieldValue("instrument_model").toString());
		m.setInstrumentUri(doc.getFieldValue("instrument_uri").toString());
		m.setPlatformName(doc.getFieldValue("platform_name").toString());
		m.setPlatformUri(doc.getFieldValue("platform_uri").toString());
		if (doc.getFieldValue("location") != null) {
			m.setLocation(doc.getFieldValue("location").toString());
		}
		if (doc.getFieldValue("elevation") != null) {
			m.setElevation(Double.parseDouble(doc.getFieldValue("elevation").toString()));
		}
		m.setDatasetUri(doc.getFieldValue("dataset_uri").toString());

		return m;
	}
}