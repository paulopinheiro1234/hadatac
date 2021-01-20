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
import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.hadatac.data.loader.JSONRecord;
import org.hadatac.entity.pojo.DataFile;
import org.hadatac.entity.pojo.MessageTopic;
import org.hadatac.entity.pojo.STR;

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
public class MqttSubscribe implements MqttCallback {

    // Private instance variables
    //private MqttAsyncClient     client;
    private MqttClient          client;
    private STR                 stream;
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
    public static final int TESTTOPICS       = 2;
    public static final int TESTLABELS       = 3;

    public static List<String> testConnection(STR stream) {

        boolean quietMode 	  = false;
        int qos 			  = 0;
        String clientId 	  = UUID.randomUUID().toString();
        boolean ssl           = false;
        String password       = null;
        String userName       = null;
        String protocol       = "tcp://";
        String broker         = stream.getMessageIP();
        int port              = Integer.parseInt(stream.getMessagePort());
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

    public static List<String> testTopics(STR stream) {
        return exec(stream, null, TESTTOPICS);
    }

    public static List<String> testLabels(STR stream, MessageTopic topic) {
        return exec(stream, topic, TESTLABELS);
    }

    public static List<String> execBatch(STR stream) {
        return exec(stream, null, SUBSCRIBE_BATCH);
    }

    public static List<String> execBatch(STR stream, MessageTopic topic) {
        return exec(stream, topic, SUBSCRIBE_BATCH);
    }

    public static List<String> exec(STR stream, MessageTopic streamTopic, int action) {

        // Default settings:
        boolean quietMode 	 = false;
        String topic 		 = "";
        String message 		 = "Message from blocking Paho MQTTv3 Java client sample";
        int qos 			 = 0;
        String broker        = stream.getMessageIP();
        int port             = Integer.parseInt(stream.getMessagePort());
        String clientId 	 = null;
        if (streamTopic == null) {
            topic = stream.getLabel() + "/#";
        } else {
            topic = streamTopic.getLabel();
        }
        boolean cleanSession = true;
        boolean ssl          = false;
        String password      = null;
        String userName      = null;

        String protocol = "tcp://";

        ssl = false;

        String url = protocol + broker + ":" + port;

        List<String> response = new ArrayList<String>();

        clientId = UUID.randomUUID().toString();

        try {

            MqttSubscribe aClient = new MqttSubscribe(stream, url, clientId, cleanSession, quietMode, userName, password, action);

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
    public MqttSubscribe(STR stream, String brokerUrl, String clientId, boolean cleanSession, boolean quietMode, String userName, String password, int action) throws MqttException {
        this.stream = stream;
        this.brokerUrl = brokerUrl;
        this.quietMode = quietMode;
        this.clean 	   = cleanSession;
        this.password = password;
        this.userName = userName;
        this.action = action;
        stat = new HashMap<String,Integer>();

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
            if (stream.getMessageArchiveId() == null) {
                log("Missing data file's id in topic " + topic.getLabel());
            } else {
                DataFile dataFile = DataFile.findById(stream.getMessageArchiveId());
                if (dataFile == null || dataFile.getAbsolutePath() == null) {
                    log("It was not possible to retrieve DataFile from data file's id in topic " + topic.getLabel());
                    file = null;
                } else {
                    file = new File(dataFile.getAbsolutePath());
                }
            }
        }

        log("Subscribing to topic \"" + topicName + "\" qos " + qos);

        client.subscribe(topicName, qos);

        // Continue waiting for messages for the specified period of time
        if (action == SUBSCRIBE_BATCH || action == TESTLABELS || action == TESTTOPICS) {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
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

    public void unsubscribe() throws MqttException {

        if (client != null) {

            log("Unsubscribing to topic \"" + "#" + " with client ID " + client.getClientId());
            client.unsubscribe("#");

            client.disconnect();
            log("Disconnected");
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
        log("Connection to " + brokerUrl + " lost!" + cause);
        //System.exit(1);
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
        respPayload.add(resp);

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

