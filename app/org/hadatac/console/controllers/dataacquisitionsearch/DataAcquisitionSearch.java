package org.hadatac.console.controllers.dataacquisitionsearch;

import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.FacetsWithCategories;
import org.hadatac.console.models.SpatialQueryResults;
import org.hadatac.console.models.SysUser;
import org.hadatac.console.models.ObjectDetails;
import org.hadatac.console.models.Pivot;

import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import org.hadatac.console.controllers.dataacquisitionsearch.routes;
import org.hadatac.console.controllers.metadataacquisition.ViewSubject;
import org.hadatac.console.views.formdata.FacetFormData;
import org.hadatac.console.views.html.dataacquisitionsearch.facetOnlyBrowser;
import org.hadatac.console.views.html.dataacquisitionsearch.dataacquisition_browser;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Alignment;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.StudyObject;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

public class DataAcquisitionSearch extends Controller {
	
	@Inject HttpExecutionContext ec;

    public static FacetFormData facet_form = new FacetFormData();
    public static FacetsWithCategories field_facets = new FacetsWithCategories();
    public static FacetsWithCategories query_facets = new FacetsWithCategories();
    public static FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public static FacetsWithCategories range_facets = new FacetsWithCategories();
    public static FacetsWithCategories cluster_facets = new FacetsWithCategories();
    public static SpatialQueryResults query_results = new SpatialQueryResults();
    
    public static List<String> getPermissions(String permissions) {
    	List<String> result = new ArrayList<String>();
    	
    	if (permissions != null) {
	    	StringTokenizer tokens = new StringTokenizer(permissions, ",");
	    	while (tokens.hasMoreTokens()) {
	    		result.add(tokens.nextToken());
	    	}
    	}
    	
    	return result;
    }
    
    private static ObjectDetails getObjectDetails(AcquisitionQueryResult results) {
    	Set<String> setObj = new HashSet<String>();
    	ObjectDetails objDetails = new ObjectDetails();
    	if (results != null) {
    		for (Measurement m: results.getDocuments()) {
    			setObj.add(m.getObjectUri());
            }
            for (String uri: setObj) {
            	if (uri != null) {
            		String html = ViewSubject.findBasicHTML(uri);
            		if (html != null) {
            			objDetails.putObject(uri, html);
            		}
            	}
            }
        }
    	
    	return objDetails;
    }

    // @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(int page, int rows) {
    	return indexInternal(0, page, rows);
    }
    
    // @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndex(int page, int rows) {
    	return index(page, rows);
    }

    // @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result indexData(int page, int rows) {
    	return indexInternal(1, page, rows);
    }
    
    // @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postIndexData(int page, int rows) {
    	return indexData(page, rows);
    }

    private Result indexInternal(int mode, int page, int rows) {
    	String facets = "";
    	if (request().body().asFormUrlEncoded() != null) {
    		facets = request().body().asFormUrlEncoded().get("facets")[0];
    	}
    	System.out.println("\n\n\n\n\nfacets: " + facets);
    	
    	FacetHandler facetHandler = new FacetHandler();
    	facetHandler.loadFacets(facets);

    	AcquisitionQueryResult results = null;
    	String ownerUri;
    	final SysUser user = AuthApplication.getLocalUser(session());
    	if (null == user) {
    	    ownerUri = "Public";
    	}
    	else {
    		ownerUri = UserManagement.getUriByEmail(user.getEmail());
    		if(null == ownerUri){
    			ownerUri = "Public";
    		}
    	}
    	results = Measurement.find(ownerUri, page, rows, facets);
    	
    	ObjectDetails objDetails = getObjectDetails(results);

		if (mode == 0) {
		    return ok(facetOnlyBrowser.render(page, rows, ownerUri, facets, results.getDocumentSize(), 
	    			results, results.toJSON(), facetHandler, objDetails.toJSON(), 
	    			Measurement.getFieldNames(), ObjectCollection.findAll()));
		} else {
		    return ok(dataacquisition_browser.render(page, rows, ownerUri, facets, results.getDocumentSize(), 
	    			results, results.toJSON(), facetHandler, objDetails.toJSON(), 
	    			Measurement.getFieldNames(), ObjectCollection.findAll()));
		}
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result download() {    	
    	String ownerUri = getOwnerUri();
    	String email = getUserEmail();
    	
    	String facets = "";
    	List<String> selectedFields = new LinkedList<String>();
    	Map<String, String[]> name_map = request().body().asFormUrlEncoded();
    	if (name_map != null) {
    		facets = name_map.get("facets")[0];
    		
    		List<String> keys = new ArrayList<String>(name_map.keySet());
    		keys.remove("facets");
    		
    		selectedFields.addAll(keys);
    	}
    	System.out.println("selectedFields: " + selectedFields);
    	
    	AcquisitionQueryResult results = Measurement.find(ownerUri, -1, -1, facets);
    	
    	final String finalFacets = facets;
    	CompletableFuture.supplyAsync(() -> Downloader.generateCSVFile(
    			results.getDocuments(), finalFacets, selectedFields, email), 
    			ec.current());
		
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	return redirect(routes.Downloader.index());
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result downloadAlignment() {
    	String ownerUri = getOwnerUri();
    	String email = getUserEmail();
    	
    	String facets = "";
    	List<String> selectedFields = new LinkedList<String>();
    	Map<String, String[]> name_map = request().body().asFormUrlEncoded();
    	if (name_map != null) {
    		facets = name_map.get("facets")[0];
    		String objectType = name_map.get("selObjectType")[0].toString();
    	}
    	
    	AcquisitionQueryResult results = Measurement.find(ownerUri, -1, -1, facets);
    	
    	Alignment alignment = new Alignment();
    	Pivot pivot = results.extra_facets.get(FacetHandler.SUBJECT_CHARACTERISTIC_FACET);
    	alignment.fromPivot(pivot);
    	
    	final String finalFacets = facets;
    	final String finalOwnerUri = ownerUri;
    	CompletableFuture.supplyAsync(() -> Downloader.generateCSVFileByAlignment(
    	        results.getDocuments(), alignment, finalFacets, email), 
    			ec.current());
		
    	try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
    	
    	return redirect(routes.Downloader.index());
    }
    
    private String getUserEmail() {
        final SysUser user = AuthApplication.getLocalUser(session());
        if (null != user) {
            return user.getEmail();
        }
        
        return "";
    }
    
    private String getOwnerUri() {
        String ownerUri = "";
        final SysUser user = AuthApplication.getLocalUser(session());
        if (null == user) {
            ownerUri = "Public";
        } else {
            ownerUri = UserManagement.getUriByEmail(user.getEmail());
            if(null == ownerUri){
                ownerUri = "Public";
            }
        }
        
        return ownerUri;
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDownload() {
    	return download();
    }
    
    @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result postDownloadAlignment() {
    	return downloadAlignment();
    }
}
