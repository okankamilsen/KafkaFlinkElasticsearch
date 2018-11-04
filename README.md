# Kafka Flink Elasticsearch-6.4.2

Thanks to keiraqz. Updated version of the keiraqz's project for Elasticsearch-6.4.2 and Kafka 0.11.
https://github.com/keiraqz/KafkaFlinkElastic

* Build the project

```
bıild : mvn clean install 
run   : mvn exec:java -Dexec.mainClass=viper.KafkaFlinkElastic
```
	```
		
### Flink 2.11 | Kafka 0.11 | Elasticsearch 6.4.2

* Prepare Kafka & Elasticsearch
	
	* Assume you have Kafka and Elasticsearch installed on your local machine
		* Create a kafka topic:
		
		```
		/usr/local/kafka/bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic viper_test
		```  
		
		* Create Elasticsearch index & doctype

		```
		# create viper-test index
		curl -XPUT 'http://localhost:9200/viper-test/' -d '{
		    "settings" : {
		        "index" : {
		            "number_of_shards" : 1, 
		            "number_of_replicas" : 0
		        }
		    }
		}'
		
		# put mapping for viper-log doctype
		curl -XPUT 'localhost:9200/viper-test/_mapping/viper-log' -d '{
			  "properties": {
				    "ip": {
				      "type": "string",
				      "index": "not_analyzed"
				    },
				    "info": {
				        "type": "string"
				    }
			  }
		}'
		```

* [Flink & Kafka Connector](https://ci.apache.org/projects/flink/flink-docs-master/apis/streaming/connectors/kafka.html)

	* Maven dependency
	
	```
	<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-connector-kafka-0.11_2.11</artifactId>
			<version>${flink.version}</version>
	</dependency>	```
	
	
* [Flink & Elasticsearch Connector](https://ci.apache.org/projects/flink/flink-docs-master/apis/streaming/connectors/elasticsearch2.html)

	* Maven dependency
	
	```
	<dependency>
			<groupId>org.apache.flink</groupId>
			<artifactId>flink-connector-elasticsearch6_2.11</artifactId>
			<version>1.6.0</version>
	</dependency>
	```
	
* Telegram 
	get your own token and chat id and change the urlString in sendToTelegram method.
