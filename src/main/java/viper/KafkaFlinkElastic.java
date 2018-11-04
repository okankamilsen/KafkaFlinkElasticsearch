package viper;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.streaming.api.datastream.DataStream;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
//import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSink;
//import org.apache.flink.streaming.connectors.elasticsearch2.ElasticsearchSinkFunction;
//import org.apache.flink.streaming.connectors.elasticsearch2.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch.RequestIndexer;
import org.apache.flink.streaming.connectors.elasticsearch.ElasticsearchSinkFunction;
import org.apache.flink.streaming.connectors.elasticsearch6.ElasticsearchSink;
import org.apache.flink.streaming.connectors.kafka.FlinkKafkaConsumer011;
import org.apache.flink.streaming.util.serialization.SimpleStringSchema;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;

import java.util.ArrayList;
import org.apache.http.HttpHost;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.Requests;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.flink.api.common.functions.MapFunction;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.Date;
import java.net.*;
import java.io.*;
import java.io.IOException;

public class KafkaFlinkElastic {
	
    public static void main(String[] args) throws Exception {
        final StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        DataStream<String> stream = readFromKafka(env);
        System.out.println("Start Listening! Kafka Flink");
        stream.print();
        writeToElastic(stream);
        // execute program
        env.execute("Viper Flink!");
    }

    public static DataStream<String> readFromKafka(StreamExecutionEnvironment env) {
    	env.enableCheckpointing(5000);
        // set up the execution environment
        Properties properties = new Properties();
        properties.setProperty("topic", "MyFirstTopic");
        properties.setProperty("bootstrap.servers", "localhost:9094");
        properties.setProperty("group.id", "test");
        DataStream<String> stream = env.addSource(
                new FlinkKafkaConsumer011<>("MyFirstTopic", new SimpleStringSchema(), properties));
        
        return stream;
    }

    public static String sendToTelegram(Date date) throws MalformedURLException,IOException{
    	String urlString = "https://api.telegram.org/botToken/sendMessage?chat_id=CHATID&text=";

    	String text = "Somebody is knocking your door. Time: "+date;

    	//urlString = String.format(urlString, apiToken, chatId, text);
    	URL oracle = new URL(urlString+text);
        URLConnection yc = oracle.openConnection();
        BufferedReader in = new BufferedReader(new InputStreamReader(
                                    yc.getInputStream()));
        String inputLine;
        while ((inputLine = in.readLine()) != null) 
            System.out.println(inputLine);
        String response = in.toString();
    	return response;
    }

    public static void writeToElastic(DataStream<String> input) {

        Map<String, String> config = new HashMap<>();

        // This instructs the sink to emit after every element, otherwise they would be buffered
        config.put("bulk.flush.max.actions", "1");
        config.put("cluster.name", "docker-cluster");

        try {
            // Add elasticsearch hosts on startup
            //List<InetSocketAddress> transports = new ArrayList<>();
            //transports.add(new InetSocketAddress("127.0.0.1", 9300)); // port is 9300 not 9200 for ES TransportClient
            List<HttpHost> httpHost = new ArrayList<>();
            //httpHost.add(new HttpHost("127.0.0.1", 9200, "http"));
            //httpHost.add(new HttpHost("10.2.3.1", 9200, "http"));
            

            

            // use a ElasticsearchSink.Builder to create an ElasticsearchSink
            ElasticsearchSink.Builder<String> esSinkBuilder = new ElasticsearchSink.Builder<>(
                    httpHost,
                    new ElasticsearchSinkFunction<String>() {
                        public IndexRequest createIndexRequest(String element) {
                            Map<String, Object> json = new HashMap<>();
                            Date date = new Date();
                            json.put("sensorValue", element);
                            json.put("date",date);
                            String response = "";
                            try{
                            	response = sendToTelegram(date);
                        	}catch(MalformedURLException ex){
                        	//do exception handling here
                        	}catch(IOException ex){
                        		//do exception handling here
                        	}
                            json.put("telegram_sended",true);
                            //System.out.println(element+date.toString());
                          
                            
                            return Requests.indexRequest()
                                    .index("knock-i")
                                    .type("knock-t")
                                    .source(json);
                        }

                        @Override
                        public void process(String element, RuntimeContext ctx, RequestIndexer indexer) {
                            indexer.add(createIndexRequest(element));
                        }
                    }
            );

            // finally, build and add the sink to the job's pipeline
            input.addSink(esSinkBuilder.build());

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
