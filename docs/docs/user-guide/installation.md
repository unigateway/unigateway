
1. Connect to NanoPi NEO with SSH
2. Download installation script: 
   ```shell
   curl -o mqgateway-install.sh -L https://raw.githubusercontent.com/aetas/mqgateway/master/installation-scripts/mqgateway-install.sh
   ```

3. Make script executable and run it:
   ```shell
   chmod +x mqgateway-install.sh
   ./mqgateway-install.sh
   ```

    Installation script will:
   
    - Enable I<sup>2</sup>C and UART ports
    - Download and build WiringNP library
    - Install Java JRE
    - Download the latest version of MqGateway
    - Prepare basic configuration for MqGateway (requires adjustments before running MqGateway)
    - Prepare and enable a service which will ensure MqGateway is running all the time, even in case of device restart
   
    In case of problems with installation - see logs in file  `/opt/mqgateway/mqgateway-install.log`

4. Edit MqGateway configuration with an editor of your choice (e.g. nano or vim) to change address of MQTT server:

    ```yaml
    configVersion: "1.1"
    name: "TestGateway"
    mqttHostname: "192.168.1.150" # set IP of your MQTT broker
    rooms: # change devices configuration
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

    For more details on configuration files see [configuration page](configuration.md#devices-configuration) and examples for [supported devices](supported-devices.md). 

3. Reboot your NanoPI NEO to enable I<sup>2</sup>C and serial ports.   
   MqGateway always starts automatically on start of NanoPi NEO.


## What's next?

- [Supported devices and configuration](supported-devices.md)