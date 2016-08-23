package org.hadatac.data.loader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Command;
import org.hadatac.utils.Feedback;

import play.Play;

public class DataContext {
	
	String username;
	String password;
	String kbURL;
	boolean verbose;
	
	public DataContext(String un, String pwd, String kb, boolean ver) {
        System.out.println("Metadata management set for knowledge base at " + kb);
	    username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

	public static Long playTotalMeasurements() {
		DataContext data = new DataContext( "user", 
				"password",
				Play.application().configuration().getString("hadatac.solr.data"), 
				false);
		
		return data.totalMeasurements();
	}
	
	public static Long playTotalDataCollections() {
		DataContext data = new DataContext( "user", 
				"password",
				Play.application().configuration().getString("hadatac.solr.data"), 
				false);
		
		return data.totalDataCollections();
	}
	
	public Long totalMeasurements() {
		SolrClient solr = new HttpSolrClient(kbURL + "/measurement");
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		
		try {
			QueryResponse response = solr.query(parameters);
			solr.close();
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return (long) -1;
	}
	
	public Long totalDataCollections() {
		SolrClient solr = new HttpSolrClient(kbURL + "/sdc");
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		
		try {
			QueryResponse response = solr.query(parameters);
			solr.close();
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
		
		return (long) -1;
	}
	
	public String cleanDataCollections(int mode) {
		String message = "";
	    String straux = "";
	    //System.out.println("Is WEB? " + (mode == Feedback.WEB));
        message += Feedback.println(mode,"   Documents before [clean]: " + totalDataCollections());
        message += Feedback.println(mode, " ");
	    // ATTENTION: For now, it erases entirely the content of the metadata collection 
	    String query1 = "<delete><query>*:*</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = Collections.getCollectionsName(Collections.DATA_COLLECTION) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = Collections.getCollectionsName(Collections.DATA_COLLECTION) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");
		    //Runtime.getRuntime().exec("curl -v " + url1);
		    //Runtime.getRuntime().exec("curl -v " + url2);
		    if (verbose) {
		        message += Feedback.println(mode, url1);
		        message += Feedback.println(mode, url2);
		    }
		    String[] cmd1 = {"curl", "-v", url1};
			message += Feedback.print(mode, "    Erasing documents... ");                
		    straux = Command.exec(mode, verbose, cmd1);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode, "");
			message += Feedback.print(mode, "   Committing... ");                
		    String[] cmd2 = {"curl", "-v", url2};
		    straux = Command.exec(mode, verbose, cmd2);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode," ");
		    message += Feedback.println(mode," ");
			message += Feedback.print(mode,"   Triples after [clean]: " + totalDataCollections());                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[DataManagement] - ERROR encoding URLs");
		    //e.printStackTrace();
		    return message;
		}
        return message;
	}
	
	public String cleanDataAcquisitions(int mode) {
		String message = "";
	    String straux = "";
	    //System.out.println("Is WEB? " + (mode == Feedback.WEB));
        message += Feedback.println(mode,"   Documents before [clean]: " + totalMeasurements());
        message += Feedback.println(mode, " ");
	    // ATTENTION: For now, it erases entirely the content of the metadata collection 
	    String query1 = "<delete><query>*:*</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = Collections.getCollectionsName(Collections.DATA_ACQUISITION) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = Collections.getCollectionsName(Collections.DATA_ACQUISITION) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");
		    //Runtime.getRuntime().exec("curl -v " + url1);
		    //Runtime.getRuntime().exec("curl -v " + url2);
		    if (verbose) {
		        message += Feedback.println(mode, url1);
		        message += Feedback.println(mode, url2);
		    }
		    String[] cmd1 = {"curl", "-v", url1};
			message += Feedback.print(mode, "    Erasing documents... ");                
		    straux = Command.exec(mode, verbose, cmd1);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode, "");
			message += Feedback.print(mode, "   Committing... ");                
		    String[] cmd2 = {"curl", "-v", url2};
		    straux = Command.exec(mode, verbose, cmd2);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode," ");
		    message += Feedback.println(mode," ");
			message += Feedback.print(mode,"   Triples after [clean]: " + totalMeasurements());                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[DataManagement] - ERROR encoding URLs");
		    //e.printStackTrace();
		    return message;
		}
        return message;
	}
}
