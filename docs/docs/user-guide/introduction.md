This section describes how MqGateway works and how it can be used for home automation.

## Why and when to use it?

Typical smart home solution is based on multiple sensors and controllers distributed around the house (e.g. temperature, motion, light sensors, switch buttons, relays, buzzers).  
Central component of the system is home automation application like OpenHab or Home Assistant which allows you to set rules and control devices with graphical interface and mobile application.  
Binding those elements is usually done either with some vendor gateway (e.g. Z-Wave Gateway) or by connecting sensors to microcontroller (e.g. ESP8266, Arduino) which communicates with home automation application.

The problem with the vendor gateway solution is these are mostly only wireless and often closed solution. This unfortunately means - monitoring and changing batteries (plus a lot of radio waves around the house). Additionally, vendor sensors are relatively expensive comparing to the bare modules which can be used with Arduino.  
Connecting a sensor to Arduino or similar microcontroller will save a lot of money on sensors, because you can use cheap, ready-to-use modules, but you still need to send data from microcontroller to home automation application. This can be done wireless (WiFi or RF), but it gets us back to the problem with batteries and radio noise. Even if you add wired connection (e.g. ethernet) to microcontroller, having single microcontroller for each one or two sensors is not very cost-effective.

Problems described above are the ones MqGateway is trying to solve.

Basic MqGateway allows you to connect:

- up to 64 digital input/output devices (like relays or switch buttons) and
- up to 16 complex devices using RS485 communication (with [MySensors](https://www.mysensors.org/)).

Connecting more devices is possible by using [MqGateway I/O Expander board](../hardware/io-expander-board.md).

List of supported devices can be found on [supported devices](supported-devices.md) page.

MqGateway supports cheap modules which work with Arduino microcontrollers, including relay modules, buttons and temperature sensors. MqGateway works with wired sensors and controllers to avoid the problem with batteries and radio noise.
Additionally, if you want to connect devices to MqGateway - you are not expected to know anything about programming. All you need to prepare is a [YAML configuration file](configuration.md#devices-configuration).

MqGateway is for you if you:

- Want to save money on sensors and controllers.
- Prefer wired over wireless components.
- Like to limit number of microcontrollers for sensors.
- Don't want to program microcontrollers yourself.


## Composition

The heart of the MqGateway is NanoPi Neo SBC. This is a board, similar to the popular Raspberry Pi. It controls all connected devices and communicates with home automation application through MQTT broker. This communication is done either using build-in ethernet controller or with additional USB WiFi adapter.

Find out more about hardware and board layout in [hardware section](../hardware/board-layout.md).

Devices can be connected through sixteen RJ45 ports. Each RJ45 port provides:

- power (+5V and GND),
- 4 digital input/output wires,
- 2 wires for UART or RS485 communication.

Look at [wiring](../hardware/wiring.md) in hardware section if you want to know more.


## Wiring examples

As mentioned earlier, common home automation system consists of multiple sensors distributed around the house. You need to connect those to home automation application which is running on a computer connected to home network.  
A simple relay or temperature sensor modules (e.g. DTH22) do not have Wi-Fi or Ethernet port. Instead, they can be controlled (or send information) with single digital wire. MqGateway will work as a proxy between those sensors and home automation application. For the communication layer it uses MQTT broker which is supported by most of the open home automation applications (including OpenHab and Home Assistant).

One example usage is when you have module of 4 relays controlling lights in your house. Then you need to use single RJ45 port of MqGateway. Orange and white-orange wires will power your relay module with 5V. Use blue, white-blue, green and white-green wires to control relay states.

Another example is to read state from PIR motion sensor module. Take a UTP cable plugged into MqGateway RJ45 port and use orange and white-orange wires to power the module. Then use any of available digital wires (green, white-green, blue or white-blue) for output connection.

When wiring is ready then you need to [prepare configuration](configuration.md) in YAML file to tell MqGateway what kind of module is connected to which RJ45 port and on which wire colour.


## What's next?

- [Hardware](../hardware/board-layout.md)
- [Quick start](quick-start.md)
