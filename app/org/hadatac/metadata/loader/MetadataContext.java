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
import org.hadatac.metadata.loader.NameSpace;
import org.hadatac.metadata.loader.NameSpaces;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import play.Play;
import play.libs.ws.*;
import play.mvc.Call;

public class MetadataContext {

    public static final int WEB = 0;
    public static final int COMMANDLINE = 1;
     
    String username = null;
    String password = null;
    String kbURL = null;   
	         // For local use:
	         //   - http://localhost:7574/solr
	         // For remote use:
	         //   - http://jeffersontest.tw.rpi.edu/solr4
    boolean verbose = false;

    String processMessage = "";
    String loadFileMessage = "";
	
    public MetadataContext(String un, String pwd, String kb, boolean ver) {
        System.out.println("Metadata management set for knowledge base at " + kb);
	    username = un;
	    password = pwd;
	    kbURL = kb;
	    verbose = ver;
    }

    public static Long playTotalTriples() {
	     MetadataContext metadata = new MetadataContext("user", 
	    		                                        "password", 
	    		                                        Play.application().configuration().getString("hadatac.solr.triplestore"), 
	    		                                        false);
      return metadata.totalTriples();
    }

    private String executeQuery(String query) throws IllegalStateException, IOException{
        CloseableHttpClient httpClient = HttpClients.createDefault();
        //Scanner in = null;
        try {
        	HttpClient client = new DefaultHttpClient();
         	//System.out.println("Query: <" + kbURL + "/store/sparql?q=" + query + ">");
         	HttpGet request = new HttpGet(kbURL + "/store/sparql?q=" + URLEncoder.encode(query, "UTF-8"));
        	request.setHeader("Accept", "application/sparql-results+xml");
        	HttpResponse response = client.execute(request);
            StringWriter writer = new StringWriter();
            IOUtils.copy(response.getEntity().getContent(), writer, "utf-8");
    
            return writer.toString();
            
        } finally
        {
            //in.close();
            //request.close();
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
	    		   message += println(mode, line);
	       }
	       //System.out.println("</ERROR>");
	       int exitVal = proc1.waitFor();
	       message += print(mode, "    exit value: [" + exitVal + "]    ");
	       //message += println(mode, "   Process: [" + command[0] + "]   exitValue: [" + exitVal + "]");
	    } catch (Throwable t) {
		       t.printStackTrace();
		}
		return message;
    }

    public static String println(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	} else {
    		str += "<br>";
    	}
    	return str;
    }
    
    public static String print(int mode, String str) {
    	if (mode == COMMANDLINE) {
    		System.out.print(str);
    	}
    	return str;
    }
    
	public String clean(int mode) {
	    String message = "";
	    String straux = "";
	    //System.out.println("Is WEB? " + (mode == WEB));
        message += println(mode,"   Triples before [clean]: " + totalTriples());
        message += println(mode, " ");
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
		        message += println(mode, url1);
		        message += println(mode, url2);
		    }
		    String[] cmd1 = {"curl", "-v", url1};
			message += print(mode, "    Erasing triples... ");                
		    straux = executeCommand(mode, cmd1);
		    if (mode == WEB) {
		    	message += straux;
		    }
		    message += println(mode, "");
			message += print(mode, "   Committing... ");                
		    String[] cmd2 = {"curl", "-v", url2};
		    straux = executeCommand(mode, cmd2);
		    if (mode == WEB) {
		    	message += straux;
		    }
		    message += println(mode," ");
		    message += println(mode," ");
			message += print(mode,"   Triples after [clean]: " + totalTriples());                
		} catch (UnsupportedEncodingException e) {
		    System.out.println("[MetadataManagement] - ERROR encoding URLs");
		    //e.printStackTrace();
		    return message;
		}
        return message; 
	}
	
	/* 
	 *   contentType correspond to the mime type required for curl to process the data provided. For example, application/rdf+xml is
	 *   used to process rdf/xml content.
	 *   
	 */
	public Long loadLocalFile(int mode, String filePath, String contentType) {
		loadFileMessage = "";
		//System.out.println("File: " + filePath + "   Content Type: " + contentType);
		Long total = totalTriples();
		if (verbose) {
			System.out.println("curl -v " + kbURL + "/store/update/bulk?commit=true -H \"Content-Type: " + contentType + "\" --data-binary @" + filePath);
		}
		String[] cmd = {"curl", "-v", kbURL + "/store/update/bulk?commit=true","-H", "Content-Type: " + contentType, "--data-binary", "@" + filePath};
		loadFileMessage += executeCommand(mode, cmd);
		Long newTotal = totalTriples();
		return (newTotal - total);
	}
	
	public String loadOntologies(int mode) {
	    String message = "";
		Long total = totalTriples();
		message += println(mode, "   Triples before [loadOntologies]: " + total);
		message += println(mode," ");
		message += NameSpaces.getInstance().copyNameSpacesLocally(mode);
		for (Map.Entry<String, NameSpace> entry : NameSpaces.table.entrySet()) {
	    	String abbrev = entry.getKey().toString();
	    	String nsURL = entry.getValue().getURL();
	    	if ((abbrev != null) && (nsURL != null) && (entry.getValue().getType() != null) && !nsURL.equals("")) {
	    		String filePath = "copy" + "-" + abbrev.replace(":","");
	    		message += print(mode, "   Uploading " + filePath);
	    		for (int i = filePath.length(); i < 50; i++) {
	    			message += print(mode, ".");
	    		}
	    		loadLocalFile(mode, filePath, entry.getValue().getType());
	    		message += loadFileMessage;
	    		Long newTotal = totalTriples();
	    		message += println(mode, "   Added " + (newTotal - total) + " triples.");
	    		
	    		total = newTotal;
	    	}	          
	    }
		message += println(mode," ");
		message += println(mode, "   Triples after [loadOntologies]: " + totalTriples());
		return message;
	}
}	
	
