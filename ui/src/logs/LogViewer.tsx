import {useEffect, useState} from "react";

import {GatewayLogsWS, Log} from "../communication/GatewayLogsWS";
import dayjs from "dayjs";
import {makeStyles} from "@material-ui/core/styles";
import {Typography} from "@material-ui/core";

const useStyles = makeStyles(() => ({
  logLine: {
    whiteSpace: "nowrap"
  },
  logField: {
    display: "inline-block",
    paddingRight: "10px",
    whiteSpace: "nowrap"
  }
}));

interface LogViewerProps {
  readLogs: boolean
  readInitialLogsOnStart?: boolean
  maxElements?: number
}

export default function LogViewer(props: LogViewerProps) {
  const classes = useStyles();

  const [logs, setLogs] = useState<Log[]>([])

  const logsWebSocket = new GatewayLogsWS(`ws://localhost:8080/logs/uiLogViewer`);

  useEffect(() => {
    if (props.readLogs) {
      start()
    } else {
      stop()
    }
    return () => { stop() }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [props.readLogs])

  const start = () => {
    logsWebSocket.onLog(log => {
        setLogs(previousLogs => {
          previousLogs.push(log);
          if (props.maxElements && previousLogs.length >= props.maxElements) {
            previousLogs.shift()
          }
          return previousLogs
        })
      }
    )
    const initialLogsPromise = logsWebSocket.startReadingLogs();
    if (props.readInitialLogsOnStart) {
      initialLogsPromise.then(initialLogs => {
        if (props.maxElements && initialLogs.length >= props.maxElements) {
          initialLogs = initialLogs.slice(0 - props.maxElements)
        }
        setLogs(initialLogs)
      })
    }
  }

  const stop = () => {
    logsWebSocket.disconnect()
  }

  const logLevelText = (logLevel: string) => {
    let color: string
    switch (logLevel) {
      case "ERROR":
        color = "red"
        break;
      case "WARN":
        color = "orange"
        break;
      case "INFO":
        color = "green"
        break;
      case "DEBUG":
        color = "black"
        break;
      case "TRACE":
        color = "gray"
        break;
      default:
        color = "black"

    }
    return (
      <Typography style={{ color: color }}>{logLevel}</Typography>
    )
  }

  return (
    <>
      {logs.map(log => {
        return (
          <div className={classes.logLine}>
            <div className={classes.logField}>{dayjs(log.time).format('YYYY-MM-DD HH:mm:ss')}</div>
            <div className={classes.logField}>{logLevelText(log.level)}</div>
            <div className={classes.logField}>{log.message}</div>
          </div>
        )
      })}
    </>
  )
}