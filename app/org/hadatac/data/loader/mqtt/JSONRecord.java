package org.hadatac.data.loader.mqtt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.hadatac.data.loader.Record;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONRecord implements Record {

	public List<String> headers = null;
	public List<String> values = null;
	
	public JSONRecord(String json) {
	    JSONParser parser = new JSONParser();
	    JSONObject obj = new JSONObject();
		try {
			obj = (JSONObject) parser.parse(json);
			headers = convertSetToList(obj.keySet());
			values = new ArrayList<String>();
			for (String header : headers) {
				Object keyvalue = obj.get(header);
				if (keyvalue == null) {
					values.add("");
				} else if (keyvalue instanceof JSONArray) {
					String straux = keyvalue.toString();
					straux = straux.replaceAll(",", ";");
					values.add(straux);
				} else {
					values.add((String)keyvalue);
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	public static <T> List<T> convertSetToList(Set<T> set) { 
        List<T> list = new ArrayList<T>(); 
        for (T t : set) 
            list.add(t); 
        return list; 
    } 
  
	public List<String> getHeaders() {
		return headers;
	}
	
	public List<String> getValues() {
		return values;
	}
		
	@Override
    public String getValueByColumnName(String columnName) {
		int index = headers.indexOf(columnName);
		if (index == -1) {
			return null;
		}
		return values.get(index);
    }

    @Override
    public String getValueByColumnIndex(int index) {
    	if (index < 0 || index >= values.size()) {
    		return null;
    	}
    	return values.get(index);
    }

    @Override
    public int size() {
    	return headers.size();
    }
}