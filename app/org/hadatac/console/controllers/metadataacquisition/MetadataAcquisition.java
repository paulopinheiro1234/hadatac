package org.hadatac.console.controllers.metadataacquisition;

import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.util.NamedList;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.http.SolrUtils;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.models.SysUser;

import java.io.IOException;
import java.util.*;

import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Variable;
import org.pac4j.play.java.Secure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.metadataacquisition.*;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.json.simple.JSONObject;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;

public class MetadataAcquisition extends Controller {

	@Inject
	Application application;
	
private static final Logger log = LoggerFactory.getLogger(MetadataAcquisition.class);

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        String collection = ConfigFactory.load().getString("hadatac.console.host_deploy")
                + request.path() + "/solrsearch";
        List<String> indicators = getIndicators();

        return ok(metadataacquisition.render(collection, indicators, user.isDataManager(),user.getEmail()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(Http.Request request) {
        return index(request);
    }

    public static List<String> getIndicators() {
        String initStudyQuery = NameSpaces.getInstance().printSparqlNameSpaceList()
                + " SELECT DISTINCT ?indicatorLabel WHERE { "
                + " ?subTypeUri rdfs:subClassOf* hasco:Study . "
                + " ?studyUri a ?subTypeUri . "
                + " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
                + " ?dataAcq hasco:hasSchema ?schemaUri ."
                + " ?schemaAttribute hasco:partOfSchema ?schemaUri . "
                + " ?schemaAttribute hasco:hasAttribute ?attribute . "
                + " {  { ?indicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?indicator rdfs:subClassOf hasco:SampleIndicator } } . "
                + " ?indicator rdfs:label ?indicatorLabel . "
                + " ?attribute rdfs:subClassOf+ ?indicator . "
                + " ?attribute rdfs:label ?attributeLabel . "
                + " }";

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
	public static boolean updateStudy() {

    	// retrieve all the studies and their attributes, such as study URI, study label, study comments, etc.
    	Map<String, Map<String,String>> studies = Study.retrieveStudiesAndAtttributes();

    	// retrieve all the indicators for all the studies
		List<String> indicators = Variable.retrieveStudyVariablesWithLabels(null);

		// combine the above two search results and prepare for the GUI side display

		HashMap<String, HashMap<String, Object>> mapStudyInfo = new HashMap<String, HashMap<String, Object>>();

		for (String indicatorLine : indicators ) {

			String[] items = indicatorLine.split(Variable.VARIABLE_SEPARATOR);

			if ( items == null || items.length < Variable.SolrPivotFacet.DASA_URI_STR.ordinal() + 1 ) {
				log.warn("Study Search skips this line: " + indicatorLine);
				continue;
			}

			if ( items[Variable.SolrPivotFacet.DASA_URI_STR.ordinal()] == null ) {
				log.warn("Study Search skips this line since it does not have a indicator value: " + indicatorLine);
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
			}
			else {
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

			String key = "", value = "", labelContent = "";
			if ( indicatorLine.indexOf("human") > 0 )  {
				int x = 1;
			}

			labelContent = getLabelContent(items[Variable.SolrPivotFacet.DASA_URI_STR.ordinal()]);
			if ( labelContent == null || labelContent.length() == 0 ) {
				log.warn("Study Search encounters empty indicator label: " + indicatorLine);
				continue;
			}

			String[] attributeAndIndicatorLabels =labelContent.split("\\"+Variable.LABEL_SEPARATOR,-1);
			if ( attributeAndIndicatorLabels != null && attributeAndIndicatorLabels.length == 2 ) {
				key = attributeAndIndicatorLabels[1];
				value = attributeAndIndicatorLabels[0];
			}

			if ( key == null || key.length() == 0 || key.equalsIgnoreCase(Variable.VARIABLE_EMPTY_LABEL)) {
				log.warn("Study Search encounters empty indicator label: " + indicatorLine);
				continue;
			}

			key = key.replace(",", "").replace(" ", "") + "_str_multi";
			String temp = "";

			if ( key.contains("TargetedAnalyte") ) {

				// since this is targeted analyte, we would like to organize the components in this order:
				// In_Relation_To *in* Entity *from* Role *at* Time”.

				value = "";
				labelContent = items[Variable.SolrPivotFacet.ROLE_STR.ordinal()];
				if (labelContent != null && labelContent.length() > 0) {
					value = " from " + labelContent + value;
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					value = value + " at " + labelContent;
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					if (!labelContent.toLowerCase().equals("human") && !labelContent.toLowerCase().equals("human@en") && !labelContent.toLowerCase().equals("sample")) {
						value = labelContent + " " + value;
					}
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					value = labelContent + " in " + value;
				}

			} else {

				labelContent = items[Variable.SolrPivotFacet.ROLE_STR.ordinal()];
				if (labelContent != null && labelContent.length() > 0) {
					temp = labelContent + "'s " + value;
					value = temp;
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.NAMED_TIME_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					temp = value + " at " + labelContent;
					value = temp;
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.ENTITY_URI_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					if (!labelContent.toLowerCase().equals("human") && !labelContent.toLowerCase().equals("human@en") && !labelContent.toLowerCase().equals("sample")) {
						temp = labelContent + " " + value;
						value = temp;
					}
				}

				labelContent = getLabelContent(items[Variable.SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]);
				if (labelContent != null && labelContent.length() > 0) {
					temp = labelContent + "'s " + value;
					value = temp;
				}

			}

			// Remove duplicate consecutive words
			value = value.replaceAll("(?i)\\b([a-z]+)\\b(?:\\s+\\1\\b)+", "$1");
			ArrayList<String> arrValues = null;
			if ( !studyInfo.containsKey(key) ) {
				arrValues = new ArrayList<String>();
				studyInfo.put(key, arrValues);
			}
			else if (studyInfo.get(key) instanceof ArrayList<?>) {
				arrValues = (ArrayList<String>)studyInfo.get(key);
			}

			if ((!arrValues.contains(value))&&(value!="")) {
				boolean dupl=false;
				int valueCharVal = 0, valCharVal = 0;
				for(String val : arrValues){
					if(val.toLowerCase().equals(value.toLowerCase())){
						dupl=true;
						//System.out.println("Value: " + value + (int)value.charAt(0) + "\tVal: " + val + (int)val.charAt(0) + "\n" );
						valueCharVal = 0;
						valCharVal = 0;
						for(int i=0;i<value.length();i++){
							valueCharVal += (int)value.charAt(i);
						}
						for(int i=0;i<val.length();i++){
							valCharVal += (int)val.charAt(i);
						}
					}
				}
				if(!dupl){
					arrValues.add(value);
				}
			}

		}

		deleteFromSolr();

		ArrayList<JSONObject> results = new ArrayList<JSONObject>();
		for (HashMap<String, Object> info : mapStudyInfo.values()) {
			results.add(new JSONObject(info));
		}

		// System.out.println(results.toString());

		return SolrUtils.commitJsonDataToSolr(
				CollectionUtil.getCollectionPath(CollectionUtil.Collection.STUDIES), results.toString());
	}

	private static String getLabelContent(String label) {
    	if ( label == null || label.indexOf("(") < 0 || label.indexOf(")") < 0 ) return null;
    	if ( label.contains(Variable.VARIABLE_EMPTY_LABEL) ) return "";
    	return label.substring(label.indexOf("(")+1,label.lastIndexOf(")"));
	}

    @SuppressWarnings("unchecked")
	public static boolean updateStudyOldVersion() {
		String strQuery = NameSpaces.getInstance().printSparqlNameSpaceList() 
				+ " SELECT DISTINCT ?studyId ?studyUri ?studyLabel ?proj ?studyTitle ?studyComment"
				+ " ?indicatorLabel ?attributeLabel ?roleLabel ?eventLabel ?entityLabel" 
				+ " ?agentName ?institutionName ?relationLabel ?relationTo ?relationToRole ?relationToRoleLabel WHERE { "
				+ " ?studyUri a ?subUri . "
				+ " ?studyUri hasco:hasId ?studyId . "
				+ " ?subUri rdfs:subClassOf* hasco:Study . "
				+ " OPTIONAL{ ?schemaAttribute hasco:partOfSchema ?schemaUri . "
				+ " ?dataAcq hasco:isDataAcquisitionOf ?studyUri ."
				+ " ?dataAcq hasco:hasSchema ?schemaUri ."
				+ " ?schemaAttribute hasco:hasAttribute ?attribute . "
				+ " {  { ?indicator rdfs:subClassOf hasco:StudyIndicator } UNION { ?indicator rdfs:subClassOf hasco:SampleIndicator } } . "
				+ " ?indicator rdfs:label ?indicatorLabel . " 
				+ " ?attribute rdfs:subClassOf+ ?indicator . " 
				+ " ?attribute rdfs:label ?attributeLabel . "
				+ "		FILTER(lang(?attributeLabel) != 'en') } . " 
				+ " OPTIONAL { ?schemaAttribute hasco:isAttributeOf ?object . "
                + " ?object hasco:hasRole ?role . "
                + " ?role rdfs:label ?roleLabel } . "
                + " OPTIONAL { ?schemaAttribute hasco:isAttributeOf ?object . "
                + " ?object sio:SIO_000668 ?relationTo . "
                + " ?object hasco:Relation ?relation . "
                + " ?relation rdfs:label ?relationLabel . "
                + " ?relationTo hasco:hasRole ?relationToRole . "
                + " ?relationToRole rdfs:label ?relationToRoleLabel} . "
                + " OPTIONAL { ?schemaAttribute hasco:hasEvent ?event . "
                + " ?event hasco:hasEntity ?eventEn . "
                + " ?eventEn rdfs:label ?eventLabel } . "
                + " OPTIONAL { ?schemaAttribute hasco:hasEntity ?org.hadatac.entity . "
                + " ?org.hadatac.entity rdfs:label ?entityLabel . "
                + "		FILTER(lang(?entityLabel) != 'en') } . "
                + " OPTIONAL{ ?studyUri rdfs:label ?studyLabel } . "
                + " OPTIONAL{ ?studyUri hasco:hasProject ?proj } . "
                + " OPTIONAL{ ?studyUri skos:definition ?studyTitle } . "
                + " OPTIONAL{ ?studyUri rdfs:comment ?studyComment } . "
                + " OPTIONAL{ ?studyUri hasco:hasAgent ?agent . "
                + "           ?agent foaf:name ?agentName } . "
                + " OPTIONAL{ ?studyUri hasco:hasInstitution ?institution . "
                + "           ?institution foaf:name ?institutionName } . "
                + " } ";

        System.out.println("strQuery: " + strQuery);

        ResultSetRewindable resultsrwStudy = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), strQuery);

        HashMap<String, HashMap<String, Object>> mapStudyInfo = new HashMap<String, HashMap<String, Object>>();
        while (resultsrwStudy.hasNext()) {
            QuerySolution soln = resultsrwStudy.next();
            String studyUri = soln.get("studyUri").toString();
            HashMap<String, Object> studyInfo = null;
            if (!mapStudyInfo.containsKey(studyUri)) {
                studyInfo = new HashMap<String, Object>();
                studyInfo.put("studyUri", studyUri);
                mapStudyInfo.put(studyUri, studyInfo);
            }
            else {
                studyInfo = mapStudyInfo.get(studyUri);
            }

            if (soln.contains("studyLabel") && !studyInfo.containsKey("studyLabel_str")) {
                studyInfo.put("studyLabel_str", "<a href=\""
                        + ConfigFactory.load().getString("hadatac.console.host_deploy")
                        + "/hadatac/studies/view?study_uri="
                        + URIUtils.replaceNameSpaceEx(studyInfo.get("studyUri").toString()) + "\">"
                        + soln.get("studyId").toString() + "</a>");
            }
            if (soln.contains("studyTitle") && !studyInfo.containsKey("studyTitle_str")) {
                studyInfo.put("studyTitle_str", soln.get("studyTitle").toString());
            }
            if (soln.contains("proj") && !studyInfo.containsKey("proj_str")){
                studyInfo.put("proj_str", soln.get("proj").toString());
            }
            if (soln.contains("studyComment") && !studyInfo.containsKey("studyComment_str")){
                studyInfo.put("studyComment_str", soln.get("studyComment").toString());
            }
            if (soln.contains("agentName") && !studyInfo.containsKey("agentName_str")){
                studyInfo.put("agentName_str", soln.get("agentName").toString());
            }
            if (soln.contains("institutionName") && !studyInfo.containsKey("institutionName_str")){
                studyInfo.put("institutionName_str", soln.get("institutionName").toString());
            }
            if (soln.contains("indicatorLabel")) {
                String key = soln.get("indicatorLabel").toString().
                        replace(",", "").replace(" ", "") + "_str_multi";
                String value = soln.get("attributeLabel").toString();
                String temp = "";
                if (soln.contains("roleLabel")) {
                    temp = soln.get("roleLabel").toString() + "'s " + value;
                    value = temp.toString();
                }
                if (soln.contains("eventLabel")){
                    temp = value + " at " + soln.get("eventLabel").toString();
                    value = temp.toString();
                }
                if (soln.contains("entityLabel")){
                    if(!soln.get("entityLabel").toString().toLowerCase().equals("human")&&!soln.get("entityLabel").toString().toLowerCase().equals("sample")){
                        temp = soln.get("entityLabel").toString() + " " + value;
                        value = temp.toString();
                    }
                }
                if (soln.contains("relationToRoleLabel")){
                    temp = soln.get("relationToRoleLabel") + "'s " + value;
                    value = temp.toString();
                }
                // Remove duplicate consecutive words
                value = value.replaceAll("(?i)\\b([a-z]+)\\b(?:\\s+\\1\\b)+", "$1");
                ArrayList<String> arrValues = null;
                if (!studyInfo.containsKey(key)) {
                    arrValues = new ArrayList<String>();
                    studyInfo.put(key, arrValues);
                }
                else if (studyInfo.get(key) instanceof ArrayList<?>) {
                    arrValues = (ArrayList<String>)studyInfo.get(key);
                }

                if ((!arrValues.contains(value))&&(value!="")) {
                    boolean dupl=false;
                    int valueCharVal = 0;
                    int valCharVal = 0;
                    for(String val : arrValues){
                        if(val.toLowerCase().equals(value.toLowerCase())){
                            dupl=true;
                            //System.out.println("Value: " + value + (int)value.charAt(0) + "\tVal: " + val + (int)val.charAt(0) + "\n" );
                            valueCharVal = 0;
                            valCharVal = 0;
                            for(int i=0;i<value.length();i++){
                                valueCharVal += (int)value.charAt(i);
                            }
                            for(int i=0;i<val.length();i++){
                                valCharVal += (int)val.charAt(i);
                            }
                        }
                    }
                    if(!dupl){
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

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result update() {
        updateStudy();

        return redirect(routes.MetadataAcquisition.index());
    }

    @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postUpdate() {
        return update();
    }
}
