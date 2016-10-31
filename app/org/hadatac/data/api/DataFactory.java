package org.hadatac.data.api;

import java.util.List;

import org.hadatac.entity.pojo.ConsoleStore;
import org.hadatac.entity.pojo.DataAcquisition;
import org.hadatac.entity.pojo.Dataset;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;

import play.Play;

public class DataFactory {

	public static String DEPLOYMENT_ABBREV = "DP";

    public static String DATA_COLLECTION_ABBREV = "DC";
    
    public static String DATASET_ABBREV = "DS";
    
    public static String CONSOLE_ID = "00000001";
    
    public static DataAcquisition createDataAcquisition(String dataCollectionUri, String deploymentUri, int triggeringEvent, String ownerUri) {
    	DataAcquisition dataCollection = null;
		Deployment deployment = Deployment.find(deploymentUri);
		
		dataCollection = new DataAcquisition();
		dataCollection.setUri(dataCollectionUri);
		dataCollection.setOwnerUri(ownerUri);
		dataCollection.setPermissionUri(ownerUri);
		dataCollection.setTriggeringEvent(triggeringEvent);
		dataCollection.setPlatformUri(deployment.platform.getUri());
		dataCollection.setInstrumentUri(deployment.instrument.getUri());
		dataCollection.setPlatformName(deployment.platform.getLabel());
		dataCollection.setInstrumentModel(deployment.instrument.getLabel());
		dataCollection.setStartedAtXsdWithMillis(deployment.getStartedAt());
		dataCollection.setDeploymentUri(deploymentUri);
		dataCollection.save();
		
		return dataCollection;
	}
	
	public static Dataset createDataset() {
		Dataset dataset = null;
		return dataset;
	}
	
	public static Deployment createDeployment(String deploymentUri, String platformUri, String instrumentUri, List<String> detectorUri, String startedAt, String type) {
		Deployment deployment;
		if (type.equalsIgnoreCase("LEGACY")) {
			deployment = Deployment.createLegacy(deploymentUri);
		} else {
			deployment = Deployment.create(deploymentUri);
		}
		
		deployment.platform = Platform.find(platformUri);
		deployment.instrument = Instrument.find(instrumentUri);
		for (int i = 0; i < detectorUri.size(); i++) {
			deployment.detectors.add(Detector.find(detectorUri.get(i)));
		}
		deployment.setStartedAtXsd(startedAt);
		deployment.save();
		
		return deployment;
	}
	
	public static Deployment createLegacyDeployment(String deploymentUri, String platformUri, String instrumentUri, List<String> detectorUri, String startedAt) {
		Deployment deployment = Deployment.createLegacy(deploymentUri);
		
		deployment.platform = Platform.find(platformUri);
		deployment.instrument = Instrument.find(instrumentUri);
		for (int i = 0; i < detectorUri.size(); i++) {
			deployment.detectors.add(Detector.find(detectorUri.get(i)));
		}
		deployment.setStartedAtXsd(startedAt);
		deployment.save();
		
		return deployment;
	}
	
	public static DataAcquisition getActiveDataAcquisition(String deploymentUri) {
		List<DataAcquisition> list;
		Deployment deployment = Deployment.find(deploymentUri);
		list = DataAcquisition.find(deployment, true);
		if (list.isEmpty()) {
			return null;
		}
		return list.get(0);
	}
	
	public static long getNextDynamicMetadataId() {
		ConsoleStore consoleStore = ConsoleStore.find();
		
		try {
			consoleStore.setLastDynamicMetadataId(consoleStore.getLastDynamicMetadataId()+1);
			consoleStore.save();
			return consoleStore.getLastDynamicMetadataId();
		} catch (Exception e) {
			System.out.println("[ERROR] DataFactory.getNextDynamicMetadataId() - e.Message: " + e.getMessage());
		}
		
		return -1;
	}

    public static String getNextURI(String category) {
    	String metadataId = Long.toHexString(DataFactory.getNextDynamicMetadataId());
    	String host = Play.application().configuration().getString("hadatac.console.host");
    	for (int i = metadataId.length(); i <= 8; i++) {
    		metadataId = "0" + metadataId;
    	}
    	return host + "/hadatac/kb/" + category + "/" + CONSOLE_ID + "/" + metadataId ;   
    }
    

}
