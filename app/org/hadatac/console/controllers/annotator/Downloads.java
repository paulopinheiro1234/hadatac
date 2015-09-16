package org.hadatac.console.controllers.annotator;

import java.net.URLDecoder;

import play.mvc.*;
import play.mvc.Http.*;
import play.mvc.Result;

import org.hadatac.console.models.CSVAnnotationHandler;
import org.hadatac.console.views.html.annotator.*;
import org.hadatac.utils.NameSpaces;

import com.fasterxml.jackson.databind.ObjectMapper;

public class Downloads extends Controller {

    public static Result uploadCCSV() {
      return ok(completeAnnotation.render());   
    }

    public static Result postUploadCCSV() {
        return ok(completeAnnotation.render());
    }

    public static Result genCCSV() {
        return ok(completeAnnotation.render());   
    }

    public static Result postGenCCSV() {
        return ok(completeAnnotation.render());
    }
    
    public static Result genPreamble(String handler_json) {

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
			int i = 0;
			for (String str : handler.getFields()) {
				System.out.println(str);
				i++;
			}
			  RequestBody body = request().body();
			  String textBody = body.asText();
			  
			  if(textBody != null) {
			    System.out.println("Got: [" + textBody + "]");
			  } else {
			    badRequest("Expecting text/plain request body");
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ok (uploadCSV.render(null, "fail", "Lost deployment information."));
		} 
    	    	
        return ok(completeAnnotation.render());   
    }

    public static final String START_PREAMBLE = "== START-PREAMBLE ==";
    public static final String END_PREAMBLE   = "== START-PREAMBLE ==";

    /* <example-kb> a hadatac:KnowledgeBase; hadatac:hasHost "http://localhost"^^xsd:anyURI .

    <example01-dataset02> a vstoi:Dataset; prov:wasGeneratedBy <dataCollection-example01>; hadatac:hasMeasurementType <mt0>,<mt1> .

    <mt0> a hadatac:MeasurementType; time:inDateTime <ts0>; hadatac:atColumn 3; oboe:ofCharacteristic hadatac-entities:EC-WindDirection; oboe:ofCharacteristic jp-characteristics:EC-WindDirection; oboe:usesStandard oboe-standards:Degree .
    <mt1> a hadatac:MeasurementType; time:inDateTime <ts0>; hadatac:atColumn 2; oboe:ofCharacteristic hadatac-entities:EC-WindSpeed; oboe:usesStandard oboe-standards:MeterPerSecond .
    <ts0> hadatac:atColumn 0 .
    */
   
    public static Result postGenPreamble(String handler_json) {

    	
    	
    	NameSpaces ns = NameSpaces.getInstance();
    	String preamble = START_PREAMBLE;
    	preamble += ns.printNameSpaceList();
    	preamble += "\n";

    	/* 
    	 * Insert KB
    	 */
    	
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
			   * Insert Data Collection
			   */
			
			  /*int i = 0;
			  for (String str : handler.getFields()) {
		  		  System.out.println(str);
	  		 	  i++;
		      }*/
			
			  RequestBody body = request().body();
			  String textBody = body.asText();
			  
			  if(textBody != null) {
			    System.out.println("Got: [" + textBody + "]");
			  } else {
			    badRequest("Expecting text/plain request body");
			  }
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return ok (completeAnnotation.render());
		} 
    	
    	preamble += END_PREAMBLE;
    	System.out.println(preamble);
        return ok(completeAnnotation.render());
    }

    public static Result cancel() {
        return ok(completeAnnotation.render());   
    }

    public static Result postCancel() {
        return ok(completeAnnotation.render());
    }

}
