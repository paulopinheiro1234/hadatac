package org.hadatac.console.controllers.dataacquisitionsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.console.views.dataacquisitionsearch.Facetable;

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
		System.out.println("\n\n\nlevel " + level);
		System.out.println("facetName: " + facet.getFacetName());
		
		try {
			Facetable object = null;
			if (facets.isEmpty()) {
				object = (Facetable)targetFacet.newInstance();
			} else if (level > facets.size()) {
				return;
			} else if (level == facets.size()) {
				object = (Facetable)targetFacet.newInstance();
			} else {
				object = (Facetable)facets.get(level).newInstance();
			}
			
			Map<Facetable, List<Facetable>> dict = object.getTargetFacets(facet, facetHandler);
			for (Facetable key : dict.keySet()) {
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
					System.out.println("pivot.count: " + pivot.getCount());
					if (pivot.getCount() > 0) {
						curPivot.addChild(pivot);
						System.out.println("retrieveFacetData for " + key.getUri());
						retrieveFacetData(level + 1, facet.getChildById(key.getUri()), facetHandler, pivot);
					}
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}

