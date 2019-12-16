package org.hadatac.data.loader.mqtt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.data.loader.Record;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;

public class MessageWorker {
	
    private static MessageWorker single_instance = null; 
  
    // public variables
    public Subscribe currentClient; 
	public Map<String,MessageTopic> topicsMap;
	public Map<String,MeasurementGenerator> topicsGen;
  
    private MessageWorker() { 
        currentClient = null; 
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
    
	public static void refreshStreamSubscription() {
        
		System.out.println("MessageWorker: refreshing topics map ");
		Map<String,MessageTopic> topicsMapTemp = new HashMap<String,MessageTopic>();
		
        List<MessageStream> streams = MessageStream.find();
        for (MessageStream stream: streams) {
        	if (stream.getStatus().equals(MessageStream.ACTIVE)) {
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

	public static Record processMessage(String topicStr, String message, int currentRow) {
		//System.out.println("TopicStr: [" + topicStr + "]   Message: [" + message + "]   0");
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
			//System.out.println("MessageWorker: 'record' variable is null in processMessage");
		} else if (MessageWorker.getInstance().topicsGen == null || 
				   MessageWorker.getInstance().topicsGen.get(topicStr) == null){
			//System.out.println("MessageWorker: 'generator' object is null in processMessage");
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
