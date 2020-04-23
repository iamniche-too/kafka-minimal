#!/bin/bash

echo HELLO WORLD
echo broker list: $KAFKA_BROKER_LIST

source env/bin/activate
python producer.py
