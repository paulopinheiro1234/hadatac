package org.hadatac.data.loader.http;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.eclipse.paho.client.mqttv3.MqttAsyncClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.hadatac.data.api.STRStore;
import org.hadatac.data.loader.JSONRecord;
import org.hadatac.data.loader.MeasurementGenerator;
import org.hadatac.data.loader.Record;
import org.hadatac.data.loader.mqtt.MqttMessageWorker;
import org.hadatac.entity.pojo.STR;

import play.mvc.Http.RequestBody;

public class HttpMessageWorker {

    private static HttpMessageWorker single_instance = null;

    // public variables
    final private Map<String,ExecutorService> executorsMap;
    final private Map<String,MeasurementGenerator> streamGenMap;

    private HttpMessageWorker() {
        executorsMap = new HashMap<String,ExecutorService>();
        streamGenMap = new HashMap<String,MeasurementGenerator>();
    }

    // static method to create instance of Singleton class
    public static HttpMessageWorker getInstance()
    {
        if (single_instance == null)
            single_instance = new HttpMessageWorker();

        return single_instance;
    }

    public MeasurementGenerator getStreamGenerator(String streamUri) {
        return streamGenMap.get(streamUri);
    }

    public void addStreamGenerator(String streamUri, MeasurementGenerator streamGen) {
        this.streamGenMap.put(streamUri, streamGen);
    }

    public ExecutorService getExecutor(String streamUri) {
        return executorsMap.get(streamUri);
    }

    public void addExecutor(String streamUri, ExecutorService executor) {
        this.executorsMap.put(streamUri, executor);
    }

    public boolean containsExecutor(STR stream) {
        if (executorsMap == null || stream == null || stream.getUri() == null) {
            return false;
        }
        return executorsMap.containsKey(stream.getUri());
    }

    public static Record processMessage(String streamUri, String topicStr, String message, int currentRow) {
        //System.out.println("TopicStr: [" + topicStr + "]   Message: [" + message + "]");

        STR stream = STRStore.getInstance().findCachedByUri(streamUri);
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

    public void stopStream(String streamUri) {

        STR stream = STRStore.getInstance().findCachedByUri(streamUri);
        stream.getMessageLogger().println("MessageWorker: stopping stream " + stream.getUri());
        if (streamGenMap != null && stream != null && stream.getUri() != null) {
            streamGenMap.remove(stream.getUri());
            stream.getMessageLogger().println("Removed stream measurement generator");
        }
        if (executorsMap != null && stream != null && stream.getUri() != null) {
            executorsMap.remove(stream.getUri());
            stream.getMessageLogger().println("Removed service executor");
        }
        stream.getMessageLogger().println("Removed measurement generator");
    }

}
