package org.hadatac.data.api;

import java.util.List;

import org.hadatac.entity.pojo.ConsoleStore;
import org.hadatac.entity.pojo.DataCollection;
import org.hadatac.entity.pojo.Dataset;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.Detector;
import org.hadatac.entity.pojo.Instrument;
import org.hadatac.entity.pojo.Platform;

public class DataFactory {
	public static DataCollection createDataCollection(String dataCollectionUri, String deploymentUri) {
		DataCollection dataCollection = null;
		Deployment deployment = Deployment.find(deploymentUri);
		
		dataCollection = new DataCollection();
		dataCollection.setUri(dataCollectionUri);
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
	
	public static Deployment createDeployment(String deploymentUri, String platformUri, String instrumentUri, String[] detectorUri, String startedAt) {
		Deployment deployment = Deployment.create(deploymentUri);
		
		deployment.platform = Platform.find(platformUri);
		deployment.instrument = Instrument.find(instrumentUri);
		for (int i = 0; i < detectorUri.length; i++) {
			deployment.detectors.add(Detector.find(detectorUri[i]));
		}
		deployment.setStartedAtXsd(startedAt);
		deployment.save();
		
		return deployment;
	}
	
	public static DataCollection getActiveDataCollection(String deploymentUri) {
		List<DataCollection> list;
		Deployment deployment = Deployment.find(deploymentUri);
		list = DataCollection.find(deployment, true);
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
}
