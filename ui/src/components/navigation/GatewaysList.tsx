import { Stack, useColorModeValue } from "@chakra-ui/react";
import GatewayListItem from "./GatewaysListItem";

type Props = {
	gateways: Gateway[];
	onGatewaySelect: (gateway: Gateway) => void;
}

const GatewaysList = ({ gateways, onGatewaySelect }: Props) => {
	return (
		<Stack
			mt={2}
			pl={4}
			bg={useColorModeValue("white", "gray.800")}
			p={4}
			align={"start"}>
			{gateways.map(gateway => (
				<GatewayListItem key={gateway.id} gateway={gateway} onClick={onGatewaySelect} />
			))}
		</Stack>
	);
};

export default GatewaysList;
