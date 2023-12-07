package org.hadatac.console.controllers.studies;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.pac4j.play.java.Secure;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;

import org.hadatac.console.views.html.studies.*;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.entity.pojo.Study;
import org.hadatac.entity.pojo.Agent;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.sparql.engine.http.QueryExceptionHTTP;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.objectcollections.OCForceFieldGraph;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.http.SPARQLUtils;
import org.hadatac.console.models.SysUser;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.NameSpaces;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;

import javax.inject.Inject;

public class StudyView extends Controller {

    private static final Logger log = LoggerFactory.getLogger(StudyView.class);

    @Inject
    Application application;
    public static int PAGESIZE = 7;

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result index(String study_uri, String oc_uri, int page, Http.Request request) {

        if (oc_uri != null && oc_uri.indexOf("STD-") > -1) {
            oc_uri = "";
        }
        //System.out.println("Study URI: [" + study_uri + "]");
        //System.out.println("SOC URI: [" + oc_uri + "]");

        try {
            study_uri = URLDecoder.decode(study_uri, "utf-8");
            oc_uri = URLDecoder.decode(oc_uri, "utf-8");
        } catch (UnsupportedEncodingException e) {
            study_uri = "";
            oc_uri = "";
        }

        System.out.println("StudyUri: " + study_uri);

        OCForceFieldGraph graph = new OCForceFieldGraph(OCForceFieldGraph.NO_TIME_SPACE, study_uri);

        if (study_uri == null || study_uri.equals("")) {
            return badRequest("ViewStudy: [ERROR] empty study URI");
        }
        Study study = Study.find(study_uri);
        if (study == null) {
            return badRequest("ViewStudy: [ERROR] Could not find any study with following URI: [" + study_uri + "]");
        }
        Agent agent = study.getAgent();
        Agent institution = study.getInstitution();

        //using config
        String fullImgPrefix = ConfigFactory.load().getString("hadatac.shiny_dashboards.fullImgPrefix");
        StringBuilder shinyAppUrl = new StringBuilder();
        shinyAppUrl.append(fullImgPrefix);

        //to add the Study
        shinyAppUrl.append(study.getId());

        System.out.println("shinyAppUrl: [" + shinyAppUrl.toString() + "]");

        Map<String, String[]> params = getRequestParameters( request);

        System.out.println("params: "+ params);
        params.forEach((key,value) -> System.out.println(key+" :"+value[0]));

        // this is the value for source when not using HHEAR Portal
        String source = "studypage";
        String studyIds = "";

        // check if entry point is from Portal
        if(params.containsKey("source")) {
            System.out.println("params.get(source): "+ params.get("source"));
            source = params.get("source")[0];
            System.out.println("params.get(source)[0]): "+ source);
        }
        if(params.containsKey("studyIds")) {
            System.out.println("params.get(studyIds): "+ params.get("studyIds"));
            studyIds = params.get("studyIds")[0];
            System.out.println("params.get(studyIds)[0]): "+ studyIds);
        }

        ObjectCollection oc = null;
        if (oc_uri != null && !oc_uri.equals("")) {
            oc = ObjectCollection.find(oc_uri);
        }

        List<StudyObject> objects = null;
        int total = 0;
        if (oc != null) {
            objects = StudyObject.findByCollectionWithPages(oc, PAGESIZE, page * PAGESIZE);
            total = StudyObject.getNumberStudyObjectsByCollection(oc_uri);
        }

        String facets = null;

        if(!StringUtils.isBlank(study_uri) && study!=null) {
            final String studyPrefix = "http://hadatac.org/kb/hhear#STD-";
            System.out.println("Study URI: [" + study.getUri() + "]");
            String studyId = study.getUri().replace(studyPrefix, "");
            System.out.println("Study Id: [" + studyId + "]");
            String[] studyIdsArr = {studyId};
            facets = getStudyFacetS(studyIdsArr);
        }

        if (params.get("source") != null) {
            // check if initiated from HHEAR Portal
            if (source.equals("generateDataSet")) {
                if (!StringUtils.isBlank(studyIds)) {
                    if (studyIds.contains("!")) {
                        String[] studyIdsArr = studyIds.split("!");
                        facets = getStudyFacetS(studyIdsArr);
                    } else {
                        String[] studyIdsArr = {studyIds};
                        facets = getStudyFacetS(studyIdsArr);
                    }
                }
            }
        }

        return ok(studyView.render(graph.getTreeQueryResult().replace("\n", " "),
                study, agent, institution, oc, objects, page, total,application.getUserEmail(request),
                source, facets, shinyAppUrl.toString()));
    }

    @Secure(authorizers = Constants.DATA_OWNER_ROLE)
    public Result postIndex(String study_uri, String oc_uri, int page, Http.Request request) {
        return index(study_uri, oc_uri, page, request);
    }

    public Map<String, String[]> getRequestParameters(Http.Request request) {
        final Map<String, String[]> parameters = new HashMap<>();
        final Map<String, String[]> urlParameters = request.queryString();
        if (urlParameters != null) {
            parameters.putAll(urlParameters);
        }
        return parameters;
    }

    private String getStudyFacetS(String[] studyIds) {
        ObjectMapper objectMapper = new ObjectMapper();
        //final String studyPrefix = "http://hadatac.org/kb/hhear#STD-";

        Map<String, List<String>> mapStudyDas = createStudyFacetDAFiles(studyIds);
        String studyFacetQuery = "";

        List<FacetS> facetSet = new ArrayList<>();

        //List<String> studIdList = Arrays.asList(studyIds);
        //System.out.println("StudyView.getStudyFacetS : studIdList=[" + studIdList + "]");
        //System.out.println("StudyView.getStudyFacetS : studIdList size =[" + studIdList.size() + "]");

        /*mapStudyDas.forEach((k,v)-> {
            System.out.println("StudyView.getStudyFacetS [study,  das]: study=" +k + ", das=: "+ v);
        });*/

        mapStudyDas.forEach((study, das) -> {
            //System.out.println("StudyView.getStudyFacetS : study=[" + study + "]");
            //System.out.println("StudyView.getStudyFacetS : das=[" + das + "]");

            //String studyNumber = study.replace(studyPrefix, "");
            //studyNumber = studyNumber.replace("STD-", "");
            //System.out.println("StudyView.getStudyFacetS : studyNumber=[" + studyNumber + "]");

            String id = study;
            String studyUriStr = study;
            FacetS facetS = new FacetS(id, studyUriStr);

            List<DAChild> lst = new ArrayList<>();
            das.forEach(da -> {
                DAChild c = new DAChild(da, da);
                lst.add(c);
            });
            facetS.setChildren(lst);
            //System.out.println("StudyView.getStudyFacetS : facetS=[" + facetS + "]");

            facetSet.add(facetS);
            //System.out.println("StudyView.getStudyFacetS : facetSet=[" + facetSet + "]");
        });

        try {
            String facetSJson = objectMapper.writeValueAsString(facetSet);
            //System.out.println("StudyView.getStudyFacetS : facetSJson=[" + facetSJson + "]");

            StringBuilder facets = new StringBuilder();
            facets.append("{");
            facets.append("\"facetsS\":");
            facets.append(facetSJson);
            facets.append(",");
            facets.append("\"facetsEC\":[],");
            facets.append("\"facetsOC\":[],");
            facets.append("\"facetsU\":[],");
            facets.append("\"facetsT\":[],");
            facets.append("\"facetsPI\":[]");
            facets.append("}");

            System.out.println("\n-----Build FACET----------\n" + facets.toString());

            studyFacetQuery = facets.toString();
        } catch (Exception e) {
            log.error("ERROR in StudyView.getStudyFacetQuery", e);
        }

        return studyFacetQuery;
    }

    private Map<String, List<String>> createStudyFacetDAFiles(String[] studyIds) {
        final String studyPrefix = "http://hadatac.org/kb/hhear#STD-";
        Map<String, List<String>> mapStudyDas = new HashMap<>();
        Study study = new Study();
        Facet facet = new Facet();

        Arrays.stream(studyIds).forEach(studyId ->
        {
            //System.out.println("StudyView.createStudyFacetDAFiles [study id]: " + studyId);
            String id = studyPrefix + studyId;
            facet.putFacet("study_uri_str", id);
        });

        /*Map<String, List<String>> mapFieldValues = facet.getFieldValues();
        mapFieldValues.forEach((k,v)-> {
            System.out.println("StudyView.createStudyFacetDAFiles [study ids]: " + v);
        });*/

        FacetHandler facetHandler = new FacetHandler();
        Map<Facetable, List<Facetable>> results = study.getTargetFacetsFromTripleStore(facet, facetHandler);

        /*results.forEach((k,v)-> {
            System.out.println("StudyView.createStudyFacetDAFiles [getTargetFacetsFromTripleStore]: " + v.get(0).getUri());
        });*/

        results.forEach((stdy,daList) ->
        {
            List<String> lst = daList.stream()
                    .map((obj) -> obj.getUri())
                    .collect(Collectors.toList());

            //System.out.println("StudyView.createStudyFacetDAFiles [getTargetFacetsFromTripleStore]: " + lst);
            //lst.forEach(n -> System.out.println(n));
            mapStudyDas.put(stdy.getUri(), lst);
        });

        return mapStudyDas;
    }


    private class FacetS {
        private String id;
        private String study_uri_str;
        private List<DAChild> children;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getStudy_uri_str() {
            return study_uri_str;
        }

        public void setStudy_uri_str(String study_uri_str) {
            this.study_uri_str = study_uri_str;
        }

        public List<DAChild> getChildren() {
            return children;
        }

        public FacetS(String id, String study_uri_str)
        {
            this.id = id;
            this.study_uri_str = study_uri_str;
        }

        public void setChildren(List<DAChild> children) {
            this.children = children;
        }
    }

    private class DAChild {
        private String id;
        private String acquisition_uri_str;

        public DAChild(String id, String acquisition_uri_str)
        {
            this.id = id;
            this.acquisition_uri_str = acquisition_uri_str;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getAcquisition_uri_str() {
            return acquisition_uri_str;
        }

        public void setAcquisition_uri_str(String acquisition_uri_str) {
            this.acquisition_uri_str = acquisition_uri_str;
        }
    }
}
