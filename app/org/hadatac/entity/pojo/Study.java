package org.hadatac.entity.pojo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.FacetField;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.client.solrj.response.FacetField.Count;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.data.model.MetadataAcquisitionQueryResult;
import org.hadatac.utils.Collections;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

import play.Play;

public class Study {
	private DateTime startedAt;
	private DateTime endedAt;
	
	@Field("studyUri")
	private String studyUri;
	@Field("permission_uri")
	private String permissionUri;
	@Field("studyLabel")
	private String label;
	@Field("proj")
	private String project;
	@Field("studyComment")
	private String comment;
	@Field("institutionName")
	private String institution;
	@Field("agentName")
	private String agent;
	
	/*@Field("uri")
	private String uri;
	@Field("permission_uri")
	private String permissionUri;
	@Field("name")
	private String name;
	@Field("label")
	private String label;
	@Field("projectTitle")
	private String projectTitle;
	@Field("comment")
	private String comment;
	@Field("description")
	private String description;
	@Field("numSubjects")
	private int numSubjects;
	@Field("numSamples")
	private int numSamples;
	@Field("institution")
	private List<String> institution;
	@Field("location")
	private List<String> location;
	@Field("PI")
	private List<String> PI;
	@Field("CI")
	private List<String> CI;
	@Field("demographics")
	private List<String> demographics;
	@Field("acculturation")
	private List<String> acculturation;
	@Field("occupation")
	private List<String> occupation;
	@Field("housingCharacteristics")
	private List<String> housingCharacteristics;
	@Field("ATIDU") // alcohol, tobacco, illicit drug use
	private List<String> ATIDU;
	@Field("socioEconomicStatus")
	private List<String> socioEconomicStatus;
	@Field("assessment")
	private List<String> assessment;
	@Field("BDN") //Behavior, Diet, and Nutrition
	private List<String> BDN;
	@Field("anthropometry")
	private List<String> anthropometry;
	@Field("laboratory")
	private List<String> laboratory;
	@Field("birthOutcomes")
	private List<String> birthOutcomes;
	*/
	// Constructer
	public Study() {
		startedAt = null;
		endedAt = null;
		permissionUri = "";
		label = "";
		project= "";
		comment = "";
		institution = "";
		agent = "";
/*		numSubjects = 0;
		numSamples = 0;
		institution = new ArrayList<String>();
		location = new ArrayList<String>();
		PI = new ArrayList<String>();
		CI = new ArrayList<String>();
		demographics = new ArrayList<String>();
		acculturation = new ArrayList<String>();
		occupation = new ArrayList<String>();
		housingCharacteristics = new ArrayList<String>();
		ATIDU = new ArrayList<String>();
		socioEconomicStatus = new ArrayList<String>();
		assessment = new ArrayList<String>();
		BDN = new ArrayList<String>();
		anthropometry = new ArrayList<String>();
		laboratory = new ArrayList<String>();
		birthOutcomes = new ArrayList<String>();
		*/
	}
	
	// get Methods
	public String getUri() {
		return studyUri;
	}
	
	public String getPermissionUri() {
		return permissionUri;
	}
	
/*	public String getName() {
		return name;
	}*/
	
	public String getLabel() {
		return label;
	}
	
	public String getProject() {
		return project;
	}
	
	public String getComment() {
		return comment;
	}
	
/*	public String getDescription() {
		return description;
	}
	
	public int getNumSamples() {
		return numSamples;
	}
	
	public int getNumSubjects() {
		return numSubjects;
	}*/
	
	public String getInstitution() {
		return institution;
	}
	
	public String getAgent() {
		return agent;
	}
	
/*	public List<String> getLocation() {
		return location;
	}
	
	public List<String> getPI() {
		return PI;
	}
	
	public List<String> getCI() {
		return CI;
	}
	
	public List<String> getDemographics() {
		return demographics;
	}
	
	public List<String> getAcculturation() {
		return acculturation;
	}
	
	public List<String> getOccupation() {
		return occupation;
	}
	
	public List<String> getHousingCharacteristics() {
		return housingCharacteristics;
	}
	
	public List<String> getATIDU() {
		return ATIDU;
	}
	
	public List<String> getSocioEconomicStatus() {
		return socioEconomicStatus;
	}
	
	public List<String> getAssessment() {
		return assessment;
	}
	
	public List<String> getBDN() {
		return BDN;
	}
	
	public List<String> getAnthropometry() {
		return anthropometry;
	}
	
	public List<String> getLaboratory() {
		return laboratory;
	}
	
	public List<String> getBirthOutcomes() {
		return birthOutcomes;
	}
	*/
	// get Start Time Methods
	public String getStartedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	public String getStartedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(startedAt);
	}
	
	// get End Time Methods
	public String getEndedAt() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	public String getEndedAtXsd() {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		return formatter.withZone(DateTimeZone.UTC).print(endedAt);
	}
	
	// set Methods
	public void setUri(String uri) {
		this.studyUri = uri;
	}
	
	public void setPermissionUri(String permissionUri) {
		this.permissionUri = permissionUri;
	}
	
/*	public void setName(String name) {
		this.name = name;
	}
	*/
	public void setLabel(String label) {
		this.label = label;
	}
		
	public void setProject(String project) {
		this.project = project;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}
	
/*	public String setDescription() {
		return description;
	}
	
	public void setNumSamples(int numSamples) {
		this.numSamples = numSamples;
	}
	
	public void setNumSubjects(int numSubjects) {
		this.numSubjects = numSubjects;
	}
*/
	public void setInstitution(String institution) {
		this.institution = institution;
	}
	
	public void setAgent(String agent) {
		this.agent = agent;
	}
	
/*	public void setLocation(List<String> location) {
		this.location = location;
	}
	
	public void setPI(List<String> PI) {
		this.PI = PI;
	}
	
	public void setCI(List<String> CI) {
		this.CI = CI;
	}
	
	public void setDemographics(List<String> demographics) {
		this.demographics = demographics;
	}
		
	public void setAcculturation(List<String> acculturation) {
		this.acculturation = acculturation;
	}
	
	public void setOccupation(List<String> occupation) {
		this.occupation = occupation;
	}
		
	public void setHousingCharacteristics(List<String> housingCharacteristics) {
		this.housingCharacteristics = housingCharacteristics;
	}
	
	public void setATIDU(List<String> ATIDU) {
		this.ATIDU = ATIDU;
	}
	
	public void setSocioEconomicStatus(List<String> socioEconomicStatus) {
		this.socioEconomicStatus = socioEconomicStatus;
	}
	
	public void setAssessment(List<String> assessment) {
		this.assessment = assessment;
	}
	
	public void setBDN(List<String> BDN) {
		this.BDN = BDN;
	}
	
	public void setAnthropometry(List<String> anthropometry) {
		this.anthropometry = anthropometry;
	}
	
	public void setLaboratory(List<String> laboratory) {
		this.laboratory = laboratory;
	}
	
	public void setBirthOutcomes(List<String> birthOutcomes) {
		this.birthOutcomes = birthOutcomes;
	}
	*/
	// set Start Time Methods
	@Field("started_at")
	public void setStartedAt(String startedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsd(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	public void setStartedAtXsdWithMillis(String startedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.startedAt = formatter.parseDateTime(startedAt);
	}
	
	// set End Time Methods
	@Field("ended_at")
	public void setEndedAt(String endedAt) {
		DateTimeFormatter formatter = DateTimeFormat.forPattern("EEE MMM dd HH:mm:ss zzz yyyy");
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsd(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTimeNoMillis();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
	public void setEndedAtXsdWithMillis(String endedAt) {
		DateTimeFormatter formatter = ISODateTimeFormat.dateTime();
		this.endedAt = formatter.parseDateTime(endedAt);
	}
/*	
	// add Methods
	public void addInstitution(String institution) {
		this.institution.add(institution);
	}
	
	public void addLocation(String location) {
		this.location.add(location);
	}
	
	public void addPI(String PI) {
		this.PI.add(PI);
	}
	
	public void addCI(String CI) {
		this.CI.add(CI);
	}
	
	public void addDemographics(String demographics) {
		this.demographics.add(demographics);
	}
	
	public void addAcculturation(String acculturation) {
		this.acculturation.add(acculturation);
	}
	
	public void addOccupation(String occupation) {
		this.occupation.add(occupation);
	}
	
	public void addHousingCharacteristics(String housingCharacteristics) {
		this.housingCharacteristics.add(housingCharacteristics);
	}
	
	public void addATIDU(String ATIDU) {
		this.ATIDU.add(ATIDU);
	}
	
	public void addSocioEconomicStatus(String socioEconomicStatus) {
		this.socioEconomicStatus.add(socioEconomicStatus);
	}
	
	public void addAssessment(String assessment) {
		this.assessment.add(assessment);
	}
	
	public void addBDN(String BDN) {
		this.BDN.add(BDN);
	}
	
	public void addAnthropometry(String anthropometry) {
		this.anthropometry.add(anthropometry);
	}
	
	public void addLaboratory(String laboratory) {
		this.laboratory.add(laboratory);
	}
	
	public void addBirthOutcomes(String birthOutcomes) {
		this.birthOutcomes.add(birthOutcomes);
	}
	*/
	public static Study convertFromSolr(SolrDocument doc) {
		Iterator<Object> i;
		DateTime date;
		Study study = new Study();
		// URI
		study.setUri(doc.getFieldValue("studyUri").toString());
		// permissions
		study.setPermissionUri(doc.getFieldValue("permission_uri").toString());
/*		// name
		if (doc.getFieldValues("name") != null) {
			study.setName(doc.getFieldValue("name").toString());
		}*/
		// label
		if (doc.getFieldValues("studyLabel") != null) {
			study.setLabel(doc.getFieldValue("studyLabel").toString());
		}
		// projectTitle
		if (doc.getFieldValues("proj") != null) {
			study.setProject(doc.getFieldValue("proj").toString());
		}
		// comment
		if (doc.getFieldValues("comment") != null) {
			study.setLabel(doc.getFieldValue("comment").toString());
		}
/*		// description
		if (doc.getFieldValues("description") != null) {
			study.setProjectTitle(doc.getFieldValue("description").toString());
		}
		// numSubjects
		if (doc.getFieldValues("numSubjects") != null) {
			study.setNumSubjects(Integer.parseInt(doc.getFieldValue("numSubjects").toString()));
		}
		// numSamples
		if (doc.getFieldValues("numSamples") != null) {
			study.setNumSamples(Integer.parseInt(doc.getFieldValue("numSamples").toString()));
		}*/
		// institution
		if (doc.getFieldValues("institutionName") != null) {
			study.setProject(doc.getFieldValue("institutionName").toString());
		}
		
		// institution
		if (doc.getFieldValues("agentName") != null) {
			study.setProject(doc.getFieldValue("agentName").toString());
		}
/*		// location(s)
		if (doc.getFieldValues("location") != null) {
			i = doc.getFieldValues("location").iterator();
			while (i.hasNext()) {
				study.addLocation(i.next().toString());
			}
		}

		// PI(s)
		if (doc.getFieldValues("PI") != null) {
			i = doc.getFieldValues("PI").iterator();
			while (i.hasNext()) {
				study.addPI(i.next().toString());
			}
		}

		// CI(s)
		if (doc.getFieldValues("CI") != null) {
			i = doc.getFieldValues("CI").iterator();
			while (i.hasNext()) {
				study.addCI(i.next().toString());
			}
		}

		// demographics
		if (doc.getFieldValues("demographics") != null) {
			i = doc.getFieldValues("demographics").iterator();
			while (i.hasNext()) {
				study.addDemographics(i.next().toString());
			}
		}

		// acculturation
		if (doc.getFieldValues("acculturation") != null) {
			i = doc.getFieldValues("acculturation").iterator();
			while (i.hasNext()) {
				study.addAcculturation(i.next().toString());
			}
		}

		// occupation
		if (doc.getFieldValues("occupation") != null) {
			i = doc.getFieldValues("occupation").iterator();
			while (i.hasNext()) {
				study.addOccupation(i.next().toString());
			}
		}

		// housingCharacteristics
		if (doc.getFieldValues("housingCharacteristics") != null) {
			i = doc.getFieldValues("housingCharacteristics").iterator();
			while (i.hasNext()) {
				study.addHousingCharacteristics(i.next().toString());
			}
		}

		// ATIDU
		if (doc.getFieldValues("ATIDU") != null) {
			i = doc.getFieldValues("ATIDU").iterator();
			while (i.hasNext()) {
				study.addATIDU(i.next().toString());
			}
		}

		// socioEconomicStatus
		if (doc.getFieldValues("socioEconomicStatus") != null) {
			i = doc.getFieldValues("socioEconomicStatus").iterator();
			while (i.hasNext()) {
				study.addSocioEconomicStatus(i.next().toString());
			}
		}

		// assessment
		if (doc.getFieldValues("assessment") != null) {
			i = doc.getFieldValues("assessment").iterator();
			while (i.hasNext()) {
				study.addAssessment(i.next().toString());
			}
		}

		// BDN
		if (doc.getFieldValues("BDN") != null) {
			i = doc.getFieldValues("BDN").iterator();
			while (i.hasNext()) {
				study.addBDN(i.next().toString());
			}
		}

		// anthropometry
		if (doc.getFieldValues("anthropometry") != null) {
			i = doc.getFieldValues("anthropometry").iterator();
			while (i.hasNext()) {
				study.addAnthropometry(i.next().toString());
			}
		}
		
		// laboratory
		if (doc.getFieldValues("laboratory") != null) {
			i = doc.getFieldValues("laboratory").iterator();
			while (i.hasNext()) {
				study.addLaboratory(i.next().toString());
			}
		}
		
		// birthOutcomes
		if (doc.getFieldValues("birthOutcomes") != null) {
			i = doc.getFieldValues("birthOutcomes").iterator();
			while (i.hasNext()) {
				study.addBirthOutcomes(i.next().toString());
			}
		}
		
		if (doc.getFieldValues("started_at") != null) {
			date = new DateTime((Date)doc.getFieldValue("started_at"));
			study.setStartedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		}
		
		if (doc.getFieldValues("ended_at") != null) {
			date = new DateTime((Date)doc.getFieldValue("ended_at"));
			study.setEndedAt(date.withZone(DateTimeZone.UTC).toString("EEE MMM dd HH:mm:ss zzz yyyy"));
		}
	*/			
		return study;
	}
	
	public static MetadataAcquisitionQueryResult find(int page, int qtd, List<String> permissions, FacetHandler handler) {
		MetadataAcquisitionQueryResult result = new MetadataAcquisitionQueryResult();
		
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES);
		SolrQuery query = new SolrQuery();
		String permission_query = "";
		String facet_query = "";
		String q;
		
		permission_query += "permission_uri:\"" + "PUBLIC" + "\"";
		if (permissions != null) {
			Iterator<String> i = permissions.iterator();
			while (i.hasNext()) {
				permission_query += " OR ";
				permission_query += "permission_uri:\"" + i.next() + "\"";
			}
		}
		
		if (handler != null) {
		    facet_query = handler.toSolrQuery();
		    /* Iterator<String> i = handler.facetsAnd.keySet().iterator();
			while (i.hasNext()) {
				String field = i.next();
				String value = handler.facetsAnd.get(field);
				facet_query += field + ":\"" + value + "\"";
				if (i.hasNext()) {
					facet_query += " AND ";
				}
				} */
		}
		
		if (facet_query.trim().equals("")) {
			facet_query = "*:*";
		}
		
		q =  "(" + permission_query + ") AND (" + facet_query + ")";
		System.out.println("!!! QUERY: " + q);
		query.setQuery(q);
		query.setStart((page-1)*qtd);
		query.setRows(qtd);
		query.setFacet(true);
		query.addFacetField("demographics,acculturation,occupation,housingCharacteristics,ATIDU,socioEconomicStatus,assessment,BDN,anthropometry,laboratory,birthOutcomes");
		// See Measurement.java as an example if we wish to add the possible facet values as pivots
		try {
			QueryResponse queryResponse = solr.query(query);
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
			
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.find(int, int, List<String>, Map<String, String>) - Exception message: " + e.getMessage());
		}
		
		return result;
	}
	
	public static List<Study> find(String uri) {
		List<Study> list = new ArrayList<Study>();
		
		System.out.println("uri:");
		System.out.println(uri);
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES);
		SolrQuery query = new SolrQuery();
		
		query.set("q", "studyUri:\"" + uri + "\"");
		query.set("sort", "started_at asc");
		query.set("rows", "10000000");
		
		try {
			QueryResponse response = solr.query(query);
			solr.close();
			SolrDocumentList results = response.getResults();
			Iterator<SolrDocument> i = results.iterator();
			while (i.hasNext()) {
				Study study = convertFromSolr(i.next());
				list.add(study);
			}
		} catch (Exception e) {
			list.clear();
			System.out.println("[ERROR] Study.find(String) - Exception message: " + e.getMessage());
		}
		
		return list;
	}
	
	public int close(String endedAt) {
		this.setEndedAtXsd(endedAt);
		return this.save();
	}
	
	public int delete() {
		SolrClient solr = new HttpSolrClient(
				Play.application().configuration().getString("hadatac.solr.data")
				+ Collections.STUDIES);
		try {
			UpdateResponse response = solr.deleteByQuery("studyUri:\"" + studyUri + "\"");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] Study.delete() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] Study.delete() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] Study.delete() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	public int save() {
		try {
			SolrClient client = new HttpSolrClient(
					Play.application().configuration().getString("hadatac.solr.data")
					+ Collections.STUDIES);
			if (endedAt.toString().startsWith("9999")) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = client.addBean(this).getStatus();
			client.commit();
			client.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Study.save() - e.Message: " + e.getMessage());
			return -1;
		}
	}
	
	public int save(SolrClient solr) {
		try {
			if (endedAt.toString().startsWith("9999")) {
				endedAt = DateTime.parse("9999-12-31T23:59:59.999Z");
			}
			int status = solr.addBean(this).getStatus();
			solr.commit();
			solr.close();
			return status;
		} catch (IOException | SolrServerException e) {
			System.out.println("[ERROR] Study.save(SolrClient) - e.Message: " + e.getMessage());
			return -1;
		}
	}
}