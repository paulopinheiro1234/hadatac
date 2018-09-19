package org.hadatac.data.loader;

import java.util.ArrayList;
import java.util.List;

import org.hadatac.console.controllers.annotator.AnnotationLog;

import com.sun.org.apache.xpath.internal.operations.And;

public class GeneratorChain {

    private List<BasicGenerator> chain = new ArrayList<BasicGenerator>();
    private String studyUri = "";
    private RecordFile file = null;
    private boolean bValid = true;

    public String getStudyUri() {
        return studyUri;
    }

    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }

    public RecordFile getRecordFile() {
        return file;
    }

    public void setRecordFile(RecordFile file) {
        this.file = file;
    }

    public boolean isValid() {
        return bValid;
    }

    public void setInvalid() {
        bValid = false;
    }

    public void addGenerator(BasicGenerator generator) {
        chain.add(generator);
    }

    public boolean generate() {
        if (!isValid()) {
            return false;
        }

        for (BasicGenerator generator : chain) {
            try {
                generator.preprocess();
                generator.createRows();
                generator.createObjects();
                generator.postprocess();
            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                AnnotationLog.printException(generator.getErrorMsg(e), generator.getFileName());
                return false;
            }
        }

        // Commit if no errors occurred
        for (BasicGenerator generator : chain) {
            if (!generator.getStudyUri().isEmpty()) {
                setStudyUri(generator.getStudyUri());
            }
            
            if (generator.getStudyUri().isEmpty() && !getStudyUri().isEmpty()) {
                generator.setStudyUri(getStudyUri());
            }
            
            try {
                generator.commitRowsToTripleStore(generator.getRows());
                //generator.commitRowsToLabKey(generator.getRows());

                generator.commitObjectsToTripleStore(generator.getObjects());
                //generator.commitObjectsToLabKey(generator.getObjects());
                generator.commitObjectsToSolr(generator.getObjects());
            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                AnnotationLog.printException(generator.getErrorMsg(e), generator.getFileName());
                return false;
            }
        }

        for (BasicGenerator generator : chain) {
            if (!generator.getStudyUri().equals("")) {
                setStudyUri(generator.getStudyUri());
            }
        }

        postprocess();

        return true;
    }

    public void delete() {
        for (BasicGenerator generator : chain) {
            try {
                generator.preprocess();
                generator.createRows();
                generator.createObjects();
                generator.postprocess();

                generator.deleteRowsFromTripleStore(generator.getRows());
                //generator.deleteRowsFromLabKey(generator.getRows());

                generator.deleteObjectsFromTripleStore(generator.getObjects());
                //generator.deleteObjectsFromLabKey(generator.getObjects());
                generator.deleteObjectsFromSolr(generator.getObjects());
            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                AnnotationLog.printException(e, generator.getFileName());
            }
        }
    }

    public void postprocess() {}
}
