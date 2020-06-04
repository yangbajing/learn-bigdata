# learn-bigdata

- 杨景 yangbajing at gmail com

## Kafka

```
// Start kafka
docker-compose -f scripts/docker-kafka.yml up -d

// List topics.
docker-compose -f scripts/docker-kafka.yml exec kafka-kafka kafka-topics.sh --list --bootstrap-server localhost:9092

// Create topic
docker-compose -f scripts/docker-kafka.yml exec kafka-kafka kafka-topics.sh --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test

// start kafka producer console
docker-compose -f scripts/docker-kafka.yml exec kafka-kafka kafka-console-producer.sh --bootstrap-server localhost:9092 --topic test

// start kafka consumer console
docker-compose -f scripts/docker-kafka.yml exec kafka-kafka kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic test --from-beginning
```
