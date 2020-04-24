#!/bin/bash

echo Hello world
echo $KAFKA_BROKER_LIST

source env/bin/activate
python consumer.py
