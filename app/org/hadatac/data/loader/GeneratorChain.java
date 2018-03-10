package org.hadatac.data.loader;

import java.util.ArrayList;
import java.util.List;

import org.hadatac.console.controllers.annotator.AnnotationLog;

public class GeneratorChain {

    private List<BasicGenerator> chain = new ArrayList<BasicGenerator>();

    public void addGenerator(BasicGenerator generator) {
        chain.add(generator);
    }

    public boolean generate() {
        for (BasicGenerator generator : chain) {
            try {				
                generator.preprocess();
                generator.createRows();
                generator.createObjects();
                generator.postprocess();

                generator.commitRowsToTripleStore(generator.getRows());
                generator.commitRowsToLabKey(generator.getRows());

                generator.commitObjectsToTripleStore(generator.getObjects());
                generator.commitObjectsToLabKey(generator.getObjects());
                generator.commitObjectsToSolr(generator.getObjects());

            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                AnnotationLog.printException(e, generator.getFileName());
                return false;
            }
        }

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
}
