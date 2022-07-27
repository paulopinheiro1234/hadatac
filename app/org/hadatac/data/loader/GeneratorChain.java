package org.hadatac.data.loader;

import java.util.ArrayList;
import java.util.List;

import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.ObjectCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GeneratorChain {

    private static final Logger log = LoggerFactory.getLogger(GeneratorChain.class);

    private List<BaseGenerator> chain = new ArrayList<BaseGenerator>();
    private DataFile dataFile = null;
    private DataFile codebookFile = null;
    private boolean bValid = true;
    private boolean pvAndVarSpec = false;
    private String sddName = "";
    private String studyUri = "";
    private String namedGraphUri = "";

    public String getStudyUri() {
        return studyUri;
    }

    public void setStudyUri(String studyUri) {
        this.studyUri = studyUri;
    }
    
    public String getNamedGraphUri() {
        return namedGraphUri;
    }

    public void setNamedGraphUri(String namedGraphUri) {
        this.namedGraphUri = namedGraphUri;
    }

    public DataFile getDataFile() {
        return dataFile;
    }

    public void setDataFile(DataFile dataFile) {
        this.dataFile = dataFile;
    }

    public DataFile getCodebookFile() {
        return codebookFile;
    }

    public void setCodebookFile(DataFile codebookFile) {
        this.codebookFile = codebookFile;
    }

    public boolean isValid() {
        return bValid;
    }

    public void setInvalid() {
        bValid = false;
    }

    public boolean getPVandVarSpec() {
        return pvAndVarSpec;
    }

    public void setPVandVarSpec(boolean pvAndVarSpec) {
        this.pvAndVarSpec = pvAndVarSpec;
    }

    public String getSddName() {
        return sddName;
    }

    public void setSddName(String sddName) {
        this.sddName = sddName;
    }
    
    public void addGenerator(BaseGenerator generator) {
        chain.add(generator);
    }
    
    public boolean generate() {
        return generate(true);
    }

    public boolean generate(boolean bCommit) {
        if (!isValid()) {
            return false;
        }

        int i = 0;
        for (BaseGenerator generator : chain) {
        	log.info("GeneratorChain: Position " + i++ + " has generator of type [" + generator.getClass().getSimpleName() + "]");
        }

        for (BaseGenerator generator : chain) {
        	System.out.println("GeneratorChain: Executing generator of type [" + generator.getClass().getSimpleName() + "]");
            try {
                generator.preprocess();
                generator.createRows();
                generator.createObjects();
                generator.postprocess();
            } catch (Exception e) {
                System.out.println("[ERROR] GenerationChain: " + generator.getErrorMsg(e));
                e.printStackTrace();
                
                generator.getLogger().printException(generator.getErrorMsg(e));
                return false;
            }
        }
        
        if (!bCommit) {
            return true;
        }

        // Commit if no errors occurred
        for (BaseGenerator generator : chain) {
            if (!generator.getStudyUri().isEmpty()) {
                setStudyUri(generator.getStudyUri());
            }
            
            if (generator.getStudyUri().isEmpty() && !getStudyUri().isEmpty()) {
                generator.setStudyUri(getStudyUri());
            }
            
            if (!getNamedGraphUri().isEmpty()) {
                generator.setNamedGraphUri(getNamedGraphUri());
            } else if (!generator.getStudyUri().isEmpty()) {
                generator.setNamedGraphUri(generator.getStudyUri());
            }
            
            try {
                generator.commitRowsToTripleStore(generator.getRows());
                generator.commitObjectsToTripleStore(generator.getObjects());
                generator.commitObjectsToSolr(generator.getObjects());
            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                
                generator.getLogger().printException(generator.getErrorMsg(e));
                return false;
            }
        }

        for (BaseGenerator generator : chain) {
            if (!generator.getStudyUri().equals("")) {
                setStudyUri(generator.getStudyUri());
            }
        }

        postprocess();

        return true;
    }

    public void delete() {
        for (BaseGenerator generator : chain) {

            if (!getNamedGraphUri().isEmpty()) {
                generator.setNamedGraphUri(getNamedGraphUri());
                log.info("deleting ... and setting the graph names...");
            } else if (!generator.getStudyUri().isEmpty()) {
                generator.setNamedGraphUri(generator.getStudyUri());
                log.info("deleting ... and setting the graph names...");
            }

            try {

                generator.preprocess();
                generator.createRows();
                generator.createObjects();
                generator.postprocess();

                generator.deleteRowsFromTripleStore(generator.getRows());
                generator.deleteObjectsFromTripleStore(generator.getObjects());
                generator.deleteObjectsFromSolr(generator.getObjects());

            } catch (Exception e) {
                System.out.println(generator.getErrorMsg(e));
                e.printStackTrace();
                
                generator.getLogger().printException(e);
            }
        }
    }

    public void postprocess() {}
}
