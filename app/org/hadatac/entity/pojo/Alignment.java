package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.STR;

public class Alignment {

    private Map<String, StudyObject> objects;
    //private List<String> timestamps;
    private Map<String, StudyObject> refObjects;
    private Map<String, Attribute> attributeCache;
    private Map<String, Entity> entityCache;
    private Map<String, Unit> unitCache;
    private Map<String, AlignmentEntityRole> roles;
    private Map<String, Variable> variables;
    private Map<String, List<String>> hCodeBook;
    private Map<String, String> studyId;  // key=socUri;  value=studyId
    private Map<String, STR> dataAcquisitions;

    Attribute ID = new Attribute();
    AttributeInRelationTo ID_IRT = new AttributeInRelationTo(ID, null);
    Attribute GROUPID = new Attribute();
    AttributeInRelationTo GROUPID_IRT = new AttributeInRelationTo(GROUPID, null);

    public Alignment() {
        objects = new HashMap<String, StudyObject>();
        //timestamps = new ArrayList<String>();
        attributeCache = new HashMap<String, Attribute>();
        entityCache = new HashMap<String, Entity>();
        unitCache = new HashMap<String, Unit>();
        roles = new HashMap<String, AlignmentEntityRole>();
        variables = new HashMap<String, Variable>();
	    hCodeBook = new HashMap<String, List<String>>();
	    studyId = new HashMap<String,String>();
	    dataAcquisitions = new HashMap<String,STR>();
	    ID.setLabel("ID");
        GROUPID.setLabel("GROUPID");
    }

    public void printAlignment() {
        System.out.println("Alignment Content: ");
        if (variables != null && variables.size() > 0) {
            for (Variable aa : variables.values()) {
                System.out.println("Label: " + aa);
            }
        }
    }

    /* objectKey adds a new object identifier into variables
     */
    public String objectKey(AlignmentEntityRole entRole) {
        Variable aa = new Variable(entRole, ID_IRT);
        return aa.toString();
    }

    /* groupKey adds a new group identifier into variables
     */
    public String groupKey(AlignmentEntityRole entRole) {
        Variable aa = new Variable(entRole, GROUPID_IRT);
        return aa.toString();
    }

    /* returns a key to retrieve variables. if needed, measuremtnKey adds new variables 
     */
    public String measurementKey(Measurement m) {
        if (variables == null) {
            System.out.println("[ERROR] alignment attribute list not initialized ");
            return null;
        }

        /* 
         * Look for existing variables
         */
        
        //System.out.println("Align-Debug: Measurement Key");

        Entity irt = null;
        String mInRelationTo = "";
        if (m.getInRelationToUri() != null && !m.getInRelationToUri().equals("")) {
            irt = entityCache.get(m.getInRelationToUri());
            if (irt != null && irt.getUri().equals(m.getInRelationToUri())) {
                mInRelationTo = irt.getUri();
            } else {		
                irt = Entity.find(m.getInRelationToUri());
                if (irt == null) {
                    System.out.println("[ERROR] retrieving entity playing inRelationTo " + m.getInRelationToUri());
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
        Attribute timeAttr = null; 
        String mAbstractTime = "";
        if (m.getAbstractTime() != null && !m.getAbstractTime().equals("")) {
            timeAttr = attributeCache.get(m.getAbstractTime());
            if (timeAttr != null && timeAttr.getUri().equals(m.getAbstractTime())) {
                mAbstractTime = timeAttr.getUri();
            } else {
                timeAttr = Attribute.find(m.getAbstractTime());
                if (timeAttr == null) {
                    System.out.println("[ERROR] could not retrieve abstract time [" + m.getAbstractTime() + "]. Ignoring abstract time.");
                } else {
                    attributeCache.put(timeAttr.getUri(),timeAttr);
                    mAbstractTime = m.getAbstractTime(); 
                }
            }
        } 

        if (!dataAcquisitions.containsKey(m.getAcquisitionUri())) {
            System.out.println("getDOI(): adding da " + m.getAcquisitionUri());
        	STR da = STR.findByUri(m.getAcquisitionUri());
        	dataAcquisitions.put(m.getAcquisitionUri(), da);
        }
        
	    String mRole = m.getRole().replace(" ","");

        String mKey =  mRole + m.getEntityUri() + m.getCharacteristicUris().get(0) + mInRelationTo + mUnit + mAbstractTime;

        //System.out.println("Align-Debug: Measurement: " + mKey);
        //System.out.println("Align-Debug: Vector: " + alignAttrs); 

        if (variables.containsKey(mKey)) {
            return variables.get(mKey).toString();
        }

        /* 
         * create new variable
         */

        Variable newVar;

        Entity entity = entityCache.get(m.getEntityUri());
        if (entity == null || !entity.getUri().equals(m.getEntityUri())) {
            entity = Entity.find(m.getEntityUri());
            if (entity == null) {
                System.out.println("[ERROR] retrieving entity " + m.getEntityUri());
                return null;
            } else {
                entityCache.put(entity.getUri(),entity);
            }
        }

        //System.out.println("Align-Debug: new alignment attribute"); 
        AlignmentEntityRole newRole = new AlignmentEntityRole(entity,mRole);

        //System.out.println("Align-Debug: new alignment characteristic: [" + m.getCharacteristicUris().get(0) + "]"); 

        Attribute attribute = attributeCache.get(m.getCharacteristicUris().get(0));
        if (attribute == null || !attribute.getUri().equals(m.getCharacteristicUris().get(0))) {
            attribute = Attribute.find(m.getCharacteristicUris().get(0));
            if (attribute == null) {
                System.out.println("[ERROR] retrieving attribute " + m.getCharacteristicUris().get(0));
                return null;
            } else {
                attributeCache.put(attribute.getUri(),attribute);
            }
        }

        //System.out.println("Align-Debug: new alignment attribute 2"); 

        if (!mInRelationTo.equals("")) {
            System.out.println("Adding the following inRelationTo " + mInRelationTo);
        }

        AttributeInRelationTo newAttrInRel = new AttributeInRelationTo(attribute, irt); 

        if (!mUnit.equals("")) {
            System.out.println("Adding the following unit " + mUnit);
        }

        if (!mAbstractTime.equals("")) {
            System.out.println("Adding the following time " + mAbstractTime);
        }

        newVar = new Variable(newRole, newAttrInRel, unit, timeAttr);
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

    public boolean containsRole(String key) {
        return roles.containsKey(key);
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

    public AlignmentEntityRole getRole(String key) {
        return roles.get(key);
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

    //public List<String> getTimestamps() {
    //	return timestamps;
    //}

    public List<AlignmentEntityRole> getRoles() {
        return new ArrayList<AlignmentEntityRole>(roles.values());
    }

    public List<Variable> getAlignmentAttributes() {
        return new ArrayList<Variable>(variables.values());
    }

    public List<List<String>> getCodes() {
        return new ArrayList<List<String>>(hCodeBook.values());
    }

    public List<String> getDOIs() {
    	List<String> resp = new ArrayList<String>();
    	if (dataAcquisitions.size() == 0) {
    		return resp;
    	}
        System.out.println("getDOI(): da size is " + dataAcquisitions.size());
    	for (Map.Entry<String,STR> entry : dataAcquisitions.entrySet())  {
            org.hadatac.entity.pojo.STR da = entry.getValue();
            System.out.println("getDOI(): da is " + da.getUri());
            for (String doi : da.getDOIs()) { 
                System.out.println("getDOI(): doi is " + doi);
            	resp.add(doi);
            }
    	}
    	return resp;
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

    public void addRole(AlignmentEntityRole entRole) {
        roles.put(entRole.getKey(), entRole);
        //System.out.println("Adding NEW ROLE: " + entRole);
        Variable newVar = new Variable(entRole,ID_IRT);
        variables.put(newVar.getKey(),newVar);
        Variable newGroupVar = new Variable(entRole,GROUPID_IRT);
        variables.put(newVar.getKey() + "GROUP",newGroupVar);
    }

    public void addCode(String attrUri, List<String> code) {
        hCodeBook.put(attrUri, code);
    }
}
