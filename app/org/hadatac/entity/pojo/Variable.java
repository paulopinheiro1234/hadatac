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
	public static final String HIERARCHICAL_FACET_SEPARATOR = ",";
	public static final String EMPTY_CONTENT = "n/a";

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

	public static List<String> retrieveStudySearchFacetResult(String studyUri) {

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
			return parsedPivotResult;
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}
		return null;
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

	static public boolean constainsStandardTestScore(List<Pair<String, String>> indicators) {
    	for ( Pair<String, String> pair: indicators ) {
			for ( int i = 0; i < multiAttributeTag.length; i++ ) {
				if ( multiAttributeTag[i].equalsIgnoreCase(FirstLabel.getPrettyLabel(pair.getLeft())) ) return true;
			}
		}
		return false;
	}

	static public String retrieveTestScoreLabel(List<Pair<String, String>> indicators) {
    	for ( Pair<String, String> pair: indicators ) {
			for ( int i = 0; i < multiAttributeTag.length; i++ ) {
				if ( multiAttributeTag[i].equalsIgnoreCase(FirstLabel.getPrettyLabel(pair.getLeft())) ) {
					return FirstLabel.getPrettyLabel(pair.getLeft());
				}
			}
		}
		return null;
	}

	/*
		Pair<String, String> would be something like this: <attributeUri, indicatorUri>
		for example, <http://purl.obolibrary.org/obo/CMO_0000105, http://purl.org/twc/HHEAR_00029>
	 */
	public static List<Pair<String, String>> computeIndicatorList(String variableUri) {

		String studyQueryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
				"SELECT DISTINCT  ?attributeUri ?indicator " +
				"WHERE { \n" +
				"   <" + variableUri + "> (hasco:hasAttribute | hasco:hasEntity) | rdfs:subClassOf  | \n" +
				"   (<http://semanticscience.org/resource/SIO_000668> / hasco:hasEntity) ?attributeUri . \n" +
				"   ?attributeUri rdfs:subClassOf* ?indicator . \n" +
				"   { ?indicator rdfs:subClassOf hasco:SampleIndicator } UNION { ?indicator rdfs:subClassOf hasco:StudyIndicator } . \n" +
				"} \n";

		Set<String> seen = new HashSet<>();
		List<Pair<String, String>> labelPairs = new ArrayList<>();  // <attributeUri, indicatorUri> is the pair

		try {

			ResultSetRewindable resultsrw = SPARQLUtilsFacetSearch.select(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), studyQueryString);

			while (resultsrw.hasNext()) {

				QuerySolution soln = resultsrw.next();

				if (soln.contains("indicator") == false ) {
					log.warn("Study Search: this variable [" + variableUri + "], does not have a indicator URI");
					continue;
				}

				if ( soln.contains("attributeUri") == false ) {
					log.warn("Study Search: this variable [" + variableUri + "], does not have a attribute URI");
					continue;
				}

				final String attributeUri = soln.get("attributeUri").toString();
				final String indicatorUri = soln.get("indicator").toString();

				if ( !seen.contains(attributeUri.toLowerCase() + "-" + indicatorUri.toLowerCase()) ) {
					labelPairs.add(new Pair<String, String>(attributeUri, indicatorUri));
					seen.add(attributeUri.toLowerCase() + "-" + indicatorUri.toLowerCase().toLowerCase());
				}
			}

		} catch (QueryExceptionHTTP e) {
			e.printStackTrace();
		}

		return labelPairs;

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
