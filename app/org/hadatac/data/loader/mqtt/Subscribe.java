package org.hadatac.data.loader.mqtt;

import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.MessageStream;
import org.hadatac.entity.pojo.MessageTopic;

/**
 * A sample application that demonstrates how to use the Paho MQTT v3.1 Client blocking API.
 *
 * It can be run from the command line in one of two modes:
 *  - as a publisher, sending a single message to a topic on the server
 *  - as a subscriber, listening for messages from the server
 *
 *  There are three versions of the sample that implement the same features
 *  but do so using using different programming styles:
 *  <ol>
 *  <li>Sample (this one) which uses the API which blocks until the operation completes</li>
 *  <li>SampleAsyncWait shows how to use the asynchronous API with waiters that block until
 *  an action completes</li>
 *  <li>SamplecAsyncCallBack shows how to use the asynchronous API where events are
 *  used to notify the application when an action completes<li>
 *  </ol>
 *
 *  If the application is run with the -h parameter then info is displayed that
 *  describes all of the options / parameters.
 */
public class Subscribe implements MqttCallback {

	// Private instance variables
	private MqttClient 			client;
	private MessageStream       stream;
	private String 				brokerUrl;
	private boolean 			quietMode;
	private MqttConnectOptions 	conOpt;
	private boolean 			clean;
	private String              password;
	private String              userName;
    private List<String>        respPayload;
    private String              plainPayload;
    private long                totalMessages;
    private int                 ingestedMessages;
    private int					partialCounter;
    private File                file;
    private int                 action;
    private Map<String,Integer> stat;

    public static final int PUBLISH          = 0;
    public static final int SUBSCRIBE_BATCH  = 1;
    public static final int SUBSCRIBE        = 2;
    public static final int TESTTOPICS       = 3;
    public static final int TESTLABELS       = 4;
    
    public static List<String> testConnection(MessageStream stream) {

    	boolean quietMode 	  = false;
		int qos 			  = 0;
		String clientId 	  = UUID.randomUUID().toString();
		boolean ssl           = false;
		String password       = null;
		String userName       = null;
		String protocol       = "tcp://";
		String broker         = stream.getIP();
		int port              = Integer.parseInt(stream.getPort());
        String url            = protocol + broker + ":" + port;
        List<String> response = new ArrayList<String>();

        try {
	    	response.add("[1/5] Initiating connection to " + url + " with client ID " + clientId);
	    	response.add("[2/5] Creating client instance");
			MqttClient clientTest = new MqttClient(url,clientId);

	    	response.add("[3/5] Creating connection options");
			MqttConnectOptions connTest = new MqttConnectOptions();
	    	connTest.setCleanSession(true);
	    	if(password != null ) {
	    	  connTest.setPassword(password.toCharArray());
	    	}
	    	if(userName != null) {
	    	  connTest.setUserName(userName);
	    	}

	    	response.add("[4/5] Creating connection");
	    	clientTest.connect(connTest);
	    	response.add("[5/5] Connected");

		} catch(MqttException me) {
	    	response.add("Connection failed");
			response.add("- Reason " + me.getReasonCode());
			response.add("- Message:  " + me.getMessage());
			response.add("- Localized Message:  " + me.getLocalizedMessage());
			response.add("- Cause: " + me.getCause());
			response.add("- Exception: " + me);
			me.printStackTrace();
		}
		
		return response;
    }

    public String getPlainPayLoad() {
    	return plainPayload;
    }
    
    public String getClientId() {
    	if (client == null) {
    		return "";
    	}
    	return client.getClientId();
    }
    
    public static List<String> testTopics(MessageStream stream) {
    	return exec(stream, null, TESTTOPICS);
    }

    public static List<String> testLabels(MessageStream stream, MessageTopic topic) {
    	return exec(stream, topic, TESTLABELS);
    }

    public static List<String> execBatch(MessageStream stream) {
    	return exec(stream, null, SUBSCRIBE_BATCH);
    }

    public static List<String> execBatch(MessageStream stream, MessageTopic topic) {
    	return exec(stream, topic, SUBSCRIBE_BATCH);
    }

    public static List<String> exec(MessageStream stream, MessageTopic topic) {
    	return exec(stream, topic, SUBSCRIBE);
    }

    public static List<String> exec(MessageStream stream, MessageTopic streamTopic, int action) {

		// Default settings:
		boolean quietMode 	 = false;
		String topic 		 = "";
		String message 		 = "Message from blocking Paho MQTTv3 Java client sample";
		int qos 			 = 0;
	    //String broker 	 = "m2m.eclipse.org";
		//String broker      = "mqtt.eclipse.org";
		//String broker        = "128.113.122.168";
		String broker        = stream.getIP();
		//int port 			 = 20004;
		int port             = Integer.parseInt(stream.getPort());
		String clientId 	 = null;
		String subTopic      = "#";
		String pubTopic;
		if (streamTopic == null) {
			pubTopic = "#";
		} else {
			pubTopic = streamTopic.getLabel();
		}
		boolean cleanSession = true;
		boolean ssl          = false;
		String password      = null;
		String userName      = null;
		
		topic = pubTopic;

		String protocol = "tcp://";

		ssl = false;

        String url = protocol + broker + ":" + port;

        List<String> response = new ArrayList<String>();

    	clientId = UUID.randomUUID().toString();
    	        
		try {

			Subscribe aClient = new Subscribe(stream, url, clientId, cleanSession, quietMode, userName, password, action);

			List<String> tempResponse;
			// Perform the requested action
			if (action == PUBLISH) {
				tempResponse = aClient.publish(topic,qos,message.getBytes());
				if (tempResponse != null) {
					response.addAll(tempResponse);
				}
			} else {
				tempResponse = aClient.subscribe(topic, qos, streamTopic);
				if (action == TESTLABELS) {
					if (aClient.getPlainPayLoad() != null) {
						JSONRecord handler = new JSONRecord(aClient.getPlainPayLoad());
						streamTopic.setHeaders(handler.getHeaders());
						response.addAll(handler.getHeaders());
					}
				} else {
					if (tempResponse != null) {
						response.addAll(tempResponse);
					}
				}
			}
			
		} catch(MqttException me) {
			// Display full details of any exception that occurs
			System.out.println("reason "+me.getReasonCode());
			System.out.println("msg "+me.getMessage());
			System.out.println("loc "+me.getLocalizedMessage());
			System.out.println("cause "+me.getCause());
			System.out.println("excep "+me);
			me.printStackTrace();
		}
		
		return response;
	}

	/**
	 * Constructs an instance of the sample client wrapper
	 * @param brokerUrl the url of the server to connect to
	 * @param clientId the client id to connect with
	 * @param cleanSession clear state at end of connection or not (durable or non-durable subscriptions)
	 * @param quietMode whether debug should be printed to standard out
     * @param userName the username to connect with
     * @param password the password for the user
	 * @throws MqttException
	 */
    public Subscribe(MessageStream stream, String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password, int action) throws MqttException {
    	this.stream = stream;
    	this.brokerUrl = brokerUrl;
    	this.quietMode = quietMode;
    	this.clean 	   = cleanSession;
    	this.password = password;
    	this.userName = userName;
    	this.action = action;
    	stat = new HashMap<String,Integer>();
    	//This sample stores in a temporary directory... where messages temporarily
    	// stored until the message has been delivered to the server.
    	//..a real application ought to store them somewhere
    	// where they are not likely to get deleted or tampered with
    	//String tmpDir = System.getProperty("java.io.tmpdir");
    	//MqttDefaultFilePersistence dataStore = new MqttDefaultFilePersistence(tmpDir);

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
		    	
			} catch (MqttException e) {
				e.printStackTrace();
				log("Unable to set up client: "+e.toString());
				//System.exit(1);
			}
    	}
    }

    /**
     * Publish / send a message to an MQTT server
     * @param topicName the name of the topic to publish to
     * @param qos the quality of service to delivery the message at (0,1,2)
     * @param payload the set of bytes to send to the MQTT server
     * @throws MqttException
     */
    public List<String> publish(String topicName, int qos, byte[] payload) throws MqttException {

    	// Connect to the MQTT server
    	log("Connecting to "+brokerUrl + " with client ID "+client.getClientId());
    	client.connect(conOpt);
    	log("Connected");

    	String time = new Timestamp(System.currentTimeMillis()).toString();
    	log("Publishing at: "+time+ " to topic \""+topicName+"\" qos "+qos);

    	// Create and configure a message
   		MqttMessage message = new MqttMessage(payload);
    	message.setQos(qos);

    	// Send the message to the server, control is not returned until
    	// it has been delivered to the server meeting the specified
    	// quality of service.
    	client.publish(topicName, message);

    	// Disconnect the client
    	client.disconnect();
    	log("Disconnected");
    	
    	return new ArrayList<String>();
    	
    }

    /**
     * Subscribe to a topic on an MQTT server
     * Once subscribed this method waits for the messages to arrive from the server
     * that match the subscription. It continues listening for messages until the enter key is
     * pressed.
     * @param topicName to subscribe to (can be wild carded)
     * @param qos the maximum quality of service to receive messages at for this subscription
     * @throws MqttException
     */
    public List<String> subscribe(String topicName, int qos, MessageTopic topic) throws MqttException {

    	if (client == null) {
    		System.out.println("Subscribe: client is null");
    		return null;
    	}
    	
    	if (client.getClientId() == null || client.getClientId().isEmpty()) {
    		System.out.println("Subscribe: clientId is null or blank");
    		return null;
    	}
    	
    	// Connect to the MQTT server
    	client.connect(conOpt);
    	log("Connected to " + brokerUrl + " with client ID " + client.getClientId());
    	respPayload = new ArrayList<String>();
    	if (topic == null) {
    		file = null;
    	} else {
    		if (stream.getDataFileId() == null) {
    	    	log("Missing data file's id in topic " + topic.getLabel());
    		} else {
	    		DataFile dataFile = DataFile.findById(stream.getDataFileId());
	    		if (dataFile == null || dataFile.getAbsolutePath() == null) {
	    	    	log("It was not possible to retrieve DataFile from data file's id in topic " + topic.getLabel());
	    			file = null;
	    		} else {
	    			file = new File(dataFile.getAbsolutePath());
	    		}
    		}
    	}
    	
    	// Subscribe to the requested topic
    	// The QoS specified is the maximum level that messages will be sent to the client at.
    	// For instance if QoS 1 is specified, any messages originally published at QoS 2 will
    	// be downgraded to 1 when delivering to the client but messages published at 1 and 0
    	// will be received at the same level they were published at.
    	log("Subscribing to topic \"" + topicName + "\" qos " + qos);
    	client.subscribe(topicName, qos);

    	// Continue waiting for messages for the specified period of time
    	if (action == SUBSCRIBE_BATCH || action == TESTLABELS || action == TESTTOPICS) {
           try {
        	   TimeUnit.SECONDS.sleep(2);
		   } catch (InterruptedException e) {
			   // TODO Auto-generated catch block
			   e.printStackTrace();
		   }
    	} 

    	if (action == SUBSCRIBE) {
    		MessageWorker.getInstance().currentClient = this;
    		totalMessages = 0;
    		partialCounter = 0;
    	}

    	if (action == SUBSCRIBE_BATCH || action == TESTLABELS || action == TESTTOPICS) {
    		client.disconnect();
    		log("Disconnected");
    	}
    	
    	if (action == TESTTOPICS) {
    		respPayload = new ArrayList<String>();
    		for (String tpc : stat.keySet()) {
    			respPayload.add(tpc + "                (Frequency: " + stat.get(tpc) + ")");
    		}
    	}
    	
    	return respPayload;
    }

    public void unsubscribe(String topicName) throws MqttException {

    	if (client != null) {
    		
	    	log("Unsubscribing to topic \"" + topicName + " with client ID " + client.getClientId());
	    	client.unsubscribe(topicName);
	
	    	client.disconnect();
	    	log("Disconnected");

    		MessageWorker.getInstance().currentClient = null;

    	}
		
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
		// Called when the connection to the server has been lost.
		// An application may choose to implement reconnection
		// logic at this point. This sample simply exits.
		log("Connection to " + brokerUrl + " lost!" + cause);
		//System.exit(1);
	}

    /**
     * @see MqttCallback#deliveryComplete(IMqttDeliveryToken)
     */
	public void deliveryComplete(IMqttDeliveryToken token) {
		// Called when a message has been delivered to the
		// server. The token passed in here is the same one
		// that was passed to or returned from the original call to publish.
		// This allows applications to perform asynchronous
		// delivery without blocking until delivery completes.
		//
		// This sample demonstrates asynchronous deliver and
		// uses the token.waitForCompletion() call in the main thread which
		// blocks until the delivery has completed.
		// Additionally the deliveryComplete method will be called if
		// the callback is set on the client
		//
		// If the connection to the server breaks before delivery has completed
		// delivery of a message will complete after the client has re-connected.
		// The getPendingTokens method will provide tokens for any messages
		// that are still to be delivered.
	}

    /**
     * @see MqttCallback#messageArrived(String, MqttMessage)
     */
	public void messageArrived(String topic, MqttMessage message) throws MqttException {
		// Called when a message arrives from the server that matches any
		// subscription made by the client
		String time = new Timestamp(System.currentTimeMillis()).toString();
		plainPayload = new String(message.getPayload());
		String resp = "  Time:\t" +time +
                      "  Topic:\t" + topic +
                      "  Message:\t" + plainPayload +
                      "  QoS:\t" + message.getQos();
		respPayload.add(resp);

		/* 
		 * SUBSCRIBE
		 */
		if (action == SUBSCRIBE) {
			totalMessages = totalMessages + 1;
			stream.setTotalMessages(totalMessages);
			partialCounter = partialCounter + 1;
			if (partialCounter >= 500) {
				partialCounter = 0;
				System.out.println("Received " + totalMessages + " messages. Ingested " + ingestedMessages + " messages.");
				stream.save();
			}
			//System.out.println("Payload: " + plainPayload);
			//System.out.println(resp);
			if (file != null) {
				try {
					FileUtils.writeStringToFile(file, resp + "\n", "utf-8", true);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			try {
				if (MessageWorker.processMessage(topic, plainPayload, ingestedMessages) != null) {
					ingestedMessages = ingestedMessages + 1;
					stream.setIngestedMessages(ingestedMessages);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		/* 
		 * TESTTOPICS
		 */
		if (action == TESTTOPICS) {
			if (stat.containsKey(topic)) {
				int current = stat.get(topic);
				stat.put(topic,current + 1);
			} else {
				stat.put(topic,1);
			}
		}
	
	}

	/****************************************************************/
	/* End of MqttCallback methods                                  */
	/****************************************************************/

}

