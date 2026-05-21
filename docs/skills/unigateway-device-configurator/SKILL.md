---
name: unigateway-device-configurator
description: Create or update UniGateway device configuration YAML (`gateway.yaml`/`unigateway.yaml`) using schema-safe rules. Use when a user wants to configure UniGateway from scratch, add one or more devices to an existing config, or validate/edit connectors and `internalDevices` references for MqGateway, Raspberry Pi, or MySensors.
---

# UniGateway Device Configuration Skill

Follow this skill to configure devices for UniGateway in two modes:

1. Full setup (`new file` or `rebuild from zero`)
2. Incremental update (`add a new device` to an existing file)

Always ask for missing details before generating final YAML.

## Supported Types and Core Schema Rules

Treat these rules as mandatory unless the user explicitly asks to bypass schema validation:

- Top-level required fields:
  - `configVersion` (must be `"1.0"`)
  - `id`
  - `name`
  - `devices` (array, at least 1 item)
- Device must have exactly one configuration style:
  - simple device: `id`, `name`, `type`, `connectors`
  - complex device: `id`, `name`, `type`, `internalDevices`
- `id` rules: no whitespace, max length 32, unique across devices.
- `name` max length: 64.
- Allowed `type` values (schema):
  - `RELAY`, `SWITCH_BUTTON`, `REED_SWITCH`, `MOTION_DETECTOR`, `EMULATED_SWITCH`, `TIMER_SWITCH`, `SHUTTER`, `GATE`, `TEMPERATURE`, `HUMIDITY`, `LIGHT`, `BUZZER`

If a user asks for an unsupported type, warn that schema validation will fail and ask whether to proceed with a non-schema extension.

## Connector Rules

### Hardware connector: MqGateway

Required:

- `portNumber` (integer 1..32)
- `wireColor` (`BLUE`, `BLUE_WHITE`, `GREEN`, `GREEN_WHITE`)

Optional:

- `debounceMs` (integer, minimum 0)

### Hardware connector: Raspberry Pi

Required:

- `gpio`

Optional:

- `debounceMs` (minimum 0)
- `pullUpDown` (`PULL_UP` or `PULL_DOWN`)

### MySensors connector

Required fields:

- `source: MYSENSORS`
- `nodeId`
- `sensorId`
- `type` (MySensors value type, e.g. `V_TEMP`, `V_HUM`, `V_TRIPPED`)

## Internal Device References (Complex Devices)

For complex devices, each nested entry must contain a valid `referenceId` pointing to another device `id`.

- `LIGHT`
  - required: `relay`
  - optional: `switch1`, `switch2`, `switch3`
- `SHUTTER`
  - required: `stopRelay`, `upDownRelay`
  - required config: `fullCloseTimeMs`, `fullOpenTimeMs`
- `GATE`
  - required control strategy:
    - either `actionButton`
    - or all three: `openButton`, `closeButton`, `stopButton`
  - optional: `openReedSwitch`, `closedReedSwitch`
  - optional config: `haDeviceClass` (`garage` or `gate`)

When adding a complex device, if required referenced internal devices do not yet exist, create them first (or in the same change set).

## Device-Specific Optional Config (Ask Only When Relevant)

- `RELAY`
  - `triggerLevel` (`LOW`/`HIGH`)
  - `haComponent` (`switch`/`light`)
  - `haDeviceClass` (if `haComponent` is `switch`)
- `SWITCH_BUTTON`
  - `longPressTimeMs`
  - `haComponent` (`binary_sensor`, `trigger`, `sensor`)
- `MOTION_DETECTOR`
  - `motionSignalLevel` (`HIGH`/`LOW`)
- `REED_SWITCH`
  - `haDeviceClass` (e.g. `door`, `window`, `opening`, `garage_door`)
- `TEMPERATURE`, `HUMIDITY`
  - `minUpdateIntervalMs`
- `EMULATED_SWITCH`, `TIMER_SWITCH`
  - no extra config required
- `BUZZER`
  - `triggerLevel` (`LOW`/`HIGH`)

## Conversation Workflow

### 1) Determine mode

Ask:

- `Do you want to create a full UniGateway config from scratch, or add a device to an existing config?`
- `Which file should I edit?` (default `gateway.yaml`/provided path)

### 2) Collect global context

Ask for:

- Gateway `name`
- Gateway `id`
- Hardware platform for hardware connectors: `MQGATEWAY` or `RASPBERRYPI`
- Whether MySensors connectors are used

If editing an existing file, parse and reuse existing top-level values unless user wants to change them.

### 3) Collect device details (repeat per device)

For each new or updated device, collect in this order:

1. `type`
2. `name`
3. `id`
4. Simple vs complex structure
5. Connector/internal device details
6. Optional `config` keys relevant to that type

If required details are missing, ask short targeted follow-up questions before writing YAML.

### 4) Validate before final output

Run this checklist:

- All required top-level fields exist.
- Every device has required fields for its structure.
- No duplicate `id` values.
- All `referenceId` values resolve to existing device `id`s.
- Connector fields match selected platform/source rules.
- Enumerations and ranges are valid.

### 5) Produce output

- For full setup:
  - return complete YAML file.
- For add-device update:
  - return exact YAML block(s) to append/insert.
  - if requested, return full updated file content.

Preserve existing style and ordering when editing existing configs. Do not delete or rewrite unrelated devices.

## Clarifying Question Bank

Use these prompts when data is missing:

- `What should be the user-facing device name and unique device id?`
- `Is this a simple device (connectors) or a complex device (internalDevices references)?`
- `Is the connector hardware-based or MySensors-based?`
- `For MqGateway: what portNumber (1-32) and wireColor (BLUE/BLUE_WHITE/GREEN/GREEN_WHITE)?`
- `For Raspberry Pi: which gpio, and do you want custom debounceMs/pullUpDown?`
- `For MySensors: what nodeId, sensorId, and message type (for example V_TEMP/V_HUM/V_TRIPPED)?`
- `If this is a complex device, which existing device ids should be referenced?`
- `Do you want optional Home Assistant tuning in config (haComponent/haDeviceClass/etc.)?`

## Editing Safety Rules

- Never introduce duplicate `id`s.
- Never leave unresolved `referenceId`s.
- Keep quoted strings as in surrounding file style when possible.
- Keep changes minimal and localized for incremental updates.
- If a requested change conflicts with schema, explain conflict and offer two paths:
  - schema-compliant alternative
  - explicit non-schema extension
