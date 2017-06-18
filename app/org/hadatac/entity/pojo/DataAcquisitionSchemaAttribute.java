package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.Collections;
import org.hadatac.utils.NameSpaces;

public class DataAcquisitionSchemaAttribute {

        private String uri;
        private String localName;
        private String position;
        private int    positionInt;
    	private String entity;
    	private String entityLabel;
    	private String attribute;
    	private String attributeLabel;
    	private String unit;
    	private String unitLabel;
        private String daso;
        private String dase;
    	
    public DataAcquisitionSchemaAttribute(String uri, String localName, String position, String entity, String entityLabel, 
                                          String attribute, String attributeLabel, String unit, String unitLabel, 
                                          String daso, String dase) {
	    this.uri = uri;
	    this.localName = localName;
	    this.position = position;
	    try {
		if (position != null && !position.equals("")) {
                    positionInt = Integer.parseInt(position);
		} else {
		    positionInt = -1;
		}
	    } catch (Exception e) {
		positionInt = -1;
	    }
	    this.entity = entity;
	    this.entityLabel = entityLabel;
	    this.attribute = attribute;
	    this.attributeLabel = attributeLabel;
	    this.unit = unit;
	    this.unitLabel = unitLabel;
            this.daso = daso;
            this.dase = dase;
	}
    	
        public String getUri() {
	    return uri;
	}

        public String getLocalName() {
	    return localName;
	}

    	public String getPosition() {
	    return position;
	}

    	public int getPositionInt() {
	    return positionInt;
	}

    	public String getEntity() {
	    return entity;
	}

    	public String getEntityLabel() {
	    return entityLabel;
	}

    	public String getAttribute() {
	    return attribute;
	}

    	public String getAttributeLabel() {
	    return attributeLabel;
	}

    	public String getUnit() {
	    return unit;
	}
        
    	public String getUnitLabel() {
	    return unitLabel;
	}
        
    	public String getObjectUri() {
	    return daso;
	}
        
    	public String getEventUri() {
	    return dase;
	}
        
        public static List<DataAcquisitionSchemaAttribute> findBySchema (String schemaUri) {
	     System.out.println("Looking for data acquisition schema attributes for " + schemaUri);
     	     if (schemaUri.startsWith("http")) {
	        schemaUri = "<" + schemaUri + ">";
	     }
	     List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
    	     String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
    			"SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
    			"   ?uri a hasco:DASchemaAttribute . " + 
    			"   ?uri hasco:partOfSchema " + schemaUri + " .  " + 
    			"   ?uri hasco:hasPosition ?hasPosition .  " + 
    			"   OPTIONAL { ?uri hasco:hasEntity ?hasEntity } . " + 
    			"   OPTIONAL { ?uri hasco:hasAttribute ?hasAttribute } . " + 
    			"   OPTIONAL { ?uri hasco:hasUnit ?hasUnit } . " + 
    			"   OPTIONAL { ?uri hasco:isAttributeOf ?hasDASO } . " + 
    			"   OPTIONAL { ?uri hasco:hasEvent ?hasDASE } . " + 
    			"   OPTIONAL { ?uri hasco:hasSource ?hasSource } . " + 
    			"   OPTIONAL { ?uri hasco:isPIConfirmed ?isPIConfirmed } . " + 
    			"}";
    	     Query query = QueryFactory.create(queryString);
		
    	     QueryExecution qexec = QueryExecutionFactory.sparqlService(
		    Collections.getCollectionsName(Collections.METADATA_SPARQL), query);
    	     ResultSet results = qexec.execSelect();
	     ResultSetRewindable resultsrw = ResultSetFactory.copyResults(results);
	     qexec.close();

	     if (!resultsrw.hasNext()) {
		   System.out.println("[WARNING] DataAcquisitionSchemaAttribute. Could not find attributes for schema: " + schemaUri);
		   return attributes;
	     }
		
	     String uriStr = "";
	     String localNameStr = "";
	     String positionStr = "";
	     String entityStr = "";
	     String entityLabelStr = "";
	     String attributeStr = "";
	     String attributeLabelStr = "";
	     String unitStr = "";
	     String unitLabelStr = "";
	     String dasoStr = "";
	     String daseStr = "";
		  
	     while (resultsrw.hasNext()) {
		      QuerySolution soln = resultsrw.next();
		      if (soln != null) {

			 try {
			     if (soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
			         uriStr = soln.getResource("uri").getURI();
			         localNameStr = soln.getResource("uri").getLocalName();
			     }
			 } catch (Exception e1) {
			     uriStr = "";
			     localNameStr = "";
			 }

			 try {
			     if (soln.getLiteral("hasPosition") != null && soln.getLiteral("hasPosition").getString() != null) {
				 positionStr = soln.getLiteral("hasPosition").getString();
			     }
			 } catch (Exception e1) {
			     positionStr = "";
			 }

			 try {
			     if (soln.getResource("hasEntity") != null && soln.getResource("hasEntity").getURI() != null) {
			         entityStr = soln.getResource("hasEntity").getURI();
			     }
			 } catch (Exception e1) {
			     entityStr = "";
			 }

			 entityLabelStr = DataAcquisitionSchema.getFirstLabel(entityStr); 
			 if (entityLabelStr.equals("")) {
			     entityLabelStr = entityStr.substring(entityStr.indexOf("#") + 1);
			 }

			 try {
			     if (soln.getResource("hasAttribute") != null && soln.getResource("hasAttribute").getURI() != null) {
			         attributeStr = soln.getResource("hasAttribute").getURI();
			     }
			 } catch (Exception e1) {
			     attributeStr = "";
			 }

			 attributeLabelStr = DataAcquisitionSchema.getFirstLabel(attributeStr); 
			 if (attributeLabelStr.equals("")) {
			     attributeLabelStr = attributeStr.substring(attributeStr.indexOf("#") + 1);
			 }

			 try {
			     if (soln.getResource("hasUnit") != null && soln.getResource("hasUnit").getURI() != null) {
				 unitStr = soln.getResource("hasUnit").getURI();
			     }
			 } catch (Exception e1) {
			     unitStr = "";
			 }

			 unitLabelStr = DataAcquisitionSchema.getFirstLabel(unitStr); 
			 if (unitLabelStr.equals("")) {
			     unitLabelStr = unitStr.substring(unitStr.indexOf("#") + 1);
			 }

			 try {
			     if (soln.getResource("hasDASO") != null && soln.getResource("hasDASO").getURI() != null) {
				 dasoStr = soln.getResource("hasDASO").getURI();
			     }
			 } catch (Exception e1) {
			     dasoStr = "";
			 }

			 try {
			     if (soln.getResource("hasDASE") != null && soln.getResource("hasDASE").getURI() != null) {
				 daseStr = soln.getResource("hasDASE").getURI();
			     }
			 } catch (Exception e1) {
			     daseStr = "";
			 }

			 if (!uriStr.equals("") && !localNameStr.equals("")) {
			     DataAcquisitionSchemaAttribute attr = new DataAcquisitionSchemaAttribute(
			        uriStr,
			        localNameStr,
				positionStr,
				entityStr,
				entityLabelStr,
				attributeStr,
				attributeLabelStr,
				unitStr,
				unitLabelStr,
				dasoStr,
				daseStr
			     );
			     attributes.add(attr);
			 } else {
			     System.out.println("[ERROR] DataAcquisitionSchemaAttribute. URI: " + uriStr + "  Position: " + positionStr);
			 }
                  }

	     }
	     return attributes;
	}
    
}
