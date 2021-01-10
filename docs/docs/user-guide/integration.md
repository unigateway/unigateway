
This page describes how to configure home automation applications (like Home Assistant and OpenHab) to integrate with them and control devices connected to MqGateway.

## Home Assistant

The easiest way to integrate MqGateway is to use [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/). MqGateway will automatically broadcast all devices' configuration under "homeassistant/" topic when connected to MQTT broker.
All you need to do is to enable MQTT integration in Home Assistant as [described in the HA documentation](https://www.home-assistant.io/integrations/mqtt/).

After MqGateway start, you should be able to see all the MqGateway configured devices as devices and entities in Home Assistant.

If you want to disable broadcasting Home Assistant configuration to MQTT, set environment variable `HOMEASSISTANT_ENABLED=false` on NanoPi NEO.


## OpenHab

[OpenHab MQTT Binding](https://www.openhab.org/addons/bindings/mqtt/) supports [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/). MqGateway will automatically broadcast all devices' configuration under "homeassistant/" topic when connected to MQTT broker.

If you want to disable broadcasting Home Assistant configuration to MQTT, set environment variable `HOMEASSISTANT_ENABLED=false` on NanoPi NEO.

Although OpenHab supports Homie convention 3.0.1 which is followed by MqGateway - using Home Assistant MQTT discovery gives more detailed configuration automatically.


## General communication

MqGateway works as the MQTT gateway. This means you can integrate it to any home automation platform which supports MQTT devices. Additionally, it supports [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/) feature which is also supported in [OpenHab MQTT binding](https://www.openhab.org/addons/bindings/mqtt/).

MqGateway follows [Homie convention](https://homieiot.github.io/) 3.0.1 for MQTT communication.
Homies MQTT topic layout follows the pattern **homie/device/node/property**.

**Device** is a name of your MqGateway (see how to set name in [configuration](configuration.md)).

**Node** is a unique string identifier of the sensor/controller connected to MqGateway (*id* in [configuration](configuration.md)).

**Property** can be a "temperature" when reading value from BME280 module or "state" in case of PIR motion sensor.

Examples:

- `homie/MyGateway/workshop_bme/temperature` for reading temperature from BME280 on the id = "workshop_bme"
- `homie/MyGateway/kitchen_bme/humidity` for reading humidity from BME280 on the id = "kitchen_bme"
- `homie/MyGateway/wardrobe_motion/state` for reading motion sensor state on the id = "wardrobe_motion" (`ON` if motion has started, `OFF` otherwise)

When device supports setting a value (e.g. relay), desired value should be send to *homie/device/node/property/set* topic

Example:

- send `ON` to `homie/MyGateway/wordrobe_light/state/set` to switch on light connected to relay on id = "wardrobe_light"









