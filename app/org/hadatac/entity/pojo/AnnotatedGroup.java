package org.hadatac.entity.pojo;

import java.util.*;

/*
   A list of AnnotatedValues where all AnnotatedValues are values of categorical variables, and the list is ordered by
   the classValue.
 */
public class AnnotatedGroup {

	// the key is a string of the values of the annotatedValues order by the key of the variables associated with each value.
	// AnnotatedValues in AnnotatedGroup must have valueClass.
	private List<AnnotatedValue> group;

    public AnnotatedGroup(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment, String categoricalOption) {
    	this.group = getGroupFromMap(valueMap, varMap, alignment, categoricalOption);
    }

	public List<AnnotatedValue> getGroup() {
		return group;
	}

	public void setGroup(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment, String categoricalOption) {
    	this.group = getGroupFromMap(valueMap, varMap, alignment, categoricalOption);
    }

	public void setGroup(List<AnnotatedValue> group) { this.group = group; }

	// AnnotatedValues without valueClasses are dropped. In a list, the first annotatedValue with a valueClass is selected.
	// key of input map: string key of variable
	// key of output map: string of the concatenation of all values ordered by the string keuy of associated variables
	private static List<AnnotatedValue> getGroupFromMap(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment, String categoricalOption) {
		List<AnnotatedValue> newGroup = new ArrayList<AnnotatedValue>();

		try {
			//System.out.println("In AnnotatedGroup: new group size BEFORE [" + newGroup.size() + "]");
			// add to the group values from the list that have both valueClass and variable
			for (Map.Entry<String, List<AnnotatedValue>> entry : valueMap.entrySet()) {
				for (AnnotatedValue av : entry.getValue()) {

					//System.out.println("In AnnotatedGroup: BEFORE value [" + av.getValue() + " , " + av.getValueClass() + "]");
					//System.out.println("In AnnotatedGroup: key [" + entry.getKey() + "]");
					Variable currVar = varMap.get(entry.getKey());
					if (currVar != null) {
						//System.out.println("In AnnotatedGroup: variable [" + currVar + "]");
						av.setVariable(currVar);
					}

					//System.out.println("In AnnotatedGroup: AFTER value [" + av.getValue() + " , " + av.getValueClass() + "]");

					// values of categorical variables have valueClass
					if (av.getValueClass() != null && !av.getValueClass().equals("")) {
						String codeLabel = Measurement.prettyCodeBookLabel(alignment, av.getValueClass());
						av.setValue(codeLabel);
					} else if (categoricalOption.equals(Measurement.NON_CATG_CATG)) {
						av = CategorizedValue.categorize(av,alignment);
					}

					if (av.getValueClass() != null && !av.getValueClass().equals("")) {
						newGroup.add(av);
					}

				}
			}

			// order the group by the key of associated variable
			newGroup.sort(new Comparator<AnnotatedValue>() {
				@Override
				public int compare(AnnotatedValue v1, AnnotatedValue v2) {
					if (v1.getVariable().getKey().equals(v2.getVariable().getKey())) {
						return v1.getValue().compareTo(v2.getValue());
					}
					return v1.getVariable().getKey().compareTo(v2.getVariable().getKey());
				}
			});

		} catch (Exception e) {
			e.printStackTrace();
		}

		//System.out.println("In AnnotatedGroup: new group size AFTER [" + newGroup.size() + "]");
		return newGroup;

    }

	public String getKey() {
    	String key = "";
    	String lastVariable = "";
    	for (AnnotatedValue value : group) {
    		if (value.getVariable().getKey().equals(lastVariable)) {
    			key = key + "-";
			} else {
    			key = key + ":";
    			lastVariable = value.getVariable().getKey();
			}
    		if (value != null) {
    			key = key + value;
			}

		}
    	return key;
    }

}
