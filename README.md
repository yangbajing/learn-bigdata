# learn-bigdata

- 杨景 yangbajing at gmail com

## Install

### Docker

Install [docker](https://docs.docker.com/get-docker/) and [docker-compose](https://docs.docker.com/compose/install/)

<a id="start-docker-with-docker-compose"></a>
#### Start docker with docker-compose

```
// Start
docker-compose -f scripts/docker-compose.yml up -d
```

- zookeeper 3
- kafka 2.5
- MySQL 5.7

### Kafka

List topics.

```
docker-compose -f scripts/docker-compose.yml exec bd-kafka kafka-topics.sh --list \
    --bootstrap-server localhost:9092
```

Create topic

```
docker-compose -f scripts/docker-compose.yml exec bd-kafka kafka-topics.sh --create \
    --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
```

start kafka producer console

```
docker-compose -f scripts/docker-compose.yml exec bd-kafka kafka-console-producer.sh \
    --bootstrap-server localhost:9092 --topic test
```

start kafka consumer console

```
docker-compose -f scripts/docker-compose.yml exec bd-kafka kafka-console-consumer.sh \
    --bootstrap-server localhost:9092 --topic test --isolation-level read_committed
```

## Examples

- Flink：[learn-flink](learn-flink)
- Spark: [learn-spark](learn-spark)
