import React from "react";
import {Accordion, AccordionDetails, AccordionSummary, TextField} from "@material-ui/core";

// TODO Where should it be displayed? It is not displayed anywhere now.
class GeneralSettings extends React.Component {

  render() {
    return (
        <Accordion>
          <AccordionSummary>General settings</AccordionSummary>
          <AccordionDetails>
            <form noValidate autoComplete="off">
              <TextField id="configVersion" label="Config version" fullWidth />
              <TextField id="name" label="Name" fullWidth />
              <TextField id="mqttHostname" label="MQTT hostname" fullWidth />
            </form>
          </AccordionDetails>
        </Accordion>
    );
  }
}

export default GeneralSettings;