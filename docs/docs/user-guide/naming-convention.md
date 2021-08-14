**Homie device** - same as MqGateway - it is not the same as "device" (see below)

**Device** - sensor or controller connected to MqGateway (e.g. relay module, switch button or MySensors RS485 node)

**MySensors RS485 node** - Additional controller (e.g. Arduino Pro Mini) used to provide communication with MqGateway, for devices which are using other communication channels than simple digital wire (e.g. serial, analog, I2C or 1-Wire). See [MySensors page](https://www.mysensors.org/) for more information. 

**Point** - each place connected to MqGateway with single ethernet/UTP cable, can have many devices connected to it

**Room** - space containing multiple points, it helps divide configuration into logical parts, but has currently no other influence on how MqGateway works (e.g. kitchen which has multiple light switches)

**Wire** - single wire of UTP cable which may be used to connect one digital device or MySensors RS485 node (requires two wires).
