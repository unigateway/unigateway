# Bill of materials

This page lists all elements required to build MqGateway device on the PCB.
PCB is prepared to work with one of the SOC:

  - [Nano PI NEO](https://www.friendlyarm.com/index.php?route=product/product&product_id=132),
  - Raspberry Pi 3 or 4.

Theoretically software should work on both, with simple configuration change, but it has not been tested on Raspberry Pi. That is why currently it is advised to use NanoPi NEO.    

## Mainboard

Components for [MqGateway Mainboard](board-layout.md).

Full BOM is available [here](https://github.com/aetas/MqGateway-PCB/blob/main/MqGateway/MqGateway_BOM.csv).

### Basic elements
| NAME                                          | AMOUNT |           |
|-----------------------------------------------|:------:|:---------:|
| DC Power Jack Plug Socket 2.1/5.5             |   1    | [link](https://www.piekarz.pl/02542-gniazdo-zasilajace-dc-gdc21-55-2-1mm)    |
| MCP23017-E/SP                                 |   4    | [link](https://www.piekarz.pl/22712-przetwornik-microchip-mcp23017-e-sp)    |
| DIP28 Socket                                  |   4    | [link](https://www.piekarz.pl/04818-podstawka-zwykla-dip-dip28p-28-torow)    |
| Screw Terminal 2-pin                          |   2    | [link](https://www.piekarz.pl/00561-laczowka-xy306a-02p-5-0mm-2-tory)    |
| RJ45 Ethernet Socket 2x8 Ports                |   1    | [link](https://www.aliexpress.com/item/1212644854.html)    |
| Tact switch button 4-pin (6x6mm)              |   2    | [link](https://www.piekarz.pl/00678-mikroprzycisk-kls-ts6601-5-6x6mm-5mm-1-5mm/)    |
| Logic level converter 4-ch 5V to 3.3V         |   1    | [link](https://www.aliexpress.com/item/32715353192.html)    |
| Pin header 2x2                                |   4    | [link](https://www.piekarz.pl/35904-piny-wtyk-pld20s-20-torow/)    |
| Pin jumper                                    |   4    | [link](https://www.piekarz.pl/00481-zwora-jumper-b-1-tor/)    |
| IDC10 Male Pin PCB Socket                     |   1    | [link](https://www.piekarz.pl/17934-wtyk-bh10-s-10-torow/)    |
| LD1117V33 Linear Voltage Regulator 3.3V       |   1    | [link](https://www.piekarz.pl/13101-stabilizator-st-microelectronics-ld1117v33c/)    |
| MAX485 TTL to RS-485 Interface Module         |   1    | [link](https://www.aliexpress.com/item/4000112077167.html)    |
| LED for power (d=3mm)                         |   1    |             |
| LED resistor (resistance depends on used LED) |   1    |             |
| 2-color LED for status                        |   1    | [link](https://en.maritex.com.pl/clearance_sale/led/led_-_diodes/ledb-3ygw30-c3.html)    |
| Green LED Resistor (10Ω)                      |   1    |             |
| Yellow LED Resistor (43Ω                      |   1    |             |
| Power switch, Rocker Switch OFF-ON            |   1    | [link](https://en.maritex.com.pl/product/show/39905)    |
| Case switch button, 12mm diameter OFF-(ON)    |   1    | [link](https://www.piekarz.pl/34283-przelacznik-pbw-12b/)    |



### Version with Nano Pi Neo (additional)

| NAME                                          | AMOUNT |      |
|-----------------------------------------------|:------:|:----:|
| NanoPi NEO (512M RAM)                         |   1    | [link](https://www.friendlyarm.com/index.php?route=product/product&product_id=132) | 
| NanoPi NEO Heat Sink                          |   1    | [link](https://www.friendlyarm.com/index.php?route=product/product&product_id=221) |
| IDC24 Male Pin PCB Socket                     |   1    | [link](https://www.maritex.com.pl/zlacza/zlacza_idc/idc_w_rastrze_2_54mm/zlacza_kolkowe_proste_w_rastrze_2_54mm_conntherm_1/bh24s-ct.html) |
| IDC24 flat ribbon cable 10 cm                 |   1    | [link](https://www.aliexpress.com/item/32824377459.html) |
| Female IDC-plug 24 pin for ribbon cable       |   2    | [link](https://en.maritex.com.pl/connectors/idc_connectors/idc_connectors_2_54mm_pitch/idc_female_sockets_for_ribbon_cable_2_54_mm_pitch_conntherm/idc24-ct.html) |
| Screw M2.5 30mm                               |   4    | [link](https://www.piekarz.pl/21920-wkret-m2-5-wkkm2530/) |
| Nut for screw M2.5                            |   4    | [link](https://www.piekarz.pl/09575-nakretka-m2-5-nom252/) |


### Version with Raspberry Pi 3/4 (not fully supported yet)

| NAME                                          | AMOUNT |
|-----------------------------------------------|:------:|
| Raspberry Pi 2/3/4                            |   1    |
| IDC40 Male Pin PCB Socket                     |   1    |
| IDC40 flat ribbon cable 10 cm                 |   1    |
| IDC40 ribbon connector                        |   2    |
| Screw with nut                                |   4    |


## I/O Expander Board

Components for [MqGateway I/O Expander Board](io-expander-board.md).

Full BOM is available [here](https://github.com/aetas/MqGateway-PCB/blob/main/IO_Expander/IO_Expander_BOM.csv).

| NAME                                          | AMOUNT |           | 
|-----------------------------------------------|:------:|:---------:|
| DC Power Jack Plug Socket 2.1/5.5             |   1    | [link](https://www.piekarz.pl/02542-gniazdo-zasilajace-dc-gdc21-55-2-1mm/) |
| MCP23017-E/SP                                 |   4    | [link](https://www.piekarz.pl/22712-przetwornik-microchip-mcp23017-e-sp/)  |
| DIP28 Socket                                  |   4    | [link](https://www.piekarz.pl/04818-podstawka-zwykla-dip-dip28p-28-torow/)  |
| Screw Terminal 2-pin                          |   4    | [link](https://www.piekarz.pl/00561-laczowka-xy306a-02p-5-0mm-2-tory/)  |
| 8P8C/RJ45 Ethernet Socket 2x8 Ports           |   1    | [link](https://www.aliexpress.com/item/1212644854.html)  |
| Pin header 2x2                                |   1    | [link](https://www.piekarz.pl/35904-piny-wtyk-pld20s-20-torow/)  |
| Pin jumper                                    |   2    | [link](https://www.piekarz.pl/00481-zwora-jumper-b-1-tor/)  |
| IDC10 Male Pin PCB Socket                     |   2    | [link](https://www.piekarz.pl/17934-wtyk-bh10-s-10-torow/)  |
| Female IDC-plug 10 pin for ribbon cable       |   2    | [link](https://www.piekarz.pl/02212-gniazdo-fc10-idc10-10-torow/)  |
| 10-core ribbon cable 25cm                     |   1    | [link](https://www.piekarz.pl/06502-przewod-plaski-awg28-10-10-zyl-linka-cu-0-09mm2/)  |
| Terminal Block connector 4-pin (pitch 3.5mm)  |   1    | [link](https://www.piekarz.pl/00604-laczowka-stl1550-4-gh-4-tory/)  |
| Red LED for power d=3mm                       |   1    |   |
| LED resistor (resistance depends on used LED) |   1    |   |
| Blue LED for CHECK d=3mm                      |   1    |   |
| LED resistor (resistance depends on used LED) |   1    |   |
| Miniature SPDT Slide Switch (pitch 3mm)       |   3    | [link](https://www.piekarz.pl/33509-przelacznik-ss02-12f20-l-6/)  |
| LM2596 DC-DC Step down converter              |   1    | [link](https://www.aliexpress.com/item/4000627309533.html)  |