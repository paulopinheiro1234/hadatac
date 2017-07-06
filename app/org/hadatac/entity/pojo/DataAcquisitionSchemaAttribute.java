package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;

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
import org.hadatac.utils.FirstLabel;
import org.hadatac.entity.pojo.DataAcquisitionSchemaObject;
import org.hadatac.entity.pojo.DataAcquisitionSchemaEvent;
import org.hadatac.metadata.loader.ValueCellProcessing;

public class DataAcquisitionSchemaAttribute {

    private String uri;
    private String localName;
    private String label;
    private String position;
    private int    positionInt;
    
    /* 
       tempPositionInt is set every time a new csv file is loaded. tempPositionIn = -1 indicates that the attribute is not valid for the given cvs
         - because an original position is out of range for the csv
         - because there is no original position and the given localName does not match any of the labels in the CSV
       
       tempPositionInt is set as follows:
         - if a DASA has a position, and the position is within range for the given csv, then the temp if the 
       
    */

    private int    tempPositionInt;
    private String entity;
    private String entityLabel;
    private String attribute;
    private String attributeLabel;
    private String unit;
    private String unitLabel;
    private String daseUri;
    private String dasoUri;
    
    public DataAcquisitionSchemaAttribute(String uri, 
					  String localName, 
					  String label, 
					  String position, 
					  String entity, 
					  String entityLabel, 
                                          String attribute, 
					  String attributeLabel, 
					  String unit, 
					  String unitLabel, 
                                          String daseUri, 
					  String dasoUri) {
	    this.uri = uri;
	    this.localName = localName;
	    this.label = label;
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
            this.daseUri = daseUri;
            this.dasoUri = dasoUri;
	}
    	
        public String getUri() {
	    return uri;
	}

        public String getLocalName() {
	    return localName;
	}

        public String getLabel() {
	    return label;
	}

    	public String getPosition() {
	    return position;
	}

    	public int getPositionInt() {
	    return positionInt;
	}

    	public int getTempPositionInt() {
	    return tempPositionInt;
	}

    	public void setTempPositionInt(int tempPositionInt) {
	    this.tempPositionInt = tempPositionInt;
	}

    	public String getEntity() {
	    return entity;
	}

    	public String getEntityLabel() {
	    if (entityLabel.equals("")) {
		return ValueCellProcessing.replaceNameSpaceEx(entity);
	    }
	    return entityLabel;
	}

    	public String getAttribute() {
	    return attribute;
	}

    	public String getAttributeLabel() {
	    if (attributeLabel.equals("")) {
		return ValueCellProcessing.replaceNameSpaceEx(attribute);
	    }
	    return attributeLabel;
	}

    	public String getUnit() {
	    return unit;
	}
        
    	public String getUnitLabel() {
	    if (unitLabel.equals("")) {
		return ValueCellProcessing.replaceNameSpaceEx(unit);
	    }
	    return unitLabel;
	}
        
    	public String getObjectUri() {
	    return dasoUri;
	}
        
    	public DataAcquisitionSchemaObject getObject() {
	    if (dasoUri == null || dasoUri.equals("")) {
		return null;
	    }
	    return DataAcquisitionSchemaObject.find(dasoUri);
	}
        
    	public String getEventUri() {
	    return daseUri;
	}
        
    	public DataAcquisitionSchemaEvent getEvent() {
	    if (daseUri == null || daseUri.equals("")) {
		return null;
	    }
	    return DataAcquisitionSchemaEvent.find(daseUri);
	}
        
        public static List<DataAcquisitionSchemaAttribute> findBySchema (String schemaUri) {
	     System.out.println("Looking for data acquisition schema attributes for " + schemaUri);
     	     if (schemaUri.startsWith("http")) {
	        schemaUri = "<" + schemaUri + ">";
	     }
	     List<DataAcquisitionSchemaAttribute> attributes = new ArrayList<DataAcquisitionSchemaAttribute>();
    	     String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
		 "SELECT ?uri ?hasPosition ?hasEntity ?hasAttribute " + 
		 " ?hasUnit ?hasDASO ?hasDASE ?hasSource ?isPIConfirmed WHERE { " + 
		 "    ?uri a hasco:DASchemaAttribute . " + 
		 "    ?uri hasco:partOfSchema " + schemaUri + " .  " + 
		 "    ?uri hasco:hasPosition ?hasPosition .  " + 
		 "    OPTIONAL { ?uri hasco:hasEntity ?hasEntity } . " + 
		 "    OPTIONAL { ?uri hasco:hasAttribute ?hasAttribute } . " + 
		 "    OPTIONAL { ?uri hasco:hasUnit ?hasUnit } . " + 
		 "    OPTIONAL { ?uri hasco:hasEvent ?hasDASE } . " + 
		 "    OPTIONAL { ?uri hasco:isAttributeOf ?hasDASO } . " + 
		 "    OPTIONAL { ?uri hasco:hasSource ?hasSource } . " + 
		 "    OPTIONAL { ?uri hasco:isPIConfirmed ?isPIConfirmed } . " + 
		 "} ORDER BY ?hasPosition ";
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
	     String labelStr = "";
	     String positionStr = "";
	     String entityStr = "";
	     String entityLabelStr = "";
	     String attributeStr = "";
	     String attributeLabelStr = "";
	     String unitStr = "";
	     String unitLabelStr = "";
	     String dasoUriStr = "";
	     String daseUriStr = "";
		  
	     while (resultsrw.hasNext()) {
		      QuerySolution soln = resultsrw.next();
		      if (soln != null) {

			 try {
			     if (soln.getResource("uri") != null && soln.getResource("uri").getURI() != null) {
			         uriStr = soln.getResource("uri").getURI();
			         localNameStr = soln.getResource("uri").getLocalName();
				 labelStr = FirstLabel.getLabel(uriStr);
			     }
			 } catch (Exception e1) {
			     uriStr = "";
			     localNameStr = "";
			     labelStr = "";
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
				 entityLabelStr = FirstLabel.getLabel(entityStr);
			     }
			 } catch (Exception e1) {
			     entityStr = "";
			     entityLabelStr = "";
			 }

			 try {
			     if (soln.getResource("hasAttribute") != null && soln.getResource("hasAttribute").getURI() != null) {
			         attributeStr = soln.getResource("hasAttribute").getURI();
				 attributeLabelStr = FirstLabel.getLabel(attributeStr);
			     }
			 } catch (Exception e1) {
			     attributeStr = "";
			     attributeLabelStr = "";
			 }

			 try {
			     if (soln.getResource("hasUnit") != null && soln.getResource("hasUnit").getURI() != null) {
				 unitStr = soln.getResource("hasUnit").getURI();
				 unitLabelStr = FirstLabel.getLabel(unitStr);
			     }
			 } catch (Exception e1) {
			     unitStr = "";
			     unitLabelStr = "";
			 }

			 try {
			     if (soln.getResource("hasDASO") != null && soln.getResource("hasDASO").getURI() != null) {
				 dasoUriStr = soln.getResource("hasDASO").getURI();
			     }
			 } catch (Exception e1) {
			     dasoUriStr = "";
			 }

			 try {
			     if (soln.getResource("hasDASE") != null && soln.getResource("hasDASE").getURI() != null) {
				 daseUriStr = soln.getResource("hasDASE").getURI();
			     }
			 } catch (Exception e1) {
			     daseUriStr = "";
			 }

			 if (!uriStr.equals("") && !localNameStr.equals("")) {
			     DataAcquisitionSchemaAttribute attr = new DataAcquisitionSchemaAttribute(
			        uriStr,
			        localNameStr,
				labelStr,
				positionStr,
				entityStr,
				entityLabelStr,
				attributeStr,
				attributeLabelStr,
				unitStr,
				unitLabelStr,
				daseUriStr,
				dasoUriStr
			     );
			     attributes.add(attr);
			 } else {
			     System.out.println("[ERROR] DataAcquisitionSchemaAttribute. URI: " + uriStr + "  Position: " + positionStr);
			 }
                  }

	     }
	     attributes.sort(Comparator.comparing(DataAcquisitionSchemaAttribute::getPositionInt));
	     return attributes;
	}
    
}
