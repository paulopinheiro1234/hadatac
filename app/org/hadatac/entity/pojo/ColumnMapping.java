package org.hadatac.entity.pojo;

import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.FirstLabel;

import java.util.*;

public class ColumnMapping {

    private Map<String, List<String>> mappings;
    private Set<String> collected;

    public ColumnMapping() {
        mappings = new HashMap<>();
        collected = new HashSet<>();
    }

    public void addToMappings(String harmonizedColumnName, String dasaUri, String valueClass) {

        if ( valueClass != null && valueClass.equalsIgnoreCase("NA") ) return;

        String originalColumnName = FirstLabel.getPrettyLabel(dasaUri);
        if ( originalColumnName == null ) {
            System.out.println("!!! cannot find the label for DASA: " + dasaUri);
            return;
        }
        //if ( collected.contains(originalColumnName) ) return;
        // collected.add(originalColumnName);

        boolean isCategorical = isCategorical(valueClass);

        String key = isCategorical? harmonizedColumnName + "|true" : harmonizedColumnName + "|false";
        List<String> columns = mappings.getOrDefault(key, new ArrayList<>());
        if ( columns.size() == 0 || columns.size() > 0 && !columns.contains(originalColumnName) ) {
            columns.add(originalColumnName);
            mappings.put(key, columns);
        }

        key = isCategorical? originalColumnName + "|true" : originalColumnName + "|false";
        columns = mappings.getOrDefault(key, new ArrayList<>());
        if ( columns.size() == 0 || columns.size() > 0 && !columns.contains(harmonizedColumnName) ) {
            columns.add(harmonizedColumnName);
            mappings.put(key, columns);
        }
    }

    public Map<String, List<String>> getMappings() {
        return mappings;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for ( Map.Entry<String, List<String>> entry : mappings.entrySet() ) {
            sb.append(entry.getKey()).append(" --> ");
            for (String column : entry.getValue() ) sb.append(column).append(", ");
            sb.setLength(sb.length()-2);
            sb.append("\n");
        }
        return sb.toString();
    }

    private boolean isCategorical(String valueClass) {
        boolean isCategorical = false;
        if ( valueClass == null || valueClass.length() == 0 ) return  isCategorical;
        if ( URIUtils.isValidURI(valueClass) ) return true;

        char[] chars = valueClass.replaceAll("\\s+","").toCharArray();
        for ( char c : chars ) {
            if (Character.isAlphabetic(c) ) return true;
        }

        return false;
    }
}
