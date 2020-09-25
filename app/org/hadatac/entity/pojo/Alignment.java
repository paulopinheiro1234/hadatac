package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Iterator;

import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.STR;

public class Alignment {

    private Map<String, StudyObject> objects;
    //private List<String> timestamps;
    private Map<String, StudyObject> refObjects;
    
    /*
     * key for attributeListCache is the outcome of calling getAttributeUris(List<Attribute> attrList)
     */
    private Map<String, List<Attribute>> attributeListCache;
    private Map<String, Attribute> attrCache;
    private Map<String, Entity> entityCache;
    private Map<String, Unit> unitCache;
    private Map<String, AlignmentEntityRole> roles;
    private Map<String, Variable> variables;
    private Map<String, List<String>> hCodeBook;
    private Map<String, String> studyId;  // key=socUri;  value=studyId
    private Map<String, STR> dataAcquisitions;

    List<Attribute> ID_LIST = new ArrayList<Attribute>();
    AttributeInRelationTo ID_IRT = new AttributeInRelationTo(ID_LIST, null);
    //List<Attribute> GROUPID_LIST = new ArrayList<Attribute>();
    //AttributeInRelationTo GROUPID_IRT = new AttributeInRelationTo(GROUPID_LIST, null);

    public Alignment() {
        objects = new HashMap<String, StudyObject>();
        //timestamps = new ArrayList<String>();
        attributeListCache = new HashMap<String, List<Attribute>>();
        attrCache = new HashMap<String, Attribute>();
        entityCache = new HashMap<String, Entity>();
        unitCache = new HashMap<String, Unit>();
        roles = new HashMap<String, AlignmentEntityRole>();
        variables = new HashMap<String, Variable>();
	    hCodeBook = new HashMap<String, List<String>>();
	    studyId = new HashMap<String,String>();
	    dataAcquisitions = new HashMap<String,STR>();

	    Attribute ID = new Attribute();
	    ID.setLabel("ID");
	    ID_LIST.add(ID);

	    //Attribute GROUPID = new Attribute();
        //GROUPID.setLabel("GROUPID");
	    //GROUPID_LIST.add(GROUPID);
    }

    public void printAlignment() {
        System.out.println("Alignment Content: ");
        if (variables != null && variables.size() > 0) {
            for (Variable aa : variables.values()) {
                System.out.println("Label: " + aa);
            }
        }
    }

    public static String getUrisFromAttributeList(List<Attribute> attrList) {
    	if (attrList == null) {
    		return "";
    	}
    	String attributeUri = "";
    	for (Attribute attr : attrList) {			
    		attributeUri = attributeUri + attr.getUri();
    	}
    	return attributeUri;
    }
    
    public static String getUrisFromStringList(List<String> attrListUri) {
    	if (attrListUri == null) {
    		return "";
    	}
    	String attributeUri = "";
    	for (String attrUri : attrListUri) {			
    		attributeUri = attributeUri + attrUri;
    	}
    	return attributeUri;
    }
    

    /* 
     * objectKey adds a new object identifier into variables
     */
    public String objectKey(AlignmentEntityRole entRole) {
        Variable aa = new Variable(entRole, ID_IRT);
        return aa.toString();
    }

    /* 
     * groupKey adds a new group identifier into variables
     */
    /*
    public String groupKey(AlignmentEntityRole entRole) {
        Variable aa = new Variable(entRole, GROUPID_IRT);
        return aa.toString();
    }
	*/
    
    /* 
     * returns a key to retrieve variables. if needed, measuremtnKey adds new variables 
     */
    public String measurementKey(Measurement m) {
        if (variables == null) {
            System.out.println("[ERROR] Alignment: alignment attribute list not initialized ");
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
                    System.out.println("[ERROR] Alignment: retrieving entity playing inRelationTo " + m.getInRelationToUri());
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
                    System.out.println("[ERROR] Alignment: could not retrieve unit [" + m.getUnitUri() + "]. Ignoring unit.");
                } else {
                    unitCache.put(unit.getUri(),unit);
                    mUnit = m.getUnitUri();
                }
            }
        } 
        Attribute timeAttr = null;
        String mAbstractTime = "";
        if (m.getAbstractTime() != null && !m.getAbstractTime().equals("")) {
            timeAttr = attrCache.get(m.getAbstractTime());
            if (timeAttr != null && timeAttr.getUri().equals(m.getAbstractTime())) {
                mAbstractTime = timeAttr.getUri();
            } else {
                timeAttr = Attribute.find(m.getAbstractTime());
                if (timeAttr == null) {
                    System.out.println("[ERROR] Alignment: could not retrieve abstract time [" + m.getAbstractTime() + "]. Ignoring abstract time.");
                } else {
                	attrCache.put(timeAttr.getUri(),timeAttr);
                    mAbstractTime = m.getAbstractTime(); 
                }
            }
        } 

        if (!dataAcquisitions.containsKey(m.getAcquisitionUri())) {
            //System.out.println("getDOI(): adding da " + m.getAcquisitionUri());
        	STR da = STR.findByUri(m.getAcquisitionUri());
        	dataAcquisitions.put(m.getAcquisitionUri(), da);
        }
        
	    String mRole = m.getRole().replace(" ","");

	    String mKey = null;
	    if (m.getCategoricalClassUri() != null && !m.getCategoricalClassUri().isEmpty()) {
        	mKey =  mRole + m.getEntityUri() + m.getCategoricalClassUri() + mInRelationTo + mUnit + mAbstractTime;
        } else {
        	String attributeUris = "";
        	for (String attrUri : m.getCharacteristicUris()) {
        		attributeUris = attributeUris + attrUri;
        	}
        	mKey =  mRole + m.getEntityUri() + attributeUris + mInRelationTo + mUnit + mAbstractTime;
        }

        //System.out.println("Align-Debug: Measurement: " + mKey);

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
                System.out.println("[ERROR] Alignment: retrieving entity " + m.getEntityUri());
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

        // Attribute List has only one entry that is the Categorical Class
        if (m.getCategoricalClassUri() != null && !m.getCategoricalClassUri().isEmpty()) {
	        //System.out.println("Align-Debug: new alignment Categorical Class [" + m.getCategoricalClassUri() + "]"); 
	        attributeList = attributeListCache.get(m.getCategoricalClassUri());
	        if (attributeList != null && attributeList.size() > 0) {
	        	attribute = attributeList.get(0);
	        }
	        if (attribute == null || !attribute.getUri().equals(m.getCategoricalClassUri())) {
	        	attribute = attrCache.get(m.getCategoricalClassUri());
	        	if (attribute == null) {
	        		attribute = Attribute.find(m.getCategoricalClassUri());
	        		if (attribute == null) {
	        			System.out.println("[ERROR] Alignment: retrieving attribute " + m.getCategoricalClassUri());
	        			return null;
	        		}
	        		attrCache.put(attribute.getUri(), attribute);
	        	}
	            attributeList = new ArrayList<Attribute>();
	            attributeList.add(attribute);
	            attributeListCache.put(attribute.getUri(), attributeList);
	        }
            //System.out.println("Align-Debug: attribute list is for categorical variable"); 
        }

        // Attribute List may have one or more elements
        else {
            //System.out.println("Align-Debug: new alignment Non-CategoricalClass ]"); 
        	attributeList = attributeListCache.get(getUrisFromStringList(m.getCharacteristicUris()));
        	//System.out.println("Align-Debug: Characteristic URIs are [" + getUrisFromStringList(m.getCharacteristicUris()) + "]");
        	//System.out.println("Align-Debug: Attribute List URIs are [" + getUrisFromAttributeList(attributeList) + "]");
	        if (attributeList == null || !getUrisFromAttributeList(attributeList).equals(getUrisFromStringList(m.getCharacteristicUris()))) {
	        	attributeList = new ArrayList<Attribute>();
	            for (String attrUri : m.getCharacteristicUris()) {
		        	attribute = attrCache.get(attrUri);
		        	if (attribute == null) {
		        		attribute = Attribute.find(attrUri);
		        		if (attribute == null) {
		        			System.out.println("[ERROR] Alignment: retrieving attribute " + attrUri);
		        			return null;
		        		}
		        		attrCache.put(attribute.getUri(), attribute);
		        	}
		        	attributeList.add(attribute);
	            }
                attributeListCache.put(getUrisFromAttributeList(attributeList),attributeList);
	        }
            //System.out.println("Align-Debug: attribute list is for non-categorical variable"); 
        }

        /*
        System.out.print("Align-Debug: attributeList is [");
        for (Attribute attr : attributeList) {
        	if (attr != null && attr.getLabel() != null) {
        		System.out.print(attr.getLabel() + " ");
        	}
        }
    	System.out.println("]"); 
        System.out.println("Align-Debug: itr is [" + irt + "]"); 
        */
        
        /*
        if (!mInRelationTo.equals("")) {
            System.out.println("Adding the following inRelationTo " + mInRelationTo);
        }*/

        AttributeInRelationTo newAttrInRel = new AttributeInRelationTo(attributeList, irt); 

        /*
        if (!mUnit.equals("")) {
            System.out.println("Adding the following unit " + mUnit);
        }*/

        /*
        if (!mAbstractTime.equals("")) {
            System.out.println("Adding the following time " + mAbstractTime);
        }*/

        newVar = new Variable(newRole, newAttrInRel, unit, timeAttr);
        //System.out.println("Align-Debug: new alignment attribute 3"); 

        //System.out.println("Align-Debug: new variable's key: [" + newVar.getKey() + "]"); 

        if (!variables.containsKey(newVar.getKey())) {
            variables.put(newVar.getKey(), newVar);
            //System.out.println("Align-Debug: adding new var to variable's list"); 
        }

        return newVar.toString();

    }

    /* ========================================== *
     *           GRAPH OPERATIONS
     * ========================================== */

    public static List<String> alignmentObjects(String currentObj, String selectedRole, String originalId) {
        //System.out.println("Align-Debug: Current Object [" + currentObj + "]"); 
    	List<String> alignObjs = new ArrayList<String>();
    	if (currentObj == null || currentObj.isEmpty() || selectedRole == null || selectedRole.isEmpty()) {
            //System.out.println("Align-Debug: Current Obj or Selected Role are empty"); 
    		return alignObjs;
    	}
    		
    	/* 
    	 * Test if the current object is already the alignment object
    	 */
    	if (selectedRole.equals(StudyObject.findSocRole(currentObj))) {
    		alignObjs.add(currentObj);
            //System.out.println("Align-Debug: Already ALIGNMENT object"); 
    		return alignObjs;
    	};

    	/* 
    	 * Test if alignment object is upstream
    	 */
        List<Map<String,String>> upstream = StudyObject.findUpstreamSocs(currentObj);
        if (upstream.size() > 0) {
        	// iteration stops for first obj with matching role 
        	for (Map<String,String> socRoleTuple :  upstream) {
                Iterator<Map.Entry<String, String>> itr = socRoleTuple.entrySet().iterator(); 
                if (itr.hasNext()) { 
                	Map.Entry<String, String> entry = itr.next();
                	if (entry.getValue().equals(selectedRole)) {
                		alignObjs.add(entry.getKey());
                        //System.out.println("Align-Debug: UPSTREAM object"); 
                		return new ArrayList<>(new HashSet<>(alignObjs));
                	}
                }
        	}
        }

    	/* 
    	 * Test if alignment object(s) is(are) downstream 
    	 */
        List<Map<String,String>> downstream = StudyObject.findDownstreamSocs(currentObj, originalId);
        if (downstream.size() > 0) {
        	// iteration is not interrupted and selects all objs with matching role 
        	for (Map<String,String> socRoleTuple :  downstream) {
                Iterator<Map.Entry<String, String>> itr = socRoleTuple.entrySet().iterator(); 
                if (itr.hasNext()) { 
                	Map.Entry<String, String> entry = itr.next();
                	if (entry.getValue().equals(selectedRole)) {
                		alignObjs.add(entry.getKey());
                	}
                }
        	}
        	if (alignObjs.size() > 1) {
        		
        	}
        	if (alignObjs.size() > 0) {
                //System.out.println("Align-Debug: DOWNSTREAM objects of size " + alignObjs.size()); 
        		return new ArrayList<>(new HashSet<>(alignObjs));
        	}
        }

    	/* 
    	 * Test if alignment object(s) is(are) downstream from some upstream object 
    	 */
        if (upstream.size() > 0) {
        	for (Map<String,String> socRoleTuple :  upstream) {
                Iterator<Map.Entry<String, String>> itr = socRoleTuple.entrySet().iterator(); 
                if (itr.hasNext()) { 
                	Map.Entry<String, String> entry = itr.next();
                	String upstreamObj = entry.getKey();
                	
                    List<Map<String,String>> downstreamFromUpstream = StudyObject.findDownstreamSocs(upstreamObj, originalId);
                    if (downstreamFromUpstream.size() > 0) {
                    	// iteration is not interrupted and selects all objs with matching role 
                    	for (Map<String,String> socRoleTuple2 :  downstreamFromUpstream) {
                            Iterator<Map.Entry<String, String>> itr2 = socRoleTuple2.entrySet().iterator(); 
                            if (itr2.hasNext()) { 
                            	Map.Entry<String, String> entry2 = itr2.next();
                            	if (entry2.getValue().equals(selectedRole)) {
                            		alignObjs.add(entry2.getKey());
                            	}
                            }
                    	}
                    }
                }
        	}
        	if (alignObjs.size() > 0) {
                //System.out.println("Align-Debug: DOWNSTREAM OF UPSTREAM objects of size " + alignObjs.size()); 
        		return new ArrayList<>(new HashSet<>(alignObjs));
        	}
        }

        System.out.println("[ERROR] Alignment: COULD NOT FIND alignment object for [" + currentObj + "]"); 
    	return alignObjs;
    }
    

    
    /* ------------------------------------------ *
     *           CONTAINS METHODS
     * ------------------------------------------ */

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

    /* ------------------------------------------ *
     *       GET INDIVIDUAL METHODS
     * ------------------------------------------ */

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
        //System.out.println("getDOI(): da size is " + dataAcquisitions.size());
    	for (Map.Entry<String,STR> entry : dataAcquisitions.entrySet())  {
            org.hadatac.entity.pojo.STR da = entry.getValue();
            //System.out.println("getDOI(): da is " + da.getUri());
            for (String doi : da.getDOIs()) { 
                //System.out.println("getDOI(): doi is " + doi);
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
        //Variable newGroupVar = new Variable(entRole,GROUPID_IRT);
        //variables.put(newVar.getKey() + "GROUP",newGroupVar);
    }

    public void addCode(String attrUri, List<String> code) {
        hCodeBook.put(attrUri, code);
    }
    
    
    
}
