package org.hadatac.data.loader.mqtt;

import java.lang.String;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataAcquisitionSchemaAttribute;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.loader.URIUtils;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MessageAnnotation {
	
	public MessageAnnotation() {}

    /********************************************************************************
     *                            STREAM MANAGEMENT                                 *
     ********************************************************************************/
    
    public static void initiateMessageStream(MessageStream stream, String newStudyId) {
    	if (!stream.getStatus().equals(MessageStream.CLOSED) || newStudyId == null || newStudyId.isEmpty()) {
    		return;
    	}
    	//System.out.println("MessageAnnotation: Initiating stream for study " + newStudyId);
    	
    	System.out.println("Initiating message stream: " + stream.getFullName());
		stream.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		stream.getLogger().resetLog();
		stream.getLogger().println(String.format("Initiating message stream: %s", stream.getFullName()));

		List<MessageTopic> topics = MessageTopic.findByStream(stream.getUri());

		if (topics != null && topics.size() > 0) {
			stream.getLogger().println(String.format("Message stream has %s topics", topics.size()));
			for (MessageTopic topic : topics) {
				startMessageTopic(topic);
			}
		}

		stream.setStudyById(newStudyId);
    	//System.out.println("MessageAnnotation: Stream study " + stream.getStudy().getId());
		if (stream.getStudy() != null && stream.getStudy().getId() != null) {
		    stream.setStatus(MessageStream.INITIATED);
			stream.getLogger().println(String.format("Message stream %s is initiated.", stream.getFullName()));
			MessageWorker.getInstance().initiateStream(stream);
		} else {
			stream.getLogger().println(String.format("Message stream %s failed to be associated with study.", stream.getFullName()));
		}
		stream.save();

    }
    
    public static void subscribeMessageStream(MessageStream stream) {
    	if (!stream.getStatus().equals(MessageStream.INITIATED)) {
    		return;
    	}
    	System.out.println("Subscribing message stream: " + stream.getFullName());
		stream.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		stream.getLogger().resetLog();
		stream.getLogger().println(String.format("Subscribing message stream: %s", stream.getFullName()));

    	DataFile archive;
    	if (stream.getDataFileId() == null || stream.getDataFileId().isEmpty()) {
            Date date = new Date();
    		String fileName = "DA-" + stream.getName().replaceAll("/","_").replaceAll(".", "_") + ".json";
    		archive = DataFile.create(fileName, "" , "", DataFile.PROCESSED);
            archive.setSubmissionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(date));
            archive.save();
    		stream.setDataFileId(archive.getId());
    		stream.save();
            stream.getLogger().println(String.format("Creating archive datafile " + fileName + " with id " + archive.getId()));
    	} else {
    		archive = DataFile.findById(stream.getDataFileId());
            stream.getLogger().println("Reusing archive datafile with id " + stream.getDataFileId());
    	}
            
		try {
			System.out.println("MessageAnnotation : calling AsyncSubscribe");
			CompletableFuture.runAsync(() -> AsyncSubscribe.exec(stream));
		} catch (Exception e) {
			stream.getLogger().println("MessageAnnotation: Error executing 'subscribe' inside startMessageStream.");
			e.printStackTrace();
		} 
		stream.setStatus(MessageStream.ACTIVE);
		stream.save();
    
    }
    
    public static void unsubscribeMessageStream(MessageStream stream) {
    	if (!stream.getStatus().equals(MessageStream.ACTIVE)) {
    		return;
    	}
    	System.out.println("Unsubscribing message stream: " + stream.getFullName());
		stream.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		stream.getLogger().resetLog();
		stream.getLogger().println(String.format("Unsubscribing message stream: %s", stream.getFullName()));
		if (!MessageWorker.getInstance().executorsMap.containsKey(stream.getFullName())) {
			stream.getLogger().println("Could not stop message stream: " + stream.getFullName() + ". Reason: currentClient is null");
		} else {
    	}

		stream.setStatus(MessageStream.INITIATED);
		stream.save();
		MessageWorker.getInstance().closeStream(stream);
    }
    
    public static void stopMessageStream(MessageStream stream) {
    	stream.getLogger().println("Stopping message stream: " + stream.getFullName());
		List<MessageTopic> topics = MessageTopic.findByStream(stream.getUri());

		if (topics != null && topics.size() > 0) {
			stream.getLogger().println(String.format("Message stream has issued command to stop %s topics", topics.size()));
			for (MessageTopic topic : topics) {
				stopMessageTopic(topic);
			}
		}
		
		stream.setTotalMessages(0);
		stream.setIngestedMessages(0);
		stream.setStudyUri(null);
        stream.setStatus(MessageStream.CLOSED);
		stream.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		stream.getLogger().println(String.format("Stopped processing of message stream: %s", stream.getFullName()));
		stream.save();		
		MessageWorker.getInstance().closeStream(stream);
		/*
		List<MessageTopic> topics = MessageTopic.findByStream(stream.getUri());
		if (topics != null && topics.size() > 0) {
			for (MessageTopic topic : topics) {
				stopMessageTopic(topic);
			}
		}
		*/
    }

    /********************************************************************************
     *                            TOPIC MANAGEMENT                                  *
     ********************************************************************************/
    
    public static void startMessageTopic(MessageTopic topic) {
		System.out.println("Starting message topic: " + topic.getLabel());
		topic.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		topic.getLogger().resetLog();
		topic.getLogger().println(String.format("Started processing of message topic: %s", topic.getLabel()));

        List<STR> strList = null;
        STR str = null;
        Deployment dpl = null;
        String str_uri = null;
        String deployment_uri = null;
        String schema_uri = null;
        boolean isValid = true;

        // Loads STR Specification
        if (topic != null) {
        	deployment_uri = URIUtils.replacePrefixEx(topic.getDeploymentUri());
        	dpl = Deployment.find(deployment_uri);
            strList = STR.find(dpl, true);
            
            if (strList != null && strList.size() > 0) {
            	str = strList.get(0);
            	str_uri = str.getUri();
            }
            if (str != null) {
                if (!str.isComplete()) {
                    topic.getLogger().printWarningByIdWithArgs("DA_00003", str_uri);
                    isValid = false;
                } else {
                    topic.getLogger().println(String.format("Stream specification is complete: <%s>", str_uri));
                }
                deployment_uri = str.getDeploymentUri();
                schema_uri = str.getSchemaUri();
            } else {
                topic.getLogger().printWarningByIdWithArgs("DA_00004", str_uri);
                isValid = false;
            }
        }

        // Verifies if SDD's URI is valid
        if (isValid) {
	        if (schema_uri == null || schema_uri.isEmpty()) {
	            topic.getLogger().printExceptionByIdWithArgs("DA_00005", str_uri);
	        } else {
	            topic.getLogger().println(String.format("Schema <%s> specified for message topic: <%s>", schema_uri, topic.getLabel()));
	        }
        }

        // Verifies if deployment's URI is valid
        if (isValid) {
	        if (deployment_uri == null || deployment_uri.isEmpty()) {
	            topic.getLogger().printExceptionByIdWithArgs("DA_00006", str_uri);
	        } else {
	            try {
	                deployment_uri = URLDecoder.decode(deployment_uri, "UTF-8");
	            } catch (UnsupportedEncodingException e) {
	                topic.getLogger().printException(String.format("URL decoding error for deployment uri <%s>", deployment_uri));
	            }
	            topic.getLogger().println(String.format("Deployment <%s> specified for message topic <%s>", deployment_uri, topic.getLabel()));
	        }
        }

        // Learns stream's labels
        /*
        if (isValid) {
	        if (str != null) {
	        	List<String> headers = Subscribe.testLabels(topic.getStream(), topic); 

	        	if (headers == null || headers.size() == 0) {
	                topic.getLogger().printException(String.format("Could not retrieve column headers for stream"));
	                isValid = false;
	        	} else {
	        		topic.setHeaders(headers);
		            topic.getLogger().println(String.format("Message topic <%s> has labels", topic.getLabel()));
		            topic.save();
	        	}
	        }
        }
        */
        
        // Retrieves SDD
        DataAcquisitionSchema schema = null;
        if (isValid) {
            schema = DataAcquisitionSchema.find(str.getSchemaUri());
            if (schema == null) {
                topic.getLogger().printExceptionByIdWithArgs("DA_00007", str.getSchemaUri());
                isValid = false;
            } else {
            	List <String> headers = new ArrayList<String>();
            	for (DataAcquisitionSchemaAttribute attr : schema.getAttributes()) {
                	headers.add(attr.getLabel());
            	}
        		topic.setHeaders(headers);
	            topic.getLogger().println(String.format("Message topic <%s> has labels <%s>", topic.getLabel(), headers.toString()));
	            topic.save();
            }
        }

        if (isValid) {
            if (!str.hasCellScope()) {
            	// Need to be fixed here by getting codeMap and codebook from sparql query
            	//DASOInstanceGenerator dasoInstanceGen = new DASOInstanceGenerator(
            	//		stream, str.getStudyUri(), str.getUri(), 
            	//		schema, stream.getName());
            	//chain.addGenerator(dasoInstanceGen);	
            	//chain.addGenerator(new MeasurementGenerator(MeasurementGenerator.MSGMODE, null, topic, str, schema, dasoInstanceGen));
                topic.getLogger().printException(String.format("Message annotation requires cell scope"));
                isValid = false;
            } 
        }
        
        if (isValid) {
            MeasurementGenerator gen = new MeasurementGenerator(MeasurementGenerator.MSGMODE, null, topic, str, schema, null);
            MessageWorker.getInstance().topicsGen.put(topic.getLabel(),gen);
            if (MessageWorker.getInstance().topicsGen.get(topic.getLabel()) == null) { 
            	topic.getLogger().printException(String.format("MeasurementGenerator is null in message annotation"));
            	isValid = false;
            }
        }
        if (isValid) {
            topic.setNamedGraphUri(URIUtils.replacePrefixEx(topic.getDeploymentUri()));
        	topic.setStreamSpecUri(str_uri);
        	topic.setStatus(MessageTopic.ACTIVE);
        
        } else {
        	topic.setStatus(MessageTopic.FAIL);
        }
		topic.save();
		
    }
    
    public static void stopMessageTopic(MessageTopic topic) {
    	if (!topic.getStatus().equals(MessageTopic.INACTIVE)) {
    		System.out.println("Stopping message topic: " + topic.getLabel());
    		topic.setStatus(MessageTopic.INACTIVE);
    		topic.setCompletionTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
    		topic.getLogger().println(String.format("Stopped processing of message topic: %s", topic.getLabel()));
    		topic.save();		
    	}
    }

}
