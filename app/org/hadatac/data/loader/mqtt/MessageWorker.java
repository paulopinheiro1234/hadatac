package org.hadatac.data.loader.mqtt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.data.loader.Record;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;

public class MessageWorker {
	
    private static MessageWorker single_instance = null; 
  
    // public variables
    public Map<String,Subscribe> clientsMap;
	public Map<String,MessageTopic> topicsMap;
	public Map<String,MeasurementGenerator> topicsGen;
  
    private MessageWorker() { 
    	clientsMap = new HashMap<String,Subscribe>();
    	topicsMap = new HashMap<String,MessageTopic>();
    	topicsGen = new HashMap<String,MeasurementGenerator>();
    } 
  
    // static method to create instance of Singleton class 
    public static MessageWorker getInstance() 
    { 
        if (single_instance == null) 
            single_instance = new MessageWorker(); 
  
        return single_instance; 
    } 
    
    public void initiateStream(MessageStream stream) {
		System.out.println("MessageWorker: adding stream " + stream.getName());
		
		if (stream.getStatus().equals(MessageStream.INITIATED) || stream.getStatus().equals(MessageStream.ACTIVE)) {
			List<MessageTopic> topics = MessageTopic.findActiveByStream(stream.getUri());
			for (MessageTopic topic : topics) {
				System.out.println("MessageWorker: adding topic " + topic.getLabel() + " of stream " + stream.getName());
				System.out.println("Adding topic [" + topic.getLabel() + "]");
				topicsMap.put(topic.getLabel(), topic);
			} 
		}	

		List<String> keyList = new ArrayList<String>(MessageWorker.getInstance().topicsMap.keySet());
		System.out.println("MessageWorker:  topicsMap's keyset size is " + keyList.size());
		for (String key : keyList) {
			System.out.println("MessageWorker:  topicsMap's key is " + key);
		}
		
    }
    
    public void closeStream(MessageStream stream) {
		List<MessageTopic> topics = MessageTopic.findActiveByStream(stream.getUri());
		for (MessageTopic topic : topics) {
			System.out.println("MessageWorker: removing topic " + topic.getLabel() + " of stream " + stream.getName());
			System.out.println("Removing topic [" + topic.getLabel() + "]");
			topicsMap.remove(topic.getLabel());
			topicsGen.remove(topic.getLabel());
		} 
		try {
			if (clientsMap != null && stream != null && clientsMap.get(stream.getName()) != null) {
				clientsMap.get(stream.getName()).unsubscribe();
			}
		} catch (MqttException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (clientsMap != null && stream != null && stream.getName() != null) {
			clientsMap.remove(stream.getName());
		}
    }
    
    /*
    public static void refreshStreamSubscription() {
        
		System.out.println("MessageWorker: refreshing topics map ");
		Map<String,MessageTopic> topicsMapTemp = new HashMap<String,MessageTopic>();
		
        List<MessageStream> streams = MessageStream.find();
        for (MessageStream stream: streams) {
        	if (stream.getStatus().equals(MessageStream.ACTIVE) && !getInstance().clientsMap.containsKey(stream.getName())) {
        		System.out.println("MessageWorker: adding " + stream.getName());
        		List<MessageTopic> topics = MessageTopic.findActiveByStream(stream.getUri());
        		for (MessageTopic topic : topics) {
            		System.out.println("MessageWorker: adding topic " + topic.getLabel() + " of stream " + stream.getName());
            		System.out.println("Adding topic [" + topic.getLabel() + "]");
            		topicsMapTemp.put(topic.getLabel(), topic);
            	} 
        	}
        }

        MessageWorker.getInstance().topicsMap = topicsMapTemp;
		System.out.println("MessageWorker: topics map refreshed");

	}
	*/

	public static Record processMessage(String topicStr, String message, int currentRow) {
		//System.out.println("TopicStr: [" + topicStr + "]   Message: [" + message + "]   0");
		//List<String> keyList = new ArrayList<String>(MessageWorker.getInstance().topicsMap.keySet());
		//System.out.println("MessageWorker:  topicsMap's keyset size is " + keyList.size());
		//for (String key : keyList) {
		//	System.out.println("MessageWorker:  topicsMap's key is " + key);
		//}
		if (!MessageWorker.getInstance().topicsMap.containsKey(topicStr)) {
			return null;
		}
		//MessageTopic topic = MessageWorker.getInstance().topicsMap.get(topicStr);
		//System.out.println("TopicStr: [" + topicStr + "]   1");
		Record record = new JSONRecord(message);
		//RecordMessage rec2 = handler.getRecord(message);
		//System.out.println("labels: [" + rec2.headers + "]   2");
		//System.out.println("values: [" + rec2.values + "]   2");
		//System.out.println("TopicStr: [" + topicStr + "]   2");
		//if (record != null) {
		//	for (int i=0; i < record.size(); i++) {
		//		System.out.println("Value " + i + " is " + record.getValueByColumnIndex(i));
		//	}
		//}
		if (record == null) {
			System.out.println("MessageWorker: 'record' variable is null in processMessage");
		} else if (MessageWorker.getInstance().topicsGen == null || 
				   MessageWorker.getInstance().topicsGen.get(topicStr) == null){
			System.out.println("MessageWorker: 'generator' object is null in processMessage");
		} else {
			try {
				//System.out.println("Processing topicStr: [" + topicStr + "]  3");
				MeasurementGenerator generator = MessageWorker.getInstance().topicsGen.get(topicStr); 
				generator.preprocess();
				generator.createObject(record, currentRow);
				generator.postprocess();
				//System.out.println("TopicStr: [" + topicStr + "]   4");
	            if (!generator.getStudyUri().isEmpty()) {
	                generator.setNamedGraphUri(generator.getStudyUri());
	            }
	            
	            try {
	                //generator.commitRowsToTripleStore(generator.getRows());
	                //generator.commitObjectsToTripleStore(generator.getObjects());
	                generator.commitObjectsToSolr(generator.getObjects());
					//System.out.println("TopicStr: [" + topicStr + "]   5");
	            } catch (Exception e) {
	                //System.out.println(generator.getErrorMsg(e));
	                e.printStackTrace();
	                
	                generator.getLogger().printException(generator.getErrorMsg(e));
	            }

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		//System.out.println(record);
		return record;
	}
	
}
