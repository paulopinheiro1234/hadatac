package org.hadatac.entity.pojo;

import java.util.Map;

import org.hadatac.utils.ConfigProp;

import java.util.HashMap;

public class DASVirtualObject {
    final String kbPrefix = ConfigProp.getKbPrefix();
    private String templateUri;
    private String originalLabel;
    private Map<String, String> objRelations;

    // takes the row created in DASchemaObjectGenerator
    // iff that row is virtual
    public DASVirtualObject(String ogLabel, Map<String, Object> dasoRow) {
        this.objRelations = new HashMap<String,String>();

        if(dasoRow.get("hasURI") == null || dasoRow.get("hasURI").equals("")){
            //handle an error
        } else {
            this.templateUri = dasoRow.get("hasURI").toString();
        }
        if(dasoRow.get("rdfs:label") == null || dasoRow.get("rdfs:label").equals("")){
            //handle an error
        } else {
            this.objRelations.put("rdfs:label", dasoRow.get("rdfs:label").toString());
        }
        if(dasoRow.get("hasco:hasEntity") == null || dasoRow.get("hasco:hasEntity").equals("")){
            //handle an error
        } else {
            this.objRelations.put("rdfs:type", dasoRow.get("hasco:hasEntity").toString());
        }
        if(dasoRow.get("hasco:hasRole") == null || dasoRow.get("hasco:hasRole").equals("")){
            //handle an error
        } else {
            this.objRelations.put("hasco:hasRole", dasoRow.get("hasco:hasRole").toString());
        }
        if(dasoRow.get("sio:SIO_000668") == null || dasoRow.get("sio:SIO_000668").equals("")){
            //handle an error
        } else {
            if(dasoRow.get("hasco:Relation") == null || dasoRow.get("hasco:Relation").equals("")){
                this.objRelations.put("sio:SIO_000001", dasoRow.get("sio:SIO_000668").toString());
            } else {
                this.objRelations.put(dasoRow.get("hasco:Relation").toString(), dasoRow.get("sio:SIO_000668").toString());
            }
        }
        if(dasoRow.get("dcterms:alternativeName") == null || dasoRow.get("dcterms:alternativeName").equals("")){
            //handle an error
        } else {
            this.originalLabel = dasoRow.get("dcterms:alternativeName").toString();
        }
    }

    public Map<String,String> getObjRelations(){
        return this.objRelations;
    }

    public String getTemplateUri(){
        return this.templateUri;
    }

    public String getOriginalLabel(){
        return this.originalLabel;
    }

    public String toString() {
        String result = "";
        result += "templateURI: " + this.templateUri + "\n";
        result += "column name: " + this.originalLabel + "\n";
        for (Map.Entry<String, String> entry : this.objRelations.entrySet()) {
            result += entry.getKey() + " " + entry.getValue() + "\n";
        }

        return result;
    }
}
