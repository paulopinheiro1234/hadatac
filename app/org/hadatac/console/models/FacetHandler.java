package org.hadatac.console.models;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.StringTokenizer;

import com.fasterxml.jackson.databind.ObjectMapper;

public class FacetHandler {

        public Map<String, Object> facetsAll;

        public List facetsEC;
        public List facetsS;
        public List facetsU;
        public List facetsPI;
	
        private class Pair {
	    String field = "";
            String value = "";

            Pair(String f, String v) {
		field = f;
		value = v;
            }

	    String getField() {
		return field;
	    }

	    String getValue() {
		return value;
	    }
        }

	public FacetHandler() {
	       

	       //System.out.println("########### FACET HANDLER CONSTRUCTOR CALLED"); 
	       facetsAll = new HashMap<String, Object>();

	       facetsEC  = new ArrayList<Pair>();
	       facetsS   = new ArrayList<Pair>();
	       facetsU   = new ArrayList<Pair>();
	       facetsPI  = new ArrayList<Pair>();

	       facetsAll.put("facetsEC", facetsEC);
	       facetsAll.put("facetsS", facetsS);
	       facetsAll.put("facetsU", facetsU);
	       facetsAll.put("facetsPI", facetsPI);
	}
	
	public String putFacetEC(String f, String v) {
	        Pair obj = new Pair(f, v);
		facetsEC.add(obj);
		return obj.getValue();
	}
	
        public void removeFacetEC(String f, String v) {
	    for (Object obj : facetsEC) {
		Pair temp = (Pair)obj;
		if ((temp.getField().equals(f)) && (temp.getValue().equals(v))) {
		   facetsEC.remove(temp);
                   break;
		}
	    }
	}

        public List<String> valuesEC() {
	    List<String> list = new ArrayList<String>();
	       for (Object obj : facetsEC) {
                   Pair pair = (Pair)obj;
		   list.add(pair.getValue());
	       }
	       return  list;
        }

	public String putFacetS(String f, String v) {
	        Pair obj = new Pair(f, v);
		facetsS.add(obj);
		return obj.getValue();
	}
	
	public void removeFacetS(String f, String v) {
	    for (Object obj : facetsS) {
		Pair temp = (Pair)obj;
		if ((temp.getField().equals(f)) && (temp.getValue().equals(v))) {
		   facetsS.remove(temp);
                   break;
		}
	    }
	}
	
        public List<String> valuesS() {
	    List<String> list = new ArrayList<String>();
	       for (Object obj : facetsS) {
                   Pair pair = (Pair)obj;
		   list.add(pair.getValue());
	       }
	       return  list;
        }

	public String putFacetU(String f, String v) {
	        Pair obj = new Pair(f, v);
		facetsU.add(obj);
		return obj.getValue();
	}
	
	public void removeFacetU(String f, String v) {
	    for (Object obj : facetsU) {
		Pair temp = (Pair)obj;
		if ((temp.getField().equals(f)) && (temp.getValue().equals(v))) {
		   facetsU.remove(temp);
                   break;
		}
	    }
	}
	
        public List<String> valuesU() {
	    List<String> list = new ArrayList<String>();
	       for (Object obj : facetsU) {
                   Pair pair = (Pair)obj;
		   list.add(pair.getValue());
	       }
	       return  list;
        }

	public String putFacetPI(String f, String v) {
	        Pair obj = new Pair(f, v);
		facetsPI.add(obj);
		return obj.getValue();
	}
	
	public void removeFacetPI(String f, String v) {
	    for (Object obj : facetsPI) {
		Pair temp = (Pair)obj;
		if ((temp.getField().equals(f)) && (temp.getValue().equals(v))) {
		   facetsPI.remove(temp);
                   break;
		}
	    }
	}
	
        public List<String> valuesPI() {
	    List<String> list = new ArrayList<String>();
	       for (Object obj : facetsPI) {
                   Pair pair = (Pair)obj;
		   list.add(pair.getValue());
	       }
	       return  list;
        }

	public String toJSON() {
		ObjectMapper mapper = new ObjectMapper();
		try {
		   String output =  mapper.writeValueAsString(this);
		   //System.out.println("facet handler toJSON(): " + output);
                   return output;
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return "";
	}
	
        private void loadOneFacet(List l, String facet) {
	    if (facet == null || facet.equals("") || facet.equals("{}")) {
		return;
	    }
            String field = "";
            String value = "";
            if (facet.indexOf('"') >= 0) {
	       facet = facet.substring(facet.indexOf('"') + 1);
	       if (facet.indexOf('"') >= 0) {
                  field = facet.substring(0,facet.indexOf('"'));
         	  if (facet.indexOf('"') >= 0) {
	             facet = facet.substring(facet.indexOf('"') + 1);				   
	             if (facet.indexOf('"') >= 0) {
  	                facet = facet.substring(facet.indexOf('"') + 1);
                        if (facet.indexOf('"') >= 0) {
    	                   value = facet.substring(0,facet.indexOf('"'));
			}
                     }
                  }
               }
	    }
            if (!field.equals("") && !value.equals("")) { 
	       Pair obj = new Pair(field, value);
	       l.add(obj);
	    }
        }

        private void loadList(List l, String str) {
	    if (str == null || str.equals("")) {
		return;
	    }
            //System.out.println(">> loadList = <" + str + ">");
	    if (str.indexOf(',') == -1) {
		loadOneFacet(l,str);}
	    else {
	        StringTokenizer st = new StringTokenizer(str,",");
	        while (st.hasMoreTokens()) {
		   loadOneFacet(l, st.nextToken());
	        }	
	    }    
	    return;
        }

        public void loadFacets(String str) {
	        if (str == null || str.equals("")) {
		   return;
	        }
		//System.out.println("str = [" + str + "]");			    
	        // EC list
	        str = str.substring(str.indexOf('['));
		String ECList = str.substring(1,str.indexOf(']'));
		if (ECList != null && !ECList.equals("") && !ECList.equals("{}")) {
		    loadList(facetsEC, ECList);
		}
		str = str.substring(str.indexOf(']'));		    
	        // S list
        	str = str.substring(str.indexOf('['));
		String SList = str.substring(1,str.indexOf(']'));		    
		if (SList != null && !SList.equals("") && !SList.equals("{}")) {
		    loadList(facetsS, SList);
		}
		str = str.substring(str.indexOf(']'));		    
	        // U list
        	str = str.substring(str.indexOf('['));
		String UList = str.substring(1,str.indexOf(']'));		    
		if (UList != null && !UList.equals("") && !UList.equals("{}")) {
		    loadList(facetsU, UList);
		}
		str = str.substring(str.indexOf(']'));		    
                // PI list
        	str = str.substring(str.indexOf('['));
		String PIList = str.substring(1,str.indexOf(']'));		    
		if (PIList != null && !PIList.equals("") && !PIList.equals("{}")) {
		    loadList(facetsPI, PIList);
		}
		//System.out.println("ECList = <" + ECList + ">");			    
		//System.out.println("SList = <" + SList + ">");			    
		//System.out.println("UList = <" + UList + ">");			    
		//System.out.println("PIList = <" + PIList + ">");			    
		return;
        }

        private String facetToSolrQuery(List facets) {
 	    String facetsQuery = "";
            if (facets == null) {
		return facetsQuery;
	    }
	    Iterator<Pair> i = facets.iterator();
	    while (i.hasNext()) {
		Pair temp = i.next();
		//System.out.println("inside pivot: " + temp.getField());
		facetsQuery += temp.getField() + ":\"" + temp.getValue() + "\"";
		if (i.hasNext()) {
		    facetsQuery += " OR ";
		}
	    }
	    if (!facetsQuery.equals("")) {
		facetsQuery = "(" + facetsQuery + ")";
	    }
	    return facetsQuery;
        }

	public String toSolrQuery() {
		String query = "";
                String query_tmp = "";
		int populatedLists = 0;
		Iterator i = facetsAll.entrySet().iterator();
		while (i.hasNext()) {
		        Map.Entry entry = (Map.Entry)i.next();
		        List tmpFacets = (List<Pair>)entry.getValue();
			//System.out.println("List's name: " + entry.getKey() + " size:" + tmpFacets.size());
                        query_tmp = facetToSolrQuery(tmpFacets);
                        if (!query_tmp.equals("")) {
    			    if (tmpFacets.size() > 0) {
			        populatedLists++;
			    }
			    if (populatedLists > 1) {
				query += " AND ";
			    }
			    query += query_tmp;
                        }
		}
		if (query.isEmpty()) {
		    query = "*:*";
		} else {
                    query = "(" + query + ")"; 
		}
		return query;
	}
}
