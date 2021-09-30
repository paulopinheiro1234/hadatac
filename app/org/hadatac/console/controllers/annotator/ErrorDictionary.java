package org.hadatac.console.controllers.annotator;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;


public class ErrorDictionary {

    public class Error {

        private String id;
        private String detail;
        private String solution;

        public Error(String id, String detail, String solution) {
            this.id = id;
            this.detail = detail;
            this.solution = solution;
        }

        public String getId() {
            return id;
        }

        public String getDetail() {
            return detail;
        }

        public String getSolution() {
            return solution;
        }

        public void setId(String id) {
            this.id = id;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public void setSolution(String solution) {
            this.solution = solution;
        }
    }

    private Map<String, Error> table = new HashMap<String, Error>();

    private static ErrorDictionary instance = null;

    public static ErrorDictionary getInstance() {
        if(instance == null) {
            instance = new ErrorDictionary();
        }

        return instance;
    }

    public Map<String, Error> getTable() {
        return table;
    }

    private ErrorDictionary() {
        System.out.println("Loading ErrorDictionary...");
        InputStream inputStream = getClass().getClassLoader()
                .getResourceAsStream("error_dictionary.json");

        JSONParser parser = new JSONParser();
        try {
            JSONObject root = (JSONObject)parser.parse(new InputStreamReader(inputStream));
            JSONObject dict = (JSONObject)root.get("content");

            for (Object fileType : dict.keySet()) {
                JSONArray errors = (JSONArray) ((JSONObject)dict.get((String)fileType)).get("content");
                for (Object obj : errors) {
                    JSONObject jsonObj = (JSONObject) obj;
                    String id = (String)jsonObj.get("id");
                    String detail = (String)jsonObj.get("detail");
                    String solution = (String)jsonObj.get("solution");

                    //System.out.println("ErrorDictionary: constructor() id=[" + id + "]");
                    Error error = new Error(id, detail, solution);
                    table.put(id, error);
                }
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public static String getDetailById(String id) {

        //System.out.println("ErrorDictionary: getDetailById() id=[" + id + "]");
        Map<String, Error> table = getInstance().getTable();
        if (!table.containsKey(id)) {
            return "";
        }
        //System.out.println("ErrorDictionary: detail=[" + table.get(id).getDetail() + "]");
        return table.get(id).getDetail();
    }

    public static String getSolutionById(String id) {
        Map<String, Error> table = getInstance().getTable();
        if (!table.containsKey(id)) {
            return "";
        }

        return table.get(id).getSolution();
    }
}
