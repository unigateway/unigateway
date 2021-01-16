This page describes the connectors for devices in MqGateway. 

## RJ45 devices ports

MqGateway has 16 RJ45 ports which are not ethernet ports, but are used to connect devices (e.g. relays) and sensors (e.g. temperature). 

Every RJ45 connector uses T568B termination. It means - wires in a connector are in following order:

[![cable termination](images/T568B.png){: style="height:150px; display: block; margin: 0 auto;transform: rotate(90deg)"}](images/T568B.png)


|   | COLOR        | USAGE          |
|---| ------------ | -------------- |
| 1 | orange       | +5V            |
| 2 | orange-white | GND            |
| 3 | blue         | digital in/out |
| 4 | blue-white   | digital in/out |
| 5 | green        | digital in/out |
| 6 | green-white  | digital in/out |
| 7 | brown        | TX or A (RS485)|
| 8 | brown-white  | RX or B (RS485)|
