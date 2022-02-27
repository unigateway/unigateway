import com.mqgateway.core.device.SingleButtonsGateDevice
import com.mqgateway.core.device.factory.DeviceFactory
import com.mqgateway.core.gatewayconfig.DeviceConfiguration
import com.mqgateway.core.gatewayconfig.DeviceType

class GateDeviceFactory : DeviceFactory<SingleButtonsGateDevice> {

  override fun deviceType(): DeviceType {
    return DeviceType.GATE
  }

  // todo many gate classes for one device type maybe we should have interface/abstract class for GateDevice and two implementations, or many DeviceTypes
  override fun create(deviceConfiguration: DeviceConfiguration): SingleButtonsGateDevice {
    TODO()
    // if (listOf("stopButton", "openButton", "closeButton").all { deviceConfig.internalDevices.containsKey(it) }) {
    //   createThreeButtonGateDevice(portNumber, deviceConfig, gateway)
    // } else if (deviceConfig.internalDevices.containsKey("actionButton")) {
    //   createSingleButtonGateDevice(portNumber, deviceConfig, gateway)
    // } else {
    //   throw UnexpectedDeviceConfigurationException(
    //     deviceConfig.id,
    //     "Gate device should have either three buttons defined (stopButton, openButton, closeButton) or single (actionButton)"
    //   )
    // }
  }
}
