# https://github.com/docker-library/openjdk/blob/master/11/jdk/Dockerfile
FROM openjdk

COPY kafka-minimal-0.0.1-jar-with-dependencies.jar /usr/local/java
COPY run-consumer.sh /usr/local/java

ENTRYPOINT ["/bin/sh", "-c", "/usr/local/java/run-consumer.sh"]
