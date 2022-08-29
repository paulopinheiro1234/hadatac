package org.hadatac.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import org.hadatac.vocabularies.HASCO;
import org.hadatac.vocabularies.SIO;

public class HAScOMapper {

    /**
     *
     * This method requests a typeResult that is the main HAScO concept to be serialized. According to this
     * main concept, this method will include filter for all the HAScO classes that are currently filtered. In
     * general, a serializeAll filter will be added for the main concept, and a more restricted filter that
     * includes just the core properties of each concept is serialized for the non-main concept.
     *
     * @param typeResult
     * @return filtered Jackson's ObjectMapper
     */
    public static ObjectMapper getFiltered(String typeResult) {
        ObjectMapper mapper = new ObjectMapper();
        SimpleFilterProvider filterProvider = new SimpleFilterProvider();

        // STUDY OBJECT
        if (typeResult.equals(HASCO.STUDY)) {
            filterProvider.addFilter("studyFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("studyFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // STUDY OBJECT
        if (typeResult.equals(HASCO.OBJECT_COLLECTION)) {
            filterProvider.addFilter("studyObjectFilter", SimpleBeanPropertyFilter.serializeAllExcept("measurements"));
        } else if (typeResult.equals(HASCO.STUDY_OBJECT)) {
            filterProvider.addFilter("studyObjectFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("studyObjectFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // VALUE
        if (typeResult.equals(HASCO.VALUE)) {
            filterProvider.addFilter("valueFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("valueFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment", "studyObjectUri", "variable"));
        }

        // DEPLOYMENT
        if (typeResult.equals(HASCO.DEPLOYMENT)) {
            filterProvider.addFilter("deploymentFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("deploymentFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // STR
        if (typeResult.equals(HASCO.DATA_ACQUISITION)) {
            filterProvider.addFilter("strFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("strFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DATA FILE
        if (typeResult.equals(HASCO.DATA_FILE)) {
            filterProvider.addFilter("dataFileFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("dataFileFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA
        if (typeResult.equals(HASCO.DA_SCHEMA)) {
            filterProvider.addFilter("sddFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("sddFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA_ATTRIBUTE
        if (typeResult.equals(HASCO.DA_SCHEMA_ATTRIBUTE)) {
            filterProvider.addFilter("variableFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("variableFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment", "variableSpec"));
        }

        // VARIABLE_SPEC
        if (typeResult.equals(HASCO.VARIABLE_SPEC)) {
            filterProvider.addFilter("variableSpecFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("variableSpecFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // DA_SCHEMA_OBJECT
        if (typeResult.equals(HASCO.DA_SCHEMA_OBJECT)) {
            filterProvider.addFilter("sddObjectFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("sddObjectFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        // ENTITY
        if (typeResult.equals(SIO.ENTITY)) {
            filterProvider.addFilter("entityFilter", SimpleBeanPropertyFilter.serializeAll());
        } else {
            filterProvider.addFilter("entityFilter",
                    SimpleBeanPropertyFilter.filterOutAllExcept("uri", "label", "typeUri", "typeLabel", "hascoTypeUri", "hascoTypeLabel", "comment"));
        }

        mapper.setFilterProvider(filterProvider);

        return mapper;
    }

}
