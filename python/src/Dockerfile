FROM python:3.7.7-stretch
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get clean && apt-get install -y python3-software-properties mailutils-

WORKDIR /usr/src/app
COPY producer.py readings.avsc run.sh requirements.txt prepare.sh install-librdkafka.sh ./
RUN ./install-librdkafka.sh
RUN python3 -m venv env
RUN /usr/src/app/prepare.sh

COPY run.sh ./
CMD [ "bash", "./run.sh" ]
