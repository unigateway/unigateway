

There are two types of the configuration for MqGateway:

- **Devices configuration** (in `gateway.yaml` file)  
  Configures what and how smart devices are connected to MqGateway and MQTT broker connection.
- **System configuration** (passed by environment variables)   
  Rarely needs adjustments. Configures stuff related to underlying hardware and features availability.   
  
## Devices configuration

Configures what and how smart devices are connected to MqGateway and MQTT broker connection.
There is a [JSON schema file](https://raw.githubusercontent.com/aetas/mqgateway/master/src/main/resources/config.schema.json) for this configuration. 
Use it with your favourite YAML editor (e.g. IntelliJ or VSCode) for autocompletion and instant validation.


### Structure

Basic structure of the configuration is like this:

```yaml
configVersion: "1.1"  # Version of MqGateway configuration file - defines configuration structure
name: "MainGateway" # Name of Gateway - unique for MQTT broker (used as part of the MQTT topic) 
mqttHostname: "192.168.1.100" # Address IP or hostname of MQTT broker
rooms: # List of rooms managed by this Gateway
  - name: "living room" # name of the room
    points: # List of points (each place connected with single UTP cable)
      - name: "outlet behind TV"
        portNumber: 10 # Number of port in Gateway to which point is connected (1-16)
        devices: # List of devices connected to single point/cable
          - name: "TV power" # Human-readable name of the device
            id: "tv_power" # Unique identifier for the device across all devices on this Gateway
            type: RELAY # type of the device (see "Supported devices")
            [...]  # further configuration structure for the device depends on the type of the device
```

For the structure of configuration for specific devices see [supported devices examples](supported-devices.md).


### Loading devices configuration

By default, MqGateway expects devices configuration file to be named `gateway.yaml` and to be available in the current working directory. 
It means, it should be available in the directory from where MqGateway application is started on NanoPi NEO.

You can change the path by specifying other path in environment variable `GATEWAY.CONFIGPATH` like this:

```shell
export GATEWAY_CONFIGPATH="/opt/gateway-configuration.yaml"
```

Remember to add this command to `~/.bashrc` if you want it to survive MqGateweay reboot.


## System configuration

| NAME                                     | DEFAULT               | DESCRIPTION                                                         |
|------------------------------------------|-----------------------|---------------------------------------------------------------------|
| GATEWAY_CONFIGPATH                       | gateway.yaml          | path to the devices configuration file                              |
| GATEWAY_SYSTEM_NETWORKADAPTER            | eth0                  | name of the ethernet interface used to connect to MQTT              |
| GATEWAY_SYSTEM_PLATFORM                  | NANOPI                | name of the controller used on MqGateway (currently on only NANOPI) |
| GATEWAY_SYSTEM_COMPONENTS_MCP23017_PORTS | ["20","21","22","23"] | I2C addresses of MCP23017 expanders                                 |
| GATEWAY_SYSTEM_SERIAL_ENABLED            | true                  | serial communication enabled/disabled                               |
| GATEWAY_SYSTEM_SERIAL_DEVICE             | /dev/ttyS1            | path to device used for serial communication                        |
| GATEWAY_SYSTEM_SERIAL_BAUD               | 9600                  | baud used for serial communication                                  |


