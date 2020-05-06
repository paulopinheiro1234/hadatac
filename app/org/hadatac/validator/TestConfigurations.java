package org.hadatac.validator;

import org.apache.commons.validator.routines.UrlValidator;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;

import com.typesafe.config.ConfigFactory;

import java.io.IOException;

public class TestConfigurations{
    private static HttpClient client = HttpClientBuilder.create().build();
    private static UrlValidator urlvalid = new UrlValidator();
    
    private static boolean testConnection(String toTest){
        if(toTest == null){
            System.out.println("::Connection test failed: host undefined");
            return false;
        }
        if(!urlvalid.isValid(toTest)){
            System.out.println("::Connection test failed: not a valid URL");
            return false;
        }
        HttpGet testGet = new HttpGet(toTest);
        HttpResponse res;
        try{
            res = client.execute(testGet);
        } catch (IOException e){
            System.out.println("::Connection test failed: " + e.getStackTrace());
            return false;            
        }
        
        int code = res.getStatusLine().getStatusCode();
        if(code == 200){
            System.out.println("::Connection to " + toTest + " successful.");
            return true;
        }
        else{
            String reason = res.getStatusLine().getReasonPhrase();
            System.out.println("::Connection test failed: " + code + " " + reason);
            return false;
        }
    }// /testConnection
    
    public static void testSolr(){
        // test Solr:
        System.out.println("Testing for Solr....");
        String data, triplestore, users;
        data = ConfigFactory.load().getString("hadatac.solr.data");
        if(!testConnection(data)){
            System.out.println("Testing failed: Solr instance for data is invalid or unreachable.\n");
            System.out.println("Check your hadatac.conf settings for the 'data' parameter.\n");
            System.exit(-1);
        }
        triplestore = ConfigFactory.load().getString("hadatac.solr.triplestore");
        if(!testConnection(triplestore)){
            System.out.println("Testing failed: SolRDF instance for metadata is invalid or unreachable.\n");
            System.out.println("Check your hadatac.conf settings for the 'triplestore' parameter.\n");
            System.exit(-1);
        }
        users = ConfigFactory.load().getString("hadatac.solr.users");
        // Can the users server be the same as the triple store?
        if(!users.equals(data)){
            if(!testConnection(users)){
                System.out.println("Testing failed: Solr instance for users is invalid or unreachable.\n");
                System.out.println("Check your hadatac.conf settings for the 'users' parameter.\n");
                System.exit(-1);
            }
        }
    }// /testSolr()
}