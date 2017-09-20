package org.hadatac.console.controllers.dataacquisitionsearch;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
	
	public void retrieveFacetData(int level, FacetHandler facetHandler, 
			Pivot curPivot, List<String> preValues, boolean bStatsFromSecondLastLevel) {
		try {
			HADatAcThing object = null;
			if (facets.isEmpty()) {
				object = (HADatAcThing)targetFacet.newInstance();
			} else if (level >= facets.size()) {
				return;
			} else {
				object = (HADatAcThing)facets.get(level).newInstance();
			}
			
			Map<HADatAcThing, List<HADatAcThing>> dict = object.getTargetFacets(preValues, facetHandler);
			for (HADatAcThing key : dict.keySet()) {
				if (facets.isEmpty()) {
					Pivot key_pivot = new Pivot();
					key_pivot.field = key.getTypeUri();
					key_pivot.value = key.getUri();
					key_pivot.count = key.getCount();
					curPivot.addChild(key_pivot);
				} else if (bStatsFromSecondLastLevel && level + 1 == facets.size()) {
					Pivot key_pivot = new Pivot();
					key_pivot.field = key.getTypeUri();
					key_pivot.value = key.getUri();
					key_pivot.count = key.getCount();
					curPivot.addChild(key_pivot);
					((List<HADatAcThing>)dict.get(key)).forEach(item->{
						Pivot item_pivot = new Pivot();
						item_pivot.field = item.getTypeUri();
						item_pivot.value = item.getUri();
						item_pivot.count = item.getCount();
						key_pivot.addChild(item_pivot);
					});
				} else {
					List<String> values = new ArrayList<String>();
					((List<HADatAcThing>)dict.get(key)).forEach(item->{
						values.add(item.getUri());
					});
					Pivot pivot = new Pivot();
					pivot.field = key.getTypeUri();
					pivot.value = key.getUri();
					pivot.count = (int)dict.get(key).get(0).getNumberFromSolr(values, facetHandler);
					curPivot.addChild(pivot);
					if (level + 1 == facets.size()) {
						((List<HADatAcThing>)dict.get(key)).forEach(item->{
							List<String> targetFacetValues = new ArrayList<String>();
							targetFacetValues.add(item.getUri());
							Pivot item_pivot = new Pivot();
							item_pivot.field = item.getTypeUri();
							item_pivot.value = item.getUri();
							item_pivot.count = (int)dict.get(key).get(0).getNumberFromSolr(targetFacetValues, facetHandler);
							pivot.addChild(item_pivot);
						});
					}
					retrieveFacetData(level + 1, facetHandler, pivot, values, bStatsFromSecondLastLevel);
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
}

