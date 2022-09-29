import React from 'react';
import {Device, GatewayConfiguration, Point, Room} from "./MqGatewayMutableTypes";
import {DeviceType, WireColor} from "./communication/MqgatewayTypes";

describe("Mutable device types", () => {

  let gatewayConfig: GatewayConfiguration

  beforeEach(() => {
    gatewayConfig = new GatewayConfiguration("", "", "", [
      new Room("testRoom-1", [
        new Point("testPoint-1-1", 1, [
          new Device("device-1-1-1", "Device 1.1.1", DeviceType.RELAY, [WireColor.BLUE], new Map(), new Map(), null, false, () => Promise.reject(), "1.1.1"),
          new Device("device-1-1-2", "Device 1.1.2", DeviceType.RELAY, [WireColor.BLUE_WHITE], new Map(), new Map(), null, false, () => Promise.reject(), "1.1.2")
        ]),
        new Point("testPoint-1-2", 1, [
          new Device("device-1-2-1", "Device 1.1.1", DeviceType.RELAY, [WireColor.BLUE], new Map(), new Map(), null, false, () => Promise.reject(), "1.2.1"),
          new Device("device-1-2-2", "Device 1.2.2", DeviceType.RELAY, [WireColor.BLUE_WHITE], new Map(), new Map(), null, false, () => Promise.reject(), "1.2.2")
        ])
      ]),
      new Room("testRoom-2", [
        new Point("testPoint-2-1", 2, [
          new Device("device-2-1-1", "Device 2.1.1", DeviceType.RELAY, [WireColor.BLUE], new Map(), new Map(), null, false, () => Promise.reject(), "2.1.1")
        ])
      ])
    ])
  })

  describe("move device between points", () => {

    it('should device appear under the destination point', () => {
      // when
      gatewayConfig.moveDeviceToPoint("1.1.1", "testRoom-2", "testPoint-2-1")

      // then
      expect(gatewayConfig.getRoom("testRoom-2").points[0].devices.length).toBe(2)
      assertDevicesEqual(gatewayConfig.getRoom("testRoom-2").points[0].devices[0],
        new Device("device-2-1-1", "Device 2.1.1", DeviceType.RELAY, [WireColor.BLUE], new Map(), new Map(), null, false, () => Promise.reject()))
      assertDevicesEqual(gatewayConfig.getRoom("testRoom-2").points[0].devices[1],
        new Device("device-1-1-1", "Device 1.1.1", DeviceType.RELAY, [WireColor.BLUE], new Map(), new Map(), null, false, () => Promise.reject()))
    });

    it('should device no longer exists under origin point', () => {
      // when
      gatewayConfig.moveDeviceToPoint("1.1.1", "testRoom-2", "testPoint-2-1")

      // then
      expect(gatewayConfig.getRoom("testRoom-1").points[0].devices.length).toBe(1)
      assertDevicesEqual(gatewayConfig.getRoom("testRoom-1").points[0].devices[0],
        new Device("device-1-1-2", "Device 1.1.2", DeviceType.RELAY, [WireColor.BLUE_WHITE], new Map(), new Map(), null, false, () => Promise.reject()))
    });

    function assertDevicesEqual(device1: Device, device2: Device) {
      expect(device1.id).toEqual(device2.id)
      expect(device1.name).toEqual(device2.name)
      expect(device1.type).toEqual(device2.type)
      expect(device1.wires).toEqual(device2.wires)
      expect(device1.config).toEqual(device2.config)
      expect(device1.internalDevices).toEqual(device2.internalDevices)
      expect(device1.referencedDeviceId).toEqual(device2.referencedDeviceId)
    }
  });

  describe("locate device", () => {

    it("should give room and point device is in", () => {
      // when
      const {room, point} = gatewayConfig.deviceLocation("1.2.1")!

      // then
      expect(room.name).toEqual("testRoom-1")
      expect(point.name).toEqual("testPoint-1-2")
    });

    it("should return null if device of given ID does not exist", () => {
      // when
      const locationResult = gatewayConfig.deviceLocation("unexisting-device")

      // then
      expect(locationResult).toBeNull()
    });

  })
})