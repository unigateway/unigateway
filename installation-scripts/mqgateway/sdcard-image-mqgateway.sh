#!/bin/bash

### This script changes clean Armbian image to include UniGateway and other required applications (JRE, MySensors Gateway)
### It should be run on Linux machine with sudo
### Tested on Ubuntu 20.04 LTS

set -euo pipefail

function usage() {
    echo "Usage: sudo $0 [-w | --working-dir <path:.>] [-o | --os-image-url <os-image-download-url:https://imola.armbian.com/dl/.../...img.xz>]"
    echo "               [-j | --java-url <jre-download-url:https://...temurin11-binaries/...tar.gz>]"
}

if [[ $UID != 0 ]]; then
    echo "Please run this script with sudo:"
    echo "sudo $0 $*"
    usage
    exit 1
fi

if ! command -v wget &> /dev/null
then
    echo "Error: wget (v1.20.3) is not installed"
    exit 1
fi

if ! command -v jq &> /dev/null
then
    echo "Error: jq (v1.6) is not installed"
    exit 1
fi

if ! command -v xz &> /dev/null
then
    echo "Error: xz (v5.2.4) is not installed"
    exit 1
fi

WORKING_DIR=.
OS_IMAGE_DOWNLOAD_URL="https://imola.armbian.com/dl/nanopineo/archive/Armbian_22.05.4_Nanopineo_jammy_current_5.15.48.img.xz"
IMG_MOUNT_PATH="/mnt/armbian"

JAVA_DOWNLOAD_URL="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16%2B8/OpenJDK11U-jre_arm_linux_hotspot_11.0.16_8.tar.gz"

UNIGATEWAY_JAR_FILE_PATH="unigateway.jar"
UNIGATEWAY_BASE_CONFIG_FILE_PATH="gateway.yaml"
UNIGATEWAY_START_SCRIPT_FILE_PATH="start_unigateway.sh"
UNIGATEWAY_SERVICE_FILE_PATH="unigateway.service"

MYSENSORS_BINARY_FILE_PATH="mysgw"
MYSENSORS_SERVICE_FILE_PATH="mysgw.service"

POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    -w|--working-dir)
      WORKING_DIR="$2"
      shift 2
      ;;
    -o|--os-image-url)
      OS_IMAGE_DOWNLOAD_URL="$2"
      shift 2
      ;;
    -j|--java-url)
      JAVA_DOWNLOAD_URL="$2"
      shift 2
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



cd "$WORKING_DIR" || exit
echo "Working dir set to: $PWD"

echo "Downloading OS image from: $OS_IMAGE_DOWNLOAD_URL"
wget "$OS_IMAGE_DOWNLOAD_URL" -O armbian.img.xz

echo "Unpacking image"
unxz armbian.img.xz

echo "Mounting image to /mnt/armbian"
mkdir -p $IMG_MOUNT_PATH
mount -o loop,offset=4194304 armbian.img $IMG_MOUNT_PATH

echo "Resizing image partition"
loop_device=$(losetup -J -O name,back-file | jq '.loopdevices[] | select(."back-file"|test("armbian.img")) | .name' -r)

fallocate -l 2000M armbian.img
losetup -c "$loop_device"

parted <<'EOT'
select ./armbian.img
resizepart 1 100%FREE
quit
EOT

partprobe -s "$loop_device"
resize2fs "$loop_device"

echo "Image partition resized"
df -h | grep $IMG_MOUNT_PATH

echo "Installing Java"
mkdir -p $IMG_MOUNT_PATH/opt/java
wget "$JAVA_DOWNLOAD_URL" -O jre.tar.gz
tar -xzvf jre.tar.gz -C $IMG_MOUNT_PATH/opt/java --strip-components=1

echo "Installing UniGateway"
mkdir -p $IMG_MOUNT_PATH/opt/unigateway
mkdir -p $IMG_MOUNT_PATH/opt/unigateway/logs
cp $UNIGATEWAY_JAR_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/unigateway.jar
cp $UNIGATEWAY_BASE_CONFIG_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/gateway.yaml
cp $UNIGATEWAY_START_SCRIPT_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/start_unigateway.sh
cp $UNIGATEWAY_SERVICE_FILE_PATH $IMG_MOUNT_PATH/etc/systemd/system/unigateway.service

echo "Installing MySensors"
cp $MYSENSORS_BINARY_FILE_PATH $IMG_MOUNT_PATH/usr/local/bin/mysgw
cp $MYSENSORS_SERVICE_FILE_PATH $IMG_MOUNT_PATH/etc/systemd/system/mysgw.service


echo "Enable UniGateway and MySensors services"
chroot $IMG_MOUNT_PATH /bin/bash <<'EOT'
ln -s /etc/systemd/system/unigateway.service /etc/systemd/system/multi-user.target.wants/unigateway.service
ln -s /etc/systemd/system/mysgw.service /etc/systemd/system/multi-user.target.wants/mysgw.service
EOT

echo "Enable ports on MqGateway"
sed -i 's/overlays=.*/overlays=i2c0 uart1 usbhost1 usbhost2/' $IMG_MOUNT_PATH/boot/armbianEnv.txt

echo "Unmounting image"
umount -l /mnt/armbian

echo "Packaging image"
xz armbian.img

echo "Image prepared successfully: $PWD/armbian.img.xz"
