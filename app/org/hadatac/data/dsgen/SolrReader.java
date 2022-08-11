package org.hadatac.data.dsgen;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrRequest;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.hadatac.console.models.FacetHandler;
import org.hadatac.data.model.AcquisitionQueryResult;
import org.hadatac.entity.pojo.Alignment;
import org.hadatac.entity.pojo.AlignmentEntityRole;
import org.hadatac.entity.pojo.Attribute;
import org.hadatac.entity.pojo.AnnotatedValue;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.Entity;
import org.hadatac.entity.pojo.Measurement;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.StudyObject;
import org.hadatac.metadata.loader.URIUtils;
import org.hadatac.utils.CollectionUtil;
import org.hadatac.vocabularies.HASCO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class SolrReader {

    private static final Logger log = LoggerFactory.getLogger(SolrReader.class);

    public static AcquisitionQueryResult readSolrByPage(String user_uri, int page, int pageSize, String facets,
                                                        AcquisitionQueryResult acquisitionQueryResult, String solrQuery)  {

        // Get documents
        long docSize = 0;
        SolrQuery query = new SolrQuery();
        query.setQuery(solrQuery);
        query.setStart(page * pageSize);
        query.setRows(pageSize);
        //query.setFacet(true);     // not sure about these two line
        //query.setFacetLimit(-1);  // not sure about these two line

        // prepare query result
        if ( acquisitionQueryResult == null ) return null;
        acquisitionQueryResult.clearDocument();

        long startTime = System.currentTimeMillis();
        try {
            SolrClient solr = new HttpSolrClient.Builder(
                    CollectionUtil.getCollectionPath(CollectionUtil.Collection.DATA_ACQUISITION)).build();
            QueryResponse queryResponse = solr.query(query, SolrRequest.METHOD.POST);
            solr.close();

            SolrDocumentList docs = queryResponse.getResults();

            docSize = docs.getNumFound();
            //System.out.println("<" + user_uri + "> is reading Solr backend with page =  " + page + ", with pageSize = " + pageSize + ", # of results: " + docs.size() ) ;

            Set<String> uri_set = new HashSet<String>();
            Map<String, STR> cachedDA = new HashMap<String, STR>();
            Map<String, String> mapClassLabel = Measurement.generateCodeClassLabelFacetSearch();

            Iterator<SolrDocument> iterDoc = docs.iterator();
            while (iterDoc.hasNext()) {
                Measurement measurement = Measurement.convertFromSolr(iterDoc.next(), cachedDA, mapClassLabel);
                acquisitionQueryResult.addDocument(measurement);
                uri_set.add(measurement.getEntityUri());
                uri_set.addAll(measurement.getCharacteristicUris());
                uri_set.add(measurement.getUnitUri());
            }

            // Assign labels of entity, characteristic, and units collectively
            Map<String, String> cachedLabels = Measurement.generateCachedLabelFacetSearch(new ArrayList<String>(uri_set));
            for (Measurement measurement : acquisitionQueryResult.getDocuments()) {
                measurement.setLabels(cachedLabels);
            }

        } catch (SolrServerException e) {
            System.out.println("[ERROR] Measurement.find() - SolrServerException message: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("[ERROR] Measurement.find() - IOException message: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("[ERROR] Measurement.find() - Exception message: " + e.getMessage());
            e.printStackTrace();
        }

        acquisitionQueryResult.setDocumentSize(docSize);

        log.info("quering solr back for a single takes: " + (System.currentTimeMillis()-startTime));

        return acquisitionQueryResult;

    }

    public static Map<String, Map<String, List<AnnotatedValue>>> readSolrPagesAndMerge(String ownerUri, String facets,
                                                                         String fileId, int pageSize,
                                                                         Map<String, List<String>> studyMap,
                                                                         Alignment alignment, Map<String,List<String>> alignCache,
                                                                         String categoricalOption, boolean keepSameValue, ColumnMapping columnMapping) {

        //System.out.println("readSolrPagesAndMerge: facets=[" + facets + "]");

        Map<String, Map<String, List<AnnotatedValue>>> results = new HashMap<String, Map<String, List<AnnotatedValue>>>();
        int page = 0;

        AcquisitionQueryResult acquisitionQueryResult = new AcquisitionQueryResult();
        String solQuery = prepareQueryAndResult(ownerUri, facets, acquisitionQueryResult);
        if ( solQuery == null ) return results;

        do {

            long startTime = System.currentTimeMillis();
            //System.out.println("Started reading page");
            readSolrByPage(ownerUri, page, pageSize, facets, acquisitionQueryResult, solQuery);
            //System.out.println("Ended reading page");


            long duration = System.currentTimeMillis() - startTime;
            log.warn("this might take longer time: " + duration);

            if (acquisitionQueryResult == null) break;
            if (acquisitionQueryResult.getDocuments() == null || acquisitionQueryResult.getDocuments().size() == 0 ) break;

            //System.out.println("Started merging page");
            parseAndMerge(results, acquisitionQueryResult.getDocuments(), studyMap, alignment, alignCache, fileId, page,
                    pageSize, acquisitionQueryResult.getDocumentSize(), categoricalOption, keepSameValue, columnMapping);
            //System.out.println("Ended merging page");

            page++;

            if ( acquisitionQueryResult.getDocuments().size() < pageSize ) break;

        } while (true);

        return results;
    }

    private static void parseAndMerge(Map<String, Map<String, List<AnnotatedValue>>> results, List<Measurement> measurements,
                               Map<String, List<String>> studyMap, Alignment alignment, Map<String, List<String>> alignCache,
                               String fileId, int page, int pageSize, long totalSize, String categoricalOption, boolean keepSameValue,
                                      ColumnMapping columnMapping) {

        /*
        if (alignCache == null) {
            System.out.println("alignCache is empty");
        } else {
            for (List<String> auxList : alignCache.values()) {
                System.out.println("alignCache list");
                for (String auxStr: auxList) {
                    System.out.println("  - alignCache value is [" + auxStr + "]");
                }
            }
        }
         */

        if ( measurements == null || measurements.size() == 0 ) return;

        updateSourceStudies(studyMap, measurements);
        List<AnnotatedValue> values = null;
        int counter = 0, prev_ratio = 0;
        DataFile dataFile = null;

        for (Measurement measurement : measurements ) {

            //System.out.println("Measurement.parseAndMerge: measurement.getObjectUri() is " + measurement.getObjectUri());

            if ( measurement.getObjectUri() == null || measurement.getObjectUri().equals("") ) continue;

            counter++;
            StudyObject referenceObj = null;
            //System.out.println("Phase I: ReferenceUri is [" + m.getStudyObjectUri() + "]   ObjectURI is [" + m.getObjectUri() + "]");
            // Perform following actions required if the object of the measurement has not been processed yet
            //   - add a row in the result set for aligning object, if such row does not exist
            //   - add entity-role to the collection of entity-roles of the alignment
            //   - add object to the collection of objects of the alignment

            List<String> alignObjs = alignCache.get(measurement.getEntryObjectUri());
            long duration = 0, threshold = 20;
            if (alignObjs == null) {

                long startTime = System.currentTimeMillis();
                // alignObjs = Alignment.alignmentObjectsWithSubjectGroupMembership(measurement.getEntryObjectUri(), measurement.getStudyUri());
                alignObjs = Alignment.alignmentObjects(measurement.getEntryObjectUri(), HASCO.SUBJECT_COLLECTION);
                duration = System.currentTimeMillis() - startTime;
                if ( duration > threshold ) log.debug("DOWNLOAD: alignment.alignmentObject: " + duration);

                if (alignObjs != null) {
                    alignCache.put(measurement.getEntryObjectUri(),alignObjs);

                            /*
                			System.out.print("Main object: [" + measurement.getObjectUri() + "] Associated Objects: [ ");
                			for (String aux : alignObjs) {
                				System.out.print(aux + " ");
                			}
                			System.out.println("]");
                             */
                }
            }

            for (String currentAlignmentObjectUri : alignObjs) {

                /* START OF REFERENCE OBJECT SCOPE */
                long startTime = System.currentTimeMillis();
                referenceObj = alignment.getObject(currentAlignmentObjectUri);
                duration = System.currentTimeMillis() - startTime;
                if ( duration > threshold ) log.debug("DOWNLOAD: alignment.getObject: " + duration);

                if (referenceObj == null || !referenceObj.getUri().equals(currentAlignmentObjectUri)) {
                    //System.out.println("Phase I: Reading object [" + currentAlignmentObjectUri + "] and study [" + measurement.getStudyUri() + "]");
                    startTime = System.currentTimeMillis();
                    referenceObj = StudyObject.findFacetSearch(currentAlignmentObjectUri, measurement.getStudyUri());
                    duration = System.currentTimeMillis() - startTime;
                    if ( duration > threshold ) log.debug("DOWNLOAD: studyObject.find: " + duration);

                    if (referenceObj != null) {
                        //System.out.println("Phase I: Caching object [" + referenceObj.getUri() + "]");
                        alignment.addObject(referenceObj);
                        addToStudyMap(studyMap, alignment, referenceObj); // we need to add the studyId to the studyMap collection
                    }
                }

                if (referenceObj == null) {
                    System.out.println("[ERROR] Measurement: could not find reference object with uri " + measurement.getObjectUri());
                } else {
                    //System.out.println("Phase I: ReferenceUri is [" + referenceObj.getUri() + "]");
                    if (!results.containsKey(referenceObj.getUri())) {

                        //System.out.println("Align-Debug: adding entity-role");
                        startTime = System.currentTimeMillis();
                        Entity referenceObjEntity = alignment.getEntity(referenceObj.getTypeUri());
                        duration = System.currentTimeMillis() - startTime;
                        if ( duration > threshold ) log.debug("DOWNLOAD: alignment.getEntity: " + duration);

                        if (referenceObjEntity == null || !referenceObjEntity.getUri().equals(referenceObj.getTypeUri())) {

                            startTime = System.currentTimeMillis();
                            referenceObjEntity = Entity.facetSearchFind(referenceObj.getTypeUri());
                            duration = System.currentTimeMillis() - startTime;
                            if ( duration > threshold ) log.debug("DOWNLOAD: Entity.find: " + duration);

                            if (referenceObjEntity == null) {
                                System.out.println("[ERROR] Measurement: retrieving entity " + referenceObj.getTypeUri());
                            } else {
                                alignment.addEntity(referenceObjEntity);
                            }
                        }

                        try {
                            if (referenceObjEntity != null) {
                                //AlignmentEntityRole referenceEntRole = new AlignmentEntityRole(referenceObjEntity,m.getRole());
                                AlignmentEntityRole referenceEntRole = new AlignmentEntityRole(referenceObjEntity, null);

                                if (!alignment.containsRole(referenceEntRole.getKey())) {  // entRole's key is the string of the role plus the label of the entity
                                    alignment.addRole(referenceEntRole);
                                }

                                if (results.get(referenceObj.getUri()) == null) {
                                    values = new ArrayList<AnnotatedValue>();
                                    results.put(referenceObj.getUri(), new HashMap<String, List<AnnotatedValue>>());

                                    if (results.get(referenceObj.getUri()) != null && alignment.objectKey(referenceEntRole) != null) {
                                        if (referenceObj.getOriginalId() != null) {
                                            //System.out.println("Phase I: adding PID " + referenceObj.getOriginalId() + " to result's map as a key: " + alignment.objectKey(referenceEntRole));
                                            values.add(new AnnotatedValue(referenceObj.getOriginalId(), null));
                                            results.get(referenceObj.getUri()).put(alignment.objectKey(referenceEntRole), values);
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    }

                }

                // assign values to results
                startTime = System.currentTimeMillis();
                String key = alignment.measurementKey(measurement);
                if ( columnMapping != null ) {
                    // this is to remember the column mapping between the hormonized data file and the original DA file
                    columnMapping.addToMappings(key, measurement.getDasaUri(), measurement.getValueClass());
                }

                duration = System.currentTimeMillis() - startTime;
                if ( duration > threshold ) log.debug("DOWNLOAD: alignment.measurementKey " + duration);

                //System.out.println("Phase I: computed measurement key [" + key + "]");
                if (key != null) {

                    AnnotatedValue finalValue = null;
                    if (categoricalOption.equals(GenConst.WITH_VALUES)){
                        finalValue = new AnnotatedValue(measurement.getValue(), measurement.getValueClass());
                    } else {
                        //System.out.println("Phase I: valueClass :[" + measurement.getValueClass() + "]    value: [" + measurement.getValue() + "]");
                        if (measurement.getValueClass() != null && !measurement.getValueClass().equals("") && URIUtils.isValidURI(measurement.getValueClass())) {
                            if (!alignment.containsCode(measurement.getValueClass())) {

                                startTime = System.currentTimeMillis();
                                String code = Attribute.findHarmonizedCode(measurement.getValueClass());
                                duration = System.currentTimeMillis() - startTime;
                                if ( duration > threshold ) log.debug("DOWNLOAD: Attribute.findHarmonizedcode: " + duration);

                                //System.out.println("Phase I: new harmonized Code [" + code + "] for URI-value [" + measurement.getValueClass() + "]");
                                if (code != null && !code.equals("")) {
                                    List<String> newEntry = new ArrayList<String>();
                                    newEntry.add(code);
                                    newEntry.add(measurement.getValue());
                                    newEntry.add(key);
                                    alignment.addCode(measurement.getValueClass(), newEntry);
                                }
                            }
                        }

                        if (alignment.containsCode(measurement.getValueClass())) {
                            // get code for qualitative variables
                            List<String> entry = alignment.getCode(measurement.getValueClass());
                            finalValue = new AnnotatedValue(entry.get(0), measurement.getValueClass());
                        } else {
                            // get actual value for quantitative variables
                            finalValue = new AnnotatedValue(measurement.getValueClass(), null);
                        }
                    }

                    if (referenceObj != null && finalValue != null) {
                        if ( measurement.isAllNumerical(finalValue.getValue()) && finalValue.getValue().contains(",") ) {
                            finalValue.setValue(finalValue.getValue().replaceAll(",", ""));
                        }
                        values = results.get(referenceObj.getUri()).get(key);
                        if (values == null) {
                            values = new ArrayList<AnnotatedValue>();
                            values.add(finalValue);
                        } else {
                            if ( values.contains(finalValue) == false || keepSameValue ) values.add(finalValue);
                        }
                        results.get(referenceObj.getUri()).put(key, values);
                        //System.out.println("Phase I: final value [" + finalValue + "]");
                    }

                } else {
                    System.out.println("[ERROR] Measurement: the following measurement could not match any alignment attribute (and no alignment " +
                                "attribute could be created for this measurement): " +
                                //m.getEntityUri() + " " + m.getCharacteristicUri());
                                measurement.getEntityUri() + " " + measurement.getCharacteristicUris().get(0));
                }

                    /* END OF REFERENCE OBJECT SCOPE */
            }

            // compute and show progress
            double ratio = (double)(page*pageSize + counter) / totalSize * 100;
            int current_ratio = (int)ratio;
            if (current_ratio > prev_ratio) {
                prev_ratio = current_ratio;
                if ( current_ratio % 20 == 0 ) {
                    System.out.println(Thread.currentThread() + " : Progress: " + current_ratio + "%");
                }

                dataFile = DataFile.findById(fileId);
                if (dataFile != null) {
                    if (dataFile.getStatus() == DataFile.DELETED) {
                        dataFile.delete();
                        return;
                    }
                    dataFile.setCompletionPercentage(current_ratio);
                    dataFile.save();
                } else {
                    return;
                }
            }

        }

        System.out.println("parseAndMerge: finished one page");

    }

    private static String prepareQueryAndResult(String user_uri, String facets, AcquisitionQueryResult acquisitionQueryResult) {

        long startTime = System.currentTimeMillis();
        List<String> ownedDAs = STR.findAllAccessibleDataAcquisition(user_uri);
        log.info("STR.findAllAccessibleDataAcquisition(user_uri) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        if (ownedDAs.isEmpty()) {
            /*
             * an empty query happens when current user is not allowed to see any
             * data acquisition
             */
            System.out.println("User with this URL: " + user_uri + ": Not allowed to access any Data Acquisition!");
            return null;
        }

        startTime = System.currentTimeMillis();
        FacetHandler facetHandler = new FacetHandler();
        facetHandler.loadFacetsFromString(facets);
        log.info("facetHandler.loadFacetsFromString(facets) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        startTime = System.currentTimeMillis();
        FacetHandler retFacetHandler = new FacetHandler();
        retFacetHandler.loadFacetsFromString(facets);
        log.info("retFacetHandler.loadFacetsFromString(facets) takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        startTime = System.currentTimeMillis();
        Measurement.getAllFacetStats(facetHandler, retFacetHandler, acquisitionQueryResult, true);
        log.info("getAllFacetStats() takes " + (System.currentTimeMillis()-startTime) + "sms to finish");

        return Measurement.buildQuery(ownedDAs, facetHandler);

    }

    private static void addToStudyMap(Map<String, List<String>> studyMap, Alignment alignment, StudyObject referenceObj) {
        if ( studyMap == null || alignment == null || referenceObj == null ) return;
        String studyId = alignment.getStudyId(referenceObj.getIsMemberOf());
        List<String> list = studyMap.getOrDefault(referenceObj.getUri(), new ArrayList<>());
        if ( !list.contains(studyId) ) list.add(studyId);
        studyMap.put(referenceObj.getUri(), list);
    }

    private static void updateSourceStudies(Map<String, List<String>> map, List<Measurement> measurements) {
        if ( measurements == null || measurements.size() == 0 ) return;
        for ( Measurement measurement : measurements ) {
            String studyId = measurement.getStudyUri();
            if ( studyId.contains("STD-")) studyId = studyId.substring(studyId.indexOf("STD-")+"STD-".length());
            List<String> list = map.getOrDefault(measurement.getObjectUri(), new ArrayList<>());
            if ( !list.contains(studyId) ) list.add(studyId);
            map.put(measurement.getObjectUri(), list);
        }
    }


}

