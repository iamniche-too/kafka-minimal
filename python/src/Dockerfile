FROM python:3.7.7-stretch
ENV DEBIAN_FRONTEND noninteractive
RUN apt-get update && apt-get clean && apt-get install -y python3-software-properties mailutils-

WORKDIR /usr/src/app
COPY requirements.txt producer.py prepare.sh install-librdkafka.sh ./
RUN ./install-librdkafka.sh
RUN python3 -m venv env
RUN /usr/src/app/prepare.sh

CMD [ "python", "./producer.py" ]
