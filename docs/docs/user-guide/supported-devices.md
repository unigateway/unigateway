
This is a list of devices currently supported by MqGateway. That means you can connect a device either with digital wires or through MySensors RS485 node to MqGateway.

All supported devices need to be compatible with 5V power supply.

!!! danger
    Electricity can be very harmful!    
    Please do not connect anything to electricity yourself if you don't have enough knowledge, 
    and you are not completely sure what you are doing.   
    
!!! warning
    Information below has been provided as a pointer. I've tried to prepare it as accurately as I was able to. However, it may contain errors.
    Always check the device manual and do not connect electrical wires if you are not absolutely sure you know what you're doing.
    


### Digital devices

#### Relay module
Relay module which allows turning on/off electrical devices and lights. Controlled with LOW signal by default.

- configuration type: `RELAY`
- additional configuration:
    - `triggerLevel` [one of: **LOW**, HIGH] - The signal LEVEL which triggers the relay and closes the circuit
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
        triggerLevel: "HIGH"
        haComponent: "light"
```
</details>

??? example "Wiring"
    Both solid-state (SSR) relay and electromechanical (EMR) relay module working with 5V power can be used. Relay should be controlled with LOW signal.  
    Any digital wire can be used for communication: blue, blue-white, green, green-white.
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN        |


#### Switch button
Switch button or push button.

- configuration type: `SWITCH_BUTTON`
- additional configuration:
    - `debounceMs` [default: 50] - number of milliseconds for debounce (helps to avoid flickering)
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
            debounceMs: 80
            haComponent: "sensor"
```
</details>

??? example "Wiring"
    Any regular switch/push button can be used for that.  
    Orange-white wire (GND) and one of the digital wires (blue, blue-white, green, green-white) should be used.


#### Emulated switch
Emulates switch button press with changing digital output from HIGH to LOW for 500ms and back to HIGH state. It is useful for controlling devices 
which has possibility to connect switch button (e.g. garage gate opener).

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

??? example "Wiring"
    Emulated switch is realized using electromechanical relay (EMR - the most common type). 
    
    Between MqGateway and the relay:
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN        |
    
    
    Between the relay and gate opener, use COM and NO on the relay. On the gate opener side - connect wires according to your gate opener manual 
    for connecting wall switch button.


#### Motion detector
PIR Motion Sensor/Detector.

- configuration type: `MOTION_DETECTOR`
- additional configuration:
    - `debounceMs` [default: 50] - number of milliseconds for debounce (helps to avoid flickering)
    - `motionSignalLevel` [one of: **HIGH**, LOW] - The signal LEVEL which means that motion has been detected

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
              motionSignalLevel: LOW
```
</details>

??? example "Wiring"
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | OUT       |

#### Reed switch

A magnetic sensor which might be used to check if door or window is open/closed.

- configuration type: `REED_SWITCH`
- additional configuration:
  - `debounceMs` [default: 50] - number of milliseconds for debounce (helps to avoid flickering)

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

??? example "Wiring"
    Any reed switch can be used for that.  
    Orange-white wire (GND) and one of the digital wires (blue, blue-white, green, green-white) should be used.


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

??? example "Wiring"
    Both solid-state (SSR) relay and electromechanical (EMR) relay module working with 5V power can be used. Relay should be controlled with LOW signal.  
    Any digital wire can be used for communication: blue, blue-white, green, green-white.
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN        |

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

??? example "Wiring"
    Window roller shutters are realized using 2-channel electromechanical (EMR) relay module (one relay for going up/down and 
    another one relay for the stop/start). Relays should be controlled with LOW signal.
    
    Between MqGateway and the 2-channel relay module:
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN1       |
    | [any-digital](../hardware/wiring.md) | IN2       |
    
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
    - `haDeviceClass` [one of: **garage**, gate]  - [device class of cover](https://www.home-assistant.io/integrations/cover/#device-class) for Home Assistant MQTT discovery

<details>
<summary>Example configuration</summary>

```yaml
rooms:
  - name: "garage"
    points:
      - name: "garage door"
        portNumber: 4
        devices:
          - name: "Right garage door"  # single button device example
            id: "right_garage_door"
            type: GATE
            internalDevices:
              actionButton:
                name: "Right garage door action button"
                id: "right_garage_door_action_button"
                wires: ["BLUE_WHITE"]
                type: EMULATED_SWITCH
              openReedSwitch: # optional
                name: "Right garage door open reed switch"
                id: "right_garage_door_open_reed"
                wires: [ "GREEN" ]
                type: REED_SWITCH
                config:
                  debounceMs: 50
              closedReedSwitch: # optional
                name: "Right garage door closed reed switch"
                id: "right_garage_door_closed_reed"
                wires: [ "GREEN_WHITE" ]
                type: REED_SWITCH
                config:
                  debounceMs: 50
  - name: outdoor
    points:
      - name: "Entrance gate"
        portNumber: 20
        devices:
          - name: "Entrance gate" # three buttons device example
            id: "entrance_gate"
            type: GATE
            internalDevices:
              openButton:
                name: "Open - entrance gate"
                id: "entrance_gate_open"
                wires: [ "BLUE_WHITE" ]
                type: EMULATED_SWITCH
              closeButton:
                name: "Close - entrance gate"
                id: "entrance_gate_close"
                wires: [ "BLUE" ]
                type: EMULATED_SWITCH
              stopButton:
                name: "Stop - entrance gate"
                id: "entrance_gate_stop"
                wires: [ "GREEN" ]
                type: EMULATED_SWITCH
              closedReedSwitch: # optional, can be also replaced with openReedSwitch
                name: "Closed gate reed switch"
                id: "entrance_gate_closed_reed_switch"
                wires: ["GREEN_WHITE"]
                type: REED_SWITCH
        config:
          haDeviceClass: gate # "garage" by default
```
</details>

??? example "Wiring"
    Single button gate (garage door) is realized using electromechanical (EMR) relay module to emulate push button.
    Relay should be controlled with LOW signal.

    Between MqGateway and the relay module:
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN        |

    Relay module should be connected with COM and NO to the gate (garage door) opener in the place for connecting push button.  
    
    | RELAY CONNECTOR      |                        |
    |----------------------|------------------------|
    | COM                  | button connection wire |
    | NO                   | button connection wire |
    | NC                   | nothing                |

    Three buttons gate (garage door) is realized using 3-channel electromechanical (EMR) relay module to emulate push button for each function. 
    Relay module should be controlled with LOW signal.
    
    Between MqGateway and the 3-channel relay module:
    
    | WIRE COLOR                           | RELAY PIN |
    |--------------------------------------|-----------|
    | orange (5V)                          | VCC / D+  |
    | orange-white (GND)                   | GND / D-  |
    | [any-digital](../hardware/wiring.md) | IN1       |
    | [any-digital](../hardware/wiring.md) | IN2       |
    | [any-digital](../hardware/wiring.md) | IN3       |

    Each relay module should be connected with COM and NO to the gate (garage door) opener in the place for connecting push button.
    This connection needs to be repeated for each relay - button.
    
    | RELAY 1,2,3 CONNECTOR |                        |
    |-----------------------|------------------------|
    | COM                   | button connection wire |
    | NO                    | button connection wire |
    | NC                    | nothing                |


### MySensors devices/nodes with RS485 Controller

This group of devices requires RS485 MySensors node on the side of the device for communication with MqGateway. This controller might be 
any microcontroller which is able to communicate through RS485 with 5V. Common choice is the Arduino Pro Mini board. 

More about how to connect and use MySensors devices with RS485 node can be found in [MySensors node page](../hardware/mysensors-node.md)  

??? example "Wiring"
    Wiring between MqGateway and MySensor node is the same for all devices below.
    For RS485 connection (with MAX485 module on both sides):
    
    | WIRE COLOR               | CONTROLLER PIN | 
    |--------------------------|----------------|
    | orange-white (GND)       | GND            |
    | orange (5V)              | VCC            |
    | green-white (ask wire)   | GPIO 2         |
    | green (ping wire)        | GPIO 7         |
    | brown-white              | A (RS485)      |
    | brown                    | B (RS485)      |

#### BME280
Temperature, humidity and barometric pressure sensor. Requires MySensors node.

- configuration type: `BME280`
- wires: always needs to be set to `["BROWN_WHITE", "BROWN"]` 
- additional configuration:
    - **`mySensorsNodeId`** [required] - MySensors node identifier
    - `humidityChildSensorId` [default: 0] - MySensors child sensor identifier for humidity
    - `temperatureChildSensorId"` [default: 1] - MySensors child sensor identifier for temperature
    - `pressureChildSensorId"` [default: 2] - MySensors child sensor identifier for pressure
    - `debugChildSensorId"` [default: 3] - MySensors child sensor identifier for debug data from node

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
            wires: ["BROWN_WHITE", "BROWN"]
            type: BME280
            config:
              mySensorsNodeId: 2
              humidityChildSensorId: 0
              temperatureChildSensorId: 1
              pressureChildSensorId: 2
              debugChildSensorId: 3
```
</details>

#### DHT22
Temperature and humidity sensor. Requires MySensors node.

- configuration type: `DHT22`
- wires: always needs to be set to `["BROWN_WHITE", "BROWN"]`
- additional configuration:
    - **`mySensorsNodeId`** [required] - MySensors node identifier
    - `humidityChildSensorId` [default: 0] - MySensors child sensor identifier for humidity
    - `temperatureChildSensorId"` [default: 1] - MySensors child sensor identifier for temperature
    - `debugChildSensorId"` [default: 2] - MySensors child sensor identifier for debug data from node

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
            wires: ["BROWN_WHITE", "BROWN"]
            type: DHT22
            config:
              mySensorsNodeId: 13
              humidityChildSensorId: 0
              temperatureChildSensorId: 1
              debugChildSensorId: 2
```
</details>