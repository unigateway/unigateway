
This page describes how to configure home automation applications (like Home Assistant and OpenHab) to integrate with them and control devices connected to UniGateway.

## Home Assistant

The easiest way to integrate UniGateway is to use [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/). 
UniGateway will automatically broadcast all devices' configuration under "homeassistant/" topic when connected to MQTT broker.
All you need to do is to:

  - enable MQTT in UniGateway by setting environment variable `GATEWAY_MQTT_ENABLED` to `true` (more about configuration [here](configuration.md#mqtt))
  - enable MQTT integration in Home Assistant as [described in the Home Assistant documentation](https://www.home-assistant.io/integrations/mqtt/).

After UniGateway start, you should be able to see all the UniGateway configured devices as devices and entities in Home Assistant.

If you want to disable broadcasting the Home Assistant configuration to MQTT, set environment variable `HOMEASSISTANT_ENABLED` to `false` (see [system configuration](configuration.md#system-configuration) for details). 

#### Home Assistant device configuration

The following per-device `config` options can be used to fine-tune how devices appear in Home Assistant:

| Config key      | Description                                                                                                                                                | Example               |
|-----------------|------------------------------------------------------------------------------------------------------------------------------------------------------------|-----------------------|
| `haComponent`   | Override the Home Assistant entity type (e.g. `light`, `switch`, `sensor`, `trigger`)                                                                      | `light`               |
| `haDeviceClass` | Override the device class (e.g., [for sensor](https://www.home-assistant.io/integrations/sensor/#device-class))                                            | `door`, `temperature` |
| `haEntityName`  | Custom entity name visible in Home Assistant                                                                                                               | `My light`            |
| `area`          | Assign the device to a Home Assistant [area](https://www.home-assistant.io/docs/organizing/areas/) automatically (sets `suggested_area` in MQTT discovery) | `Living Room`         |

All of these are optional. Example usage:

```yaml
devices:
  - id: "living_room_light"
    name: "Living room light"
    type: RELAY
    connectors:
      state:
        gpio: 17
    config:
      haComponent: "light"
      area: "Living Room"
```


## OpenHab

[OpenHab MQTT Binding](https://www.openhab.org/addons/bindings/mqtt/) supports [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/). UniGateway will automatically broadcast all devices' configuration under "homeassistant/" topic when connected to MQTT broker.

If you want to disable broadcasting Home Assistant configuration to MQTT, set environment variable `HOMEASSISTANT_ENABLED` to `false` (see [system configuration](configuration.md#system-configuration) for details).

Although OpenHab supports Homie convention 3.0.1 which is followed by UniGateway - using Home Assistant MQTT discovery gives more detailed configuration automatically.


## General communication

UniGateway can work as the MQTT gateway. This means you can integrate it to any home automation platform which supports MQTT devices. Additionally, it supports [Home Assistant MQTT discovery](https://www.home-assistant.io/docs/mqtt/discovery/) feature which is also supported in [OpenHab MQTT binding](https://www.openhab.org/addons/bindings/mqtt/).

UniGateway follows [Homie convention](https://homieiot.github.io/) 3.0.1 for MQTT communication.
Homies MQTT topic layout follows the pattern **homie/device/node/property**.

**Device** is a name of your UniGateway instance (see how to set name in [configuration](configuration.md)).

**Node** is a unique string identifier of the sensor/controller connected to UniGateway (*id* in [configuration](configuration.md)).

**Property** can be a "temperature" when reading value from temperature sensor module or "state" in case of PIR motion sensor.

Examples:

- `homie/MyGateway/workshop_bme/temperature` for reading temperature from BME280 on the id = "workshop_bme"
- `homie/MyGateway/kitchen_bme/humidity` for reading humidity from BME280 on the id = "kitchen_bme"
- `homie/MyGateway/wardrobe_motion/state` for reading motion sensor state on the id = "wardrobe_motion" (`ON` if motion has started, `OFF` otherwise)

When device supports setting a value (e.g. relay), desired value should be sent to *homie/device/node/property/set* topic

Example:

- send `ON` to `homie/MyGateway/wordrobe_light/state/set` to switch on light connected to relay on id = "wardrobe_light"









