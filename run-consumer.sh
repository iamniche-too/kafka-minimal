export CONSUMER_GROUP_ID="consumerGroup0"
export BOOTSTRAP_SERVERS="localhost:9092"
export TOPIC_NAMES="sensor0"
java -cp ./target/kafka-minimal-0.0.1-jar-with-dependencies.jar MemoryUseReportingConsumerMain -XX:+UseG1GC
