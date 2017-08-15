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
	
	public static Long playTotalDataAcquisitions() {
		DataContext data = new DataContext( "user", 
				"password",
				Play.application().configuration().getString("hadatac.solr.data"), 
				false);
		
		return data.totalDataAcquisitions();
	}
	
	private Long totalDocuments(String solrCoreName) {
		SolrClient solr = new HttpSolrClient.Builder(kbURL + solrCoreName).build();
		SolrQuery parameters = new SolrQuery();
		parameters.set("q", "*:*");
		parameters.set("rows", 0);
		
		try {
			QueryResponse response = solr.query(parameters);
			solr.close();
			return response.getResults().getNumFound();
		} catch (SolrServerException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		return (long) -1;
	}
	
	public Long totalMeasurements() {
		return totalDocuments(Collections.DATA_ACQUISITION);
	}
	
	public Long totalUsers() {
		return totalDocuments(Collections.AUTHENTICATE_USERS);
	}
	
	public Long totalDataAcquisitions() {
		return totalDocuments(Collections.DATA_COLLECTION);
	}
	
	private String cleanAllDocuments(int mode, String solrCoreName) {
		String message = "";
	    String straux = "";
	    
        message += Feedback.println(mode,"   Documents before [clean]: " + totalDocuments(solrCoreName));
        message += Feedback.println(mode, " ");

	    String query1 = "<delete><query>*:*</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = Collections.getCollectionsName(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = Collections.getCollectionsName(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");

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
			message += Feedback.print(mode,"   Triples after [clean]: " + totalDocuments(solrCoreName));                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[DataManagement] - ERROR encoding URLs");
		    return message;
		}
		
        return message;
	}
	
	private String cleanSpecifiedStudy(int mode, String studyURI) {
		String solrCoreName = Collections.STUDIES;
		String message = "";
	    String straux = "";
	    
        message += Feedback.println(mode,"   Documents before [clean]: " + totalDocuments(solrCoreName));
        message += Feedback.println(mode, " ");

	    String query1 = "<delete><query>studyUri:\"" + studyURI +"\"</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = Collections.getCollectionsName(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = Collections.getCollectionsName(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");

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
			message += Feedback.print(mode,"   Triples after [clean]: " + totalDocuments(solrCoreName));                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[DataManagement] - ERROR encoding URLs");
		    return message;
		}
		
        return message;
	}
	
	public String cleanDataAcquisitions(int mode) {
		return cleanAllDocuments(mode, Collections.DATA_COLLECTION);
	}
	
	public String cleanDataUsers(int mode) {
		return cleanAllDocuments(mode, Collections.AUTHENTICATE_USERS);
	}
	
	public String cleanDataAccounts(int mode) {
		return cleanAllDocuments(mode, Collections.AUTHENTICATE_ACCOUNTS);
	}
	
	public String cleanAcquisitionData(int mode) {
		return cleanAllDocuments(mode, Collections.DATA_ACQUISITION);
	}
	
	public String cleanStudy(int mode, String studyURI) {
		return cleanSpecifiedStudy(mode, studyURI);
	}
}
