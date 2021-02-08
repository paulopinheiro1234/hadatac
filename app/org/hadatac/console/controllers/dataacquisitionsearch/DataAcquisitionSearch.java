package org.hadatac.console.controllers.dataacquisitionsearch;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import com.typesafe.config.ConfigFactory;
import module.DatabaseExecutionContext;
import org.hadatac.console.activities.SearchActivityAnnotation;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.*;
import org.hadatac.console.views.html.dataacquisitionsearch.dataacquisition_browser;
import org.hadatac.console.views.html.dataacquisitionsearch.facetOnlyBrowser;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.SPARQLUtilsFacetSearch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.libs.concurrent.HttpExecutionContext;
import play.mvc.Controller;
import play.mvc.Result;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;

@SearchActivityAnnotation()
public class DataAcquisitionSearch extends Controller {

    private static final Logger log = LoggerFactory.getLogger(DataAcquisitionSearch.class);

    @Inject HttpExecutionContext ec;
    @Inject
    DatabaseExecutionContext databaseExecutionContext;

    public static FacetFormData facet_form = new FacetFormData();
    public static FacetsWithCategories field_facets = new FacetsWithCategories();
    public static FacetsWithCategories query_facets = new FacetsWithCategories();
    public static FacetsWithCategories pivot_facets = new FacetsWithCategories();
    public static FacetsWithCategories range_facets = new FacetsWithCategories();
    public static FacetsWithCategories cluster_facets = new FacetsWithCategories();
    public static SpatialQueryResults query_results = new SpatialQueryResults();

    // looks like no one is calling this
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
                	// NEEDS TO REPLACE WITH VIEWSTUDYOBJECT
                    //String html = ViewSubject.findBasicHTML(uri);
                    //if (html != null) {
                    //    objDetails.putObject(uri, html);
                    //}
                }
            }
        }

        return objDetails;
    }

    // @Restrict(@Group(AuthApplication.DATA_OWNER_ROLE))
    public Result index(int page, int rows) {

        //printMemoryStats();
        /*long startTime = System.currentTimeMillis();
        Model model = SPARQLUtilsFacetSearch.createInMemoryModel();
        System.out.println("in-memory model created, taking " + (System.currentTimeMillis()-startTime) + "ms, with # of triples = " + model.size());
        */
        //printMemoryStats();

        SPARQLUtilsFacetSearch.clearCache();
        // SolrUtilsFacetSearch.clearCache();

        if ( "ON".equalsIgnoreCase(ConfigFactory.load().getString("hadatac.facet_search.concurrency")) ) {
            log.debug("using async calls for facet search....");
            return indexInternalAsync(0, page, rows);
        } else {
            return indexInternal(0, page, rows);
        }
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

        //System.out.println("\n\n\n\n\nfacets: " + facets);

        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);

        AcquisitionQueryResult results = null;
        String ownerUri;
        final SysUser user = AuthApplication.getLocalUser(session());
        if (null == user) {
            ownerUri = "Public";
        }
        else {
            ownerUri = UserManagement.getUriByEmail(user.getEmail());
            if (null == ownerUri){
                ownerUri = "Public";
            }
        }
        //System.out.println("OwnerURI: " + ownerUri);

        results = Measurement.find(ownerUri, page, rows, facets);

        ObjectDetails objDetails = getObjectDetails(results);

        //System.out.println("\n\n\n\nresults to JSON: " + results.toJSON());
        List<ObjectCollection> objectCollections = ObjectCollection.findAllFacetSearch();

        SPARQLUtilsFacetSearch.reportStats();
        // SolrUtilsFacetSearch.reportStats();

        if (mode == 0) {
            return ok(facetOnlyBrowser.render(page, rows, ownerUri, facets, results.getDocumentSize(),
                    results, results.toJSON(), facetHandler, objDetails.toJSON(),
                    Measurement.getFieldNames(), objectCollections));
        } else {
            return ok(dataacquisition_browser.render(page, rows, ownerUri, facets, results.getDocumentSize(),
                    results, results.toJSON(), facetHandler, objDetails.toJSON(),
                    Measurement.getFieldNames(), objectCollections));
        }
    }

    private Result indexInternalAsync(int mode, int page, int rows) {

        String facets = "";
        if (request().body().asFormUrlEncoded() != null) {
            facets = request().body().asFormUrlEncoded().get("facets")[0];
        }

        log.debug("facets: " + facets);

        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);

        AcquisitionQueryResult results = null;
        String ownerUri;

        long startTime = System.currentTimeMillis();
        final SysUser user = AuthApplication.getLocalUser(session());
        log.debug("---> AuthApplication.getLocalUser() takes " + (System.currentTimeMillis() - startTime) + "sms to finish");

        if (null == user) {
            ownerUri = "Public";
        }
        else {
            ownerUri = UserManagement.getUriByEmail(user.getEmail());
            if (null == ownerUri){
                ownerUri = "Public";
            }
        }

        // do the following two concurrently

        startTime = System.currentTimeMillis();

        CompletableFuture<List<ObjectCollection>> promiseOfObjs = CompletableFuture.supplyAsync((
                () -> { return ObjectCollection.findAllFacetSearch(); }
        ), databaseExecutionContext);

        String finalFacets = facets;
        String finalOwnerUri = ownerUri;
        CompletableFuture<AcquisitionQueryResult> promiseOfFacetStats = CompletableFuture.supplyAsync((
                () -> { return Measurement.findAsync(finalOwnerUri, page, rows, finalFacets, databaseExecutionContext); }
        ), databaseExecutionContext);

        List<String> fileNames = Measurement.getFieldNames();
        log.debug("---> Measurement.getFieldNames() takes " + (System.currentTimeMillis() - startTime) + "sms to finish");

        List<ObjectCollection> objs = null;
        try {
            objs = promiseOfObjs.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        try {
            results = promiseOfFacetStats.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        log.debug("---> ObjectCollection.findAllFacetSearch() + Measurement.findAsync() takes " + (System.currentTimeMillis() - startTime) + "sms to finish");

        startTime = System.currentTimeMillis();
        ObjectDetails objDetails = getObjectDetails(results);
        log.debug("---> getObjectDetails() takes " + (System.currentTimeMillis() - startTime) + "sms to finish");

        //System.out.println("\n\n\n\nresults to JSON: " + results.toJSON());

        SPARQLUtilsFacetSearch.reportStats();
        // SolrUtilsFacetSearch.reportStats();

        if (mode == 0) {
            return ok(facetOnlyBrowser.render(page, rows, ownerUri, facets, results.getDocumentSize(), 
                    results, results.toJSON(), facetHandler, objDetails.toJSON(), 
                    fileNames, objs));
        } else {
            return ok(dataacquisition_browser.render(page, rows, ownerUri, facets, results.getDocumentSize(), 
                    results, results.toJSON(), facetHandler, objDetails.toJSON(), 
                    fileNames, objs));
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
        //System.out.println("selectedFields: " + selectedFields);

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
        String objectType = "";
        String categoricalValues = "";
        String timeResolution = "";
        List<String> selectedFields = new LinkedList<String>();
        Map<String, String[]> name_map = request().body().asFormUrlEncoded();
        if (name_map != null) {
            facets = name_map.get("facets")[0];
            objectType = name_map.get("selObjectType")[0].toString();
            categoricalValues = name_map.get("selCatValue")[0].toString();
            timeResolution = name_map.get("selTimeRes")[0].toString();
        }

        long startTime = System.currentTimeMillis();
        AcquisitionQueryResult results = Measurement.findAsync(ownerUri, -1, -1, facets,databaseExecutionContext);
        log.debug("DOWNLOAD: Measurement find takes " + (System.currentTimeMillis()-startTime) + "ms to finish");

        final String finalFacets = facets;
        final String categoricalOption = categoricalValues;
        final String timeOption = timeResolution;
    	//System.out.println("Object type inside alignment: " + objectType);

        CompletionStage<Integer> promiseOfResult = null;
        long currentTime = System.currentTimeMillis();

        if (objectType.equals(Downloader.ALIGNMENT_SUBJECT)) {
        	//System.out.println("Selected subject alignment");
        	promiseOfResult = CompletableFuture.supplyAsync(() -> Downloader.generateCSVFileBySubjectAlignment(
		        results.getDocuments(), finalFacets, email, categoricalOption),
                    databaseExecutionContext);
        } else if (objectType.equals(Downloader.ALIGNMENT_TIME)) {
        	//System.out.println("Selected time alignment");
	        promiseOfResult = CompletableFuture.supplyAsync(() -> Downloader.generateCSVFileByTimeAlignment(
			        results.getDocuments(), finalFacets, email, categoricalOption, timeOption),
                    databaseExecutionContext);
        }

        promiseOfResult.whenComplete(
                (result, exeception) -> {
                    log.debug("DOWNLOAD: downloading DA files is done, taking " + (System.currentTimeMillis()-currentTime) + "ms to finish");
                });

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return redirect(org.hadatac.console.controllers.workingfiles.routes.WorkingFiles.index("/", "/", false));
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

    private static void printMemoryStats() {
        /* Total number of processors or cores available to the JVM */
        System.out.println("Available processors (cores): " +
                Runtime.getRuntime().availableProcessors());

        /* Total amount of free memory available to the JVM */
        System.out.println("Free memory (bytes): " +
                Runtime.getRuntime().freeMemory());

        /* This will return Long.MAX_VALUE if there is no preset limit */
        long maxMemory = Runtime.getRuntime().maxMemory();
        /* Maximum amount of memory the JVM will attempt to use */
        System.out.println("Maximum memory (bytes): " +
                (maxMemory == Long.MAX_VALUE ? "no limit" : maxMemory));

        /* Total memory currently in use by the JVM */
        System.out.println("Total memory (bytes): " +
                Runtime.getRuntime().totalMemory());

    }
}
