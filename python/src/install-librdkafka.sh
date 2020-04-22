#!/bin/bash

apt-get install -y wget gnupg

VERSION=5.4
#VERSION=5.2

wget -qO - https://packages.confluent.io/deb/$VERSION/archive.key | apt-key add -
add-apt-repository "deb [arch=amd64] https://packages.confluent.io/deb/$VERSION stable main"
apt-get update

sleep 5
apt-get install -y librdkafka-dev
