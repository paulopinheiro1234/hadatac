package org.hadatac.data.loader.mqtt;

import java.lang.String;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.entity.pojo.STR;
import org.hadatac.entity.pojo.Deployment;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.DataAcquisitionSchema;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.metadata.loader.URIUtils;
import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

public class MessageAnnotationSubscribe {

    public void exec(MessageStream stream) {

    	class SubscribeWorker implements MqttCallback, Runnable {

    	    /****************************************************************/
    		/* Private Instance Variables                                   */
    		/****************************************************************/

    		private volatile boolean stop;


    		private MessageStream       stream;
    		private MqttClient 			client;
    		private MqttConnectOptions 	conOpt;

    		private String 				protocol;
    		private String 				broker;
    		private int 				port;
    		private String              clientId;       

    	    private String 				brokerUrl;
    		private String              topic;
    		private boolean 			clean;
    		private boolean 			quietMode;
    		private boolean 			ssl;
    		private int                 qos;
    		private String              userName;
    		private String              password;

    		private String              plainPayload;
    	    private long                totalMessages;
    	    private int                 ingestedMessages;
    	    private int					partialCounter;
    	    private File                file;

    	    /****************************************************************/
    		/* Constructor                                                  */
    		/****************************************************************/

    	    public SubscribeWorker(MessageStream stream) throws MqttException {

    	    	this.stream        = stream;
    	    	
    	    	this.protocol      = "tcp://";
    			this.broker        = stream.getIP();
    			this.port          = Integer.parseInt(stream.getPort());
    	    	this.brokerUrl     = protocol + broker + ":" + port;
    			this.clientId 	   = UUID.randomUUID().toString();
    			this.clean         = true;
    			this.quietMode 	   = false;
    			this.userName      = null;
    			this.password      = null;

    			this.qos 		   = 0;
    			this.topic 		   = "#";
    			this.ssl           = false;

    	    	if (clientId != null && !clientId.isEmpty()) {
    		    	try {
    		    		// Construct the connection options object that contains connection parameters
    		    		// such as cleanSession and LWT
    			    	conOpt = new MqttConnectOptions();    
    			    	conOpt.setCleanSession(clean);
    			    	if(password != null ) {
    			    	  conOpt.setPassword(this.password.toCharArray());
    			    	}
    			    	if(userName != null) {
    			    	  conOpt.setUserName(this.userName);
    			    	}
    		
    		    		// Construct an MQTT blocking mode client
    					//client = new MqttClient(this.brokerUrl,clientId, dataStore);
    					client = new MqttClient(this.brokerUrl,clientId);
    		
    					// Set this wrapper as the callback handler
    			    	client.setCallback(this);
    			    	
    			    	client.connect(conOpt);
    			    	log("Connected to " + brokerUrl + " with client ID " + client.getClientId());

    		    		if (stream.getDataFileId() == null || stream.getDataFileId().isEmpty()) {
    		    			String error = "[ERROR] No valid data file ID in MessageStream";
    		    	    	log(error);
    		    			file = null;
    		    			throw new Exception(error);
    		    		} else {
    		    			DataFile dataFile = DataFile.findById(stream.getDataFileId());
    		    			if (dataFile == null) {
    			    			String error = "[ERROR] No valid data file ID in MessageStream";
    			    	    	log(error);
    			    	    	file = null;
    			    			throw new Exception(error);
    		    			} else {
    		    				file = new File(dataFile.getAbsolutePath());
    		    			}
    		    		}
    			    	
    			    	log("Subscribing to topic \"" + topic + "\" with qos " + qos);
    			    	client.subscribe(topic, qos);

    			    	totalMessages = 0;
    			    	partialCounter = 0;

    				} catch (Exception e) {
    					log("Unable to set up client: " + e.toString());
    					e.printStackTrace();
    					//System.exit(1);
    				}
    	    	}
    	    }

    	    /****************************************************************/
    		/* Method to implement the Runnable interface                   */
    		/****************************************************************/

    	    @Override
    		public void run() {
    	    	
    	    	boolean finished = false;
    	    	stop = false;
   		     	while (!stop && !finished) {
   	    			try {
   	    				SubscribeWorker aClient = new SubscribeWorker(stream);
   	    		    	//MessageWorker.getInstance().currentClient = aClient;
   	    			} catch(MqttException me) {
   	    				System.out.println("reason: " + me.getReasonCode());
   	    				System.out.println("message: " + me.getMessage());
   	    				System.out.println("localized message: " + me.getLocalizedMessage());
   	    				System.out.println("cause: " + me.getCause());
   	    				System.out.println("exception: " + me);
   	    				me.printStackTrace();
   	    			}
   		     	}
    	    }
    	   

    	    /****************************************************************/
    		/* Supporting Methods                                           */
    		/****************************************************************/

   		    public void stopGracefully() {
 			   stop = true;
 		    }

    	    private void log(String message) {
    	    	if (!quietMode) {
    	    		System.out.println(message);
    	    	}
    	    }

    		/****************************************************************/
    		/* Methods to implement the MqttCallback interface              */
    		/****************************************************************/

    		public void connectionLost(Throwable cause) {
    			log("Connection to " + brokerUrl + " lost!" + cause);
    			//System.exit(1);
    		}

    		public void deliveryComplete(IMqttDeliveryToken token) {
    		}

    		public void messageArrived(String topic, MqttMessage message) throws MqttException {
    			String time = new Timestamp(System.currentTimeMillis()).toString();
    			plainPayload = new String(message.getPayload());
    			String resp = "  Time:\t" + time +
    	                      "  Topic:\t" + topic +
    	                      "  Message:\t" + plainPayload +
    	                      "  QoS:\t" + message.getQos();
    			totalMessages = totalMessages + 1;
    			partialCounter = partialCounter + 1;
    			if (partialCounter >= 500) {
    				partialCounter = 0;
    				System.out.println("Received " + totalMessages + " messages. Ingested " + ingestedMessages + " messages.");
    			}
    		
    			// If needed, print the message
    			//System.out.println("Payload: " + plainPayload);
    			//System.out.println("Response: " + resp);
    			
    			// Store full message if an archive file is available
    			if (file != null) {
    				try {
    					FileUtils.writeStringToFile(file, resp + "\n", "utf-8", true);
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
    			}

    			// Pass the message to MessageWorker to be ingested into HADatAc
    			try {
    				if (MessageWorker.processMessage(topic, plainPayload, ingestedMessages) != null) {
    					ingestedMessages = ingestedMessages + 1;
    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}

    		/****************************************************************/
    		/* End of MqttCallback methods                                  */
    		/****************************************************************/

    	}
    	
    	if (!stream.getStatus().equals(MessageStream.INITIATED)) {
    		return;
    	}
    	System.out.println("Subscribing message stream: " + stream.getName());
		stream.setLastProcessTime(new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
		stream.getLogger().resetLog();
		stream.getLogger().println(String.format("Subscribing message stream: %s", stream.getName()));

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
		    Thread t = new Thread(new SubscribeWorker(stream));
			t.start();
		} catch (Exception e) {
			stream.getLogger().println("MessageAnnotation: Error executing 'subscribe' inside startMessageStream.");
			e.printStackTrace();
		}
		stream.setStatus(MessageStream.ACTIVE);
		stream.save();

		// Stream Subscription refresh needs to occur after the stream is activated
		MessageWorker.refreshStreamSubscription();
    
    }
    
}
