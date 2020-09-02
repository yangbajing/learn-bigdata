# Kafka

```
./bin/zookeeper-server-start.sh -daemon config/zookeeper.properties
./bin/kafka-server-start.sh -daemon config/server.properties 
```

## Kafka connect

配置 `config/connect-distributed.properties`：
```
bootstrap.servers=localhost:9092
rest.host.name=localhost
rest.port=8083
```

启动
```
 ./bin/connect-distributed.sh config/connect-distributed.properties
```

添加 connector：
```
curl -H "Content-Type:application/json" -H "Accept:application/json" \
  http://localhost:8083/connectors -X POST \
  --data '{"name":"file-connector","config":{"connector.class":"org.apache.kafka.connect.file.FileStreamSourceConnector","file":"/var/log/nginx/access.log","tasks.max":"1","topic":"access_log"}}'
```

监听 kafka streams 输出：
```
bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic browser-check --from-beginning \
  --property value.deserializer=org.apache.kafka.common.serialization.LongDeserializer \
  --property print.key=true \
  --property key.deserializer=org.apache.kafka.streams.kstream.TimeWindowedDeserializer \
  --property key.deserializer.default.windowed.key.serde.inner=org.apache.kafka.common.serialization.Serdes\$StringSerde 
```
