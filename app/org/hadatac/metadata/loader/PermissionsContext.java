package org.hadatac.metadata.loader;

import java.util.Map;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.jena.query.DatasetAccessor;
import org.apache.jena.query.DatasetAccessorFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RiotNotFoundException;
import org.apache.jena.shared.NotFoundException;
import org.hadatac.utils.Collections;
import org.hadatac.utils.Feedback;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import play.Play;
import play.libs.ws.*;
import play.mvc.Call;

public class PermissionsContext implements RDFContext {

    String username = null;
    String password = null;
    String kbURL = null;   
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";
	
    public PermissionsContext(String un, String pwd, String kb, boolean ver) {
        //System.out.println("Permissions management set for knowledge base at " + kb);
	    username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

    public static Long playTotalTriples() {
	     PermissionsContext permissions = new PermissionsContext("user", 
	    		                                        "password", 
	    		                                        Play.application().configuration().getString("hadatac.solr.permissions"), 
	    		                                        false);
      return permissions.totalTriples();
    }

    private String executeQuery(String query) throws IllegalStateException, IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
         	HttpGet request = new HttpGet(kbURL + Collections.PERMISSIONS_SPARQL+ "?q=" + URLEncoder.encode(query, "UTF-8"));
        	request.setHeader("Accept", "application/sparql-results+xml");
        	HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
    
            return writer.toString();
            
        } finally {
        }
    }// /executeQuery()
    
    public Long totalTriples() {
    	String query = "SELECT (COUNT(*) as ?tot) WHERE { ?s ?p ?o . }";
    	try {
			String result = executeQuery(query);
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = factory.newDocumentBuilder();
		    InputSource is = new InputSource(new StringReader(result));
		    Document doc = builder.parse(is);
		    //System.out.println(result);
		    return Long.valueOf(doc.getElementsByTagName("literal").item(0).getTextContent()).longValue();
    	} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return (long) -1;
		} 
    }
    
    private String executeCommand(int mode, String[] command) {
    	String message = "";
		try {
	       Runtime rt = Runtime.getRuntime();
	       Process proc1 = rt.exec(command);
	       InputStream stderr = proc1.getErrorStream();
	       InputStreamReader isr = new InputStreamReader(stderr);
	       BufferedReader br = new BufferedReader(isr);
	       String line = null;
	       //System.out.println("<ERROR>");
	       if (verbose) {
	    	   while ( (line = br.readLine()) != null)
	    		   message += Feedback.println(mode, line);
	       }
	       //System.out.println("</ERROR>");
	       int exitVal = proc1.waitFor();
	       message += Feedback.print(mode, "    exit value: [" + exitVal + "]    ");
	       //message += println(mode, "   Process: [" + command[0] + "]   exitValue: [" + exitVal + "]");
	    } catch (Throwable t) {
		       t.printStackTrace();
		}
		return message;
    }

	public String clean(int mode) {
	    String message = "";
	    String straux = "";
	    //System.out.println("Is WEB? " + (mode == Feedback.WEB));
        message += Feedback.println(mode,"   Triples before [clean]: " + totalTriples());
        message += Feedback.println(mode, " ");
	    // ATTENTION: For now, it erases entirely the content of the metadata collection 
	    String query1 = "<delete><query>*:*</query></delete>";
	    String query2 = "<commit/>";
	    
	    String url1;
	    String url2;
		try {
		    url1 = kbURL + "/store/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
		    url2 = kbURL + "/store/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");
		    //Runtime.getRuntime().exec("curl -v " + url1);
		    //Runtime.getRuntime().exec("curl -v " + url2);
		    if (verbose) {
		        message += Feedback.println(mode, url1);
		        message += Feedback.println(mode, url2);
		    }
		    String[] cmd1 = {"curl", "-v", url1};
			message += Feedback.print(mode, "    Erasing triples... ");                
		    straux = executeCommand(mode, cmd1);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode, "");
			message += Feedback.print(mode, "   Committing... ");                
		    String[] cmd2 = {"curl", "-v", url2};
		    straux = executeCommand(mode, cmd2);
		    if (mode == Feedback.WEB) {
		    	message += straux;
		    }
		    message += Feedback.println(mode," ");
		    message += Feedback.println(mode," ");
			message += Feedback.print(mode,"   Triples after [clean]: " + totalTriples());                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[MetadataManagement] - ERROR encoding URLs");
		    //e.printStackTrace();
		    return message;
		}
        return message; 
	}
	
	public String getLang(String contentType) {
		if (contentType.contains("turtle")) {
			return "TTL";
		} else if (contentType.contains("rdf+xml")) {
			return "RDF/XML";
		} else {
			return "";
		}
	}
	
	/* 
	 *   contentType correspond to the mime type required for curl to process the data provided. For example, application/rdf+xml is
	 *   used to process rdf/xml content.
	 *   
	 */
	public Long loadLocalFile(int mode, String filePath, String contentType) {
		Model model = ModelFactory.createDefaultModel();
		DatasetAccessor accessor = DatasetAccessorFactory.createHTTP(kbURL + Collections.PERMISSIONS_GRAPH);		

		loadFileMessage = "";
		Long total = totalTriples();
		try {
			model.read(filePath, getLang(contentType));
			accessor.add(model);
		} catch (NotFoundException e) {
			System.out.println("NotFoundException: file " + filePath);
			System.out.println("NotFoundException: " + e.getMessage());
		} catch (RiotNotFoundException e) {
			System.out.println("RiotNotFoundException: file " + filePath);
			System.out.println("RiotNotFoundException: " + e.getMessage());
		} catch (Exception e) {
			System.out.println("Exception: file " + filePath);
			System.out.println("Exception: " + e.getMessage());
		}
		
		Long newTotal = totalTriples();
		return (newTotal - total);
	}
	
}	
	
