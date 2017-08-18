package org.hadatac.console.controllers.annotator;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;

import play.Play;
import play.mvc.*;
import play.mvc.Http.*;
import play.mvc.Result;

import org.apache.commons.io.FileUtils;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.dataacquisitionsearch.LoadCCSV;
import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.data.api.DataFactory;
import org.hadatac.data.loader.util.Arguments;
import org.hadatac.data.loader.util.FileFactory;
import org.hadatac.data.model.ParsingResult;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpaces;

import com.fasterxml.jackson.databind.ObjectMapper;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class Downloads extends Controller {

	/* 
	 *  Download operations
	 */
	public static final String OPER_UPLOAD     = "Upload CCSV";
	public static final String OPER_CCSV       = "Download CCSV";
	public static final String OPER_PREAMBLE   = "Download Preamble";
	public static final String OPER_FINISH     = "Finish";
	
	/*
	 *  Preamble fragments
	 */

	public static final String FRAG_START_PREAMBLE          = "== START-PREAMBLE ==\n";
    public static final String FRAG_END_PREAMBLE            = "== END-PREAMBLE ==\n";

    public static final String FRAG_KB_PART1                = "<kb> a hadatac:KnowledgeBase; hadatac:hasHost \"";
    public static final String FRAG_KB_PART2                = "\"^^xsd:anyURI . \n\n";

    public static final String FRAG_DATASET                 = " a vstoi:Dataset; prov:wasGeneratedBy <";
    public static final String FRAG_HAS_MEASUREMENT_TYPE    = " hadatac:hasMeasurementType ";
    public static final String FRAG_MT                      = "<mt";

    public static final String FRAG_MEASUREMENT_TYPE_PART1  = "> a hadatac:MeasurementType; ";
    public static final String FRAG_MEASUREMENT_TYPE_PART2  = " hadatac:atColumn ";
    public static final String FRAG_MEASUREMENT_TYPE_PART3  = "; hasco:hasEntity ";
    public static final String FRAG_MEASUREMENT_TYPE_PART4  = "; hasco:hasAttribute ";
    public static final String FRAG_MEASUREMENT_TYPE_PART5  = "; hasco:hasUnit ";

    public static final String FRAG_IN_DATE_TIME            = "time:inDateTime";
    public static final String FRAG_IN_DATE_TIME_SUFFIX     = " <ts0>; ";
    public static final String FRAG_IN_DATE_TIME_STATEMENT  = "<ts0> hadatac:atColumn ";

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public static Result postGenerate(String handler_json) {

    	String oper = "";
    	
    	RequestBody body = request().body();
    	if (body == null) {
            return ok(completeAnnotation.render("Error processing form: form appears to be empty."));       		    		
    	}
    	
    	String textBody = body.asText();
    	Properties p = new Properties();
    	try {
	    	p.load(new StringReader(textBody));
		} catch (Exception e) {
			e.printStackTrace();
            return ok(completeAnnotation.render("Error processing form: form appears to be empty."));       		
		}

    	System.out.println("Selection: " + p.getProperty("submitButton"));
    	if (p.getProperty("submitButton") != null)
    		oper = p.getProperty("submitButton");
    	
    	if (oper.equals(OPER_FINISH)) {
            return ok(completeAnnotation.render("Annotation operation finished."));       		
    	}
    	
    	String preamble = FRAG_START_PREAMBLE;
    	preamble += NameSpaces.getInstance().printTurtleNameSpaceList();
    	preamble += "\n";

    	/* 
    	 * Insert KB
    	 */
    	
    	preamble += FRAG_KB_PART1;
    	preamble += Play.application().configuration().getString("hadatac.console.kb"); 
    	preamble += FRAG_KB_PART2;
    	
    	try {
			handler_json = URLDecoder.decode(handler_json, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
		}
    	System.out.println(handler_json);
    	
    	ObjectMapper mapper = new ObjectMapper();    	
    	CSVAnnotationHandler handler = null;
    	try {
			handler = mapper.readValue(handler_json, CSVAnnotationHandler.class);

			  /* 
			   * Insert Data Set
			   */
			
			  preamble += "<" + DataFactory.getNextURI(DataFactory.DATASET_ABBREV) + ">";
			  preamble += FRAG_DATASET;
			  preamble += handler.getDataAcquisitionUri() + ">; ";
			
			  int i = 0;
			  int timeStampIndex = -1;
			  ArrayList<Integer> mt = new ArrayList<Integer>();
			  for (String str : handler.getFields()) {
				  System.out.println(str);
				  System.out.println("get " + i + "-entity: [" + p.getProperty(i + "-entity") + "]");
				  System.out.println("get " + i + "-characteristic: [" + p.getProperty(i + "-characteristic") + "]");
				  System.out.println("get " + i + "-unit:           [" + p.getProperty(i + "-unit") + "]");
				  if ((p.getProperty(i + "-entity") != null) && 
					  (!p.getProperty(i + "-entity").equals("")) &&
					  (p.getProperty(i + "-characteristic") != null) && 
					  (!p.getProperty(i + "-characteristic").equals("")) && 
					  (p.getProperty(i + "-unit") != null) && 
					  (!p.getProperty(i + "-unit").equals(""))) {

					  if (p.getProperty(i + "-unit").equals(FRAG_IN_DATE_TIME)) {
						  timeStampIndex = i; 
					  } else {
						  mt.add(i);
					  }
				  }
				  i++;
			  }
			  
			  preamble += FRAG_HAS_MEASUREMENT_TYPE;	
			  int aux = 0;
			  for (Integer mt_count : mt) {
				  preamble += FRAG_MT + aux + "> ";
				  if(aux != (mt.size() - 1)){
					  preamble += ", ";
				  }
				  aux++;
			  }
			  preamble += ".\n\n";
			  System.out.println(preamble);
			  
			  /*
			   * Insert measurement types
			   */
			  
			  aux = 0;
			  for (Integer mt_count : mt) {
				  preamble += FRAG_MT + aux;
				  preamble += FRAG_MEASUREMENT_TYPE_PART1;
				  if (timeStampIndex != -1) {
					  preamble += FRAG_IN_DATE_TIME;
					  preamble += FRAG_IN_DATE_TIME_SUFFIX;
				  }
				  preamble += FRAG_MEASUREMENT_TYPE_PART2;
				  preamble += mt_count;
				  preamble += FRAG_MEASUREMENT_TYPE_PART3;
				  preamble += "<" + p.getProperty(mt_count + "-entity") + ">"; 
				  preamble += FRAG_MEASUREMENT_TYPE_PART4;
				  preamble += "<" + p.getProperty(mt_count + "-characteristic") + ">"; 
				  preamble += FRAG_MEASUREMENT_TYPE_PART5;
				  preamble += "<" + p.getProperty(mt_count + "-unit") + ">"; 
				  preamble += " .\n";
				  aux++;
			  }

			  if (timeStampIndex != -1) {
				  preamble += "\n";
				  preamble += FRAG_IN_DATE_TIME_STATEMENT + " " + timeStampIndex + "  . \n";  
			  }
			  
			  if (textBody == null) {
				  badRequest("Expecting text/plain request body");
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ok(completeAnnotation.render("Error processing form. Please restart form."));
		} 
    	
    	preamble += FRAG_END_PREAMBLE;
    	
    	if (oper.equals(OPER_PREAMBLE)) {
    		return ok(preamble).as("text/turtle");
    	}
    	
    	if (oper.equals(OPER_CCSV)) {
		    File newFile = new File(handler.getDatasetName()); 
		    try {
				preamble += FileUtils.readFileToString(newFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				return ok(completeAnnotation.render("Error reading cached CSV file. Please restart form."));
			}
	        return ok(preamble).as("text/turtle");
    	}
    	
    	if (oper.equals(OPER_UPLOAD)) {
    		File newFile = new File(handler.getDatasetName()); 
		    try {
				preamble += FileUtils.readFileToString(newFile, "UTF-8");
			} catch (IOException e) {
				e.printStackTrace();
				return ok(completeAnnotation.render("Error reading cached CSV file. Please restart form."));
			}

		    try {
				FileUtils.writeStringToFile(new File(LoadCCSV.UPLOAD_NAME), preamble);
			} catch (IOException e) {
				e.printStackTrace();
				return ok(completeAnnotation.render("Error aving CCSV file locally. Please restart form."));
			}
		    
		    ParsingResult result = LoadCCSV.playLoadCCSV();
			return ok(completeAnnotation.render(result.getMessage()));
		    
    	}
    	
		return ok(completeAnnotation.render("Error processing form: unspecified download operation."));
    }

}
