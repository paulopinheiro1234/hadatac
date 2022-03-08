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

    public AnnotatedGroup(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment) {
    	this.group = getGroupFromMap(valueMap, varMap, alignment);
    }

	public List<AnnotatedValue> getGroup() {
		return group;
	}

	public void setGroup(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment) { this.group = getGroupFromMap(valueMap, varMap, alignment); }

	public void setGroup(List<AnnotatedValue> group) { this.group = group; }

	// AnnotatedValues without valueClasses are dropped. In a list, the first annotatedValue with a valueClass is selected.
	// key of input map: string key of variable
	// key of output map: string of the concatenation of all values ordered by the string keuy of associated variables
	private static List<AnnotatedValue> getGroupFromMap(Map<String, List<AnnotatedValue>> valueMap, Map<String, Variable> varMap, Alignment alignment) {
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

					/*
					if (true) {
						av = CategorizedValue.categorize(av, alignment);
					}
					*/

					//System.out.println("In AnnotatedGroup: AFTER value [" + av.getValue() + " , " + av.getValueClass() + "]");

					// values of categorical variables have valueClass
					if (av.getValueClass() != null && !av.getValueClass().equals("")) {
						String codeLabel = Measurement.prettyCodeBookLabel(alignment, av.getValueClass());
						av.setValue(codeLabel);
						newGroup.add(av);
					}
				}
			}

			// order the group by the key of associated variable
			newGroup.sort(new Comparator<AnnotatedValue>() {
				@Override
				public int compare(AnnotatedValue v1, AnnotatedValue v2) {
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
    	boolean firstTime = true;
    	for (AnnotatedValue value : group) {
    		if (firstTime) {
				firstTime = false;
			} else {
				key = key + ":";
			}
    		if (value != null) {
    			key = key + value;
			}

		}
    	return key;
    }

}
