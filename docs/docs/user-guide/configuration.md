

There are two types of the configuration for UniGateway:

- **Devices configuration** (in `gateway.yaml` file)  
  Configures what and how smart devices are connected to UniGateway.  
  Typically stored in `/opt/unigateway/gateway.yaml`.
- **System configuration** (passed by environment variables)   
  Configures stuff related to underlying hardware and features availability.  
  Typically stored in `/opt/unigateway/start_unigateway.sh`.
  
## Devices configuration

Configures what and how smart devices are connected to UniGateway.

!!! hint
    There is a [JSON schema file](https://raw.githubusercontent.com/unigateway/unigateway/master/src/main/resources/config.schema.json) for this configuration. 
    Use it with your favourite YAML editor (e.g. IntelliJ or VSCode) for autocompletion and instant validation.


### Structure

Basic structure of the configuration is like this:

```yaml
configVersion: "1.0" # Version of UniGateway configuration file - defines configuration structure
name: "MainGateway" # User-friendly name of the UniGateway installation - preferably unique in the network
id: "MainGateway" # Identifier of Gateway - should be unique for UniGateway installations in the network
devices: # List of devices configurations
  - name: "TV power" # User-friendly name of the device - might be visible in integrated system (e.g. Home Assistant) 
    id: "tv_power" # Unique identifier for the device across all devices on this Gateway
    type: RELAY # type of the device (see "Supported devices")
    connectors: # List of connectors which structure is specific for the underlying hardware (example for MqGateway)
      state: 
        portNumber: 1
        wires: BLUE_WHITE
    config: # contains properties for the device used to adjust device behaviour or integrations 
      delayMs: 100
      haComponent: SWITCH
```

For the structure of configuration for specific devices see [supported devices examples](supported-devices.md).

### Connectors configuration

There are two types of devices:
  - simple device - requires connectors in configuration which reference underlying hardware inputs/outputs (e.g. RELAY or SWITCH) 
  - complex device - composed of other devices (simple or complex), requires "internalDevices" in configuration (e.g. GATE or SHUTTER)

Every **simple device** has one or many connectors in the configuration. Connector references real pins in the hardware (i.e. by GPIO number in Raspberry Pi 
and by RJ45 port and wire color for MqGateway) or MySensors node sensor. 
Connector configuration structure depends on the underlying hardware implementation. It will be different for RaspberryPi, MqGateway and MySensors.

#### Raspberry Pi

| NAME       | DEFAULT | DESCRIPTION                                      |
|------------|---------|--------------------------------------------------|
| gpio       |         | GPIO number from Raspberry Pi pinout (REQUIRED)  |
| debounceMs | 50      | debounce for input pin in milliseconds           |
| pullUpDown | PULL_UP | start state for input pin (PULL_UP or PULL_DOWN) |

 

??? Example open
    ```yaml
    devices:
    - name: Workshop light switch
      id: workshop_light_switch
      type: SWITCH_BUTTON
      connectors:
        state:
          gpio: 17 # GPIO number from Rapberry Pi pinout
          debounceMs: 70 # optional (default: 50)
          pullUpDown: PULL_DOWN # optional (default: PULL_UP)
    ```

#### MqGateway

| NAME       | DEFAULT | DESCRIPTION                                                                                                       |
|------------|---------|-------------------------------------------------------------------------------------------------------------------|
| portNumber |         | number of the MqGateway port where device is connected (REQUIRED)                                                 |
| wireColor  |         | color of the UTP cable wire, the device is connected with (REQUIRED one of: BLUE, BLUE_WHITE, GREEN, GREEN_WHITE) |
| debounceMs | 50      | debounce for input pin in milliseconds                                                                            |


??? Example open
    ```yaml
    devices:
    - name: Workshop light switch
      id: workshop_light_switch
      type: SWITCH_BUTTON
      connectors:
        state:
          portNumber: 2 # number of the MqGateway
          wireColor: BLUE_WHITE # color of the UTP cable wire
          debounceMs: 70 # optional (default: 50)
    ```

#### MySensors

| NAME     | DEFAULT | DESCRIPTION                                                                                                                                               |
|----------|---------|-----------------------------------------------------------------------------------------------------------------------------------------------------------|
| source   |         | specifies that connector is using MySensors (REQUIRED: "MYSENSORS")                                                                                       |
| nodeId   |         | MySensors node identifier (REQUIRED)                                                                                                                      |
| sensorId |         | MySensors child sensor identifier (REQUIRED)                                                                                                              |
| type     | 50      | Type of the value returned from the sensor, in line with [MySensors documentation](https://www.mysensors.org/download/serial_api_20) (e.g. V_TEMP, V_HUM) |


??? Example open
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


### Loading devices configuration

By default, UniGateway expects devices configuration file to be named `gateway.yaml` and to be available in the current working directory. 
It means, it should be available in the directory from where UniGateway application is started.

You can change the path by specifying other path in environment variable `GATEWAY.CONFIGPATH` like this:

```shell
export GATEWAY_CONFIGPATH="/opt/gateway-configuration.yaml"
```

Remember to add this command to `~/.bashrc` if you want it to survive operating system reboot.

### Internal device reference

When you need to configure complex device with "internalDevices" you have to "reference" the internal devices.
It means you have to configure the simple device and then use its `id` value in `referenceId` property of "internalDevices" entry.

See the example below which configures garage door (GATE device) with reed switch configured as referenced device.

??? Example
    ```yaml
    name: "GarageGateway"
    id: "GarageGateway"
    devices:
      - name: "Right garage door action button"
        id: "right_garage_door_action_button" # <-- value used for "referenceId" 
        type: EMULATED_SWITCH
        connectors:
          state:
            portNumber: 1
            wires: BLUE_WHITE
      - name: "Right garage door reed switch"
        id: "right_garage_door_closed_reed"
        type: REED_SWITCH
        connectors:
          state:
            portNumber: 3
            wireColor: GREEN_WHITE
      - name: "Right garage door"
        id: "right_garage_door"
        type: GATE
        internalDevices:
          actionButton:
            referenceId: "right_garage_door_action_button"  # <-- it references to the EMULATED_SWITCH defined earlier
          closedReedSwitch:
            referenceId: "right_garage_door_closed_reed"    # <-- it references to the REED_SWITCH defined earlier   
    ```


## System configuration

Configures stuff related to underlying hardware and features availability.

This configurations can be set as environment variables like this:

```shell
export GATEWAY_MQTT_ENABLED=true
```

Add this commands to UniGateway start script, `.bashrc` or `.zshrc` so they survive operating system restart.

### Basic configuration

| NAME                           | DEFAULT      | DESCRIPTION                                                        |
|--------------------------------|--------------|--------------------------------------------------------------------|
| GATEWAY_CONFIG_PATH            | gateway.yaml | path to the devices configuration file                             |
| GATEWAY_SYSTEM_NETWORK_ADAPTER | eth0         | name of the ethernet interface used to connect to MQTT / WebSocket |
| GATEWAY_SYSTEM_PLATFORM        | SIMULATED    | type of the underlying hardware (RASPBERRYPI or MQGATEWAY)         |


### Protocols and integrations

#### MQTT

| NAME                  | DEFAULT   | DESCRIPTION                                             |
|-----------------------|-----------|---------------------------------------------------------|
| GATEWAY_MQTT_ENABLED  | false     | Enable control through MQTT communication               |
| GATEWAY_MQTT_HOSTNAME | (not set) | MQTT broker hostname or ip address (e.g. 192.168.1.100) |
| GATEWAY_MQTT_PORT     | 1883      | MQTT port number                                        |
| GATEWAY_MQTT_USERNAME | (not set) | MQTT user name (optional)                               |
| GATEWAY_MQTT_PASSWORD | (not set) | MQTT user password (optional)                           |

#### MySensors

UniGateway communicates with the MySensors gateway in one of two ways:

- by the virtual serial when MySensors gateway is installed on the same hardware (default)
- by the real serial when MySensors gateway is installed on the separate hardware

UniGateway installation SD cards have MySensors gateway already installed and configured to use UART and RS485 communication to nodes.  

More about how to connect and use MySensors devices with RS485 can be found on specific hardware documentation and [MySensors page](https://www.mysensors.org/).

| NAME                                     | DEFAULT        | DESCRIPTION                                               |
|------------------------------------------|----------------|-----------------------------------------------------------|
| GATEWAY_SYSTEM_MYSENSORS_ENABLED         | false          | Enable communication with MySensors gateway               |
| GATEWAY_SYSTEM_MYSENSORS_PORT_DESCRIPTOR | "/dev/ttys000" | Serial port address to communicate with MySensors gateway |
| GATEWAY_SYSTEM_MYSENSORS_BAUD_RATE       | 9600           | Baud rate for serial communication with MySensors gateway |


#### Home Assistant

| NAME                     | DEFAULT       | DESCRIPTION                                                                       |
|--------------------------|---------------|-----------------------------------------------------------------------------------|
| HOMEASSISTANT_ENABLED    | true          | Enable integration with Home Assistant through MQTT (requires MQTT to be enabled) |
| HOMEASSISTANT_ROOT_TOPIC | homeassistant | Root topic for Home Assistant MQTT discovery                                      |



### Hardware specific

#### Raspberry Pi

| NAME                                             | DEFAULT | DESCRIPTION                                         |
|--------------------------------------------------|---------|-----------------------------------------------------|
| GATEWAY_SYSTEM_PLATFORM_CONFIG_DEFAULTDEBOUNCEMS | 50      | default debounce for input pins in milliseconds     |
| GATEWAY_SYSTEM_PLATFORM_CONFIG_DEFAULTPULLUPDOWN | PULL_UP | default state for input pins (PULL_UP or PULL_DOWN) |

#### MqGateway

| NAME                                                     | DEFAULT                              | DESCRIPTION                                                 |
|----------------------------------------------------------|--------------------------------------|-------------------------------------------------------------|
| GATEWAY_SYSTEM_PLATFORM_CONFIG_EXPANDER_ENABLED          | false                                | should be "true" if you have _I/O Expander board_ connected |
| GATEWAY_SYSTEM_PLATFORM_CONFIG_DEFAULTDEBOUNCEMS         | 50                                   | default debounce for input pins in milliseconds             |
| GATEWAY_SYSTEM_PLATFORM_CONFIG_COMPONENTS_MCP23017_PORTS | (depends on I/O expander enablement) | I2C addresses of MCP23017 expanders                         |


