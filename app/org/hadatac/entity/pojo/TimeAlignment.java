package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TimeAlignment {

    private Map<String, StudyObject> objects;
    //private List<String> timestamps;
    private Map<String, StudyObject> refObjects;
    private Map<String, List<Attribute>> attributeListCache;
    private Map<String, Attribute> attrCache;
    private Map<String, Entity> entityCache;
    private Map<String, Unit> unitCache;
    private Map<String, AlignmentEntityRole> roles;
    private Map<String, TimeVariable> variables;
    private Map<String, List<String>> hCodeBook;
    private Map<String, String> studyId;  // key=socUri;  value=studyId

    //AttributeInRelationTo ID_IRT = new AttributeInRelationTo(ID, null);

    public TimeAlignment() {
        objects = new HashMap<String, StudyObject>();
        attrCache = new HashMap<String, Attribute>();
        attributeListCache = new HashMap<String, List<Attribute>>();
        entityCache = new HashMap<String, Entity>();
        unitCache = new HashMap<String, Unit>();
        roles = new HashMap<String, AlignmentEntityRole>();
        variables = new HashMap<String, TimeVariable>();
	    hCodeBook = new HashMap<String, List<String>>();
	    studyId = new HashMap<String,String>();
    }

    public void printAlignment() {
        System.out.println("Alignment Content: ");
        if (variables != null && variables.size() > 0) {
            for (TimeVariable aa : variables.values()) {
                System.out.println("Label: " + aa);
            }
        }
    }

    /* returns a key to retrieve variables. if needed, measuremtnKey adds new variables 
     */
    public String timeMeasurementKey(Measurement m) {
        if (variables == null) {
            System.out.println("[ERROR] tiime alignment attribute list not initialized ");
            return null;
        }

        /* 
         * Look for existing variables
         */
        
        //System.out.println("Align-Debug: Time Measurement Key");

        StudyObject obj = null; 
        String mObj = "";
        if (m.getObjectUri() != null && !m.getObjectUri().equals("")) {
            obj = objects.get(m.getObjectUri());
            if (obj != null) {
                mObj = obj.getOriginalId();
            } else {
                obj = StudyObject.find(m.getObjectUri());
                if (obj == null) {
                    System.out.println("[ERROR] could not retrieve object [" + m.getObjectUri() + "]. Ignoring Object.");
                } else {
                    objects.put(obj.getUri(),obj);
                    mObj = obj.getOriginalId(); 
                }
            }
        } 

        Entity irt = null;
        String mInRelationTo = "";
        if (m.getInRelationToUri() != null && !m.getInRelationToUri().equals("")) {
            irt = entityCache.get(m.getInRelationToUri());
            if (irt != null && irt.getUri().equals(m.getInRelationToUri())) {
                mInRelationTo = irt.getUri();
            } else {		
                irt = Entity.find(m.getInRelationToUri());
                if (irt == null) {
                    System.out.println("[ERROR] retrieving org.hadatac.entity playing inRelationTo " + m.getInRelationToUri());
                } else {
                    entityCache.put(irt.getUri(),irt);
                    mInRelationTo = m.getInRelationToUri();
                }
            }
        } 
        Unit unit = null;
        String mUnit = "";
        if (m.getUnitUri() != null && !m.getUnitUri().equals("")) {
            unit = unitCache.get(m.getUnitUri());
            if (unit != null && unit.getUri().equals(m.getUnitUri())) {
                mUnit = unit.getUri();
            } else {
                unit = Unit.find(m.getUnitUri());
                if (unit == null) {
                    System.out.println("[ERROR] could not retrieve unit [" + m.getUnitUri() + "]. Ignoring unit.");
                } else {
                    unitCache.put(unit.getUri(),unit);
                    mUnit = m.getUnitUri();
                }
            }
        } 

	    String mRole = m.getRole().replace(" ","");

        String mKey =  mObj + mRole + m.getEntityUri() + m.getCharacteristicUris().get(0) + mInRelationTo + mUnit;

        //System.out.println("Align-Debug: Measurement: " + mKey);
        //System.out.println("Align-Debug: Vector: " + alignAttrs); 

        if (variables.containsKey(mKey)) {
            return variables.get(mKey).toString();
        }

        /* 
         * create new variable
         */

        TimeVariable newVar;

        Entity entity = entityCache.get(m.getEntityUri());
        if (entity == null || !entity.getUri().equals(m.getEntityUri())) {
            entity = Entity.find(m.getEntityUri());
            if (entity == null) {
                System.out.println("[ERROR] retrieving org.hadatac.entity " + m.getEntityUri());
                return null;
            } else {
                entityCache.put(entity.getUri(),entity);
            }
        }

        //System.out.println("Align-Debug: new alignment attribute"); 
        AlignmentEntityRole newRole = new AlignmentEntityRole(entity,mRole);

        //System.out.println("Align-Debug: new alignment characteristic: [" + m.getCharacteristicUris().get(0) + "]"); 

        Attribute attribute = null;
        List<Attribute> attributeList = null;
        if (m.getCategoricalClassUri() != null && !m.getCategoricalClassUri().isEmpty()) {
	        attribute = attributeListCache.get(m.getCategoricalClassUri()).get(0);
	        if (attribute == null || !attribute.getUri().equals(m.getCategoricalClassUri())) {
	        	attribute = attrCache.get(m.getCategoricalClassUri());
	        	if (attribute == null) {
	        		attribute = Attribute.find(m.getCategoricalClassUri());
	        		if (attribute == null) {
	        			System.out.println("[ERROR] retrieving attribute " + m.getCategoricalClassUri());
	        			return null;
	        		}
	        		attrCache.put(attribute.getUri(), attribute);
	        	}
	            attributeList = new ArrayList<Attribute>();
	            attributeList.add(attribute);
	            attributeListCache.put(attribute.getUri(), attributeList);
	        }
        } else {
        	attributeList = attributeListCache.get(Alignment.getUrisFromStringList(m.getCharacteristicUris()));
	        if (attributeList == null || !Alignment.getUrisFromAttributeList(attributeList).equals(Alignment.getUrisFromStringList(m.getCharacteristicUris()))) {
	        	attributeList = new ArrayList<Attribute>();
	            for (String attrUri : m.getCharacteristicUris()) {
		        	attribute = attrCache.get(attrUri);
		        	if (attribute == null) {
		        		attribute = Attribute.find(attrUri);
		        		if (attribute == null) {
		        			System.out.println("[ERROR] retrieving attribute " + attrUri);
		        			return null;
		        		}
		        		attrCache.put(attribute.getUri(), attribute);
		        	}
		        	attributeList.add(attribute);
	            }
                attributeListCache.put(Alignment.getUrisFromAttributeList(attributeList),attributeList);
	        }
        }

        //System.out.println("Align-Debug: new alignment attribute 2"); 

        //if (!mInRelationTo.equals("")) {
        //    System.out.println("Adding the following inRelationTo " + mInRelationTo);
        //}

        AttributeInRelationTo newAttrInRel = new AttributeInRelationTo(attributeList, irt); 

        //if (!mUnit.equals("")) {
        //    System.out.println("Adding the following unit " + mUnit);
        //}

        newVar = new TimeVariable(obj, newRole, newAttrInRel, unit);
        //System.out.println("Align-Debug: new alignment attribute 3"); 

        if (!variables.containsKey(newVar.getKey())) {
            variables.put(newVar.getKey(), newVar);
            return newVar.toString();
        }

        return null;

    }

    /* CONTAINS METHODS
     */

    public boolean containsObject(String uri) {
        return objects.containsKey(uri);
    }

    //public boolean containsTimestamp(String timestamp) {
    //    return timestamp.contains(timestamp);
    //}

    public boolean containsEntity(String uri) {
        return entityCache.containsKey(uri);
    }

    public boolean containsRole(StudyObject obj, String entKey) {
    	String roleKey = obj.getOriginalId() + entKey;
        return roles.containsKey(roleKey);
    }

    public boolean containsCode(String uri) {
        return hCodeBook.containsKey(uri);
    }

    /* GET INDIVIDUAL METHODS
     */

    public StudyObject getObject(String uri) {
    	return objects.get(uri);
    }

    public Entity getEntity(String uri) {
        return entityCache.get(uri);
    }

    public AlignmentEntityRole getRole(StudyObject obj, String entKey) {
        return roles.get(obj.getUri() + entKey);
    }

    public List<String> getCode(String key) {
        return hCodeBook.get(key);
    }

    public Map<String, List<String>> getCodeBook() {
    	return hCodeBook;
    }
    
    public String getStudyId(String uri) {
        return studyId.get(uri);
    }

    /* GET LIST METHODS
     */ 

    public List<StudyObject> getObjects() {
        return new ArrayList<StudyObject>(objects.values());
    }

    public List<AlignmentEntityRole> getRoles() {
        return new ArrayList<AlignmentEntityRole>(roles.values());
    }

    public List<TimeVariable> getAlignmentAttributes() {
        return new ArrayList<TimeVariable>(variables.values());
    }

    public List<List<String>> getCodes() {
        return new ArrayList<List<String>>(hCodeBook.values());
    }

    /* ADD METHODS
     */

    public void addObject(StudyObject obj) {
        objects.put(obj.getUri(), obj);
        if (!studyId.containsKey(obj.getIsMemberOf())) {
        	ObjectCollection soc = ObjectCollection.find(obj.getIsMemberOf());
        	if (soc != null) {
        		Study std = soc.getStudy();
        		if (std != null && std.getId() != null) {
        			studyId.put(obj.getIsMemberOf(), std.getId());
        		}
        	}
        }
    }

    public void addEntity(Entity ent) {
        entityCache.put(ent.getUri(), ent);
    }

    public void addRole(StudyObject obj, AlignmentEntityRole entRole) {
    	String roleKey = obj.getOriginalId() + entRole.getKey();
        roles.put(roleKey, entRole);
        //System.out.println("Adding NEW ROLE: " + entRole);
        //TimeVariable newVar = new TimeVariable(obj, entRole,ID_IRT);
        //variables.put(newVar.getKey(),newVar);
    }

    public void addCode(String attrUri, List<String> code) {
        hCodeBook.put(attrUri, code);
    }
}
