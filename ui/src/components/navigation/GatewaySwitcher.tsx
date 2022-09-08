import { Box, Flex, Icon, Link, Popover, PopoverContent, PopoverTrigger, Text, useBreakpointValue, useColorModeValue } from "@chakra-ui/react";
import { ChevronDownIcon } from "@chakra-ui/icons";
import GatewaysList from "./GatewaysList";

type Props = {
	selectedGateway: Gateway;
	gateways: Gateway[];
	onGatewaySelect: (gateway: Gateway) => void;
	open: boolean;
	onToggle: () => void;
}

const GatewaySwitcher = ({ selectedGateway, gateways, onGatewaySelect, open, onToggle }: Props) => {
	return (
		<Box>
			<Popover isOpen={open} placement={"bottom-start"}>
				<PopoverTrigger>
					<Flex
						as={Link}
						px={3}
						py={2}
						onClick={onToggle}
						rounded={"md"}
						_hover={{
							textDecoration: "none",
							bg: useColorModeValue("gray.200", "gray.700"),
						}}>
						<Text
							textAlign={useBreakpointValue({ base: "center", md: "left" })}
							fontFamily={"heading"}
							color={useColorModeValue("gray.800", "white")}>
							{selectedGateway.name}
						</Text>
						<Icon
							as={ChevronDownIcon}
							transition={"all .25s ease-in-out"}
							transform={open ? "rotate(180deg)" : ""}
							w={6}
							h={6}
						/>
					</Flex>
				</PopoverTrigger>

				<PopoverContent
					display={{ base: "none", md: "flex" }}
					border={0}
					boxShadow={"xl"}
					p={4}
					rounded={"xl"}
					minW={"sm"}>
					<GatewaysList gateways={gateways} onGatewaySelect={onGatewaySelect} />
				</PopoverContent>

			</Popover>
		</Box>
	);
};

export default GatewaySwitcher;
