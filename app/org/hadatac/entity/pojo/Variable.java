package org.hadatac.entity.pojo;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.PivotField;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.util.NamedList;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.NameSpaces;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class Variable {

	private static final Logger log = LoggerFactory.getLogger(Variable.class);
	public static final String LABEL_SEPARATOR = "|";
	public static final String VARIABLE_SEPARATOR = ";";
	public static final String VARIABLE_EMPTY_LABEL = "**";

	// patch for multi-valued attributes
	private static final String[] multiAttributeTag = { "Z-Score", "T-Score", "standard score", "Age Equivalent" };

	private Entity ent;
    private String role;
    private List<Attribute> attrList;
    private Entity inRelationTo;
    private Unit unit;
    private Attribute timeAttr;

	// study_uri_str,role_str,entity_uri_str,dasa_uri_str,in_relation_to_uri_str,named_time_str
    public enum SolrPivotFacet {
    	STUDY_URI_STR(0),
		ROLE_STR(1),
		ENTITY_URI_STR(2),
		DASA_URI_STR(3),
		IN_RELATION_TO_URI_STR(4),
		NAMED_TIME_STR(5);

    	private final int value;

		SolrPivotFacet(int value) {
			this.value = value;
		}
	}

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel) {
    	this(entRole, attrInRel, null, null);
    }

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit) {
    	this(entRole, attrInRel, unit, null);
    }

    public Variable(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit, Attribute timeAttr) {
    	this.ent = entRole.getEntity();
    	this.role = entRole.getRole();
    	this.attrList = attrInRel.getAttributeList();
    	this.inRelationTo = attrInRel.getInRelationTo();
    	this.unit = unit;
    	this.timeAttr = timeAttr;
    }

    public String getKey() {
    	return getRole() + getEntityStr() + getAttributeListStr() + getInRelationToStr() + getUnitStr() + getTimeStr();
    }

    public Entity getEntity() {
    	return ent;
    }

    public String getEntityStr() {
        if (ent == null || ent.getUri() == null || ent.getUri().isEmpty()) { 
        	return "";
        }
    	return ent.getUri();
    }

    public String getRole() {
    	if (role == null) {
    		return "";
    	}
    	return role;
    }

    public List<Attribute> getAttributeList() {
    	return attrList;
    }

    public String getAttributeListStr() {
    	if (attrList == null || attrList.isEmpty()) {
    		return "";
    	}
    	String resp = "";
    	for (Attribute attr : attrList) {
    		if (attr != null && attr.getUri() != null && !attr.getUri().isEmpty()) { 
        		resp = resp + attr.getUri();
    		}
    	}
        return resp;
    }

    public Entity getInRelationTo() {
    	return inRelationTo;
    }

    public String getInRelationToStr() {
        if (inRelationTo == null || inRelationTo.getUri() == null ||inRelationTo.getUri().isEmpty()) { 
            return "";
        }
    	return inRelationTo.getUri();
    }

    public Unit getUnit() {
    	return unit;
    }

    public String getUnitStr() {
        if (unit == null || unit.getUri() == null || unit.getUri().isEmpty()) { 
            return "";
        }
    	return unit.getUri();
    }

    public Attribute getTime() {
    	return timeAttr;
    }

    public String getTimeStr() {
        if (timeAttr == null || timeAttr.getUri() == null || timeAttr.getUri().isEmpty()) { 
            return "";
        }
    	return timeAttr.getUri();
    }

    public static String upperCase(String orig) {
    	String[] words = orig.split(" ");
    	StringBuffer sb = new StringBuffer();

    	for (int i = 0; i < words.length; i++) {
    		sb.append(Character.toUpperCase(words[i].charAt(0)))
    		.append(words[i].substring(1)).append(" ");
    	}          
    	return sb.toString().trim();
    }      

    public String prep(String orig) {
    	String aux = upperCase(orig);
    	return aux.replaceAll(" ","-").replaceAll("[()]","");
    }

    public static List<String> retrieveStudyVariablesWithLabels(String studyUri) {

		/*"params":{
			"q":"*:*",
					"facet.pivot":"study_uri_str,role_str,entity_uri_str,dasa_uri_str,in_relation_to_uri_str,named_time_str",
					"indent":"on",
					"rows":"1",
					"facet":"on",
					"wt":"json",
					"_":"1608051191814"}},
			*/

		// default the search to all studies
		String queryString = "*:*";
		// but update it if a specific study is given
		if ( studyUri != null && studyUri.length() > 0 ) {
			queryString = "study_uri_str:\"" + studyUri + "\"";
		}
		SolrQuery query = new SolrQuery();

		query.setQuery(queryString);
		query.setRows(0);
		query.setFacet(true);
		query.set("facet.pivot", "study_uri_str,role_str,entity_uri_str,dasa_uri_str,in_relation_to_uri_str,named_time_str");

		try {
			SolrClient solr = new HttpSolrClient.Builder(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
			QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
			solr.close();
			NamedList<List<PivotField>> facetPivot = queryResponse.getFacetPivot();
			List<String> parsedPivotResult = parsePivotResult(facetPivot);
			/*parsedPivotResult.forEach((s) -> {
				System.out.println(s);
			});*/
			List<String> pivotResultWithLabels = retrieveLabelsForPivotResult(parsedPivotResult);
			pivotResultWithLabels.forEach( s -> System.out.println(s) );
			return pivotResultWithLabels;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
    	return null;
	}

	private static List<String> retrieveLabelsForPivotResult(List<String> parsedPivotResult) {

		List<String> result = new ArrayList<>();
		if ( parsedPivotResult == null || parsedPivotResult.size() == 0 ) return result;

		// study_uri_str,role_str,entity_uri_str,dasa_uri_str,in_relation_to_uri_str,named_time_str
		parsedPivotResult.forEach( line -> {

			String tmpLabel = VARIABLE_EMPTY_LABEL;
			String[] items = line.split(",");
			StringBuffer sb = new StringBuffer();

			if ( items == null || items.length != 6 ) {
				log.warn("Study search parsing pivot facet issue:" + line);
			} else {
				sb.append(items[SolrPivotFacet.STUDY_URI_STR.ordinal()]).append(VARIABLE_SEPARATOR).append(items[SolrPivotFacet.ROLE_STR.ordinal()]).append(VARIABLE_SEPARATOR);
				if ( "n/a".equalsIgnoreCase(items[SolrPivotFacet.ENTITY_URI_STR.ordinal()]) ) {
					tmpLabel = VARIABLE_EMPTY_LABEL;
				} else {
					tmpLabel = FirstLabel.getPrettyLabel(items[SolrPivotFacet.ENTITY_URI_STR.ordinal()]);
					if (tmpLabel == null || tmpLabel.length() == 0) tmpLabel = VARIABLE_EMPTY_LABEL;
				}
				sb.append(items[SolrPivotFacet.ENTITY_URI_STR.ordinal()]).append("(").append(tmpLabel).append(")").append(VARIABLE_SEPARATOR);
				sb.append(items[SolrPivotFacet.DASA_URI_STR.ordinal()]).append("(");
				String indicatorLabel = retrieveIndicatorAndAttributeLabel(items[SolrPivotFacet.DASA_URI_STR.ordinal()]);
				if ( indicatorLabel == null || indicatorLabel.length() == 0 || (VARIABLE_EMPTY_LABEL+LABEL_SEPARATOR+VARIABLE_EMPTY_LABEL).equalsIgnoreCase(indicatorLabel)) {
					indicatorLabel = retrieveIndicatorAndAttributeLabel(items[SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]);
				}
				if ( indicatorLabel == null || indicatorLabel.length() == 0 || (VARIABLE_EMPTY_LABEL+LABEL_SEPARATOR+VARIABLE_EMPTY_LABEL).equalsIgnoreCase(indicatorLabel) ) {
					log.warn("Study search parsing pivot facet issue - cannot find indicator label:" + line);
				}
				sb.append(indicatorLabel).append(")").append(VARIABLE_SEPARATOR);
				if ( "n/a".equalsIgnoreCase(items[SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]) ) {
					tmpLabel = VARIABLE_EMPTY_LABEL;
				} else {
					tmpLabel = FirstLabel.getPrettyLabel(items[SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]);
					if (tmpLabel == null || tmpLabel.length() == 0) tmpLabel = VARIABLE_EMPTY_LABEL;
				}
				sb.append(items[SolrPivotFacet.IN_RELATION_TO_URI_STR.ordinal()]).append("(").append(tmpLabel).append(")").append(VARIABLE_SEPARATOR);
				if ( "n/a".equalsIgnoreCase(items[SolrPivotFacet.NAMED_TIME_STR.ordinal()]) ) {
					tmpLabel = VARIABLE_EMPTY_LABEL;
				} else {
					tmpLabel = FirstLabel.getPrettyLabel(items[SolrPivotFacet.NAMED_TIME_STR.ordinal()]);
					if (tmpLabel == null || tmpLabel.length() == 0) tmpLabel = VARIABLE_EMPTY_LABEL;
				}
				sb.append(items[SolrPivotFacet.NAMED_TIME_STR.ordinal()]).append("(").append(tmpLabel).append(")");
				result.add(sb.toString());
			}
		});

		return result;
	}

	private static List<String> parsePivotResult(final NamedList<List<PivotField>> pivotEntryList) {
		final Set<String> outputItems = new HashSet<>();
		for (final Map.Entry<String, List<PivotField>> pivotEntry : pivotEntryList) {
			//log.debug("Key: " + pivotEntry.getKey());
			pivotEntry.getValue().forEach((pivotField) -> {
				renderOutput(new StringBuilder(), pivotField, outputItems);
			});
		}
		final List<String> output = new ArrayList<>(outputItems);
		Collections.sort(output);
		return output;
	}

	private static void renderOutput(final StringBuilder sb, final PivotField field, final Set<String> outputItems) {

		final String HIERARCHICAL_FACET_SEPARATOR = ",", EMPTY_CONTENT = "n/a";
		final String fieldValue = field.getValue() != null ? ((String) field.getValue()).trim() : null;
		final StringBuilder outputBuilder = new StringBuilder(sb);
		if (field.getPivot() != null) {
			if (outputBuilder.length() > 0) {
				outputBuilder.append(HIERARCHICAL_FACET_SEPARATOR);
			}
			outputBuilder.append(fieldValue != null && fieldValue.length() > 0 ? fieldValue : EMPTY_CONTENT);
			// outputItems.add(new StringBuilder(outputBuilder).append(" (").append(field.getCount()).append(")").toString());
			// outputItems.add(new StringBuilder(outputBuilder).toString());
			field.getPivot().forEach((subField) -> {
				renderOutput(outputBuilder, subField, outputItems);
			});
		} else {
			if (outputBuilder.length() > 0) {
				outputBuilder.append(HIERARCHICAL_FACET_SEPARATOR);
			}
			outputBuilder.append(fieldValue != null && fieldValue.length() > 0 ? fieldValue : EMPTY_CONTENT);
			//outputItems.add(outputBuilder.append(" (").append(field.getCount()).append(") END").toString());
			outputItems.add(outputBuilder.toString());
		}
	}

	public static String retrieveIndicatorAndAttributeLabel(String targetUri) {
    	
		String studyQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT DISTINCT  ?attributeUri ?indicatorLabel ?attributeLabel " +
				"WHERE { \n" +
				"   <" + targetUri + "> ?x ?attributeUri . \n" +
				"   ?attributeUri rdfs:label ?attributeLabel . \n" +
				"   ?attributeUri rdfs:subClassOf* ?indicator . \n" +
				"	?indicator rdfs:label ?indicatorLabel . \n" +
				"   #FILTER(lang(?attributeLabel) != 'en') . \n" +
				"   { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . \n" +
				"} \n";

		String attributeLabel = VARIABLE_EMPTY_LABEL, indicatorLabel = VARIABLE_EMPTY_LABEL;
		Map<String, String> attributeMap = new HashMap<>();

		try {

			ResultSetRewindable resultsrw = SPARQLUtilsFacetSearch.select(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), studyQueryString);

			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();
				if (soln.contains("indicatorLabel")) {
					indicatorLabel = FirstLabel.getPrettyLabel(soln.get("indicatorLabel").toString());
				}
				if ( soln.contains("attributeLabel")) {
					attributeLabel = FirstLabel.getPrettyLabel(soln.get("attributeLabel").toString());
					if ( soln.contains("attributeUri") ) {
						String attributeUri = soln.get("attributeUri").toString();
						if (!attributeMap.containsKey(attributeUri)) {
							attributeMap.put(attributeUri, attributeLabel);
						}
					}
				}
			}

		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		if ( attributeMap.size() <= 1 ) {
			return attributeLabel + LABEL_SEPARATOR + indicatorLabel;
		} else {
			String attributeTag = null;
			for ( Map.Entry<String, String>  entry: attributeMap.entrySet() ) {
				for ( int i = 0; i < multiAttributeTag.length; i++ ) {
					if ( multiAttributeTag[i].equalsIgnoreCase(entry.getValue()) ) {
						attributeTag = multiAttributeTag[i];
						break;
					}
				}
			}
			if ( attributeTag == null || attributeTag.length() == 0 ) {
				return attributeLabel + LABEL_SEPARATOR + indicatorLabel;
			} else {
				StringBuffer sb = new StringBuffer();
				for (Map.Entry<String, String> entry : attributeMap.entrySet()) {
					if (attributeTag.equalsIgnoreCase(entry.getValue())) continue;
					if (sb.length() > 0) {
						sb.append(",").append(entry.getValue());
					} else sb.append(entry.getValue());
				}
				return attributeTag + " (" + sb.toString() + ")" + LABEL_SEPARATOR + indicatorLabel;
			}
		}

	}

    public String toString() {
    	//System.out.println("[" + attr.getLabel() + "]");
    	String str = "";
    	if (role != null && !role.isEmpty()) {
    		str += prep(role) + "-";
    	}
    	if (ent != null && ent.getLabel() != null && !ent.getLabel().isEmpty()) {
    		str += prep(ent.getLabel());
    	}
    	if (attrList != null && attrList.size() > 0) {
	    	for (Attribute attr : attrList) {
	    		if (attr != null && attr.getLabel() != null && !attr.getLabel().isEmpty()) {
	    			str += "-" + prep(attr.getLabel());
	    		}
	    	}
    	}
    	if (inRelationTo != null && !inRelationTo.getLabel().isEmpty()) {
    		str += "-" + prep(inRelationTo.getLabel());
    	}
    	if (unit != null && unit.getLabel() != null && !unit.getLabel().isEmpty()) {
    		str += "-" + prep(unit.getLabel());
    	}
    	if (timeAttr != null && timeAttr.getLabel() != null && !timeAttr.getLabel().isEmpty()) {
    		str += "-" + prep(timeAttr.getLabel());
    	}
    	return str;
    }

    // getStudyVariables()
	// getStudyVariablesWithLabels(Study studyUri)

}
