
!!! info
    This quick start guide assumes you have MqGateway hardware ready. If you are looking for how to prepare the hardware - look into [hardware section](../hardware/board-layout.md).

Quick start guide shows how to connect a relay module and configure MqGateway to control it. Connecting other types of devices is similar and can be done with the similar steps.


## What do you need
- NanoPI NEO with [Armbian](https://www.armbian.com/nanopi-neo/) installed and SSH connection established
- Assembled MqGateway with 5V power supply
- Running MQTT broker (e.g. [Mosquitto](https://mosquitto.org/))
- MQTT client installed on your computer (e.g. [MQTT.js](https://github.com/mqttjs/MQTT.js#readme))
- Relay module with screw connectors
- Additional ethernet cable (with RJ45 connector on one side only) to connect relay module


## Connect and configure device

!!! warning 
    Make sure MqGateway is never connected to power supply and micro USB port at the same time. This will destroy the device and may damage your computer.

// TODO some simple installation script to configure Armbian to enable I2C + serial and install MqGateway application on NanoPi Neo is needed

1. Connect to NanoPi NEO with SSH and run MqGateway installation script
   ```shell
   curl TODO prepare automatic script to do everything
   ```
   
1. Prepare ethernet cable with the connector on one side (according to standard T568B)
   [![cable termination](images/T568B.png){: style="height:150px; display: block; margin: 0 auto;transform: rotate(90deg)"}](images/T568B.png)

2. Connect relay module to ethernet cable ([photo](images/relay-connected.jpg))

     | module pin | wire color   |
     |------------|--------------|
     | VCC/DC+    | orange       |
     | ground/DC- | orange-white |
     | control/IN | blue         |

3. Connect the module to MqGateway port 1 ([ports](images/mqgateway-ports-numbers.jpg))
4. Prepare configuration file `gateway.yaml` - remember to set `mqttHostname` to point to your MQTT broker<br>
```yaml
configVersion: "1.1"
name: "TestGateway"
mqttHostname: "192.168.1.52" # set IP of your MQTT broker
rooms:
  - name: "workshop"
    points:
      - name: "point with test relay"
        portNumber: 1
        devices:
           - name: "test relay"
             id: "my-relay"
             wires: ["BLUE"]
             type: RELAY
```
<br>
For more details on configuration files see [configuration page](configuration.md#devices-configuration).

5. Upload [latest MqGateway application](https://github.com/aetas/mqgateway/releases/latest) and `gateway.yaml` configuration file to NanoPi NEO (application and configuration files should be in the same directory)  

6. Subscribe to MQTT topic to see device has been initialized (replace IP address with your MQTT broker address)

    ```bash
    mqtt sub -t 'homie/TestGateway/#' -h '192.168.1.52' -v
    ```
    <details>
      <summary>Sample output</summary>
      ```
      // TODO put output of this command
      ```
    </details>

7. Send MQTT message to switch relay state (replace IP address with your MQTT broker address)
   
    ```bash
    mqtt pub -t 'homie/TestGateway/my-relay/state/set' -h '192.168.1.52' -m 'ON'
    ```

State of the relay should change.<br>
Congratulation! You have just configured your first device on MqGateway 🎉.

If you have Home Assistant configured to use the same MQTT broker - new device will be also available there already. 


// TODO test if this description works fine

## What's next?

- [Read full user guide starting with naming conventions](../naming-convention)
- [Find out all supported devices](../supported-devices)

  
