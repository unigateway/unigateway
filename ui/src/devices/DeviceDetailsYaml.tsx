import React from 'react';
import {Box} from "@material-ui/core";
import yaml from 'js-yaml';
import SyntaxHighlighter from 'react-syntax-highlighter';
import {githubGist} from 'react-syntax-highlighter/dist/cjs/styles/hljs';
import {DeviceForm} from "./DeviceDetailsDialog";

interface DeviceDetailsYamlProps {
  device: DeviceForm | null
}

export default function DeviceDetailsYaml(props: DeviceDetailsYamlProps) {

  const device = props.device

  const toYamlableObject = (device: DeviceForm | null) => {
    if (device == null) {
      return {}
    }
    return DeviceForm.toDevice(device).toDataObject()
  }

  return (
    <Box>
        <SyntaxHighlighter language="yaml" style={githubGist}>
          {yaml.dump(toYamlableObject(device))}
        </SyntaxHighlighter>
    </Box>
  )

}
