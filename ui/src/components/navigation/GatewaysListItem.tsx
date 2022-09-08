import React from "react";
import { Flex, Link, Text, useColorModeValue } from "@chakra-ui/react";

type Props = {
	gateway: Gateway;
	onClick: (gateway: Gateway) => void
}

const GatewayListItem = ({ gateway, onClick }: Props) => (
	<Flex
		px={2}
		py={1}
		rounded={"md"}
		as={Link}
		href={"#"}
		w="100%"
		justify={"space-between"}
		align={"center"}
		onClick={() => onClick(gateway)}
		_hover={{
			textDecoration: "none",
			bg: useColorModeValue("gray.200", "gray.700"),
		}}>
		<Text
			fontWeight={600}
			color={useColorModeValue("gray.600", "gray.200")}>
			{gateway.name}
		</Text>
		<Text
			fontSize='xs'
			color={useColorModeValue("gray.600", "gray.200")}>
			{gateway.properties?.IP_ADDRESS}
		</Text>
	</Flex>
);

export default GatewayListItem;
