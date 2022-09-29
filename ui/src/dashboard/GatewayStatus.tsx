import React, {useContext, useEffect, useState} from 'react';
import Title from './Title';
import {GatewayCommunicationContext} from "../App";
import GatewayStatusResource, {ReleaseInfo} from "../communication/GatewayStatusResource";
import {makeStyles, Theme} from "@material-ui/core/styles";

const useStyles = makeStyles((theme: Theme) => ({
    elementsContainer: {
      paddingLeft: theme.spacing(2),
      paddingTop: theme.spacing(2),
      columnCount: 2
    },
    elementName: {
      display: "inline-block",
      fontWeight: "bold",
      paddingBottom: "5px",
      width: "150px"
    },
    upgradeInfo: {
      fontWeight: "bold",
      color: theme.palette.primary.main
    }
  })
);

export default function GatewayStatus() {

  const classes = useStyles();

  const { gatewayConnection } = useContext(GatewayCommunicationContext)

  const [status, setStatus] = useState<GatewayStatusResource | null>(null)
  const [newVersionAvailable, setNewVersionAvailable] = useState<ReleaseInfo | null>(null)

  useEffect(() => {
    gatewayConnection.fetchStatus().then(result => {
      setStatus(result)
      if (isUpgradeAvailable(result.firmwareVersion, result.mqGatewayLatestVersion.tag_name)) {
        setNewVersionAvailable(result.mqGatewayLatestVersion)
      } else {
        setNewVersionAvailable(null)
      }
    })
    const interval = setInterval(() => {
      gatewayConnection.fetchStatus().then(result => {
        setStatus(result)
        if (isUpgradeAvailable(result.firmwareVersion, result.mqGatewayLatestVersion.tag_name)) {
          setNewVersionAvailable(result.mqGatewayLatestVersion)
        } else {
          setNewVersionAvailable(null)
        }
      })
    }, 10000)
    return () => clearInterval(interval)
  }, [gatewayConnection])

  const isUpgradeAvailable = (currentVersion: string, latestVersion: string) => {
    const cleanedVersion = currentVersion.substr(0, currentVersion.indexOf("-"))
    return latestVersion && "v" + cleanedVersion !== latestVersion
  }

  const statusData = [
    {name: "CPU temperature", value: status?.cpuTemperature + "℃"},
    {name: "Uptime", value: status?.uptimeSeconds + " seconds"},
    {name: "IP address", value: status?.ipAddress},
    {name: "Free memory", value: status?.freeMemoryBytes ? Math.trunc(status.freeMemoryBytes / 1000000) + "MB" : "unknown"},
    {name: "MQTT status", value: status?.mqttConnected ? "connected" : "disconnected"},
    {name: "MySensors status", value: status?.mySensorsEnabled ? "enabled" : "disabled"},
    {name: "Firmware version", value: status?.firmwareVersion}
  ]

  return (
    <>
      <Title>Status</Title>
      <div className={classes.elementsContainer}>
        {statusData.map(element => (
          <div key={element.name}>
            <span className={classes.elementName}>{element.name}</span>
            <span>{element.value}</span>
          </div>
        ))}
        {newVersionAvailable && (
          <a href={newVersionAvailable.html_url} className={classes.upgradeInfo}>Upgrade to {newVersionAvailable.tag_name} available</a>
        )}
      </div>
    </>
  );
}
