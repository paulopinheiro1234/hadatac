package org.hadatac.console.http;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.hadatac.Constants;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.utils.CollectionUtil;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;
import org.pac4j.play.java.Secure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import play.data.DynamicForm;
import play.data.FormFactory;

import javax.inject.Inject;

public class SolrSearchProxy extends Controller {

    private static final Logger log = LoggerFactory.getLogger(SolrSearchProxy.class);

    @Inject
    private FormFactory formFactory;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getSolrSearch(String path) {
        InputStream is = null;
        URL url = null;
        HttpURLConnection con = null;

        try {
            url = new URL(path.substring(0, path.indexOf('?')));
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Accept-Charset", "utf-8");
            con.setDoOutput(true);
            DataOutputStream wr = new DataOutputStream(con.getOutputStream());
            wr.writeBytes(path.substring(path.indexOf('?')+1, path.length()));
            wr.flush();
            wr.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try (OutputStream output = con.getOutputStream()) {
            output.write(path.getBytes("utf-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            is = con.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (is != null) {
            return ok(is);
        } else {
            return ok();
        }
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getIndicatorValueDownload(String lm) {
        File file = new File(lm);
        return ok(file);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getDataAcquisitionDownload(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String request_fl;
        String request_wt;
        String request_rows;
        String request_q;
        String request_encoding = "";

        if (form.rawData().size() == 0) {
            return badRequest("[ERROR] getDataAcuisitionDownload expects some data");
        } else {
            request_fl = form.get("fl");
            request_wt = form.get("wt");
            request_rows = form.get("rows");
            request_q = form.get("q");
            request_encoding = "wt=" + request_wt + "&rows=" + request_rows + "&q=" + request_q;
            if (!request_fl.equals("")) {
                request_encoding += "&fl=" + request_fl;
            }
            System.out.println("Request: " + request_encoding);
        }
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)
                + "/select" + "?" + request_encoding;

        return getSolrSearch(path).as("text/csv");
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getStudyAcquisitionDownload(Http.Request request) {
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.STUDY_ACQUISITION)
                + "/select" + request.toString().split((request.path()))[1];

        return getSolrSearch(path).as("text/csv");
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getStudyAcquisition(Http.Request request) {
        // get the request parameter from ajax call. for this case, we have only one parameter
        String queryStr = null;
        Map<String, String[]> params = request.body().asFormUrlEncoded();
        log.info("Params : " + params.size());
        for (Map.Entry<String, String[]> param : params.entrySet()) {
            log.info(param.getKey() + " = " + param.getValue()[0]);
            queryStr = param.getValue()[0];
            break;
        }

        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.STUDY_ACQUISITION) + "/select?" + queryStr;
        log.info("query string to Solr: " + path);

        return getSolrSearch(path);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getAnalytesAcquisition(Http.Request request) {
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.ANALYTES_ACQUISITION)
                + "/select" + request.toString().split((request.path()))[1];
        return getSolrSearch(path);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getMetadataDataAcquisition(Http.Request request) {
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_COLLECTION)
                + "/select" + request.toString().split((request.path()))[1];
        return getSolrSearch(path);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getDataAcquisition(Http.Request request) {
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.METADATA_AQUISITION)
                + "/select" + request.toString().split((request.path()))[1];
        return getSolrSearch(path);
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result getSchemaAttributes(Http.Request request) {
        String path = CollectionUtil.getCollectionPath(CollectionUtil.Collection.SA_ACQUISITION)
                + "/select" + request.toString().split((request.path()))[1];
        return getSolrSearch(path);
    }

    public Result getApiStudyDownload(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String request_token;

        if (form.rawData().size() == 0) {
            return badRequest("[ERROR] getApiStudyDownload expects some data");
        } else {
            request_token = form.get("token");
            if (!request_token.equals("TESTTOKEN")) {
                return badRequest("[ERROR] getApiStudyDownload token mismatch");
            }
            return getStudyAcquisitionDownload(request);
        }
    }

    public Result getApiStudyVariableDownload(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String request_token;

        if (form.rawData().size() == 0) {
            return badRequest("[ERROR] getApiStudyVariableDownload expects some data");
        } else {
            request_token = form.get("token");
            if (!request_token.equals("TESTTOKEN")) {
                return badRequest("[ERROR] getApiStudyVariableDownload token mismatch");
            }
            return getStudyAcquisitionDownload(request);
        }
    }

    public Result getApiStudyVariableDataDownload(Http.Request request) {
        DynamicForm form = formFactory.form().bindFromRequest(request);
        String request_token;

        if (form.rawData().size() == 0) {
            return badRequest("[ERROR] getApiStudyVariableDataDownload expects some data");
        } else {
            request_token = form.get("token");
            if (!request_token.equals("TESTTOKEN")) {
                return badRequest("[ERROR] getApiStudyVariableDataDownload token mismatch");
            }
            return getStudyAcquisitionDownload(request);
        }
    }
}
