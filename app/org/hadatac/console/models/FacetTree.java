package org.hadatac.console.models;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FacetTree {

    private List<Class<?>> facets = new ArrayList<Class<?>>();
    private Class<?> targetFacet = null;

    public FacetTree () {}

    public Class<?> getTargetFacet() {
        return targetFacet;
    }

    public void setTargetFacet(Class<?> facet) {
        targetFacet = facet;
    }

    public void addUpperFacet(Class<?> facet) {
        facets.add(facet);
    }

    public void print() {
        System.out.println("target facet: " + targetFacet.getName());
        for (Class<?> facet : facets) {
            System.out.println("facet: " + facet.getName());
        }
    }

    public void retrieveFacetData(int level, Facet facet, 
            FacetHandler facetHandler, 
            Pivot curPivot) {
        //System.out.println("\n\n\nlevel " + level);
    	/*
    	System.out.println("FacetTree:  level: " + level + 
        		"  facetName: " + facet.getFacetName() + 
        		"  facetsSize: " + facets.size() + 
        		"  curPivotField: " + curPivot.getField() +
        		"  curPivotUri: " + curPivot.getTooltip());
    	*/
    	
        try {
            Facetable object = null;
            if (facets.isEmpty()) {
                object = (Facetable)targetFacet.getDeclaredConstructor().newInstance();
            } else if (level > facets.size()) {
                return;
            } else if (level == facets.size()) {
                object = (Facetable)targetFacet.getDeclaredConstructor().newInstance();
            } else {
                object = (Facetable)facets.get(level).getDeclaredConstructor().newInstance();
            }

            Map<Facetable, List<Facetable>> dict = object.getTargetFacets(facet, facetHandler);
            if (dict == null) {
    	        System.out.println("[ERROR] FacetTree: retrieving object for level: " + level + 
    	        		"  facetName: " + facet.getFacetName() + 
    	        		"  facetsSize: " + facets.size() + 
    	        		"  curPivotField: " + curPivot.getField() +
    	        		"  curPivotUri: " + curPivot.getTooltip());
            } else {
	            for (Facetable key : dict.keySet()) {
	            	/*
	            	if (facet.getFacetName().contentEquals("facetsEC2")) {
	            		System.out.println("Facetable: level=[" + level + "]  key.Field=[" + key.getField() + " key.uri=[" + key.getUri() + "]");
	            		System.out.println("Facets size: " + facets.size());
	            	}
	            	*/
	                if (facets.isEmpty()) {
	                    Pivot pivot = new Pivot();
	                    pivot.setField(key.getField());
	                    pivot.setValue(key.getLabel());
	                    pivot.setTooltip(key.getUri());
	                    pivot.setQuery(key.getQuery());
	                    pivot.setCount(key.getCount());
	
	                    if (pivot.getCount() > 0) {
	                        curPivot.addChild(pivot);
	                    }
	                } else {
	                    Pivot pivot = new Pivot();
	                    pivot.setField(key.getField());
	                    pivot.setValue(key.getLabel());
	                    pivot.setTooltip(key.getUri());
	                    pivot.setQuery(key.getQuery());
	                    if (key.getCount() == 0) {
	                        pivot.setCount((int)dict.get(key).get(0).getNumber(
	                                facet.getChildById(key.getUri()), facetHandler));
	                    } else {
	                        pivot.setCount(key.getCount());
	                    }
		            	//if (facet.getFacetName().contentEquals("facetsEC2")) {
		            	//	System.out.println("pivot.count: " + pivot.getCount());
		            	//}
	                    if (pivot.getCount() > 0) {
	                        curPivot.addChild(pivot);
	    	            	//if (facet.getFacetName().contentEquals("facetsEC2")) {
	    	            	//	System.out.println("retrieveFacetData for " + key.getUri());
	    	            	//}
	                        retrieveFacetData(level + 1, facet.getChildById(key.getUri()), facetHandler, pivot);
	                    }
	                }
	            }
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    } 
}

