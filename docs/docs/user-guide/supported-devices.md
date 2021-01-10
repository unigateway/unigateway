
This is a list of devices currently supported by MqGateway. That means you can connect a device either with digital wires or through RS485/UART controller to MqGateway.

All supported devices need to be compatible with 5V power supply.


### Digital devices

#### Relay module
Relay module which allows turning on/off electrical devices and lights. Controlled with LOW signal.

- configuration type: `RELAY`
- additional configuration:
    - `haComponent` [one of: **switch**, light] - type of the entity for Home Assistant MQTT discovery
  
<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
- name: "workshop"
  points:
  - name: "point with relay"
    portNumber: 1
    devices:
    - name: "workshop light"
      id: "workshop_light"
      wires: ["BLUE"]
      type: RELAY
      config:
        haComponent: "light"
```
</details>


#### Switch button
Switch button or push button.

- configuration type: `SWITCH_BUTTON`
- additional configuration:
    - `debounceMs` [default: 0] - number of milliseconds for debounce (helps to avoid flickering)
    - `longPressTimeMs` [default: 1000] - if switch button is pressed for longer then this value (in milliseconds) then "long press" event is sent from MqGateway 
    - `haComponent` [one of: **binary_sensor**, trigger, sensor] - type of the entity for Home Assistant MQTT discovery

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "workshop"
    points:
      - name: "point with relay"
      portNumber: 1
      devices:
        - name: "workshop light switch"
          id: "workshop_light_switch"
          wires: ["BLUE_WHITE"]
          type: SWITCH_BUTTON
          config:
            longPressTimeMs: 800
            debounceMs: 50
            haComponent: "sensor"
```
</details>


#### Emulated switch
Emulates switch button press with changing digital output from HIGH to LOW for 500ms and back to HIGH state. It is useful for controlling devices which has possibility to connect switch button (e.g. garage gate opener).

- configuration type: `EMULATED_SWITCH`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "garage"
    points:
      - name: "2-channels relay module"
        portNumber: 1
        devices:
          - name: "Right gate"
            id: "garage_right_gate"
            wires: ["BLUE_WHITE"]
            type: EMULATED_SWITCH
```
</details>

#### Motion detector
PIR Motion Sensor/Detector.

- configuration type: `MOTION_DETECTOR`
- additional configuration:
    - `debounceMs` [default: 0] - number of milliseconds for debounce (helps to avoid flickering)

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "garage"
    points:
      - name: "Right garage entry"
        portNumber: 1
        devices:
          - name: "Garage motion"
            id: "garage_motion"
            wires: ["GREEN"]
            type: MOTION_DETECTOR
            config:
              debounceMs: 100
```
</details>


#### Reed switch

A magnetic sensor which might be used to check if door or window is open/closed.

- configuration type: `REED_SWITCH`
- additional configuration:
  - `debounceMs` [default: 0] - number of milliseconds for debounce (helps to avoid flickering)

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "workshop"
    points:
      - name: "Workshop over door box"
        portNumber: 1
        devices:
          - name: "Workshop door"
            id: "workshop_door"
            wires: ["GREEN"]
            type: REED_SWITCH
            config:
              debounceMs: 100
```
</details>

#### Timer switch

Switch which can be turned on for a given time and will be automatically turned off after that time. Typical use case for this device is a sprinkler system which you want to turn off after specified time even if your main automation system has failed for any reason.

- configuration type: `TIMER_SWITCH`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "garage"
    points:
      - name: "sprinklers box"
        portNumber: 5
        devices:
          - name: "Sprinklers zone 1"
            id: "sprinklers_zone_1"
            wires: ["GREEN_WHITE"]
            type: TIMER_SWITCH
```
</details>

#### Window shutter

Window shutter/roller blind control. Replaces switch button control with 2-channels relay module. 

- configuration type: `SHUTTER`
- additional configuration:
    - `fullCloseTimeMs` [required] - time in milliseconds for the shutter from being fully open to fully closed
    - `fullOpenTimeMs` [required] - time in milliseconds for the shutter from being fully closed to fully open

<details>
<summary>Example configuration</summary>

```yaml
rooms:
  - name: "living room"
    points:
      - name: "shutters controller relays"
        portNumber: 7
        devices:
          - name: "living room shutter"
            id: "living_room_shutter"
            type: SHUTTER
            internalDevices:
              stopRelay:
                name: "living room shutter stop relay"
                id: "lr_shutter_stop_relay"
                type: RELAY
                wires: [ "GREEN" ]
              upDownRelay:
                name: "living room shutter up-down relay"
                id: "lr_shutter_updown_relay"
                type: RELAY
                wires: [ "GREEN_WHITE" ]
            config:
              fullCloseTimeMs: 20000
              fullOpenTimeMs: 27000
```
</details>


### Complex devices with RS485/UART Controller

This group of devices requires RS485/UART controller on the side of the device for communication with MqGateway. This controller might be any microcontroller which is able to communicate through RS485 or UART with 5V.

#### BME280
Temperature, humidity and barometric pressure sensor. Requires RS485/UART controller.

- configuration type: `BME280`
- wires: 
  - to controller wire - used to ask controller for data
  - from controller wire - used to send a ping from controller
- additional configuration:
    - `periodBetweenAskingForDataInSec` [default: 180] - time in seconds between asking controller to send another measurements
    - `acceptablePingPeriodInSec` [default: 60] - maximum time in seconds, between ping calls from the controller, after which sensor will be considered unavailable

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "Kids room"
    points:
      - name: "Kids room measurement box"
        portNumber: 14
        devices:
          - name: "Kids room box"
            id: "bme280_kids_room"
            wires: ["GREEN_WHITE", "GREEN"]
            type: BME280
            config:
              periodBetweenAskingForDataInSec: 30
              acceptablePingPeriodInSec: 120
```
</details>

#### DHT22
Temperature and humidity sensor. Requires RS485/UART controller.

- configuration type: `DHT22`
- wires:
  - to controller wire - used to ask controller for data
  - from controller wire - used to send a ping from controller
- additional configuration:
  - `periodBetweenAskingForDataInSec` [default: 180] - time in seconds between asking controller to send another measurements
  - `acceptablePingPeriodInSec` [default: 60] - maximum time in seconds, between ping calls from the controller, after which sensor will be considered unavailable

<details>
<summary>Example configuration</summary>

```yaml
[...]
rooms:
  - name: "Kids room"
    points:
      - name: "Kids room measurement box"
        portNumber: 14
        devices:
          - name: "Kids room box"
            id: "bme280_kids_room"
            wires: ["GREEN_WHITE", "GREEN"]
            type: DHT22
            config:
              periodBetweenAskingForDataInSec: 30
              acceptablePingPeriodInSec: 120
```
</details>