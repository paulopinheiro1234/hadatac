package org.hadatac.entity.pojo;

import com.fasterxml.jackson.annotation.JsonFilter;
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
import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.PropertyValueType;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.FirstLabel;
import org.hadatac.utils.NameSpaces;
import org.hadatac.vocabularies.HASCO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

@JsonFilter("variableSpecFilter")
public class VariableSpec extends HADatAcThing {

	private static final String className = "hasco:VariableSpec";

	private static final Logger log = LoggerFactory.getLogger(VariableSpec.class);
	public static final String LABEL_SEPARATOR = "|";
	public static final String VARIABLE_SEPARATOR = ";";
	public static final String VARIABLE_EMPTY_LABEL = "**";
	public static final String HIERARCHICAL_FACET_SEPARATOR = ",";
	public static final String EMPTY_CONTENT = "n/a";

	// patch for multi-valued attributes
	private static final String[] multiAttributeTag = { "Z-Score", "T-Score", "standard score", "Age Equivalent" };

	// Mandatory properties for Variable

	@PropertyField(uri="hasco:hasEntity", valueType=PropertyValueType.URI)
	private String entUri;
	private String entLabel;

	@PropertyField(uri="hasco:hasRole")
	private String role;

	@PropertyField(uri="hasco:hasAttribute")
	private List<String> attrListUri;
	private String attrListLabel;

	// Optional properties for Variables
	@PropertyField(uri="hasco:hasInRelationTo", valueType=PropertyValueType.URI)
    private String inRelationToUri;
	private String inRelationToLabel;

    private String relation;

	@PropertyField(uri="hasco:hasUnit", valueType=PropertyValueType.URI)
	private String unitUri;
	private String unitLabel;

	@PropertyField(uri="hasco:hasEvent", valueType=PropertyValueType.URI)
    private String timeAttrUri;
	private String timeAttrLabel;

	@PropertyField(uri="hasco:isCategorical")
	private boolean isCategorical;

    private List<PossibleValue> codebook;
	private Map<String, String> relations = new HashMap<String, String>();

	private static Map<String, VariableSpec> varSpecCache;

	private static Map<String, VariableSpec> getCache() {
		if (varSpecCache == null) {
			varSpecCache = new HashMap<String, VariableSpec>();
		}
		return varSpecCache;
	}
	public static void resetCache() {
		varSpecCache = null;
	}

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

	public VariableSpec() {
    	this.typeUri = HASCO.VARIABLE_SPEC;
    	this.hascoTypeUri = HASCO.VARIABLE_SPEC;
	}

    public VariableSpec(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel) {
    	this(entRole, attrInRel, null, null);
    }

    public VariableSpec(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit) {
    	this(entRole, attrInRel, unit, null);
    }

    public VariableSpec(AlignmentEntityRole entRole, AttributeInRelationTo attrInRel, Unit unit, Attribute timeAttr) {
		this(HASCO.VARIABLE_SPEC, HASCO.VARIABLE_SPEC, entRole.getEntity(), entRole.getRole(), attrInRel.getAttributeList(),
				attrInRel.getInRelationTo(), unit, timeAttr, false);
    }

	public VariableSpec(String typeUri, String hascoTypeUri , Entity ent, String role, List<Attribute> attrList, Entity inRelationTo, Unit unit, Attribute timeAttr, boolean isCategorical) {
		this.typeUri = typeUri;
		this.hascoTypeUri = hascoTypeUri;
		if (ent != null) {
			this.entUri = ent.getUri();
		}
		this.role = role;
		this.attrListUri = new ArrayList<String>();
		if (attrList != null) {
			for (Attribute attr : attrList) {
				this.attrListUri.add(attr.getUri());
			}
		}
		if (inRelationTo != null) {
			this.inRelationToUri = inRelationTo.getUri();
		}
		if (unit != null) {
			this.unitUri = unit.getUri();
		}
		if (timeAttr != null) {
			this.timeAttrUri = timeAttr.getUri();
		}
		this.isCategorical = false;
	}

	public VariableSpec(VariableSpec varSpec) {
		if (varSpec != null) {
			this.typeUri = varSpec.getTypeUri();
			this.hascoTypeUri = varSpec.getHascoTypeUri();
			this.entUri = varSpec.getEntityUri();
			this.role = varSpec.getRole();
			this.attrListUri = varSpec.getAttributeListUri();
			this.inRelationToUri = varSpec.getInRelationToUri();
			this.unitUri = varSpec.getUnitUri();
			this.timeAttrUri = varSpec.getTimeUri();
			this.isCategorical = varSpec.getIsCategorical();
		}
	}

	public VariableSpec(DataAcquisitionSchemaAttribute dasa) {
		if (dasa != null) {
			try {
				this.typeUri = HASCO.VARIABLE_SPEC;
				this.hascoTypeUri = HASCO.VARIABLE_SPEC;
				if (dasa.getEntity() != null && !dasa.getEntity().isEmpty()) {
					this.entUri = dasa.getEntity();
				}
				//this.role = dasa.getRole();
				if (dasa.getAttributes() != null && dasa.getAttributes().size() > 0) {
					this.attrListUri = dasa.getAttributes();
				}
				if (dasa.getInRelationToUri() != null && !dasa.getInRelationToUri().isEmpty()) {
					this.inRelationToUri = dasa.getInRelationToUri();
				}
				if (dasa.getUnit() != null && !dasa.getUnit().isEmpty()) {
					this.unitUri = dasa.getUnit();
				}
				if (dasa.getEventUri() != null && !dasa.getEventUri().isEmpty()) {
					this.timeAttrUri = dasa.getEventUri();
				}
				this.setLabel();
				this.setUri();
				System.out.println("Inside VariableSpec(dasa). label is [" + this.label + "]  uri is [" + this.uri + "]");
				this.comment = "A variable specification";
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public VariableSpec(List<VariableSpec> sourceList) {
		if (sourceList != null && sourceList.get(0) != null) {
			this.setEntityUri(sourceList.get(0).getEntityUri());
			this.setRole(sourceList.get(0).getRole());
			this.setAttributeListUri(sourceList.get(0).getAttributeListUri());
			this.setInRelationToUri(sourceList.get(0).getInRelationToUri());
			this.setUnitUri(sourceList.get(0).getUnitUri());
			this.setTimeUri(sourceList.get(0).getTimeUri());
		}
	}

	public String getKey() {
    	return getRole() + getEntityStr() + getAttributeListStr() + getInRelationToStr() + getUnitStr() + getTimeStr();
    }

    public void setUri() {
		final String kbPrefix = ConfigProp.getKbPrefix();
		String uriNs;
		if (this.label == null || this.label.isEmpty()) {
			uriNs = kbPrefix + "Var-Spec-" + this.toString();
		} else {
			uriNs = kbPrefix + "Var-Spec-" + this.label;
		}
		this.uri = URIUtils.replacePrefixEx(uriNs);
	}

	public void setLabel() {
		this.label = this.toString();
	}

	public Entity getEntity() {
		Entity ent = Entity.find(this.entUri);
    	return ent;
    }

    public String getEntityLabel() {
		if (this.entLabel != null) {
			return this.entLabel;
		}
		Entity ent = getEntity();
		if (ent == null) {
			this.entLabel = "";
			return this.entLabel;
		}
		return ent.getLabel();
	}

	public String getEntityUri() {
		return this.entUri;
	}

	public String getEntityStr() {
        if (entUri == null || entUri.isEmpty()) {
        	return "";
        }
    	return entUri;
    }

    public void setEntityUri(String entUri) {
    	this.entUri = entUri;
	}

    public String getRole() {
    	if (role == null) {
    		return "";
    	}
    	return role;
    }

	public void setRole(String role) {
		this.role = role;
	}

    public List<Attribute> getAttributeList() {
		List<Attribute> attrList = new ArrayList<Attribute>();
		if (attrListUri == null || attrListUri.size() <= 0) {
			return attrList;
		}
		for (String attrUri: attrListUri) {
			Attribute attr = Attribute.find(attrUri);
			if (attr != null) {
				attrList.add(attr);
			}
		}
    	return attrList;
    }

	public void setAttributeListUri(List<String> attrListUri) {
    	this.attrListUri = attrListUri;
	}

	public List<String> getAttributeListUri() {
		return this.attrListUri;
	}

	public String getAttributeListStr() {
    	if (attrListUri == null || attrListUri.isEmpty()) {
    		return "";
    	}
    	String resp = "";
    	for (String attrUri : attrListUri) {
    		if (attrUri != null && !attrUri.isEmpty()) {
        		resp = resp + attrUri;
    		}
    	}
        return resp;
    }

	public List<String> getAttributeListLabel() {
		List<String> attrListLabel = new ArrayList<String>();
		if (attrListUri == null || attrListUri.isEmpty()) {
			return attrListLabel;
		}
		List<Attribute> attrList = this.getAttributeList();
		if (attrList == null || attrList.size() <= 0) {
			return attrListLabel;
		}
		for (Attribute attr : attrList) {
			if (attr != null && attr.getLabel() != null) {
				attrListLabel.add(attr.getLabel());
			}
		}
		return attrListLabel;
	}

	public Entity getInRelationTo() {
		if (this.inRelationToUri == null || this.inRelationToUri.isEmpty()) {
			return null;
		}
		return Entity.find(this.inRelationToUri);
    }

	public String getInRelationToUri() {
		return inRelationToUri;
	}

	public void setInRelationToUri(String inRelationToUri) {
    	this.inRelationToUri = inRelationToUri;
	}

	public String getInRelationToStr() {
        if (inRelationToUri == null || inRelationToUri.isEmpty()) {
            return "";
        }
    	return inRelationToUri;
    }

	public String getInRelationToLabel() {
		if (this.inRelationToLabel != null) {
			return this.inRelationToLabel;
		}
		Entity inRelationTo = getInRelationTo();
		if (inRelationTo == null) {
			this.inRelationToLabel = "";
			return this.inRelationToLabel;
		}
		return inRelationTo.getLabel();
	}

	public List<String> getRelationsList() {
		return new ArrayList(relations.values());
	}

	public void addRelation(String key, String relation) {
		relations.put(key, relation);
	}

	public Unit getUnit() {
		if (unitUri == null || unitUri.isEmpty()) {
			return null;
		}
    	return Unit.find(unitUri);
    }

	public void setUnitUri(String unitUri) {
    	this.unitUri = unitUri;
	}

	public String getUnitUri() {
		return unitUri;
	}

	public String getUnitStr() {
        if (unitUri == null || unitUri.isEmpty()) {
            return "";
        }
    	return unitUri;
    }

	public String getUnitLabel() {
		if (this.unitLabel != null) {
			return this.unitLabel;
		}
		Unit unit = getUnit();
		if (unit == null) {
			this.unitLabel = "";
			return this.unitLabel;
		}
		return unit.getLabel();
	}

	public Attribute getTime() {
  		if (timeAttrUri == null || timeAttrUri.isEmpty()) {
  			return null;
		}
    	return Attribute.find(timeAttrUri);
    }

	public void setTimeUri(String timeAttrUri) {
		this.timeAttrUri = timeAttrUri;
	}

	public String getTimeUri() {
		return timeAttrUri;
	}

	public String getTimeStr() {
        if (timeAttrUri == null || timeAttrUri.isEmpty()) {
            return "";
        }
    	return timeAttrUri;
    }

	public String getTimeLabel() {
		if (this.timeAttrLabel != null) {
			return this.timeAttrLabel;
		}
		Attribute timeAttr = getTime();
		if (timeAttr == null) {
			this.timeAttrLabel = "";
			return this.timeAttrLabel;
		}
		return timeAttr.getLabel();
	}

	public boolean getIsCategorical() {
		return isCategorical;
	}

	public List<PossibleValue> getCodebook() {
		return codebook;
	}

	public void addPossibleValue(PossibleValue possibleValue) {
		if (possibleValue == null) {
			return;
		}
		if (codebook == null) {
			codebook = new ArrayList<PossibleValue>();
		}
		codebook.add(possibleValue);
		return;
	}

	public void setCodebook(List<PossibleValue> codebook) {
		this.codebook = codebook;
	}

	public boolean isCategorical() {
		return isCategorical;
	}

	public void setIsCategorical(boolean isCategorical) {
    	this.isCategorical = isCategorical;
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

    public static String prep(String orig) {
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
			log.info("\n\n\nStudy Search Update result:");
			parsedPivotResult.forEach((s) -> {
				log.info(s);
			});
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
				"   <" + variableUri + "> (hasco:hasAttribute/rdf:rest*/rdf:first | hasco:hasEntity) | rdfs:subClassOf  | \n" +
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

	public static VariableSpec find(String spec_uri) {

		try {
			if (VariableSpec.getCache().get(spec_uri) != null) {
				return VariableSpec.getCache().get(spec_uri);
			}
			VariableSpec varSpec = null;
			//System.out.println("Looking for variable spec with URI <" + spec_uri + ">");

			String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() +
					"SELECT ?hasEntity ?hasAttribute ?hascoTypeUri " +
					" ?hasUnit ?hasDASO ?hasDASE ?relation ?inRelationTo ?label WHERE { \n" +
					"    <" + spec_uri + "> a hasco:VariableSpec . \n" +
					"    <" + spec_uri + "> hasco:hascoType hasco:VariableSpec . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:hasEntity ?hasEntity } . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:hasAttribute/rdf:rest*/rdf:first ?hasAttribute } . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:hasUnit ?hasUnit } . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:hasEvent ?hasDASE } . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:isAttributeOf ?hasDASO } . \n" +
					"    OPTIONAL { <" + spec_uri + "> hasco:Relation ?relation . \n" +
					"               <" + spec_uri + "> ?relation ?inRelationTo . } . \n" +
					"    OPTIONAL { <" + spec_uri + "> rdfs:label ?label } . \n" +
					"}";

			//System.out.println("VariableSpec find() queryString: \n" + queryString);

			ResultSetRewindable resultsrw = SPARQLUtils.select(
					CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

			if (!resultsrw.hasNext()) {
				System.out.println("[WARNING] VariableSpec. Could not find VarSpec with URI: <" + spec_uri + ">");
				return varSpec;
			}

			String localNameStr = "";
			String labelStr = "";
			String entityStr = "";
			String attributeStr = "";
			List<String> attributeList = new ArrayList<String>();
			String unitStr = "";
			String dasoUriStr = "";
			String daseUriStr = "";
			String inRelationToUri = "";
			String relationUri = "";

			Map<String, String> relationMap = new HashMap<>();
			while (resultsrw.hasNext()) {
				QuerySolution soln = resultsrw.next();

				/*
				 *  The label should be the exact value in the SDD, e.g., cannot be altered be something like
				 *  FirstLabel.getPrettyLabel(spec_uri) since that would prevent the matching of the label with
				 *  the column header of the data acquisition file/message
				 */
				labelStr = soln.get("label").toString();

				if (soln.get("hasEntity") != null) {
					entityStr = soln.get("hasEntity").toString();
				}
				if (soln.get("hasAttribute") != null) {
					attributeList.add(soln.get("hasAttribute").toString());
				}
				if (soln.get("hasUnit") != null) {
					unitStr = soln.get("hasUnit").toString();
				}
				if (soln.get("hasDASO") != null) {
					dasoUriStr = soln.get("hasDASO").toString();
				}
				if (soln.get("hasDASE") != null) {
					daseUriStr = soln.get("hasDASE").toString();
				}
				if (soln.get("inRelationTo") != null) {
					inRelationToUri = soln.get("inRelationTo").toString();
				}
				if (soln.get("relation") != null) {
					relationUri = soln.get("relation").toString();
				}

				if (relationUri != null && relationUri.length() > 0 && inRelationToUri != null && inRelationToUri.length() > 0) {
					relationMap.put(relationUri, inRelationToUri);
					relationUri = "";
					inRelationToUri = "";
				}

			}

			//System.out.println("Variable spec [" + spec_uri + "]. Entity Str is [" + entityStr + "]");

			Entity entity = Entity.find(entityStr);
			Entity inRelationTo = Entity.find(inRelationToUri);
			List<Attribute> attrList = new ArrayList<Attribute>();
			for (String attrUri : attributeList) {
				Attribute attr = Attribute.find(attrUri);
				if (attr != null) {
					attrList.add(attr);
				}
			}
			Unit unit = Unit.find(unitStr);
			Attribute timeAttr = Attribute.find(daseUriStr);

			varSpec = new VariableSpec(HASCO.VARIABLE_SPEC, HASCO.VARIABLE_SPEC, entity, "", attrList, inRelationTo, unit, timeAttr, false);

			varSpec.setUri(spec_uri);

			for (Map.Entry<String, String> entry : relationMap.entrySet()) {
				varSpec.addRelation(entry.getKey(), entry.getValue());
			}

			VariableSpec.getCache().put(spec_uri, varSpec);

			return varSpec;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static String toString(String role, Entity ent, List<Attribute> attrList, Entity inRelationTo, Unit unit, Attribute timeAttr) {
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

	public String toString() {
		String toString = VariableSpec.toString(role, this.getEntity(), this.getAttributeList(), this.getInRelationTo(), this.getUnit(), this.getTime());
		return toString;
    }

	@Override
	public void save() {
		System.out.println("inside VariableSpec.save()");
		this.saveToTripleStore();
	}

	@Override
	public boolean saveToSolr() {
		return true;
	}

	@Override
	public int deleteFromSolr() {
		return 0;
	}

}
