#!/bin/bash

MQGATEWAY_DIR=/opt/mqgateway
MQGATEWAY_VERSION=${1-"0.19.0"}
MQGATEWAY_CONFIG_URL="https://raw.githubusercontent.com/aetas/mqgateway/v$MQGATEWAY_VERSION/installation-scripts/gateway.yaml"
MQGATEWAY_APP_URL="https://github.com/aetas/mqgateway/releases/download/v$MQGATEWAY_VERSION/mqgateway-$MQGATEWAY_VERSION.jar"
MQGATEWAY_JAR=$MQGATEWAY_DIR/mqgateway-$MQGATEWAY_VERSION-all.jar
MQGATEWAY_START_SCRIPT=$MQGATEWAY_DIR/start_gateway.sh
MQGATEWAY_HEAP_MEMORY=64m
MQGATEWAY_SERVICE_URL="https://raw.githubusercontent.com/aetas/mqgateway/v$MQGATEWAY_VERSION/installation-scripts/mqgateway.service"
ARMBIAN_CONFIG_FILE=/boot/armbianEnv.txt

LOGS_FILE=magateway-install.log

# Make sure only root can run our script
if [[ $EUID -ne 0 ]]; then
   echo "This script must be run as root" 1>&2
   exit 1
fi

function configureNanoPiNeo {
  echo "[MqGateway Installer] Configuring hardware of NanoPi NEO..." | tee -a $LOGS_FILE
  sed -i 's/overlays=.*/overlays=i2c0 uart1 usbhost1 usbhost2/' $ARMBIAN_CONFIG_FILE
  echo "[MqGateway Installer] NanoPi NEO hardware configured" | tee -a $LOGS_FILE
}

function prepareWiringNP {
  echo "[MqGateway Installer] Building WiringNP..." | tee -a $LOGS_FILE
  [ ! -d "/opt" ] && mkdir /opt
  cd /opt || exit

  [ ! -d "/opt/WiringNP" ] && git clone --single-branch --branch nanopi-m1 https://github.com/friendlyarm/WiringNP
  cd /opt/WiringNP || exit
  chmod +x build
  ./build &> mqgateway-install.log
  cd /opt || exit
  echo "[MqGateway Installer] WiringNP built finished" | tee -a $LOGS_FILE
}

function createDirectories {
  echo "[MqGateway Installer] Prepare directories for MqGateway..."  | tee -a $LOGS_FILE
  [ ! -d "$MQGATEWAY_DIR" ] && mkdir -p $MQGATEWAY_DIR/logs
}

function prepareBasicConfig {
  echo "[MqGateway Installer] Prepare base configuration file for MqGateway..." | tee -a $LOGS_FILE
  curl -o $MQGATEWAY_DIR/gateway.yaml -L "$MQGATEWAY_CONFIG_URL"
  echo "[MqGateway Installer] gateway.yaml prepared - remember to change mqttHostname" | tee -a $LOGS_FILE
}

function installJava {
  echo "[MqGateway Installer] Installing Java..." | tee -a $LOGS_FILE
  apt-get update &> mqgateway-install.log
  apt-get install --assume-yes openjdk-8-jre-headless &> mqgateway-install.log
  echo "[MqGateway Installer] Java installed" | tee -a $LOGS_FILE
}

function downloadMqGateway {
  echo "[MqGateway Installer] Downloading MqGateway application..." | tee -a $LOGS_FILE
  curl -o $MQGATEWAY_JAR -L $MQGATEWAY_APP_URL
  chmod 644 $MQGATEWAY_JAR
  echo "[MqGateway Installer] MqGateway application downloaded" | tee -a $LOGS_FILE
}

function prepareService {
  echo "[MqGateway Installer] Preparing systemd mqgateway.service..." | tee -a $LOGS_FILE
  echo "cd /opt/mqgateway || exit" > $MQGATEWAY_START_SCRIPT
  echo "java -Xms$MQGATEWAY_HEAP_MEMORY -Xmx$MQGATEWAY_HEAP_MEMORY -jar mqgateway-$MQGATEWAY_VERSION-all.jar" >> $MQGATEWAY_START_SCRIPT
  chmod 744 $MQGATEWAY_START_SCRIPT

  curl -o /lib/systemd/system/mqgateway.service -L "$MQGATEWAY_SERVICE_URL" | tee -a $LOGS_FILE
  systemctl enable mqgateway.service | tee -a $LOGS_FILE
  echo "[MqGateway Installer] Systemd mqgateway.service ready" | tee -a $LOGS_FILE
}

echo "[MqGateway Installer] Installing MqGateway $MQGATEWAY_VERSION..."

configureNanoPiNeo
prepareWiringNP
createDirectories
prepareBasicConfig
installJava
downloadMqGateway
prepareService

printf "\n\n\n"
echo "[MqGateway Installer] MqGateway installed successfully" | tee -a $LOGS_FILE
echo "[MqGateway Installer] It is time to adjust configuration in file $MQGATEWAY_DIR/gateway.yaml" | tee -a $LOGS_FILE
echo "[MqGateway Installer] When your configuration is ready - restart NanoPi NEO - MqGateway will start automatically" | tee -a $LOGS_FILE