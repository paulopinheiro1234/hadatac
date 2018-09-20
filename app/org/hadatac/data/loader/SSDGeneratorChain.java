package org.hadatac.data.loader;

import java.util.List;
import org.hadatac.entity.pojo.ObjectCollection;
import org.hadatac.console.controllers.annotator.AnnotationLog;

public class SSDGeneratorChain extends GeneratorChain {

    @Override
    public void postprocess() {
        if (this.getStudyUri() == null) {
            return;
        }
        
        List<ObjectCollection> studyOCs = ObjectCollection.findByStudyUri(this.getStudyUri());
        for (ObjectCollection oc: studyOCs) {
            //AnnotationLog.println("SOC has URI  " + oc.getUri() + " and label " + oc.getLabel(), file.getFile().getName());
            String labelResult = ObjectCollection.computeRouteLabel(oc, studyOCs);
            if (labelResult == null) {
                AnnotationLog.println("Label for " + oc.getSOCReference() + ": ERROR could not find path to colletion with grounding label", this.getRecordFile().getFile().getName());
            } else {
                AnnotationLog.println("Label for " + oc.getSOCReference() + ": " + labelResult, this.getRecordFile().getFile().getName());
                oc.saveRoleLabel(labelResult);
            }
        } 
    }
}
