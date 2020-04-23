FROM python:3.7.7-stretch
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get clean && apt-get install -y apt-utils

WORKDIR /usr/src/app
COPY install-librdkafka.sh ./
RUN ./install-librdkafka.sh

RUN python3 -m venv env

COPY requirements.txt prepare.sh ./
RUN /usr/src/app/prepare.sh

RUN apt-get clean

COPY producer.py readings.avsc run.sh ./
CMD [ "bash", "./run.sh" ]
