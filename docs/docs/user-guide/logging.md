In case of any problems it is worth to look into MqGateway logs. This page describes where to find logs.

## Current log file

Current logs of MqGateway application can be found in `logs/application.log` file. Path is relative from where MqGateway is started. 


## Logs rotation

Log files are rolled every day at midnight. Logs from previous days can be found in files with named: `logs/app-%d{yyyy-MM-dd}.log`.
Log files older than 30 days are deleted automatically.