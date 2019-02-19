package org.hadatac.entity.pojo;

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;
import org.hadatac.utils.FirstLabel;
import org.hadatac.annotations.PropertyField;
import org.hadatac.annotations.PropertyValueType;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.metadata.loader.LabkeyDataHandler;
import org.hadatac.metadata.loader.URIUtils;
import org.labkey.remoteapi.CommandException;


public class Sample extends StudyObject {

    public static String LINE3 = INDENT1 + "a hasco:Sample;  ";
    public static String PREFIX = "SP-";

    @PropertyField(uri="hasco:isFrom", valueType=PropertyValueType.URI)
    public String isFrom = "";

    public Sample(String uri, String isMemberOf) {
        this.setUri(uri);
        this.setTypeUri("");
        this.setOriginalId("");
        this.setLabel("");
        this.setIsMemberOf(isMemberOf);
        this.setComment("");
        this.setIsFrom("");
    }

    public Sample(String uri,
            String typeUri,
            String originalId,
            String label,
            String isMemberOf,
            String comment,
            String isFrom) { 
        this.setUri(uri);
        this.setTypeUri(typeUri);
        this.setOriginalId(originalId);
        this.setLabel(label);
        this.setIsMemberOf(isMemberOf);
        this.setComment(comment);
        this.setIsFrom(isFrom);
    }

    public void setIsFrom(String isFrom) {
        this.isFrom = isFrom;
    }

    public String getIsFrom() {
        return this.isFrom;
    }

    public static Sample find(String sp_uri) {
        Sample sp = null;
        System.out.println("Looking for sample with URI " + sp_uri);
        if (sp_uri.startsWith("http")) {
            sp_uri = "<" + sp_uri + ">";
        }
        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT  ?spType ?originalId ?isMemberOf ?hasLabel " + 
                " ?hasComment ?hasSource ?isPIConfirmed WHERE { " + 
                "    " + sp_uri + " a ?spType . " + 
                "    " + sp_uri + " hasco:isMemberOf ?isMemberOf .  " + 
                "    OPTIONAL { " + sp_uri + " hasco:originalID ?originalId } . " + 
                "    OPTIONAL { " + sp_uri + " rdfs:label ?hasLabel } . " + 
                "    OPTIONAL { " + sp_uri + " rdfs:comment ?hasComment } . " + 
                "    OPTIONAL { " + sp_uri + " hasco:isFrom ?isFrom } . " + 
                "    OPTIONAL { " + sp_uri + " hasco:hasSource ?hasSource } . " + 
                "    OPTIONAL { " + sp_uri + " hasco:isPIConfirmed ?isPIConfirmed } . " + 
                "}";
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);

        if (!resultsrw.hasNext()) {
            System.out.println("[WARNING] Sample. Could not find SP with URI: " + sp_uri);
            return sp;
        }

        String typeStr = "";
        String originalIdStr = "";
        String labelStr = "";
        String isMemberOfStr = "";
        String commentStr = "";
        String isFromStr = "";

        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null) {

                try {
                    if (soln.getResource("spType") != null && soln.getResource("spType").getURI() != null) {
                        typeStr = soln.getResource("spType").getURI();
                    }
                } catch (Exception e1) {
                    typeStr = "";
                }

                try {
                    if (soln.getLiteral("originalId") != null && soln.getLiteral("originalId").getString() != null) {
                        originalIdStr = soln.getLiteral("originalId").getString();
                    }
                } catch (Exception e1) {
                    originalIdStr = "";
                }

                labelStr = FirstLabel.getLabel(sp_uri);

                try {
                    if (soln.getResource("isMemberOf") != null && soln.getResource("isMemberOf").getURI() != null) {
                        isMemberOfStr = soln.getResource("isMemberOf").getURI();
                    }
                } catch (Exception e1) {
                    isMemberOfStr = "";
                }

                try {
                    if (soln.getLiteral("hasComment") != null && soln.getLiteral("hasComment").getString() != null) {
                        commentStr = soln.getLiteral("hasComment").getString();
                    }
                } catch (Exception e1) {
                    commentStr = "";
                }

                try {
                    if (soln.getResource("isFrom") != null && soln.getResource("isFrom").getURI() != null) {
                        isFromStr = soln.getResource("isFrom").getURI();
                    }
                } catch (Exception e1) {
                    isFromStr = "";
                }

                sp = new Sample(sp_uri,
                        typeStr,
                        originalIdStr,
                        labelStr,
                        isMemberOfStr,
                        commentStr,
                        isFromStr);
            }
        }
        return sp;
    }

    public static List<Sample> findSamplesByCollection(ObjectCollection oc) {
        if (oc == null) {
            return null;
        }
        List<Sample> samples = new ArrayList<Sample>();

        String queryString = NameSpaces.getInstance().printSparqlNameSpaceList() + 
                "SELECT ?uri WHERE { " + 
                "   ?uri hasco:isMemberOf  <" + oc.getUri() + "> . " +
                " } ";
        System.out.println("Sample findByCollection: " + queryString);
        
        ResultSetRewindable resultsrw = SPARQLUtils.select(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_SPARQL), queryString);
        
        while (resultsrw.hasNext()) {
            QuerySolution soln = resultsrw.next();
            if (soln != null && soln.getResource("uri").getURI() != null) { 
                System.out.println("URI: [" + soln.getResource("uri").getURI() + "]");
                Sample sample = Sample.find(soln.getResource("uri").getURI());
                samples.add(sample);
            }
        }
        return samples;
    }

    @Override
    public boolean saveToTripleStore() {
        System.out.println("Saving <" + uri + ">");
        if (uri == null || uri.equals("")) {
            System.out.println("[ERROR] Trying to save SP without assigning an URI");
            return false;
        }
        if (isMemberOf == null || isMemberOf.equals("")) {
            System.out.println("[ERROR] Trying to save SP without assigning DAS's URI");
            return false;
        }

        return super.saveToTripleStore();
    }

    @Override
    public int saveToLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri()));
        row.put("a", URIUtils.replaceNameSpaceEx(getTypeUri()));
        row.put("hasco:originalID", getOriginalId());
        row.put("rdfs:label", getLabel());
        row.put("hasco:isMemberOf", URIUtils.replaceNameSpaceEx(getIsMemberOf()));
        row.put("rdfs:comment", getComment());
        row.put("hasco:isFrom", URIUtils.replaceNameSpaceEx(getIsFrom()));
        row.put("hasco:hasSource", "");
        row.put("hasco:isVirtual", "");
        row.put("hasco:isPIConfirmed", "false");
        rows.add(row);
        int totalChanged = 0;
        try {
            totalChanged = loader.insertRows("Sample", rows);
        } catch (CommandException e) {
            try {
                totalChanged = loader.updateRows("Sample", rows);
            } catch (CommandException e2) {
                System.out.println("[ERROR] Could not insert or update Sample(s)");
            }
        }
        return totalChanged;
    }

    @Override
    public int deleteFromLabKey(String user_name, String password) {
        LabkeyDataHandler loader = LabkeyDataHandler.createDefault(user_name, password);
        List< Map<String, Object> > rows = new ArrayList< Map<String, Object> >();
        Map<String, Object> row = new HashMap<String, Object>();
        row.put("hasURI", URIUtils.replaceNameSpaceEx(getUri().replace("<","").replace(">","")));
        rows.add(row);
        for (Map<String,Object> r : rows) {
            System.out.println("deleting sample " + r.get("hasURI"));
        }

        try {
            return loader.deleteRows("Sample", rows);
        } catch (CommandException e) {
            System.out.println("[ERROR] Could not delete Sample(s)");
            e.printStackTrace();
            return 0;
        }
    }
}
