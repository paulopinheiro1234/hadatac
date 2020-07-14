package org.hadatac.data.loader.mqtt;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.hadatac.data.api.STRStore;
import org.hadatac.data.loader.JSONRecord;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.data.loader.Record;
import org.hadatac.entity.pojo.STR;

public class MqttMessageWorker {
	
    private static MqttMessageWorker single_instance = null; 
    
    // public variables
    final private Map<String,ExecutorService> executorsMap;
    final private Map<String,MqttAsyncClient> clientsMap;
	final private Map<String,MeasurementGenerator> streamGenMap;
  
    private MqttMessageWorker() { 
    	executorsMap = new HashMap<String,ExecutorService>();
    	clientsMap = new HashMap<String,MqttAsyncClient>();
    	streamGenMap = new HashMap<String,MeasurementGenerator>();
    } 
  
    // static method to create instance of Singleton class 
    public static MqttMessageWorker getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new MqttMessageWorker(); 
  
        return single_instance; 
    } 
    
	public ExecutorService getExecutor(String streamUri) {
		return executorsMap.get(streamUri);
	}

	public void addExecutor(STR stream, ExecutorService executor) {
		this.executorsMap.put(stream.getUri(), executor);
	}

	public boolean containsExecutor(STR stream) {
		if (executorsMap == null || stream == null || stream.getUri() == null) {
			return false;
		}
		return executorsMap.containsKey(stream.getUri());
	}


	public MqttAsyncClient getClient(String streamUri) {
		return clientsMap.get(streamUri);
	}

	public void addClient(STR stream, MqttAsyncClient client) {
		this.clientsMap.put(stream.getUri(), client);
	}

	public MeasurementGenerator getStreamGenerator(String streamUri) { 
		return streamGenMap.get(streamUri);
	}

	public void addStreamGenerator(String streamUri, MeasurementGenerator streamGen) { 
		this.streamGenMap.put(streamUri, streamGen);
	}

	public static Record processMessage(String streamUri, String topicStr, String message, int currentRow) {
		//System.out.println("TopicStr: [" + topicStr + "]   Message: [" + message + "]");

		STR stream = STRStore.getInstance().findCachedByUri(streamUri);
		MeasurementGenerator generator = MqttMessageWorker.getInstance().getStreamGenerator(streamUri);
		Record record = new JSONRecord(message, stream.getHeaders());
		if (generator == null) { 
			System.out.println("MessageWorker: stream generator is missing in processMessage");
		} else {
			try {
				generator.createObject(record, currentRow, topicStr);
				//generator.postprocess();
	            
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return record;
	}
	
    public void stopStream(String streamUri) {

    	STR stream = STRStore.getInstance().findCachedByUri(streamUri);
    	stream.getMessageLogger().println("MessageWorker: stopping stream " + stream.getUri());
		try {
			if (clientsMap != null && stream != null && clientsMap.get(stream.getUri()) != null) {
				clientsMap.get(stream.getUri()).unsubscribe(stream.getLabel() + "/#");
				clientsMap.get(stream.getUri()).disconnectForcibly();
				clientsMap.put(stream.getUri(),null);
				stream.getMessageLogger().println("Unsubscribed mqtt stream [" + stream.getUri() + "]");
			}
		} catch (MqttException e) {
			if (executorsMap != null && stream != null && executorsMap.get(stream.getUri()) != null) {
				executorsMap.get(stream.getUri()).shutdownNow();
				executorsMap.put(stream.getUri(),null);
				stream.getMessageLogger().println("Stopped stream thread [" + stream.getUri() + "]");
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (clientsMap != null && stream != null && stream.getUri() != null) {
			clientsMap.remove(stream.getUri());
			stream.getMessageLogger().println("Removed stream MQTT client");
		}
		if (executorsMap != null && stream != null && stream.getUri() != null) {
			executorsMap.remove(stream.getUri());
			stream.getMessageLogger().println("Removed service executor");
		}
		MqttMessageWorker.getInstance().streamGenMap.remove(stream.getUri());
		stream.getMessageLogger().println("Removed measurement generator");
    }
		
}
