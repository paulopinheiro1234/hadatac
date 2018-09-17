package org.hadatac.entity.pojo;

import java.util.HashMap;
import java.util.Map;

import org.apache.jena.update.UpdateExecutionFactory;
import org.apache.jena.update.UpdateFactory;
import org.apache.jena.update.UpdateProcessor;
import org.apache.jena.update.UpdateRequest;

import org.apache.commons.text.StrSubstitutor;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

public class DASOInstance {
	public static String INDENT1 = "     ";
	public static String INSERT_LINE1 = "INSERT DATA {  ";
	public static String DELETE_LINE1 = "DELETE WHERE {  ";
	public static String LINE3 = INDENT1 + "a         hasco:DASchemaAttribute;  ";
	public static String DELETE_LINE3 = " ?p ?o . ";
	public static String LINE_LAST = "}  ";

	final String kbPrefix = ConfigFactory.load().getString("hadatac.community.ont_prefix") + "-kb:";
	
	static final String URI_TEMPLATE = "${study}/${id}/${modifier}";
	
	private String uri; // generated here in this class
	private String label; // provided by template+codebook
	private String studyId; // provided by DA file
	private String rowKey; // provided by row
	private String type; // provided by template
	private String origValue;
	private HashMap<String,String> relations;

	public void setUri(String uri){ this.uri = uri; }
	public void setLabel(String label){ this.label = label; }
	public void setStudyId(String id){ this.studyId = id; }
	public void setType(String type){ this.type = type; }
	public void setRowKey(String row){ this.rowKey = row; }
	public void setOrigValue(String ov){ this.origValue = ov; }
	public void setRelations(HashMap<String,String> relations){ this.relations = relations; }

	public String getUri(){ return this.uri; }
	public String getLabel(){ return this.label; }
	public String getStudyId(){ return this.studyId; }
	public String getType(){ return this.type; }
	public String getRowKey(){ return this.rowKey; }
	public String getOrigValue(){ return this.origValue; }
	public HashMap<String,String> getRelations(){ return this.relations; }    
	public String getUriNamespace() { return URIUtils.replaceNameSpaceEx(uri); }
	//public HashMap<String,String> getTemplateValues(){ return this.templateValues; }

	/*
	public DASOInstance(String uri, String label, String type, HashMap<String,String> relations){
		this.setUri(uri);
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
	}// /constructor with uri given
	*/

	public DASOInstance(String studyId, String rowKey, String label, String type, HashMap<String,String> relations){
		this.setStudyId(studyId);
		this.setRowKey(rowKey);
		this.setLabel(label);
		this.setType(type);
		this.setRelations(relations);
		this.setOrigValue("");
		this.setUri(generateURI());

		//this.save();
	}// /constructor


	// this may still need some fixing
	public String generateURI(){
		HashMap<String,String> templateValues = new HashMap<String,String>();
		templateValues.put("study", this.studyId);
		templateValues.put("id", this.rowKey);
		if(this.rowKey.equals("summaryClass")){
			if(this.relations.containsKey("rdfs:subClassOf")){
				String t = this.relations.get("rdfs:subClassOf").split(":",2)[1];
				String[] temp = t.split("_",2);
				String mod = temp[0];
				if(temp.length > 1) mod += "/val/"+temp[1];
				this.setOrigValue(mod);
				if(this.relations.containsKey("sio:existsAt")){
					System.out.println("[DASOInstance] time = " + this.relations.get("sio:existsAt"));
					mod += "/time/" + this.relations.get("sio:existsAt");
				}
				templateValues.put("modifier", mod.replaceAll("[ ';.,]",""));
			}
			else
				templateValues.put("modifier", "");
		} else {
			templateValues.put("modifier", "");
		}
		StrSubstitutor sub = new StrSubstitutor(templateValues);
		String generatedURI = sub.replace(URI_TEMPLATE);
		//generatedURI = kbPrefix + generatedURI;
		return generatedURI;
	}// /generateURI


	//private String uri; // generated here in this class
	//private String label; // provided by template+codebook
	//private String studyId; // provided by DA file
	//private String rowKey; // provided by row
	//private String type; // provided by template
	//private HashMap<String,String> relations;

	public void save() {
		//delete();  // delete any existing triple for the current DASA
		if (this.getUri() == null || this.getUri().equals("")) {
			System.out.println("[DASOInstance] [ERROR] Trying to save DASOInstance without assigning a URI");
			return;
		}
		if (this.getType() == null || this.getType().equals("")) {
			System.out.println("[DASOInstance] [ERROR] Trying to save DASOInstance without assigning a type");
			return;
		}
		String insert = "";
		insert += NameSpaces.getInstance().printSparqlNameSpaceList();
		insert += INSERT_LINE1;
		insert += this.getUri() + " a " + this.getType() + " . ";
		insert += this.getUri() + " rdfs:label  \"" + this.getLabel() + "\" . ";
		if (this.getStudyId().startsWith("http")) {
			insert += this.getUri() + " sio:isPartOf <" + this.getStudyId() + "> . ";
		} else {
			insert += this.getUri() + " sio:isPartOf " + this.getStudyId() + " . ";
		}
		for(Map.Entry rel : this.getRelations().entrySet()){
			String p = "";
			String o = "";
			String relKey = (String)rel.getKey();
			String relVal = (String)rel.getValue();
			if(relKey.startsWith("http")){
				p = "<" + relKey + ">";
			} else {
				p = relKey;
			}
			if(relVal.startsWith("http")){
				o = "<" + relVal + ">";
			} else {
				o = relVal;
			}
			insert += this.getUri() + " " + p + " " + o + " . ";
		}
		insert += LINE_LAST;
		System.out.println("[DASOInstance] insert query (pojo's save): <" + insert + ">");
		//UpdateRequest request = UpdateFactory.create(insert);
		//UpdateProcessor processor = UpdateExecutionFactory.createRemote(
		//	request, Collections.getCollectionsName(Collections.METADATA_UPDATE));
		//processor.execute();
	}// /save()

	public void delete() {
		String query = "";
		if (this.getUri() == null || this.getUri().equals("")) {
			return;
		}
		query += NameSpaces.getInstance().printSparqlNameSpaceList();
		query += DELETE_LINE1;
		if (this.getUri().startsWith("http")) {
			query += "<" + this.getUri() + ">";
		} else {
			query += this.getUri();
		}
		query += DELETE_LINE3;
		query += LINE_LAST;
		System.out.println("[DASOI] SPARQL query inside delete(): " + query);
		UpdateRequest request = UpdateFactory.create(query);
		UpdateProcessor processor = UpdateExecutionFactory.createRemote(request, CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_UPDATE));
		processor.execute();
	}// /delete()
	

	// TODO: finish
	/*
	public static DASOInstance find(String URI){		
	}// /find
	*/

	// TODO: finish
	/*
	public static List<DASOInstance> findByStudy(String studyID){
		List<DASOInstance> instances = new ArrayList<DASOInstance>();
	}// /findByStudy
	*/
	
	public String toString(){
		String pretty = this.getUri() + "\n";
		for(Map.Entry rel : this.getRelations().entrySet()) {
			pretty += rel.getKey() + " : " + rel.getValue();
		}
		return pretty;
	}// /toString

} // /class
