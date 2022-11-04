#!/bin/bash

cd /opt/unigateway || exit

export GATEWAY_SYSTEM_PLATFORM=RASPBERRYPI
export GATEWAY_SYSTEM_MYSENSORS_ENABLED=true
export GATEWAY_SYSTEM_MYSENSORS_PORT_DESCRIPTOR=/myserial
export GATEWAY_MQTT_ENABLED=false
# export GATEWAY_MQTT_HOSTNAME=192.168.1.100

/opt/java/bin/java -Xms64m -Xmx64m -jar unigateway.jar
