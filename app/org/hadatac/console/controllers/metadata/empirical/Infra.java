package org.hadatac.console.controllers.metadata.empirical;

import java.util.List;
import java.util.Map;

import org.hadatac.entity.pojo.DetectorType;
import org.hadatac.entity.pojo.GenericInstance;
import org.hadatac.entity.pojo.HADatAcClass;
import org.hadatac.entity.pojo.HADatAcThing;
import org.hadatac.entity.pojo.InstrumentType;
import org.hadatac.entity.pojo.Platform;
import org.hadatac.entity.pojo.PlatformType;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.models.OtMSparqlQueryResults;
import org.hadatac.console.views.html.metadata.*;
import org.hadatac.console.views.html.metadata.empirical.*;
import org.hadatac.console.views.html.error_page;
import play.mvc.Result;
import play.mvc.Controller;

public class Infra extends Controller {

	final static int PAGESIZE = 12;

	public static String INFRA_PLATFORM_TYPE = "PlatformType";
	public static String INFRA_PLATFORM = "Platform";
	public static String INFRA_INSTRUMENT_TYPE = "InstrumentType";
	public static String INFRA_INSTRUMENT = "Instrument";
	public static String INFRA_DETECTOR_TYPE = "DetectorType";
	public static String INFRA_DETECTOR = "Detector";

	@Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(String dir, String filename, String da_uri) {
		String platformJson = GenericInstance.jsonInstanceStatisticsByType("vstoi:" + Infra.INFRA_PLATFORM); 
		String instrumentJson = GenericInstance.jsonInstanceStatisticsByType("vstoi:" + Infra.INFRA_INSTRUMENT); 
		String detectorJson = GenericInstance.jsonInstanceStatisticsByType("vstoi:" + Infra.INFRA_DETECTOR); 
        return ok(infra.render(dir, filename, da_uri, platformJson, instrumentJson, detectorJson));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postIndex(String dir, String filename, String da_uri) {
        return index(dir, filename, da_uri);
    }

    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result hierarchy(String tabName, String dir, String filename, String da_uri) {
        //SparqlQuery query = new SparqlQuery();
        //GetSparqlQueryDynamic query_submit = new GetSparqlQueryDynamic(query);
        OtMSparqlQueryResults results;
        List<HADatAcClass> objects = null;
        String query_json = null;
        String labelUri = null;
        System.out.println("MetadataEntry.java is requesting: " + tabName);
        try {
        	if (tabName.equals(INFRA_PLATFORM_TYPE)) {
        		PlatformType pt = new PlatformType();
        		query_json = pt.getHierarchyJson();
        		labelUri = pt.mapTypeLabelToUri();
        		objects = pt.findGeneric();
        	} else if (tabName.equals(INFRA_INSTRUMENT_TYPE)){
        		InstrumentType it = new InstrumentType();
        		query_json = it.getHierarchyJson();
        		labelUri = it.mapTypeLabelToUri();
        		objects = it.findGeneric();
        	} else if (tabName.equals(INFRA_DETECTOR_TYPE)){
        		DetectorType dt = new DetectorType();
        		query_json = dt.getHierarchyJson();
        		labelUri = dt.mapTypeLabelToUri();
        		objects = dt.findGeneric();
        	}
    		System.out.println(query_json);
        	//query_json = query_submit.executeQuery(tabName);
            if (query_json.isEmpty()) {
                return badRequest();
            }
            //System.out.println("JSON: [" + query_json + "]");
            results = new OtMSparqlQueryResults(query_json, true, tabName);
            //System.out.println("OtM: [" + results + "]");
        } catch (IllegalStateException | NullPointerException e1) {
            return internalServerError(error_page.render(e1.toString(), tabName));
        }
        return ok(infra_browser.render(dir, filename, da_uri, results, objects, labelUri, tabName));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postHierarchy(String concept, String dir, String filename, String da_uri) {
        return hierarchy(concept, dir, filename, da_uri);
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result findWithPages(String concept, String dir, String filename, String da_uri, int offset) {
    	List<GenericInstance> instances = GenericInstance.findGenericWithPages("vstoi:" + concept, PAGESIZE, offset * PAGESIZE);
    	int total = GenericInstance.getNumberGenericInstances("vstoi:" + concept);
        return ok(instanceManagement.render(dir, filename, da_uri, total, PAGESIZE, offset, instances, concept));
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
	public Result postFindWithPages(String concept, String dir, String filename, String da_uri, int offset) {
        return findWithPages(concept, dir, filename, da_uri, offset);
    }

}