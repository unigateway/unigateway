# Bill of materials

This page lists all elements required to build MqGateway device on the PCB.
PCB is prepared to work with one of the SOC:

  - [Nano PI NEO](https://www.friendlyarm.com/index.php?route=product/product&product_id=132),
  - Raspberry Pi 3 or 4.

Theoretically software should work on both, with simple configuration change, but it has not been tested on Raspberry Pi. That is why currently it is advised to use NanoPi NEO.    

## Mainboard

Components for [MqGateway Mainboard](board-layout.md).


### Basic elements
| NAME                                          | AMOUNT |
|-----------------------------------------------|:------:|
| DC Power Jack Plug Socket 2.1/5.5             |   1    |
| MCP23017                                      |   4    |
| DIP28 Socket                                  |   4    |
| Screw Terminal 2-pin                          |   2    |
| RJ45 Ethernet Socket 2x8 Ports                |   1    |
| Micro switch button 4-pin                     |   2    |
| Logic level converter 4-ch 5V to 3.3V         |   1    |
| Pin header 2x2                                |   4    |
| Pin jumper                                    |   4    |
| IDC10 Male Pin PCB Socket                     |   1    |
| LD1117V33 Linear Voltage Regulator 3.3V       |   1    |
| MAX485 module                                 |   1    |
| LED for power                                 |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| 2-color LED for status                        |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| Power switch button                           |   1    |
| Case switch button                            |   1    |



### Version with Nano Pi Neo (additional)

| NAME                                          | AMOUNT |
|-----------------------------------------------|:------:|
| NanoPi NEO                                    |   1    |
| IDC24 Male Pin PCB Socket                     |   1    |
| IDC24 flat ribbon cable 10 cm                 |   1    |
| IDC24 ribbon connector                        |   2    |
| Screw M2.5 30mm with nut                      |   4    |


### Version with Raspberry Pi 3/4 (not fully supported yet)

| NAME                                          | AMOUNT |
|-----------------------------------------------|:------:|
| Raspberry Pi 2/3/4                            |   1    |
| IDC40 Male Pin PCB Socket                     |   1    |
| IDC40 flat ribbon cable 10 cm                 |   1    |
| IDC40 ribbon connector                        |   2    |
| Screw with nut ????                           |   4    |


## I/O Expander Board

Components for [MqGateway I/O Expander Board](io-expander-board.md).


| NAME                                          | AMOUNT |
|-----------------------------------------------|:------:|
| DC Power Jack Plug Socket 2.1/5.5             |   1    |
| MCP23017                                      |   4    |
| DIP28 Socket                                  |   4    |
| Screw Terminal 2-pin                          |   4    |
| RJ45 Ethernet Socket 2x8 Ports                |   1    |
| Pin header 2x2                                |   1    |
| Pin jumper                                    |   1    |
| IDC10 Male Pin PCB Socket                     |   2    |
| Terminal Block connector 4-pin (pitch 3.5mm)  |   1    |
| LED for power                                 |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| LED for CHECK                                 |   1    |
| LED resistor (resistance depends on used LED) |   1    |
| Slide switch (pitch 3mm)                      |   3    |
| LM2596 DC-DC Step down converter              |   1    | 