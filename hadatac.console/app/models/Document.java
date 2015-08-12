package models;

import java.util.ArrayList;
import java.util.TreeMap;

public class Document{
	
	public TreeMap<String, String> fields = new TreeMap<String, String>();
	public ArrayList<String> characteristic = new ArrayList<String>();
	
	public Document() {}
	
	public Document(TreeMap<String, String> fields,
					ArrayList<String> characteristic) {
		
		for (String key : fields.keySet()){
			this.fields.put(key, fields.get(key));
		}
		
		for (String c : characteristic){
			if (c == null) continue;
			this.characteristic.add(c);
		}
		
	}
	
}