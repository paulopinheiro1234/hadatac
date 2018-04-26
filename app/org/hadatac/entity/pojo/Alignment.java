package org.hadatac.entity.pojo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.Pivot;

public class Alignment {

    private Map<String, StudyObject> objects;
    private Map<String, Attribute> attributeCache;
    private Map<String, Entity> entityCache;
    private Map<String, Unit> unitCache;
    private Map<String, AlignmentEntityRole> roles;
    private Map<String, AlignmentAttribute> alignAttrs;
    private Map<String, String> replacementUris;

    Attribute ID = new Attribute();
    AttributeInRelationTo ID_IRT = new AttributeInRelationTo(ID, null);

    public Alignment() {
        objects = new HashMap<String, StudyObject>();
        attributeCache = new HashMap<String, Attribute>();
        entityCache = new HashMap<String, Entity>();
        unitCache = new HashMap<String, Unit>();
        roles = new HashMap<String, AlignmentEntityRole>();
        alignAttrs = new HashMap<String, AlignmentAttribute>();
        replacementUris = new HashMap<String, String>();
        ID.setLabel("ID");
    }

    public void printAlignment() {
        System.out.println("Alignment Content: ");
        if (alignAttrs != null && alignAttrs.size() > 0) {
            for (AlignmentAttribute aa : alignAttrs.values()) {
                System.out.println("Label: " + aa);
            }
        }
    }


    /* objectKey adds a new object identifier into aligment attributes
     */
    public String objectKey(AlignmentEntityRole entRole) {
        AlignmentAttribute aa = new AlignmentAttribute(entRole, ID_IRT);
        return aa.toString();
    }

    /* returns a key to retrieve alignment attributes. if needed, measuremtnKey adds new alignment attributes 
     */
    public String measurementKey(Measurement m) {
        if (alignAttrs == null) {
            System.out.println("[ERROR] alignment attribute list not initialized ");
            return null;
        }

        /* Look for existing alignment attributes
         */
        //System.out.println("Measurement Key");

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

        String mRole = inferRole(m);

        String mKey =  mRole + m.getEntityUri() + m.getCharacteristicUri() + mInRelationTo + mUnit + mAbstractTime;

        //System.out.println("Measurement: " + mKey);
        //System.out.println("Vector: " + alignAttrs); 

        if (alignAttrs.containsKey(mKey)) {
            return alignAttrs.get(mKey).toString();
        }

        /* create new alignment attribute
         */
        AlignmentAttribute newAA;
        Entity entity = Entity.find(m.getEntityUri());
        if (entity == null) {
            System.out.println("[ERROR] retrieving entity " + m.getEntityUri());
            return null;
        }
        AlignmentEntityRole newRole = new AlignmentEntityRole(entity,mRole);

        Attribute attribute = Attribute.find(m.getCharacteristicUri());
        if (attribute == null) {
            System.out.println("[ERROR] retrieving attribute " + m.getCharacteristicUri());
            return null;
        }

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

        newAA = new AlignmentAttribute(newRole, newAttrInRel, unit, timeAttr);

        if (!alignAttrs.containsKey(newAA.getKey())) {
            alignAttrs.put(newAA.getKey(), newAA);
            return newAA.toString();
        }

        return null;

    }

    public String replaceUri(String origUri) {
        String responseUri = origUri;
        if (replacementUris.containsKey(origUri)) {
            responseUri = replacementUris.get(origUri);
        }
        return responseUri;
    }

    /* CONTAINS METHODS
     */

    public boolean containsObject(String uri) {
        return objects.containsKey(uri);
    }

    public boolean containsRole(String key) {
        return roles.containsKey(key);
    }

    public boolean containsReplacementUri(String uri) {
        return replacementUris.containsKey(uri);
    }


    /* GET INDIVIDUAL METHODS
     */

    public StudyObject getObject(String uri) {
        return objects.get(uri);
    }

    public AlignmentEntityRole getRole(String key) {
        return roles.get(key);
    }


    /* GET LIST METHODS
     */ 

    public List<StudyObject> getObjects() {
        return new ArrayList<StudyObject>(objects.values());
    }

    public List<AlignmentEntityRole> getRoles() {
        return new ArrayList<AlignmentEntityRole>(roles.values());
    }

    public List<AlignmentAttribute> getAlignmentAttributes() {
        return new ArrayList<AlignmentAttribute>(alignAttrs.values());
    }

    /* ADD METHODS
     */

    public void addObject(StudyObject obj) {
        objects.put(obj.getUri(), obj);
    }

    public void addReplacementUri(String orig, String replace) {
        replacementUris.put(orig, replace);
    }

    public void addRole(AlignmentEntityRole entRole) {
        roles.put(entRole.getKey(), entRole);
        System.out.println("Adding NEW ROLE: " + entRole);
        AlignmentAttribute newAA = new AlignmentAttribute(entRole,ID_IRT);
        alignAttrs.put(newAA.getKey(),newAA);
    }

    public String inferRole(Measurement m) {
        String result = "";
        String query = "";
        query += NameSpaces.getInstance().printSparqlNameSpaceList();
        query += "SELECT ?roleLabel WHERE { \n" +
                " <" + m.getDasaUri() + "> hasco:isAttributeOf <" + m.getDasoUri() + "> . \n" +
                " <" + m.getDasoUri() + "> hasco:hasRole ?roleUri . \n" +
                " <" + m.getDasaUri() + "> hasco:hasEntity <" + m.getEntityUri() + "> . \n" +
                " <" + m.getDasaUri() + "> hasco:hasAttribute <" + m.getCharacteristicUri() + "> .  \n" +
                " ?roleUri rdfs:label ?roleLabel . \n" +
                "} \n"; 

        try {
            ResultSetRewindable resultsrw = SPARQLUtils.select(
                    CollectionUtil.getCollectionsName(CollectionUtil.METADATA_SPARQL), query);

            if (resultsrw.hasNext()) {
                QuerySolution soln = resultsrw.next();
                if (soln.get("roleLabel") != null && !soln.get("roleLabel").toString().isEmpty()) {
                    result = soln.get("roleLabel").toString();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        //System.out.println("Find role: [" + result + "]");

        return result;
    }

}
