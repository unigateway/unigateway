
## What do you need
- Raspberry Pi or [MqGateway](https://mqgateway.com) with NanoPI NEO
- SD card (minimum 8GB)

## Installation steps

[//]: # (TODO test this part - especially unigateway.local:8080/ui)
1. Download latest UniGateway SD card image for your hardware: 
    - [MqGateway](https://github.com/unigateway/unigateway/releases/latest/download/UniGateway_SDCardImage_MqGateway.img.xz)
    - [Raspberry Pi](https://github.com/unigateway/unigateway/releases/latest/download/UniGateway_SDCardImage_RaspberryPi.img.xz)

2. Flash SD card with downloaded image (you can use [Balena Etcher](https://www.balena.io/etcher/))
3. Power up your hardware and wait until it has started
4. Open browser and go to [http://unigateway.local:8080/ui](http://unigateway.local:8080/ui) to check it works

## After installation

You can configure UniGateway and add devices by changing the configuration file on the device.  
Configuration file with the devices is stored in `/opt/unigateway/gateway.yaml` by default.

You can now read more about [what kind of devices you can configure](supported-devices.md) and [how the configuration works](configuration.md).


## What's next?

- [Quick start on Raspberry Pi](quick-start-raspberrypi.md)
- [Quick start on MqGateway](quick-start-mqgateway.md)
- [Supported devices and configuration](supported-devices.md)
