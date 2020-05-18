package org.hadatac.data.api;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.entity.pojo.STR;

public class STRStore {
	
    private static STRStore store = null; 
    
    // public variables
    final public Map<String,STR> cache;
  
    private STRStore() { 
    	cache = new HashMap<String,STR>();
    	refreshStore();
    } 
  
    // static method to create instance of Singleton class 
    public static STRStore getInstance() 
    { 
        if (store == null) 
            store = new STRStore(); 
  
        return store; 
    } 
    
	public STR findCachedByUri(String streamUri) {
		return cache.get(streamUri);
	}

	public List<STR> findCachedOpenStreams() {
		List<STR> list = new ArrayList<STR>();
		for (Map.Entry<String, STR> entry: cache.entrySet()) {
			list.add(entry.getValue());
		}
		Collections.sort(list);
		return list;
	}
	
	public void refreshStore() {
		this.cache.clear();
		List<STR> cacheList = STR.findOpenStreams();
		if (cacheList == null) {
			return;
		}
		for (STR str : cacheList) {
			this.cache.put(str.getUri(), str);
		}
	}

}
