package org.hadatac.data.loader.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.hadatac.data.loader.JSONRecord;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.data.loader.Record;
import org.hadatac.entity.pojo.STR;

import play.mvc.Http.RequestBody;

public class HttpMessageWorker {
	
    private static HttpMessageWorker single_instance = null; 
    
    // public variables
    final public Map<String,ExecutorService> executorsMap;
	private Map<String,STR> streamMap;
	private Map<String,MeasurementGenerator> streamGenMap;
  
    private HttpMessageWorker() { 
    	executorsMap = new HashMap<String,ExecutorService>();
    	streamGenMap = new HashMap<String,MeasurementGenerator>();
    	streamMap = new HashMap<String,STR>();
    } 
  
    // static method to create instance of Singleton class 
    public static HttpMessageWorker getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new HttpMessageWorker(); 
  
        return single_instance; 
    } 
    
	public STR getStream(String streamUri) {
		return streamMap.get(streamUri);
	}

	public void addStream(STR stream) {
		this.streamMap.put(stream.getUri(), stream);
	}

	public void removeStream(String streamUri) {
		this.streamMap.remove(streamUri);
	}

	public MeasurementGenerator getStreamGenerator(String streamUri) { 
		return streamGenMap.get(streamUri);
	}

	public void addStreamGenerator(String streamUri, MeasurementGenerator streamGen) { 
		this.streamGenMap.put(streamUri, streamGen);
	}

	public void removeStreamGenerator(String streamUri) { 
		this.streamGenMap.remove(streamUri);
	}

	public static Record processMessage(String streamUri, String topicStr, String message, int currentRow) {
		//System.out.println("TopicStr: [" + topicStr + "]   Message: [" + message + "]");

		STR stream = HttpMessageWorker.getInstance().getStream(streamUri);
		MeasurementGenerator generator = HttpMessageWorker.getInstance().getStreamGenerator(streamUri);
		Record record = new JSONRecord(message, stream.getHeaders());
		if (generator == null) { 
			System.out.println("MessageWorker: stream generator is null in processMessage");
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
	
}
