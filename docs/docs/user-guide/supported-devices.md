
This is a list of devices currently supported by UniGateway. That means you can connect a device with wires to UniGateway and control it.

!!! danger
    Electricity can be very harmful!    
    Please do not connect anything to electricity yourself if you don't have enough knowledge, 
    and you are not completely sure what you are doing.   

### Simple devices

#### Relay module
Relay module which allows turning on/off electrical devices and lights. Controlled with LOW signal by default.

- configuration type: `RELAY`
- additional configuration:
    - `triggerLevel` [one of: **LOW**, HIGH] - The signal LEVEL which triggers the relay and closes the circuit
    - `haComponent` [one of: **switch**, light] - type of the entity for Home Assistant MQTT discovery
    - `haDeviceClass` [one of: **switch**, outlet] - only if `haComponent` is `switch`, [device class of switch](https://www.home-assistant.io/integrations/switch/#device-class) for Home Assistant MQTT discovery
  
<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "workshop_light"
    type: RELAY
    name: "Workshop light"
    connectors:
      state: # connector configuration example for RaspberryPi
        gpio: 23
    config:
      triggerLevel: "LOW"
      haComponent: "light"
```
</details>

??? example "Wiring"
    Both solid-state (SSR) relay and electromechanical (EMR) relay module, working with correct voltage for used hardware, can be used.  
    Any digital output pin can be used for communication.


#### Switch button
Switch button or push button.

- configuration type: `SWITCH_BUTTON`
- additional configuration:
    - `longPressTimeMs` [default: 1000] - if switch button is pressed for longer then this value (in milliseconds) then "long press" event is sent from MqGateway 
    - `haComponent` [one of: **binary_sensor**, trigger, sensor] - type of the entity for Home Assistant MQTT discovery

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "workshop_light_switch"
    type: SWITCH_BUTTON
    name: "Workshop light switch"
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 1
        wireColor: "BLUE_WHITE"
    config:
      longPressTimeMs: 800
      haComponent: "sensor"
```
</details>

??? example "Wiring"
    Any regular switch/push button can be used for that.  
    Any digital input pin can be used for communication.


#### Emulated switch
Emulates switch button press with changing digital output from HIGH to LOW for 500ms and back to HIGH state. It is useful for controlling devices 
which has possibility to connect switch button (e.g. garage gate opener).

- configuration type: `EMULATED_SWITCH`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "garage_gate_switch"
    type: EMULATED_SWITCH
    name: "Garage gate switch"
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 1
        wires: BLUE_WHITE
```
</details>

??? example "Wiring"
    Emulated switch is realized using electromechanical relay (EMR - the most common type). 
    
    Between UniGateway hardware and the relay any digital output pin can be used for communication.   
    
    Between the relay and gate opener, use COM and NO on the relay. On the gate opener side - connect wires according to your gate opener manual 
    for connecting wall switch button.


#### Motion detector
PIR Motion Sensor/Detector.

- configuration type: `MOTION_DETECTOR`
- additional configuration:
    - `motionSignalLevel` [one of: **HIGH**, LOW] - The signal LEVEL which means that motion has been detected

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "garage_motion"
    type: MOTION_DETECTOR
    name: "Garage motion"
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 4
        wireColor: GREEN
        debounceMs: 100
    config:
      motionSignalLevel: HIGH
```
</details>

??? example "Wiring"
    Any digital input pin can be used for communication.

#### Reed switch

A magnetic sensor which might be used to check if door or window is open/closed.

- configuration type: `REED_SWITCH`
- additional configuration:
    - `haDeviceClass` [one of: **opening**, window, door, garage_door, ...] - choose from [device class of binary sensor](https://www.home-assistant.io/integrations/binary_sensor/#device-class) for Home Assistant MQTT discovery

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "workshop_door"
    type: REED_SWITCH
    name: "Workshop door"
    connectors: 
      state: # connector configuration example for RaspberryPi
        gpio: 24
    config:
      haDeviceClass: "door"
```
</details>

??? example "Wiring"
    Any reed switch can be used for that.  
    Any of the digital input pins can be used.


#### Timer switch

Switch which can be turned on for a given time and will be automatically turned off after that time. Typical use case for this device is a sprinkler system which you want to turn off after specified time even if your main automation system has failed for any reason.

- configuration type: `TIMER_SWITCH`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - id: "sprinklers_zone_1"
    type: TIMER_SWITCH
    name: "Sprinklers zone 1"
    connectors:
      state: # connector configuration example for RaspberryPi
        gpio: 24
```
</details>

??? example "Wiring"
    Both solid-state (SSR) relay and electromechanical (EMR) relay module, working with correct voltage for used hardware, can be used.  
    Any digital output pin can be used for communication.

#### Temperature sensor
Temperature sensor. Examples are BME280, DHT22, DS18B20, SHT31.  
Currently requires MySensors node.

- configuration type: `TEMPERATURE`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - name: "Workshop temperature"
    id: "workshop_temperature"
    type: TEMPERATURE
    connectors:
      state:
        source: MYSENSORS
        nodeId: 10
        sensorId: 1
        type: V_TEMP
```
</details>

#### Humidity sensor
Humidity sensor. Examples are BME280, DHT22, SHT31.  
Currently requires MySensors node.

- configuration type: `HUMIDITY`
- additional configuration: NONE

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - name: "Workshop humidity"
    id: "workshop_humidity"
    type: HUMIDITY
    connectors:
      state:
        source: MYSENSORS
        nodeId: 10
        sensorId: 0
        type: V_HUM
```
</details>


### Complex devices

Complex devices are composed of multiple other devices.

#### Light

Light (controlled by the relay) with one or more switches to toggle it.
Allows to control the light with physical wall switches/buttons even if external automation controller 
(e.g. Home Assistant or OpenHab) is not working.

- configuration type: `LIGHT`
- internal devices:
    - `relay` ([relay](#relay-module)) [required]
    - `switch1` ([switch button](#switch-button)) [optional]
    - `switch2` ([switch button](#switch-button)) [optional]
    - `switch3` ([switch button](#switch-button)) [optional]

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - name: "Living room light"
    id: "living_room_light"
    type: LIGHT
    internalDevices:
      relay:
        referenceId: lr_light_relay
      switch1:
        referenceId: lr_light_switch1
      switch2:
        referenceId: lr_light_switch2
  # Internal devices used by living_room_shutter
  - name: "Living room light relay"
    id: "lr_light_relay"
    type: RELAY
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 11
        wireColor: BLUE
  - name: "Living Room light switch near entrance"
    id: "lr_light_switch1"
    type: RELAY
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 11
        wireColor: BLUE_WHITE
  - name: "Living Room light switch near kitchen"
    id: "lr_light_switch2"
    type: RELAY
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 12
        wireColor: BLUE_WHITE
```
</details>

??? example "Wiring"
    Light is realized using electromechanical (EMR) relay module and optional switch buttons.

    See [relay](#relay-module) and [switch button](#switch-button) devices descriptions to learn how to connect them.


#### Window shutter

Window shutter/roller blind control. Replaces the regular switch button control with 2-channels relay module.
Allows to fully open/close shutters and partial opening with percentage (e.g. 30% open).

- configuration type: `SHUTTER`
- internal devices:
    - `stopRelay` ([relay](#relay-module)) [required]
    - `upDownRelay` ([relay](#relay-module)) [required]
- additional configuration:
    - `fullCloseTimeMs` [required] - time in milliseconds for the shutter from being fully open to fully closed
    - `fullOpenTimeMs` [required] - time in milliseconds for the shutter from being fully closed to fully open

<details>
<summary>Example configuration</summary>

```yaml
devices:
  - name: "Living room shutter"
    id: "living_room_shutter"
    type: SHUTTER
    internalDevices:
      stopRelay:
        referenceId: lr_shutter_stop_relay
      upDownRelay:
        referenceId: lr_shutter_updown_relay
    config:
      fullCloseTimeMs: 20000
      fullOpenTimeMs: 27000
  # Internal devices used by living_room_shutter
  - name: "livingroom left shutter stop relay"
    id: "lr_shutter_stop_relay"
    type: RELAY
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 14
        wireColor: GREEN
  - name: "livingroom left shutter up-down relay"
    id: "lr_shutter_updown_relay"
    type: RELAY
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 14
        wireColor: GREEN_WHITE
```
</details>

??? example "Wiring"
    Window roller shutters are realized using 2-channel electromechanical (EMR) relay module (one relay for going up/down and another one relay for the stop/start).

    Between UniGateway hardware and the 2-channel relay module any digital output pins can be used.
    
    Connect neutral wire and earth directly to the roller shutter wires.
    Between relay module and roller shutter control:
    
    | RELAY 1 CONNECTOR    |                        |
    |----------------------|------------------------|
    | COM                  | Live wire (from grid)  |
    | NO                   | Wire to Relay 2 COM    |
    | NC                   | nothing                |    
    
    | RELAY 2 CONNECTOR    |                           |
    |----------------------|---------------------------|
    | COM                  | Wire from Relay 1 NO      |
    | NO                   | Go up wire from shutter   |
    | NC                   | Go down wire from shutter |


#### Gate / garage door

Garage door or gate (fence) control. Use connection in the gate opener usually used to connect control buttons. It may be eiter single
button (_actionButton_) or three separate buttons (_openButton, closeButton and stopButton_).
It is possible to add open and closed reed switches for better control and position information (_openReedSwitch and closedReedSwitch_).
Emulated switches are realized with relay modules.

- configuration type: `GATE`
- internal devices:
    - `actionButton` ([emulated switch](#emulated-switch)) - either this one or `stopButton`, `openButton` and `closeButton` are required
    - `stopButton` ([emulated switch](#emulated-switch)) - either this one or `actionButton` is required
    - `openButton` ([emulated switch](#emulated-switch)) - either this one or `actionButton` is required
    - `closeButton` ([emulated switch](#emulated-switch)) - either this one or `actionButton` is required
    - `openReedSwitch` ([reed switch](#reed-switch))
    - `closedReedSwitch` ([reed switch](#reed-switch))
- additional configuration:
    - `haDeviceClass` [one of: **garage**, gate] - [device class of cover](https://www.home-assistant.io/integrations/cover/#device-class) for Home Assistant MQTT discovery

<details>
<summary>Example configuration</summary>

Single (open/close/stop) button garage door or gate
```yaml
devices:
  - id: "garage_door"
    type: GATE
    name: "Garage door"
    internalDevices:
      actionButton:
        referenceId: "garage_door_action_button"
      closedReedSwitch:
        referenceId: "garage_door_closed_reed"
  # Internal devices used by garage_door
  - id: "garage_door_action_button"
    name: "Garage door action button"
    type: EMULATED_SWITCH
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 1
        wires: BLUE_WHITE
  - id: "garage_door_closed_reed"
    name: "Garage door closed"
    type: REED_SWITCH
    connectors:
      state: # connector configuration example for MqGateway
        portNumber: 3
        wireColor: GREEN_WHITE
```

Three buttons (open/close/stop) button garage door or gate
```yaml
devices:
  - name: "Entrance gate"
    id: "entrance_gate"
    type: GATE
    internalDevices:
      openButton:
        referenceId: "entrance_gate_open"
      closeButton:
        referenceId: "entrance_gate_close"
      stopButton:
        referenceId: "entrance_gate_stop"
      closedReedSwitch:
        referenceId: "gate_reed_switch"
    config:
      haDeviceClass: gate # "garage" by default
  - name: "Open - entrance gate"
    id: "entrance_gate_open"
    type: EMULATED_SWITCH
    connectors:
      state:
        portNumber: 20
        wireColor: BLUE_WHITE
  - name: "Close - entrance gate"
    id: "entrance_gate_close"
    type: EMULATED_SWITCH
    connectors:
      state:
        portNumber: 20
        wireColor: BLUE
  - name: "Stop - entrance gate"
    id: "entrance_gate_stop"
    type: EMULATED_SWITCH
    connectors:
      state:
        portNumber: 20
        wireColor: GREEN
  - name: "Gate reed switch"
    id: "gate_reed_switch"
    type: REED_SWITCH
    connectors:
      state:
        portNumber: 16
        wireColor: BLUE_WHITE
```
</details>

??? example "Wiring"
    Single button gate (garage door) is realized using electromechanical (EMR) relay module to emulate push button.

    Between UniGateway hardware and the relay module use any digital output pin for control.

    Relay module should be connected with COM and NO to the gate (garage door) opener in the place for connecting push button.  
    
    | RELAY CONNECTOR      |                        |
    |----------------------|------------------------|
    | COM                  | button connection wire |
    | NO                   | button connection wire |
    | NC                   | nothing                |

    Three buttons gate (garage door) is realized using 3-channel electromechanical (EMR) relay module to emulate push button for each function. 
    
    Between UniGateway hardware and the 3-channel relay module use three separate digital output pins for control.

    Each relay module should be connected with COM and NO to the gate (garage door) opener in the place for connecting push button.
    This connection needs to be repeated for each relay - button.
    
    | RELAY 1,2,3 CONNECTOR |                        |
    |-----------------------|------------------------|
    | COM                   | button connection wire |
    | NO                    | button connection wire |
    | NC                    | nothing                |
