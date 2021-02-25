package org.hadatac.console.controllers.metadataacquisition;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.typesafe.config.ConfigFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.views.html.metadataacquisition.metadataacquisition;
import org.hadatac.entity.pojo.Pair;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Variable;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MetadataAcquisition extends Controller {

	private static final Logger log = LoggerFactory.getLogger(MetadataAcquisition.class);

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index() {
    	final SysUser user = AuthApplication.getLocalUser(session());
    	String collection = ConfigFactory.load().getString("hadatac.console.host_deploy") 
    			+ request().path() + "/solrsearch";
    	List<String> indicators = getIndicators();
    	
    	return ok(metadataacquisition.render(collection, indicators, user.isDataManager()));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex() {
    	return index();
    }
    
    public static List<String> getIndicators() {
		String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList()
				+ "SELECT DISTINCT ?indicatorLabel\n" +
				"WHERE {\n" +
				"  ?attribute ?x ?attributeUri .\n" +
				"  ?attributeUri rdfs:label ?attributeLabel .\n" +
				"  ?attributeUri rdfs:subClassOf* ?indicator . \n" +
				"  ?indicator rdfs:label ?indicatorLabel . \n" +
				"  #FILTER(lang(?attributeLabel) != 'en') .\n" +
				"   { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . \n" +
				"  }";
		
		ResultSetRewindable resultsrwStudy = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), initStudyQuery);
		
		List<String> results = new ArrayList<String>();
		while (resultsrwStudy.hasNext()) {
			QuerySolution soln = resultsrwStudy.next();
			results.add(soln.get("indicatorLabel").toString());
		}
		java.util.Collections.sort(results);
		
		return results; 
    }

	@SuppressWarnings("unchecked")
	public static boolean updateStudySearch() {

		// retrieve all the studies and their attributes, such as study URI, study label, study comments, etc.
		Map<String, Map<String,String>> studies = Study.retrieveStudiesAndAtttributes();

		// retrieve the facet results from Solr
		List<String> facetResult = Variable.retrieveStudySearchFacetResult(null);

		// combine the above two search results and prepare for the GUI side display

		HashMap<String, HashMap<String, Object>> mapStudyInfo = new HashMap<String, HashMap<String, Object>>();

		/*
			a typical facetLine would look like this:
			study URI: http://hadatac.org/kb/hhear#STD-2016-1431
			role (NOT URI!): Child Sample
			entity URI: http://purl.obolibrary.org/obo/UBERON_0001088
			DASA URI: http://hadatac.org/kb/hhear#DASA-Lab-LOD
			InRelationTo URI: http://purl.obolibrary.org/obo/CHEBI_27528,http://purl.org/twc/HHEAR_00342
		 */
		for (String facetLine : facetResult ) {

			String[] items = facetLine.split(Variable.HIERARCHICAL_FACET_SEPARATOR);

			if ( items == null || items.length < Variable.SolrPivotFacet.DASA_URI_STR.ordinal() + 1 ) {
				log.warn("Study Search skips this line because it is too short: " + facetLine);
				continue;
			}

			if ( items[Variable.SolrPivotFacet.DASA_URI_STR.ordinal()] == null ) {
				log.warn("Study Search skips this line since it does not have a DASA URI: " + facetLine);
				continue;
			}

			String studyUri = items[Variable.SolrPivotFacet.STUDY_URI_STR.ordinal()];
			Map<String, String> studyDetails = studies.get(studyUri);

			if ( studyDetails == null ) {
				log.warn("Study Search: cannot find the study details for this study:" + studyUri);
				continue;
			}

			HashMap<String, Object> studyInfo = null;
			if (!mapStudyInfo.containsKey(studyUri)) {
				studyInfo = new HashMap<String, Object>();
				studyInfo.put("studyUri", studyUri);
				mapStudyInfo.put(studyUri, studyInfo);
			} else {
				studyInfo = mapStudyInfo.get(studyUri);
			}
			if (studyDetails.containsKey("studyLabel") && !studyInfo.containsKey("studyLabel_str")) {
				studyInfo.put("studyLabel_str", "<a href=\""
						+ ConfigFactory.load().getString("hadatac.console.host_deploy")
						+ "/hadatac/studies/view?study_uri="
						+ URIUtils.replaceNameSpaceEx(studyInfo.get("studyUri").toString()) + "\"><b>"
						+ studyDetails.get("studyId") + "</b></a>");
			}
			if (studyDetails.containsKey("studyLabel") && !studyInfo.containsKey("studyShortDesc_str")) {
				studyInfo.put("studyShortDesc_str", studyDetails.get("studyLabel"));
			}
			if (studyDetails.containsKey("studyTitle") && !studyInfo.containsKey("studyTitle_str")) {
				studyInfo.put("studyTitle_str", studyDetails.get("studyTitle"));
			}
			if (studyDetails.containsKey("proj") && !studyInfo.containsKey("proj_str")){
				studyInfo.put("proj_str", studyDetails.get("proj"));
			}
			if (studyDetails.containsKey("studyComment") && !studyInfo.containsKey("studyComment_str")){
				studyInfo.put("studyComment_str", studyDetails.get("studyComment"));
			}
			if (studyDetails.containsKey("agentName") && !studyInfo.containsKey("agentName_str")){
				studyInfo.put("agentName_str", studyDetails.get("agentName"));
			}
			if (studyDetails.containsKey("institutionName") && !studyInfo.containsKey("institutionName_str")){
				studyInfo.put("institutionName_str", studyDetails.get("institutionName"));
			}

			// for each component in the facet line, get their labels

			List<Pair<String, String>> indicators = Variable.computeIndicatorList(items[Variable.SolrPivotFacet.DASA_URI_STR.ordinal()]);
			if ( indicators == null || indicators.size() == 0 ) {
				indicators = Variable.computeIndicatorList(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]);
			}
			if ( indicators == null || indicators.size() == 0 ) {
				log.warn("Study Search encounters empty indicator URI(s) for this facet search result line: " + facetLine);
				continue;
			}

			boolean multipleIndicators = indicators.size() > 1 ? true : false;
			boolean hasStandardTestScore = Variable.constainsStandardTestScore(indicators);

			for ( Pair<String, String> labelAndIndcator : indicators ) {

				String key = "", value = "", labelContent = "";

				if ( hasStandardTestScore == false ) value = FirstLabel.getPrettyLabel(labelAndIndcator.getLeft());
				else {
					final String testScoreTag = Variable.retrieveTestScoreLabel(indicators);
					if ( testScoreTag != null ) {
						if ( testScoreTag.equalsIgnoreCase(FirstLabel.getPrettyLabel(labelAndIndcator.getLeft())) ) continue;
						value = testScoreTag + " (" + FirstLabel.getPrettyLabel(labelAndIndcator.getLeft()) + ")";
					} else value = FirstLabel.getPrettyLabel(labelAndIndcator.getLeft());
				}
				key = FirstLabel.getPrettyLabel(labelAndIndcator.getRight());

				if ( multipleIndicators ) {
					log.info("multiple indicators detected: " + items[Variable.SolrPivotFacet.DASA_URI_STR.ordinal()] + " :" + value + " | " + key);
				}

				key = key.replace(",", "").replace(" ", "") + "_str_multi";
				String temp = "";

				if (key.contains("TargetedAnalyte") || key.contains("BiologicalResponse")) {

					value = "";  // targeted analyte uses this format: In_Relation_To *in* Entity *from* Role *at* Time”.

					// get the role label
					value = " from " + items[Variable.SolrPivotFacet.ROLE_STR.ordinal()] + value;

					// get named-time label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]) ) {
						value = value + " at " + FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]);
					}

					// get entity label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]) ) {
						labelContent = FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]);
						if (labelContent != null && labelContent.length() > 0) {
							if (!labelContent.toLowerCase().equals("human") && !labelContent.toLowerCase().equals("human@en") && !labelContent.toLowerCase().equals("sample")) {
								value = labelContent + " " + value;
							}
						}
					}

					// get the inRelationTo label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]) ) {
						value = FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]) + " in " + value;
					}

				} else {

					// role label
					temp = FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.ROLE_STR.ordinal()]) + "'s " + value;
					value = temp;

					// get named time label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]) ) {
						temp = value + " at " + FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]);
						value = temp;
					}

					// get entity label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]) ) {
						labelContent = FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]);
						if (labelContent != null && labelContent.length() > 0) {
							if (!labelContent.toLowerCase().equals("human") && !labelContent.toLowerCase().equals("human@en") && !labelContent.toLowerCase().equals("sample")) {
								value = labelContent + " " + value;
							}
						}
					}

					// get inRelationTo label
					if ( !Variable.EMPTY_CONTENT.equalsIgnoreCase(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]) ) {
						temp = FirstLabel.getPrettyLabel(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]) + "'s " + value;
						value = temp;
					}

				}

				// Remove duplicate consecutive words
				value = value.replaceAll("(?i)\\b([a-z]+)\\b(?:\\s+\\1\\b)+", "$1");
				ArrayList<String> arrValues = null;
				if (!studyInfo.containsKey(key)) {
					arrValues = new ArrayList<String>();
					studyInfo.put(key, arrValues);
				} else if (studyInfo.get(key) instanceof ArrayList<?>) {
					arrValues = (ArrayList<String>) studyInfo.get(key);
				}

				if ((!arrValues.contains(value)) && (value != "")) {
					boolean dupl = false;
					int valueCharVal = 0, valCharVal = 0;
					for (String val : arrValues) {
						if (val.toLowerCase().equals(value.toLowerCase())) {
							dupl = true;
							//System.out.println("Value: " + value + (int)value.charAt(0) + "\tVal: " + val + (int)val.charAt(0) + "\n" );
							valueCharVal = 0;
							valCharVal = 0;
							for (int i = 0; i < value.length(); i++) {
								valueCharVal += (int) value.charAt(i);
							}
							for (int i = 0; i < val.length(); i++) {
								valCharVal += (int) val.charAt(i);
							}
						}
					}
					if (!dupl) {
						arrValues.add(value);
					}
				}
			}

		}

		deleteFromSolr();

		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapStudyInfo.values()) {
			results.add(new JSONObject(info));
		}

		log.info("here is the json string before writing to Solr:");
		log.info(results.toString());
		// System.out.println(results.toString());

		return SolrUtils.commitJsonDataToSolr(
				CollectionUtil.getCollectionPath(CollectionUtil.Collection.STUDIES), results.toString());
	}

	public static int deleteFromSolr() {
		try {
			SolrClient solr = new HttpSolrClient.Builder(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.STUDIES)).build();
			UpdateResponse response = solr.deleteByQuery("*:*");
			solr.commit();
			solr.close();
			return response.getStatus();
		} catch (SolrServerException e) {
			System.out.println("[ERROR] MetadataAcquisition.deleteFromSolr() - SolrServerException message: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("[ERROR] MetadataAcquisition.deleteFromSolr() - IOException message: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("[ERROR] MetadataAcquisition.deleteFromSolr() - Exception message: " + e.getMessage());
		}
		
		return -1;
	}
	
	@Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result update() {
		updateStudySearch();
		return redirect(routes.MetadataAcquisition.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_MANAGER_ROLE))
    public Result postUpdate() {
    	return update();
    }
}
