package org.hadatac.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.beans.Field;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;


public class NameSpace {

	public static final String OBOE                    = "oboe";
	public static final String OBOE_CORE               = "oboe-core";
	public static final String OBOE_STANDARDS          = "oboe-standards";
	public static final String OBOE_CHEMISTRY          = "oboe-chemistry";
	public static final String OBOE_ECOLOGY            = "oboe-ecology";
	public static final String OBOE_CHARACTERISTICS    = "oboe-characteristics";
	public static final String RDF                     = "rdf";
	public static final String RDFS                    = "rdfs";
	public static final String XSD                     = "xsd";
	public static final String OWL                     = "owl";
	public static final String VSTOI                   = "vstoi";
	public static final String PROV                    = "prov";
	public static final String HASNETO                 = "hasneto";
	public static final String HASCO               		= "hasco";
	public static final String FOAF                    = "foaf";
	public static final String HADATAC_SN              = "hadatac-sn";
	public static final String HADATAC_ENTITIES        = "hadatac-entities";
	public static final String HADATAC_STANDARDS       = "hadatac-standards";
	
	@Field("abbreviation")
	private String nsAbbrev = "";
	@Field("name_str")
	private String nsName = "";
	@Field("mime_type_str")
	private String nsType = "";
	@Field("url_str")
	private String nsURL = "";
	
	public NameSpace () {
	}
	
	public NameSpace (String abbrev, String name, String type, String url) {
		nsAbbrev = abbrev;
		nsName = name;
		nsType = type;
		nsURL = url;
	}   
	
	public String getAbbreviation() {
		return nsAbbrev;
	}
	
	public void setAbbreviation(String abbrev) {
		nsAbbrev = abbrev;		
	}
	
	public String getName() {
		return nsName;
	}
	
	public void setName(String name) {
		nsName = name;
	}
	
	public String getType() {
		return nsType;
	}
	
	public void setType(String type) {
		nsType = type;
	}
	
	public String getURL() {
		return nsURL;
	}

	public void setURL(String url) {
		nsURL = url;
	}
	
	public String toString() {
		if (nsAbbrev == null) {
			return "null";
		}
		String showType = "null";
		if (nsType != null)
			showType = nsType;
		if (nsURL == null)
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", NO URL)";
		else 
			return "<" + nsAbbrev + ":> " + nsName + " (" + showType + ", " + nsURL + ")";
	}
	
	public int save() {        
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();
            
            int status = client.addBean(this).getStatus();
            client.commit();
            client.close();
            return status;
        } catch (IOException | SolrServerException e) {
            System.out.println("[ERROR] Namespace.save() - e.Message: " + e.getMessage());
            return -1;
        }
    }
	
	public int delete() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();
            UpdateResponse response = client.deleteById(getAbbreviation());
            client.commit();
            client.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] NameSpace.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] NameSpace.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] NameSpace.delete() - Exception message: " + e.getMessage());
        }
        
        return -1;
    }
	
	public static int deleteAll() {
        try {
            SolrClient client = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();
            UpdateResponse response = client.deleteByQuery("*:*");
            client.commit();
            client.close();
            return response.getStatus();
        } catch (SolrServerException e) {
            System.out.println("[ERROR] NameSpace.delete() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] NameSpace.delete() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] NameSpace.delete() - Exception message: " + e.getMessage());
        }
        
        return -1;
    }
    
    public static NameSpace convertFromSolr(SolrDocument doc) {
        NameSpace object = new NameSpace();
        object.setAbbreviation(doc.getFieldValue("abbreviation").toString());
        object.setName(doc.getFieldValue("name_str").toString());
        object.setType(doc.getFieldValue("mime_type_str").toString());
        object.setURL(doc.getFieldValue("url_str").toString());
        
        return object;
    }
    
    public static List<NameSpace> findByQuery(SolrQuery query) {
        List<NameSpace> list = new ArrayList<NameSpace>();
        
        SolrClient solr = new HttpSolrClient.Builder(
                CollectionUtil.getCollectionPath(CollectionUtil.Collection.NAMESPACE)).build();

        try {
            QueryResponse response = solr.query(query);
            solr.close();
            SolrDocumentList results = response.getResults();
            Iterator<SolrDocument> i = results.iterator();
            while (i.hasNext()) {
                list.add(convertFromSolr(i.next()));
            }
        } catch (Exception e) {
            list.clear();
            System.out.println("[ERROR] OperationMode.find(SolrQuery) - Exception message: " + e.getMessage());
        }
        
        return list;
    }
    
    public static List<NameSpace> findAll() {
        SolrQuery query = new SolrQuery();
        query.set("q", "*:*");
        query.set("rows", "10000000");
        
        return findByQuery(query);
    }
    
    public static NameSpace findByAbbreviation(String abbreviation) {
        SolrQuery query = new SolrQuery();
        query.set("q", "abbreviation:\"" + abbreviation + "\"");
        query.set("rows", "10000000");
        List<NameSpace> namespaces = findByQuery(query);
        if (namespaces.isEmpty()) {
            return null;
        }
        
        return namespaces.get(0);
    }
}
