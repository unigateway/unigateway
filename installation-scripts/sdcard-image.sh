#!/bin/bash

### This script changes clean Armbian image to include UniGateway and other required applications (JRE, MySensors Gateway)
### It should be run on Linux machine with sudo
### Tested on Ubuntu 20.04 LTS

set -euo pipefail

function usage() {
    echo "Usage: sudo $0 --system <mqgateway|raspberrypi>"
    echo "               [-w | --working-dir <path:.>]"
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

if ! command -v parted &> /dev/null
then
    echo "Error: parted is not installed"
    exit 1
fi

if ! command -v kpartx &> /dev/null
then
    echo "Error: kpartx is not installed"
    exit 1
fi

SYSTEM=mqgateway
SCRIPT_DIR=$( cd -- "$( dirname -- "${BASH_SOURCE[0]}" )" &> /dev/null && pwd )
WORKING_DIR=.

POSITIONAL_ARGS=()

while [[ $# -gt 0 ]]; do
  case $1 in
    -s|--system)
      SYSTEM="$2"
      shift 2
      ;;
    -w|--working-dir)
      WORKING_DIR="$2"
      shift 2
      ;;
    -h|--help)
      usage
      exit 0
      ;;
     -*)
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

IMG_MOUNT_PATH="/mnt/armbian"
PARTITION_SIZE=5000M

JAVA_DOWNLOAD_URL="https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.16%2B8/OpenJDK11U-jre_arm_linux_hotspot_11.0.16_8.tar.gz"

UNIGATEWAY_JAR_DOWNLOAD_URL="https://github.com/unigateway/unigateway/releases/latest/download/unigateway.jar"
UNIGATEWAY_JAR_FILE_PATH="unigateway.jar"
UNIGATEWAY_BASE_CONFIG_FILE_PATH="$SCRIPT_DIR/$SYSTEM/gateway.yaml"
UNIGATEWAY_START_SCRIPT_FILE_PATH="$SCRIPT_DIR/$SYSTEM/start_unigateway.sh"
UNIGATEWAY_SERVICE_FILE_PATH="$SCRIPT_DIR/unigateway.service"

MYSENSORS_DOWNLOAD_URL="http://downloads.unigateway.io/$SYSTEM/mysgw"
MYSENSORS_BINARY_FILE_PATH="mysgw"
MYSENSORS_SERVICE_FILE_PATH="mysgw.service"

if [ "$SYSTEM" = "mqgateway" ]; then
  OS_IMAGE_DOWNLOAD_URL="https://redirect.armbian.com/nanopineo/Jammy_current"
elif [ $SYSTEM = "raspberrypi" ]; then
  OS_IMAGE_DOWNLOAD_URL="https://redirect.armbian.com/rpi4b/Jammy_current"
fi

mkdir -p "$WORKING_DIR"
cd "$WORKING_DIR" || exit
echo "Working dir set to: $PWD"

echo "Downloading OS image from: $OS_IMAGE_DOWNLOAD_URL"
wget "$OS_IMAGE_DOWNLOAD_URL" -O armbian.img.xz

echo "Unpacking image"
unxz armbian.img.xz

echo "Mounting image to /mnt/armbian"
partition_number="$(parted -ms armbian.img UNIT b print | tail -n 1 | cut -d ':' -f 1)"
mkdir -p $IMG_MOUNT_PATH
kpartx -v -a armbian.img
loop_device=$(losetup -J -O name,back-file | jq '.loopdevices[] | select(."back-file"|test("armbian.img")) | .name' -r)
loop_device_partition="/dev/mapper/${loop_device/\/dev\//}p${partition_number}"
mount "$loop_device_partition" $IMG_MOUNT_PATH

echo "Resizing image partition"

fallocate -l $PARTITION_SIZE armbian.img
losetup -c "$loop_device"

parted --script ./armbian.img resizepart "$partition_number" 100%FREE

partprobe -s "$loop_device"

echo "Re-mounting partition to be sure it is possible to resize partition"
umount $IMG_MOUNT_PATH
kpartx -d -v armbian.img
kpartx -v -a armbian.img
mount "$loop_device_partition" $IMG_MOUNT_PATH

echo "Resizing partition to maximum image size"
resize2fs "$loop_device_partition"

echo "Image partition resized"
df -h | grep $IMG_MOUNT_PATH

echo "Installing Java"
mkdir -p $IMG_MOUNT_PATH/opt/java
wget "$JAVA_DOWNLOAD_URL" -O jre.tar.gz
tar -xzvf jre.tar.gz -C $IMG_MOUNT_PATH/opt/java --strip-components=1

echo "Download UniGateway JAR"
wget "$UNIGATEWAY_JAR_DOWNLOAD_URL" -O "$UNIGATEWAY_JAR_FILE_PATH"

echo "Installing UniGateway"
mkdir -p $IMG_MOUNT_PATH/opt/unigateway
mkdir -p $IMG_MOUNT_PATH/opt/unigateway/logs
cp $UNIGATEWAY_JAR_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/unigateway.jar
cp $UNIGATEWAY_BASE_CONFIG_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/gateway.yaml
cp $UNIGATEWAY_START_SCRIPT_FILE_PATH $IMG_MOUNT_PATH/opt/unigateway/start_unigateway.sh
cp $UNIGATEWAY_SERVICE_FILE_PATH $IMG_MOUNT_PATH/etc/systemd/system/unigateway.service

echo "Download MySensors binary"
wget "$MYSENSORS_DOWNLOAD_URL" -O "$MYSENSORS_BINARY_FILE_PATH"

echo "Installing MySensors"
cp $MYSENSORS_BINARY_FILE_PATH $IMG_MOUNT_PATH/usr/local/bin/mysgw
cp $MYSENSORS_SERVICE_FILE_PATH $IMG_MOUNT_PATH/etc/systemd/system/mysgw.service


echo "Enable UniGateway and MySensors services"
chroot $IMG_MOUNT_PATH /bin/bash <<'EOT'
ln -s /etc/systemd/system/unigateway.service /etc/systemd/system/multi-user.target.wants/unigateway.service
ln -s /etc/systemd/system/mysgw.service /etc/systemd/system/multi-user.target.wants/mysgw.service
EOT

if [ "$SYSTEM" = "mqgateway" ]; then
  echo "Enable ports on MqGateway"
  sed -i 's/overlays=.*/overlays=i2c0 uart1 usbhost1 usbhost2/' $IMG_MOUNT_PATH/boot/armbianEnv.txt
fi

echo "Change hostname to unigateway"
echo "unigateway" > $IMG_MOUNT_PATH/etc/hostname

echo "Unmounting image"
umount -l $IMG_MOUNT_PATH
kpartx -d -v armbian.img

echo "Packaging image"
xz armbian.img

echo "Image prepared successfully: $PWD/armbian.img.xz"
