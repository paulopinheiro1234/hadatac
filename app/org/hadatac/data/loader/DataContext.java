package org.hadatac.data.loader;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.utils.CollectionUtil.Collection;
import org.hadatac.utils.Command;
import org.hadatac.utils.Feedback;

import com.typesafe.config.ConfigFactory;

public class DataContext {

    String username;
    String password;
    String kbURL;
    boolean verbose;

    public DataContext(String un, String pwd, String kb, boolean ver) {
        username = un;
        password = pwd;
        kbURL = kb;
        verbose = ver;
    }

    public static Long playTotalMeasurements() {
        DataContext data = new DataContext( "user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.data"),
                false);

        return data.totalMeasurements();
    }

    public static Long playTotalDataAcquisitions() {
        DataContext data = new DataContext( "user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.data"),
                false);

        return data.totalDataAcquisitions();
    }

    public static Long playTotalDataFiles() {
        DataContext data = new DataContext( "user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.data"),
                false);

        return data.totalDataFiles();
    }

    public static Long playTotalNameSpaces() {
        DataContext data = new DataContext( "user",
                "password",
                ConfigFactory.load().getString("hadatac.solr.data"),
                false);

        return data.totalNameSpaces();
    }

    private Long totalDocuments(String solrCoreName) {
        SolrClient solr = new HttpSolrClient.Builder(kbURL + solrCoreName).build();
        SolrQuery parameters = new SolrQuery();
        parameters.set("q", "*:*");
        parameters.set("rows", 0);

        try {
            QueryResponse response = solr.query(parameters);
            solr.close();
            return response.getResults().getNumFound();
        } catch (SolrServerException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return (long) -1;
    }

    public Long totalMeasurements() {
        return totalDocuments(CollectionUtil.getCollectionName(Collection.DATA_ACQUISITION.get()));
    }

    public Long totalUsers() {
        return totalDocuments(CollectionUtil.getCollectionName(Collection.AUTHENTICATE_USERS.get()));
    }

    public Long totalDataAcquisitions() {
        return totalDocuments(CollectionUtil.getCollectionName(Collection.DATA_COLLECTION.get()));
    }

    public Long totalDataFiles() {
        return totalDocuments(CollectionUtil.getCollectionName(Collection.CSV_DATASET.get()));
    }

    public Long totalNameSpaces() {
        return totalDocuments(CollectionUtil.getCollectionName(Collection.NAMESPACE.get()));
    }

    private String cleanAllDocuments(int mode, Collection solrCoreName) {
        System.out.println("inside cleanAllDocuments: " + solrCoreName.toString());
        String message = "";
        String straux = "";

        message += Feedback.println(mode, "   Documents before [clean]: " + totalDocuments(CollectionUtil.getCollectionName(solrCoreName.get())));
        message += Feedback.println(mode, " ");

        String query1 = "<delete><query>*:*</query></delete>";
        String query2 = "<commit/>";

        String url1;
        String url2;
        try {
            url1 = CollectionUtil.getCollectionPath(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
            url2 = CollectionUtil.getCollectionPath(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");

            System.out.println("url1: " + url1);
            System.out.println("url2: " + url2);

            if (verbose) {
                message += Feedback.println(mode, url1);
                message += Feedback.println(mode, url2);
            }
            String[] cmd1 = {"curl", "-v", url1};
            message += Feedback.print(mode, "    Erasing documents... ");
            straux = Command.exec(mode, verbose, cmd1);
            if (mode == Feedback.WEB) {
                message += straux;
            }
            message += Feedback.println(mode, "");
            message += Feedback.print(mode, "   Committing... ");
            String[] cmd2 = {"curl", "-v", url2};
            straux = Command.exec(mode, verbose, cmd2);
            if (mode == Feedback.WEB) {
                message += straux;
            }
            message += Feedback.println(mode," ");
            message += Feedback.println(mode," ");
            message += Feedback.print(mode, "   Triples after [clean]: " + totalDocuments(CollectionUtil.getCollectionName(solrCoreName.get())));
        } catch (UnsupportedEncodingException e) {
            System.out.println("[DataManagement] - ERROR encoding URLs");
            return message;
        }

        return message;
    }

    private String cleanSpecifiedStudy(int mode, String studyURI) {
        Collection solrCoreName = Collection.STUDIES;
        String message = "";
        String straux = "";

        message += Feedback.println(mode,"   Documents before [clean]: " + totalDocuments(CollectionUtil.getCollectionName(solrCoreName.get())));
        message += Feedback.println(mode, " ");

        String query1 = "<delete><query>studyUri:\"" + studyURI +"\"</query></delete>";
        String query2 = "<commit/>";

        String url1;
        String url2;

        try {
            url1 = CollectionUtil.getCollectionPath(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query1, "UTF-8");
            url2 = CollectionUtil.getCollectionPath(solrCoreName) + "/update?stream.body=" + URLEncoder.encode(query2, "UTF-8");

            if (verbose) {
                message += Feedback.println(mode, url1);
                message += Feedback.println(mode, url2);
            }
            String[] cmd1 = {"curl", "-v", url1};
            message += Feedback.print(mode, "    Erasing documents... ");
            straux = Command.exec(mode, verbose, cmd1);
            if (mode == Feedback.WEB) {
                message += straux;
            }
            message += Feedback.println(mode, "");
            message += Feedback.print(mode, "   Committing... ");
            String[] cmd2 = {"curl", "-v", url2};
            straux = Command.exec(mode, verbose, cmd2);
            if (mode == Feedback.WEB) {
                message += straux;
            }
            message += Feedback.println(mode," ");
            message += Feedback.println(mode," ");
            message += Feedback.print(mode,"   Triples after [clean]: " + totalDocuments(CollectionUtil.getCollectionName(solrCoreName.get())));
        } catch (UnsupportedEncodingException e) {
            System.out.println("[DataManagement] - ERROR encoding URLs");
            return message;
        }

        return message;
    }

    public String cleanDataAcquisitions(int mode) {
        return cleanAllDocuments(mode, Collection.DATA_COLLECTION);
    }

    public String cleanDataUsers(int mode) {
        return cleanAllDocuments(mode, Collection.AUTHENTICATE_USERS);
    }

    public String cleanDataAccounts(int mode) {
        return cleanAllDocuments(mode, Collection.AUTHENTICATE_ACCOUNTS);
    }

    public String cleanAcquisitionData(int mode) {
        return cleanAllDocuments(mode, Collection.DATA_ACQUISITION);
    }

    public String cleanDataFiles(int mode) {
        return cleanAllDocuments(mode, Collection.CSV_DATASET);
    }

    public String cleanNameSpaces(int mode) {
        return cleanAllDocuments(mode, Collection.NAMESPACE);
    }

    public String cleanStudy(int mode, String studyURI) {
        return cleanSpecifiedStudy(mode, studyURI);
    }
}
