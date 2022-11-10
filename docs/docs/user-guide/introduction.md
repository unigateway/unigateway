This section describes how UniGateway works and how it can be used to create automation system like smart home solution.

## Why and when to use it?

Typical smart home solution is based on multiple sensors and controllers distributed around the house (e.g. temperature, motion, light sensors, switch buttons, relays, buzzers).  
Central component of the system is home automation application like OpenHab or Home Assistant which allows you to set rules and control devices with graphical interface and mobile application.  
Binding those elements is usually done either with some vendor gateway (e.g. Z-Wave Gateway) or by connecting sensors to microcontroller (e.g. ESP8266, Arduino) which communicates with home automation application.

The problem with the vendor gateway solution is these are mostly only wireless and often closed solution. This unfortunately means - monitoring and changing batteries (plus a lot of radio waves around the house). Additionally, vendor sensors are relatively expensive comparing to the bare modules which can be used with Arduino.  
Connecting a sensor to Arduino or similar microcontroller will save a lot of money on sensors, because you can use cheap, ready-to-use modules, but you still need to send data from microcontroller to home automation application. This can be done wireless (WiFi or RF), but it gets us back to the problem with batteries and radio noise. Even if you add wired connection (e.g. ethernet) to microcontroller, having single microcontroller for each one or two sensors is not very cost-effective.

Problems described above are the ones UniGateway is trying to solve.

List of supported devices (sensors/controllers) can be found on [supported devices](supported-devices.md) page.

It is possible to install UniGateway on single-board computer (SBC) like Raspberry Pi, or more advanced hardware with multiple inputs/outputs like [MqGateway](https://mqgateway.com).  

UniGateway supports cheap, Arduino-compatible modules for sensors and controllers. It includes relay modules, buttons and temperature sensors. UniGateway works with wired sensors and controllers to avoid the problem with batteries and radio noise.
Additionally, if you want to manage devices with UniGateway - you are not expected to know anything about programming. All you need to prepare is a [YAML configuration file](configuration.md#devices-configuration).

UniGateway is for you if you:

- Want to save money on sensors and controllers.
- Prefer wired over wireless components.
- Like to limit number of microcontrollers for sensors.
- Don't want to program microcontrollers yourself.
- Don't like to be tied to single hardware.
- Look for a solution which is fully local, open and extensible. 


As mentioned earlier, common home automation system consists of multiple sensors distributed around the house. You need to connect those to home automation application which is running on a computer connected to home network.  
A simple relay or temperature sensor modules (e.g. DTH22) do not have Wi-Fi or Ethernet port. Instead, they can be controlled (or send information) with single digital wire. UniGateway will work as a proxy between those sensors and home automation application.

For the external communication, UniGateway supports WebSockets or MQTT. This makes it easy to integrate it with most of the open home automation applications (including OpenHAB and Home Assistant).


## What's next?

- [Quick start with Raspberry Pi](quick-start-raspberrypi.md)
- [Quick start with MqGateway](quick-start-mqgateway.md)
