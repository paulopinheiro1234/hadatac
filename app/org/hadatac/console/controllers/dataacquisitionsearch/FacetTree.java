package org.hadatac.console.controllers.dataacquisitionsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.hadatac.console.models.Facet;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.console.models.Pivot;
import org.hadatac.entity.pojo.HADatAcThing;

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
		System.out.println("level " + level);
		System.out.println("facetName: " + facet.getFacetName());
		
		try {
			HADatAcThing object = null;
			if (facets.isEmpty()) {
				object = (HADatAcThing)targetFacet.newInstance();
			} else if (level > facets.size()) {
				return;
			} else if (level == facets.size()) {
				object = (HADatAcThing)targetFacet.newInstance();
			} else {
				object = (HADatAcThing)facets.get(level).newInstance();
			}
			
			Map<HADatAcThing, List<HADatAcThing>> dict = object.getTargetFacets(facet, facetHandler);
			for (HADatAcThing key : dict.keySet()) {
				if (facets.isEmpty()) {
					Pivot pivot = new Pivot();
					pivot.field = key.getField();
					pivot.value = key.getLabel();
					pivot.tooltip = key.getUri();
					pivot.count = key.getCount();
					
					if (pivot.count > 0) {
						curPivot.addChild(pivot);
					}
				} else {
					Pivot pivot = new Pivot();
					pivot.field = key.getField();
					pivot.value = key.getLabel();
					pivot.tooltip = key.getUri();
					if (key.getCount() == 0) {
						pivot.count = (int)dict.get(key).get(0).getNumberFromSolr(
								facet.getChildById(key.getUri()), facetHandler);
					} else {
						pivot.count = key.getCount();
					}
					System.out.println("pivot.count: " + pivot.count);
					if (pivot.count > 0) {
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

