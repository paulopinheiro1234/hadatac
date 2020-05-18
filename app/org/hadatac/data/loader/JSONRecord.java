package org.hadatac.data.loader;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JSONRecord implements Record {

	public List<String> headers = null;
	public List<String> values = null;
	
	@SuppressWarnings("unchecked")
	public JSONRecord(String json) {
		this(json, null);
	}
	
	@SuppressWarnings("unchecked")
	public JSONRecord(String json, List<String> order) {
		//System.out.println("JSON: {" + json + "}");
		//System.out.println("Order: {" + order + "}");
	    JSONParser parser = new JSONParser();
	    JSONObject obj = new JSONObject();
		try {
			obj = (JSONObject) parser.parse(json);
			headers = convertSetToList(obj.keySet());
			values = new ArrayList<String>();
			for (String header : headers) {
				Object keyvalue = obj.get(header);
				//System.out.println("found key: {" + header + "}");
				if (keyvalue == null) {
					values.add("");
				} else {
					String straux = keyvalue.toString();
					values.add(straux);
					//System.out.println("found keyvalue: {" + straux + "}");
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
		reorder(order);
	}
	
	private void reorder(List<String> order) {
		// reorder
		List<String> newOrder = new ArrayList<String>();
		for (int i=0; i < order.size(); i++) {
			newOrder.add("");
		}
		Iterator itHeaders = headers.iterator(); 
		int auxint = 0;
		while (itHeaders.hasNext()) {
			Object obj = itHeaders.next();
			int index = order.indexOf(obj);
			//System.out.println("index of [" + obj.toString() + "] is [" + index + "]");
			if (index >= 0 && index < order.size() && values.get(auxint) != null) {
				newOrder.set(index,values.get(auxint));
			}
			auxint++;
		}
		headers = order;
		values = newOrder;
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