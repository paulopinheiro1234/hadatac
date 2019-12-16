package org.hadatac.data.loader.mqtt;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.UUID;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.MessageStream;

public class SubscribeWorkerOld implements MqttCallback, Runnable {

    /****************************************************************/
	/* Private Instance Variables                                   */
	/****************************************************************/

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

	public SubscribeWorkerOld(MessageStream stream) throws MqttException {

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
    	
		try {
			SubscribeWorkerOld aClient = new SubscribeWorkerOld(stream);
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

    /****************************************************************/
	/* Supporting Methods                                           */
	/****************************************************************/

     public String getPlainPayLoad() {
 
    	return plainPayload;
    }
    
    public String getClientId() {
    	if (client == null) {
    		return "";
    	}
    	return client.getClientId();
    }
	
    public void unsubscribe() throws MqttException {

    	if (client != null) {
    		
	    	log("Unsubscribing to topic \"" + topic + " with client ID " + client.getClientId());
	    	client.unsubscribe(topic);
	
	    	client.disconnect();
	    	log("Disconnected");

    		MessageWorker.getInstance().currentClient = null;

    	}
		
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

