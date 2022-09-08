import React, { useState } from "react";
import { ChakraProvider } from "@chakra-ui/react"
import theme from "./components/theme";
import Devices from "./components/devices/Devices";
import Navigation from "./components/navigation/Navigation";

const gateways: Gateway[] = [
	{ id: "gateway-1", name: "Gateway #1", properties: { IP_ADDRESS: "192.168.1.121" } },
	{ id: "gateway-2", name: "Gateway #2", properties: { IP_ADDRESS: "192.168.1.122" } },
	{ id: "gateway-3", name: "Gateway #3", properties: { IP_ADDRESS: "192.168.1.123" } },
	{ id: "gateway-4", name: "Gateway #4", properties: { IP_ADDRESS: "192.168.1.124" } },
	{ id: "gateway-5", name: "Gateway #5", properties: { IP_ADDRESS: "192.168.1.125" } },
]

function App() {
	const [selectedGateway, setSelectedGateway] = useState<Gateway>(gateways[0]);

	return (
		<ChakraProvider theme={theme}>
			<Navigation
				selectedGateway={selectedGateway}
				gateways={gateways}
				onGatewaySelect={setSelectedGateway} />
			<Devices />
		</ChakraProvider>
	);
}

export default App;

