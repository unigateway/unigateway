import { Box, Collapse, Flex, useColorModeValue, useDisclosure, } from "@chakra-ui/react";
import GatewaySwitcher from "./GatewaySwitcher";
import GatewaysList from "./GatewaysList";
import SmallLogo from "../logo/SmallLogo";

type Props = {
	selectedGateway: Gateway;
	gateways: Gateway[];
	onGatewaySelect: (gateway: Gateway) => void;
}

const Navigation = (props: Props) => {
	const { isOpen, onClose, onToggle } = useDisclosure();

	const onGatewaySelect = (gateway: Gateway) => {
		props.onGatewaySelect(gateway);
		onClose();
	}

	return (
		<Box>
			<Flex
				bg={useColorModeValue("white", "gray.800")}
				color={useColorModeValue("gray.600", "white")}
				minH={"60px"}
				py={{ base: 2 }}
				px={{ base: 4 }}
				borderBottom={1}
				borderStyle={"solid"}
				borderColor={useColorModeValue("gray.200", "gray.900")}
				align={"center"}>
				<Flex>
					<SmallLogo />
				</Flex>
				<Flex flex={{ base: 1 }} justify={{ base: "center", md: "start" }}>
					<GatewaySwitcher
						selectedGateway={props.selectedGateway}
						gateways={props.gateways}
						onGatewaySelect={onGatewaySelect}
						open={isOpen}
						onToggle={onToggle} />
				</Flex>
			</Flex>

			<Collapse
				in={isOpen}
				animateOpacity>
				<Box
					display={{ base: "block", md: "none" }}
					borderBottom={1}
					borderStyle={"solid"}
					borderColor={useColorModeValue("gray.200", "gray.700")}>
					<GatewaysList
						gateways={props.gateways}
						onGatewaySelect={onGatewaySelect} />
				</Box>
			</Collapse>
		</Box>
	);
}

export default Navigation;
