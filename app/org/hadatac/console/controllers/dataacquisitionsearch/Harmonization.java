package org.hadatac.console.controllers.dataacquisitionsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.apache.jena.base.Sys;
import org.hadatac.Constants;
import org.hadatac.console.controllers.Application;
import org.hadatac.console.controllers.AuthApplication;
import org.hadatac.console.controllers.triplestore.UserManagement;
import org.hadatac.console.models.SysUser;
import org.hadatac.entity.pojo.*;
import org.pac4j.play.java.Secure;
import play.libs.Files.TemporaryFile;
import play.mvc.*;
import play.data.Form;
import play.data.FormFactory;
import play.mvc.Http.MultipartFormData.FilePart;

import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.hadatac.console.views.html.dataacquisitionsearch.*;
import org.hadatac.metadata.loader.MetadataContext;
import org.hadatac.utils.ConfigProp;
import org.hadatac.utils.Feedback;
import org.hadatac.utils.NameSpace;
import org.hadatac.utils.NameSpaces;

import com.typesafe.config.ConfigFactory;

import be.objectify.deadbolt.java.actions.Group;
import be.objectify.deadbolt.java.actions.Restrict;


public class Harmonization extends Controller {

    @Inject
    private FormFactory formFactory;
    @Inject
    Application application;

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result index(String oper, Http.Request request) {

        List<Study> studyList = Study.find();

        if (oper.equals("study")) {
            List<Study> queryList = new ArrayList<Study>();
            return ok(harmonizationStudy.render(studyList, queryList, application.getUserEmail(request)));
        }

        List<ObjectCollection> queryList = new ArrayList<ObjectCollection>();
        List<ObjectCollection> socList = ObjectCollection.findAll();
        return ok(harmonizationSoc.render(studyList, socList, queryList, application.getUserEmail(request)));
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndex(String oper, Http.Request request) {
        return index(oper, request);
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result indexSoc(Http.Request request) {

        String ownerUri = getOwnerUri(request);
        String email = getUserEmail(request);
        List<ObjectCollection> socs = new ArrayList<ObjectCollection>();
        Map<String, List<Variable>> vars = new HashMap<String, List<Variable>>();
        Map<String, HarmonizedVariable> harmonization = new HashMap<String, HarmonizedVariable>();

        try {
            Form form = formFactory.form().bindFromRequest(request);
            Map<String, String> data = form.rawData();
            System.out.println("inside indexSoc: form.size=[" + data.size() + "]");

            /*
            for (Map.Entry<String, String> entry : data.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
             */


            int i = -1;
            String socUri = "";
            do {
                i = i + 1;
                socUri = data.get("soc" + String.valueOf(i));
                System.out.println("  - Arg value: [" + socUri + "]");

                if (socUri != null) {
                    ObjectCollection soc = ObjectCollection.find(socUri);
                    if (soc != null) {
                        socs.add(soc);
                    }
                }
            } while (socUri != null);

            System.out.println("inside indexSoc: socs.size=[" + socs.size() + "]");
            for (ObjectCollection soc : socs) {
                System.out.println("  - Selected SOC: [" + soc.getUri() + "]");
            }
            if (socs.size() < 2) {
                return ok(harmonizationSocResult.render(new ArrayList<ObjectCollection>(), new ArrayList<HarmonizedVariable>(), application.getUserEmail(request)));
            }
            String summaryType = Measurement.SUMMARY_TYPE_NONE;
            String categoricalOption = Measurement.NON_CATG_IGNORE;
            boolean keepSameValue = false;
            String fileId = "";
            String facets;

            for (ObjectCollection soc : socs) {
                facets = "{\"facetsEC\":[],\"facetsS\":[{\"study_uri_str\":\"" + soc.getStudyUri() + "\"}],\"facetsOC\":[{\"role_str\":\"" + soc.getRoleLabel() + "\"}],\"facetsU\":[],\"facetsT\":[],\"facetsPI\":[]}";
                System.out.println("harmonizationSoc: facets=[" + facets + "]");

                // Initiate Alignment
                Alignment alignment = new Alignment();
                ColumnMapping columnMapping = new ColumnMapping();
                Map<String, List<String>> alignCache = new HashMap<String, List<String>>();
                Map<String, List<String>> studyMap = new HashMap<>();

                // read the page size from config
                String sPageSize = ConfigFactory.load().getString("hadatac.download.pageSize");
                int pageSize = 32000;
                try {
                    pageSize = Integer.parseInt(sPageSize);
                } catch (Exception e) {
                }

                // read backend Solr page by page and merge the results
                System.out.println("start the pagination process...pageSize = " + pageSize);
                Map<String, Map<String, List<AnnotatedValue>>> results = Measurement.readSolrPagesAndMerge(ownerUri, facets, fileId, pageSize, studyMap, alignment, alignCache, categoricalOption, keepSameValue, columnMapping);

                Map<String, AnnotatedGroupSummary> groupSummaryMap = new HashMap<String, AnnotatedGroupSummary>();

                // write the results to hard drive
                System.out.print("start to write the dataset to hard drive...");
                DataFile dataFile = null;

                try {
                    List<AnnotatedValue> values = null;

                    // Write headers: Labels are derived from collected alignment attributes
                    vars.put(soc.getUri(), alignment.getVariables());
                    Map<String, Variable> varMap = new HashMap<String, Variable>();
                    vars.get(soc.getUri()).sort(new Comparator<Variable>() {
                        @Override
                        public int compare(Variable o1, Variable o2) {
                            return o1.toString().compareTo(o2.toString());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            for (int ind1 = 0; ind1 < socs.size(); ind1++) {
                for (int ind2 = ind1 + 1; ind2 < socs.size(); ind2++) {
                    System.out.println("Processing SOCS [" + socs.get(ind1).getUri() + "] and [" + socs.get(ind2).getUri() + "]");
                    for (Variable var1 : vars.get(socs.get(ind1).getUri())) {
                        for (Variable var2 : vars.get(socs.get(ind2).getUri())) {
                            System.out.print("Var1: " + var1 + "  Var2: " + var2 + "    ");
                            if (HarmonizedVariable.harmonizable(var1,var2)) {
                                List<Variable> sourceVars = new ArrayList<Variable>();
                                sourceVars.add(var1);
                                sourceVars.add(var2);
                                String concatKeys = HarmonizedVariable.concatenatedKeys(sourceVars);
                                System.out.println("Concat Roles: [" + concatKeys + "]");
                                if (!harmonization.containsKey(concatKeys)) {
                                    harmonization.put(concatKeys, new HarmonizedVariable(sourceVars));
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<HarmonizedVariable> harmonizationList = new ArrayList<HarmonizedVariable>();
        for (HarmonizedVariable var : harmonization.values()) {
            harmonizationList.add(var);
        }
        harmonizationList.sort(new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return ok(harmonizationSocResult.render(socs, harmonizationList, application.getUserEmail(request)));
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndexSoc(Http.Request request) {
        return indexSoc(request);
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result indexStudy(Http.Request request) {

        String ownerUri = getOwnerUri(request);
        String email = getUserEmail(request);
        List<Study> studies = new ArrayList<Study>();
        Map<String, List<Variable>> vars = new HashMap<String, List<Variable>>();
        Map<String, HarmonizedVariable> harmonization = new HashMap<String, HarmonizedVariable>();

        try {
            Form form = formFactory.form().bindFromRequest(request);
            Map<String, String> data = form.rawData();
            System.out.println("inside indexStudy: form.size=[" + data.size() + "]");

            /*
            for (Map.Entry<String, String> entry : data.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
             */


            int i = -1;
            String studyUri = "";
            do {
                i = i + 1;
                studyUri = data.get("study" + String.valueOf(i));
                System.out.println("  - Arg value: [" + studyUri + "]");

                if (studyUri != null) {
                    Study study = Study.find(studyUri);
                    if (study != null) {
                        studies.add(study);
                    }
                }
            } while (studyUri != null);

            System.out.println("inside indexStudy: study.size=[" + studies.size() + "]");
            for (Study study : studies) {
                System.out.println("  - Selected Study: [" + study.getUri() + "]");
            }
            if (studies.size() < 2) {
                // send results back with an empty soc list
                return ok(harmonizationStudyResult.render(new ArrayList<Study>(), new ArrayList<HarmonizedVariable>(), new ArrayList<Variable>(), application.getUserEmail(request)));
            }
            String summaryType = Measurement.SUMMARY_TYPE_NONE;
            String categoricalOption = Measurement.NON_CATG_IGNORE;
            boolean keepSameValue = false;
            String fileId = "";
            String facets;

            for (Study study : studies) {
                facets = "{\"facetsEC\":[],\"facetsS\":[{\"study_uri_str\":\"" + study.getUri() + "\"}],\"facetsOC\":[],\"facetsU\":[],\"facetsT\":[],\"facetsPI\":[]}";
                System.out.println("harmonizationSoc: facets=[" + facets + "]");

                // Initiate Alignment
                Alignment alignment = new Alignment();
                ColumnMapping columnMapping = new ColumnMapping();
                Map<String, List<String>> alignCache = new HashMap<String, List<String>>();
                Map<String, List<String>> studyMap = new HashMap<>();

                // read the page size from config
                String sPageSize = ConfigFactory.load().getString("hadatac.download.pageSize");
                int pageSize = 32000;
                try {
                    pageSize = Integer.parseInt(sPageSize);
                } catch (Exception e) {
                }

                // read backend Solr page by page and merge the results
                System.out.println("start the pagination process...pageSize = " + pageSize);
                Map<String, Map<String, List<AnnotatedValue>>> results = Measurement.readSolrPagesAndMerge(ownerUri, facets, fileId, pageSize, studyMap, alignment, alignCache, categoricalOption, keepSameValue, columnMapping);

                Map<String, AnnotatedGroupSummary> groupSummaryMap = new HashMap<String, AnnotatedGroupSummary>();

                // write the results to hard drive
                System.out.print("start to write the dataset to hard drive...");
                DataFile dataFile = null;

                try {
                    List<AnnotatedValue> values = null;

                    // Write headers: Labels are derived from collected alignment attributes
                    vars.put(study.getUri(), alignment.getVariables());
                    Map<String, Variable> varMap = new HashMap<String, Variable>();
                    vars.get(study.getUri()).sort(new Comparator<Variable>() {
                        @Override
                        public int compare(Variable o1, Variable o2) {
                            return o1.toString().compareTo(o2.toString());
                        }
                    });

                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            for (int ind1 = 0; ind1 < studies.size(); ind1++) {
                for (int ind2 = ind1 + 1; ind2 < studies.size(); ind2++) {
                    //System.out.println("Processing SOCS [" + studies.get(ind1).getUri() + "] and [" + studies.get(ind2).getUri() + "]");
                    for (Variable var1 : vars.get(studies.get(ind1).getUri())) {
                        for (Variable var2 : vars.get(studies.get(ind2).getUri())) {
                            //System.out.print("Var1: " + var1 + "  Var2: " + var2 + "    ");
                            if (HarmonizedVariable.harmonizable(var1,var2)) {
                                List<Variable> sourceVars = new ArrayList<Variable>();
                                sourceVars.add(var1);
                                sourceVars.add(var2);
                                String concatKeys = HarmonizedVariable.concatenatedKeys(sourceVars);
                                System.out.println("ConcatKey: [" + concatKeys + "]");
                                if (!harmonization.containsKey(concatKeys)) {
                                    harmonization.put(concatKeys, new HarmonizedVariable(sourceVars));
                                }
                            }
                        }
                    }
                }
            }

            //System.out.println("Studies size: " + studies.size());
            //System.out.println("Harmonization size: " + harmonization.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        List<HarmonizedVariable> harmonizationList = new ArrayList<HarmonizedVariable>();
        List<Variable> sourceList = new ArrayList<Variable>();
        for (HarmonizedVariable var : harmonization.values()) {
            harmonizationList.add(var);
            sourceList.addAll(var.getSourceList());
        }
        harmonizationList.sort(new Comparator<Variable>() {
            @Override
            public int compare(Variable o1, Variable o2) {
                return o1.toString().compareTo(o2.toString());
            }
        });
        return ok(harmonizationStudyResult.render(studies, harmonizationList, sourceList, application.getUserEmail(request)));
    }

    // @Secure(authorizers = Constants.DATA_MANAGER_ROLE)
    public Result postIndexStudy(Http.Request request) {
        return indexStudy(request);
    }

    public static String playLoadOntologies(String oper) {
        NameSpaces.getInstance();
        MetadataContext metadata = new
                MetadataContext("user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.triplestore"),
                false);
        return metadata.loadOntologies(Feedback.WEB, oper);
    }

    private String getUserEmail(Http.Request request) {
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
        if (null != user) {
            return user.getEmail();
        }

        return "";
    }

    private String getOwnerUri(Http.Request request) {
        String ownerUri = "";
        final SysUser user = AuthApplication.getLocalUser(application.getUserEmail(request));
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

}
