package com.mqgateway.core.device.switchbutton

import com.mqgateway.core.hardware.simulated.SimulatedBinaryInput
import com.mqgateway.core.io.BinaryState

final class TestWrappingSwitchButtonDevice {

  final SwitchButtonDevice device
  private final SimulatedBinaryInput binaryInput

  private TestWrappingSwitchButtonDevice(SwitchButtonDevice device, SimulatedBinaryInput binaryInput) {
    this.device = device
    this.binaryInput = binaryInput
  }

  static TestWrappingSwitchButtonDevice create(String id, String name) {
    SimulatedBinaryInput binaryInput = new SimulatedBinaryInput(BinaryState.HIGH)
    SwitchButtonDevice device = new SwitchButtonDevice(id, name, binaryInput, 100, [:])
    device.init()
    return new TestWrappingSwitchButtonDevice(device, binaryInput)
  }

  void press() {
    binaryInput.low()
  }

  void release() {
    binaryInput.high()
  }
}
