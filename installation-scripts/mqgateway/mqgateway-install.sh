#!/bin/bash

### This script installs JRE and UniGateway with basic configuration on MqGateway
### It has been prepared to be run directly on MqGateway with Armbian OS

set -euo pipefail

function usage() {
    echo "Usage: sudo $0 [-v | --version <unigateway-version:latest-stable>]"
}

# Make sure only root can run this script
if [[ $UID != 0 ]]; then
    echo "Please run this script with sudo:"
    echo "sudo $0 $*"
    usage
    exit 1
fi

if ! command -v curl &> /dev/null
then
    echo "Error: curl is not installed"
    exit 1
fi

JAVA_DOWNLOAD_URL=https://download.bell-sw.com/java/21.0.7+9/bellsoft-jdk21.0.7+9-linux-arm32-vfp-hflt.tar.gz
JAVA_DESTINATION_PATH=/opt/java

UNIGATEWAY_VERSION="latest-stable"
UNIGATEWAY_APP_DOWNLOAD_URL="https://unigateway.io/releases/${UNIGATEWAY_VERSION}/unigateway-${UNIGATEWAY_VERSION}-all.jar"
UNIGATEWAY_CONFIG_DOWNLOAD_URL="https://unigateway.io/releases/${UNIGATEWAY_VERSION}/mqgateway/gateway.yaml"
UNIGATEWAY_SERVICE_DOWNLOAD_URL="https://unigateway.io/releases/${UNIGATEWAY_VERSION}/mqgateway/unigateway.service"
UNIGATEWAY_START_SCRIPT_DOWNLOAD_URL="https://unigateway.io/releases/${UNIGATEWAY_VERSION}/mqgateway/start_unigateway.sh"

# For testing only
UNIGATEWAY_APP_DOWNLOAD_URL="https://fileserver.aetas.pl/releases/${UNIGATEWAY_VERSION}/unigateway-${UNIGATEWAY_VERSION}-all.jar"
UNIGATEWAY_CONFIG_DOWNLOAD_URL="https://fileserver.aetas.pl/releases/${UNIGATEWAY_VERSION}/mqgateway/gateway.yaml"
UNIGATEWAY_SERVICE_DOWNLOAD_URL="https://fileserver.aetas.pl/releases/${UNIGATEWAY_VERSION}/mqgateway/unigateway.service"
UNIGATEWAY_START_SCRIPT_DOWNLOAD_URL="https://fileserver.aetas.pl/releases/${UNIGATEWAY_VERSION}/mqgateway/start_unigateway.sh"


UNIGATEWAY_DIR=/opt/unigateway
UNIGATEWAY_JAR_DESTINATION_PATH=$UNIGATEWAY_DIR/unigateway.jar
UNIGATEWAY_START_SCRIPT_DESTINATION_PATH=$UNIGATEWAY_DIR/start_unigateway.sh

ARMBIAN_CONFIG_FILE=/boot/armbianEnv.txt

MYSENSORS_ENABLED=false # TODO this should enable mysensors in start_unigateway.sh somehow
MYSENSORS_DIR=/opt/mysensors
MYSENSORS_BINARY_DESTINATION_PATH="$MYSENSORS_DIR/mysgw"
MYSENSORS_BINARY_DOWNLOAD_URL="https://fileserver.aetas.pl/releases/${UNIGATEWAY_VERSION}/mqgateway/mysgw"
MYSENSORS_SERVICE_DOWNLOAD_URL=


LOGS_FILE=$UNIGATEWAY_DIR/install.log

POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    -v|--version)
      UNIGATEWAY_VERSION="$2"
      shift 2
      ;;
    -m|--my-sensors)
      MYSENSORS_ENABLED=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
     -*|--*)
      echo "Unknown option: $1"
      usage
      exit 1
      ;;
    *)
      POSITIONAL_ARGS+=("$1") # save positional arg
      shift
      ;;
  esac
done

set -- "${POSITIONAL_ARGS[@]}" # restore positional parameters


function createDirectories {
  echo "[UniGateway Installer] Prepare directories for UniGateway..."
  [ ! -d "$UNIGATEWAY_DIR" ] && mkdir -p $UNIGATEWAY_DIR/logs && touch $LOGS_FILE
}

function configureNanoPiNeo {
  echo "[UniGateway Installer] Configuring hardware of NanoPi NEO..." |& tee -a "$LOGS_FILE"
  sed -i 's/overlays=.*/overlays=i2c0 uart1 usbhost1 usbhost2/' $ARMBIAN_CONFIG_FILE
  echo "[UniGateway Installer] NanoPi NEO hardware configured" |& tee -a "$LOGS_FILE"
}

function prepareBasicGatewayConfig {
  echo "[UniGateway Installer] Prepare base configuration file for UniGateway..." |& tee -a "$LOGS_FILE"
  curl -o $UNIGATEWAY_DIR/gateway.yaml -L "$UNIGATEWAY_CONFIG_DOWNLOAD_URL"
  echo "[UniGateway Installer] gateway.yaml prepared" |& tee -a "$LOGS_FILE"
}

function installJava {
  echo "[UniGateway Installer] Installing Java..." |& tee -a "$LOGS_FILE"
  mkdir -p $JAVA_DESTINATION_PATH
  wget "$JAVA_DOWNLOAD_URL" -O jre.tar.gz
  tar -xzvf jre.tar.gz -C $JAVA_DESTINATION_PATH --strip-components=1
  rm jre.tar.gz
  echo "[UniGateway Installer] Java installed in $JAVA_DESTINATION_PATH" |& tee -a "$LOGS_FILE"
}

function downloadUniGateway {
  echo "[UniGateway Installer] Downloading UniGateway application..." |& tee -a "$LOGS_FILE"
  curl -o $UNIGATEWAY_JAR_DESTINATION_PATH -L $UNIGATEWAY_APP_DOWNLOAD_URL
  chmod 644 $UNIGATEWAY_JAR_DESTINATION_PATH
  echo "[UniGateway Installer] UniGateway application downloaded" |& tee -a "$LOGS_FILE"
}

function prepareStartScript {
  echo "[UniGateway Installer] Preparing UniGateway start script..." |& tee -a "$LOGS_FILE"
  curl -o $UNIGATEWAY_START_SCRIPT_DESTINATION_PATH -L $UNIGATEWAY_START_SCRIPT_DOWNLOAD_URL
  chmod 744 $UNIGATEWAY_START_SCRIPT_DESTINATION_PATH
  echo "[UniGateway Installer] UniGateway start script prepared" |& tee -a "$LOGS_FILE"
}

function prepareUniGatewayService {
  echo "[UniGateway Installer] Preparing systemd unigateway.service..." |& tee -a "$LOGS_FILE"
  curl -o /lib/systemd/system/unigateway.service -L "$UNIGATEWAY_SERVICE_DOWNLOAD_URL" |& tee -a "$LOGS_FILE"
  systemctl enable unigateway.service |& tee -a "$LOGS_FILE"
  echo "[UniGateway Installer] Systemd unigateway.service ready" |& tee -a "$LOGS_FILE"
}

function downloadMySensorsBinary {
  echo "[UniGateway Installer] Downloading MySensors binary..." |& tee -a "$LOGS_FILE"
  curl -o $MYSENSORS_BINARY_DESTINATION_PATH -L $MYSENSORS_BINARY_DOWNLOAD_URL
  chmod 744 $MYSENSORS_BINARY_DESTINATION_PATH
  echo "[UniGateway Installer] MySensors binary downloaded..." |& tee -a "$LOGS_FILE"
}

function prepareMySensorsService {
  echo "[UniGateway Installer] Preparing systemd mysgw.service..." |& tee -a "$LOGS_FILE"
  curl -o /lib/systemd/system/mysgw.service -L "$MYSENSORS_SERVICE_DOWNLOAD_URL" |& tee -a "$LOGS_FILE"
  systemctl enable mysgw.service |& tee -a "$LOGS_FILE"
  systemctl start mysgw.service |& tee -a "$LOGS_FILE"
  echo "[UniGateway Installer] Systemd mysgw.service ready" |& tee -a "$LOGS_FILE"
}

echo "[UniGateway Installer] Installing UniGateway $UNIGATEWAY_VERSION..."

createDirectories
configureNanoPiNeo
prepareBasicGatewayConfig
installJava
downloadUniGateway
prepareStartScript
prepareUniGatewayService

if $MYSENSORS_ENABLED; then
  downloadMySensorsBinary
  prepareMySensorsService
fi

# TODO - test if it does work


printf "\n\n\n"
echo "[UniGateway Installer] UniGateway installed successfully" |& tee -a "$LOGS_FILE"
echo "[UniGateway Installer] It is time to adjust configuration in file $UNIGATEWAY_DIR/gateway.yaml" |& tee -a "$LOGS_FILE"
echo "[UniGateway Installer] When your configuration is ready - restart NanoPi NEO - UniGateway will start automatically" |& tee -a "$LOGS_FILE"
