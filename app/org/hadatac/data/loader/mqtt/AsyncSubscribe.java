package org.hadatac.data.loader.mqtt;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;

public class AsyncSubscribe implements MqttCallback {

	private MqttAsyncClient     client;
	private MessageStream       stream;
	private String 				brokerUrl;
	private boolean 			quietMode;
	private MqttConnectOptions 	conOpt;
	private boolean 			clean;
	private int                 qos;
	private String              password;
	private String              userName;
    private List<String>        respPayload;
    private String              plainPayload;
    private long                totalMessages;
    private int                 ingestedMessages;
    private int					partialCounter;
    private File                file;

    public String getPlainPayLoad() {
    	return plainPayload;
    }
    
    public String getClientId() {
    	if (client == null) {
    		return "";
    	}
    	return client.getClientId();
    }
    
    public static void exec(MessageStream stream) {

		// Default settings:
		String broker        = stream.getIP();
		int port             = Integer.parseInt(stream.getPort());
		boolean ssl          = false;
		String password      = null;
		String userName      = null;
		
		String protocol = "tcp://";

        String url = protocol + broker + ":" + port;

        ExecutorService executor = Executors.newFixedThreadPool(1);

        // Runnable, return void, nothing, submit and run the task async
        executor.submit((Runnable) () -> {
        
        	try {
        		new AsyncSubscribe(stream, url, userName, password);
        	    TimeUnit.MILLISECONDS.sleep(300);
        	} catch(MqttException me) {
        		// Display full details of any exception that occurs
        		System.out.println("reason " + me.getReasonCode());
        		System.out.println("msg " + me.getMessage());
        		System.out.println("loc " + me.getLocalizedMessage());
        		System.out.println("cause " + me.getCause());
        		System.out.println("excep " + me);
        		me.printStackTrace();
        	} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}        

        }); 
        
        MessageWorker.getInstance().executorsMap.put(stream.getName(), executor);
        
    }

    public AsyncSubscribe(MessageStream stream, String brokerUrl, String userName, String password) throws MqttException {
    	this.stream    = stream;
    	this.brokerUrl = brokerUrl;
    	this.quietMode = false;
    	this.clean 	   = true;
    	this.password  = password;
    	this.userName  = userName;
    	this.qos       = 0;

    	String clientId = UUID.randomUUID().toString();
        
    	if (clientId == null || clientId.isEmpty()) {
    		System.out.println("AsyncSubscribe: client is null");
    		return;
    	}
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
				//client = new MqttAsyncClient(this.brokerUrl,clientId);
				client = new MqttAsyncClient(this.brokerUrl,clientId);
	
				// Set this wrapper as the callback handler
		    	client.setCallback(this);

		    	MessageWorker.getInstance().clientsMap.put(stream.getName(), client);		    	
		    	
			} catch (MqttException e) {
				e.printStackTrace();
				log("Unable to set up client: "+e.toString());
				//System.exit(1);
			}
    	}

    	//public void subscribe(String topicName, int qos, MessageTopic topic) throws MqttException {
    	
    	//log("Async 1");

    	// Connect to the MQTT server
    	IMqttToken token = client.connect(conOpt);
    	token.waitForCompletion();
    	log("Connected to " + brokerUrl + " with client ID " + client.getClientId());
 
    	/*	
    	respPayload = new ArrayList<String>();
    	if (stream.getDataFileId() == null) {
    		log("Missing data file's id in topic " + stream.getLabel());
    	} else {
    		DataFile dataFile = DataFile.findById(stream.getDataFileId());
    		if (dataFile == null || dataFile.getAbsolutePath() == null) {
    			log("It was not possible to retrieve DataFile from data file's id in topic " + stream.getLabel());
    			file = null;
    		} else {
    			file = new File(dataFile.getAbsolutePath());
    		}
    	}
    	*/
    	
    	log("Subscribing to topic \"" + stream.getName() + "\" qos " + qos);

    	client.subscribe(stream.getName() + "/#", qos);

    	totalMessages = 0;
    	partialCounter = 0;

    }

    /**
     * Utility method to handle logging. If 'quietMode' is set, this method does nothing
     * @param message the message to log
     */
    private void log(String message) {
    	if (!quietMode) {
    		System.out.println(message);
    	}
    }

	/****************************************************************/
	/* Methods to implement the MqttCallback interface              */
	/****************************************************************/

    /**
     * @see MqttCallback#connectionLost(Throwable)
     */
	public void connectionLost(Throwable cause) {
		log("Connection to " + brokerUrl + " lost!" + cause);
	}

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
	public void deliveryComplete(IMqttDeliveryToken token) {
	}

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		String time = new Timestamp(System.currentTimeMillis()).toString();
		plainPayload = new String(message.getPayload());
		String resp = "  Time:\t" +time +
				"  Topic:\t" + topic +
				"  Message:\t" + plainPayload +
				"  QoS:\t" + message.getQos();

		/* 
		 *    Compute totals. Save ingested content after reading 500 messages
		 */
		totalMessages = totalMessages + 1;
		stream.setTotalMessages(totalMessages);
		partialCounter = partialCounter + 1;
		if (partialCounter >= 200) {
			partialCounter = 0;
			System.out.println("Received " + totalMessages + " messages. Ingested " + ingestedMessages + " messages.");

			/*
			try {
				Thread.currentThread();
				Thread.sleep(200);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			*/
			
			stream.save();
		}

		//System.out.println(resp);
	
		/*
		if (file != null) {
			try {
				FileUtils.writeStringToFile(file, resp + "\n", "utf-8", true);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		 */

		/*
		 *   Ingest message content
		 */
		try {
			if (MessageWorker.processMessage(topic, plainPayload, ingestedMessages) != null) {
				ingestedMessages = ingestedMessages + 1;
				stream.setIngestedMessages(ingestedMessages);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		if(Thread.currentThread().isInterrupted()) {
			//System.out.println("Thread INTERRUPTED");
			MqttAsyncClient client = MessageWorker.getInstance().clientsMap.get(stream.getFullName());
			IMqttToken token1 = client.unsubscribe(stream.getName());
			token1.waitForCompletion();				
			IMqttToken token2 = client.disconnect();
			token2.waitForCompletion();			
		} else {
			//System.out.println("Thread not interrrupted");
		}
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

}

