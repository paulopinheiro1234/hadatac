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
			Pivot curPivot, 
			boolean bStatsFromSecondLastLevel) {
		try {
			HADatAcThing object = null;
			if (facets.isEmpty()) {
				object = (HADatAcThing)targetFacet.newInstance();
			} else if (level >= facets.size()) {
				return;
			} else {
				object = (HADatAcThing)facets.get(level).newInstance();
			}
			
			Map<HADatAcThing, List<HADatAcThing>> dict = object.getTargetFacets(facet, facetHandler);
			for (HADatAcThing key : dict.keySet()) {
				if (facets.isEmpty()) {
					Pivot key_pivot = new Pivot();
					key_pivot.field = key.getField();
					key_pivot.value = key.getLabel();
					key_pivot.tooltip = key.getUri();
					key_pivot.count = key.getCount();
					curPivot.addChild(key_pivot);
				} else if (bStatsFromSecondLastLevel && level + 1 == facets.size()) {
					Pivot key_pivot = new Pivot();
					key_pivot.field = key.getField();
					key_pivot.value = key.getLabel();
					key_pivot.tooltip = key.getUri();
					key_pivot.count = key.getCount();
					curPivot.addChild(key_pivot);
					
					((List<HADatAcThing>)dict.get(key)).forEach(item->{
						Pivot item_pivot = new Pivot();
						item_pivot.field = item.getField();
						item_pivot.value = item.getLabel();
						item_pivot.tooltip = item.getUri();
						item_pivot.count = item.getCount();
						key_pivot.addChild(item_pivot);
					});
				} else {				
					Pivot pivot = new Pivot();
					pivot.field = key.getField();
					pivot.value = key.getLabel();
					pivot.tooltip = key.getUri();
					pivot.count = (int)dict.get(key).get(0).getNumberFromSolr(
							facet.getChildById(key.getUri()), facetHandler);
					System.out.println("pivot.count: " + pivot.count);
					curPivot.addChild(pivot);
					if (level + 1 == facets.size()) {
						((List<HADatAcThing>)dict.get(key)).forEach(item->{
							Pivot item_pivot = new Pivot();
							item_pivot.field = item.getField();
							item_pivot.value = item.getLabel();
							item_pivot.tooltip = item.getUri();
							item_pivot.count = (int)dict.get(key).get(0).getNumberFromSolr(
									facet.getChildById(item.getUri()), facetHandler);
							System.out.println("pivot.count: " + pivot.count);
							pivot.addChild(item_pivot);
						});
					}
					retrieveFacetData(level + 1, facet.getChildById(key.getUri()), facetHandler, pivot, bStatsFromSecondLastLevel);
					System.out.println("retrieveFacetData for " + key.getUri());
				}
			}
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}
	
	public Pivot mergeFacetTree(int levelToBegin, int curLevel, List<Integer> levelsToSkip, Pivot base, 
			Pivot pivot, String value, List<Pivot> retChildren) {
		if (levelToBegin == curLevel) {
			base = pivot;
		}
		if (pivot.children.isEmpty()) {
			pivot.value = value + " " + pivot.value;
			retChildren.add(pivot);
		} else {
			for (Pivot child : pivot.children) {
				if (curLevel <= levelToBegin) {
					mergeFacetTree(levelToBegin, curLevel + 1, levelsToSkip, base, 
							child, "", retChildren);
				} else {
					String passed_value = "";
					if (levelsToSkip.contains(curLevel)) {
						passed_value = value;
					} else {
						passed_value = value + " " + pivot.value;
					}
					mergeFacetTree(levelToBegin, curLevel + 1, levelsToSkip, base, 
							child, passed_value, retChildren);
				}
			}
		}
		if (levelToBegin == curLevel) {
			base.children.clear();
			for (Pivot child : retChildren) {
				if (base != child) {
					base.children.add(child);
				}
			}
			retChildren.clear();
		}
		
		return pivot;
	}
}

