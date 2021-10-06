import React, {useContext, useState} from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import {GatewayConfigurationContext} from "../App";
import {Fab, List, Paper} from "@material-ui/core";
import RoomComponent from "./RoomComponent";
import AddIcon from "@material-ui/icons/Add";
import RoomDetailsDialog, {RoomForm} from "./RoomDetailsDialog";
import {GatewayConfiguration, Point, Room} from "../MqGatewayMutableTypes";
import PointDetailsDialog, {PointForm} from "./PointDetailsDialog";

const useStyles = makeStyles((theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  },
  fabs: {
    position: 'absolute',
    bottom: theme.spacing(5),
    right: theme.spacing(8),
  },
}));

export default function Topology() {
  const classes = useStyles();

  const { gatewayConfiguration, setGatewayConfiguration } = useContext(GatewayConfigurationContext);
  const [chosenRoom, setChosenRoom] = useState<Room | null>(null)
  const [chosenPoint, setChosenPoint] = useState<Point | null>(null)
  const [chosenPointRoom, setChosenPointRoom] = useState<Room | null>(null)
  const [roomDetailsDialogOpen, setRoomDetailsDialogOpen] = useState(false)
  const [pointDetailsDialogOpen, setPointDetailsDialogOpen] = useState(false)

  const handleOpenRoomDetailsDialog = (room: Room | null) => {
    setChosenRoom(room)
    setRoomDetailsDialogOpen(true)
  }

  const handleCloseRoomDetailsDialog = () => {
    setChosenRoom(null)
    setRoomDetailsDialogOpen(false)
  }

  const handleDeleteRoom = (roomForm: RoomForm) => {
    setGatewayConfiguration(previousGatewayConfiguration => {
      previousGatewayConfiguration.deleteRoom(roomForm.uuid!)

      const newGatewayConfiguration = new GatewayConfiguration(previousGatewayConfiguration.configVersion, previousGatewayConfiguration.name,
        previousGatewayConfiguration.mqttHostname, previousGatewayConfiguration.rooms);
      newGatewayConfiguration.isModified = true
      return newGatewayConfiguration
    })
    handleCloseRoomDetailsDialog()
  }

  const handleSaveRoomDetails = (roomForm: RoomForm) => {
    setGatewayConfiguration(previousGatewayConfiguration => {
      const existingRoom = previousGatewayConfiguration.findRoomByUuid(roomForm.uuid!)
      if (existingRoom) {
        existingRoom.name = roomForm.name
      } else {
        previousGatewayConfiguration.addRoom(new Room(roomForm.name, []))
      }
      const newGatewayConfiguration = new GatewayConfiguration(previousGatewayConfiguration.configVersion, previousGatewayConfiguration.name,
        previousGatewayConfiguration.mqttHostname, previousGatewayConfiguration.rooms);
      newGatewayConfiguration.isModified = true

      return newGatewayConfiguration
    })
    setChosenRoom(null)
    setRoomDetailsDialogOpen(false)
  }


  const handleOpenPointDetailsDialog = (point: Point | null, room: Room) => {
    setChosenPoint(point)
    setChosenPointRoom(room)
    setPointDetailsDialogOpen(true)
  }

  const handleClosePointDetailsDialog = () => {
    setPointDetailsDialogOpen(false)
    setChosenPoint(null)
    setChosenPointRoom(null)
  }

  const handleDeletePoint = (pointForm: PointForm) => {
    setGatewayConfiguration(previousGatewayConfiguration => {
      chosenPointRoom!.deletePoint(pointForm.uuid!)

      const newGatewayConfiguration = new GatewayConfiguration(previousGatewayConfiguration.configVersion, previousGatewayConfiguration.name,
        previousGatewayConfiguration.mqttHostname, previousGatewayConfiguration.rooms);
      newGatewayConfiguration.isModified = true
      return newGatewayConfiguration
    })
    handleClosePointDetailsDialog()
  }

  const handleSavePointDetails = (pointForm: PointForm) => {
    setGatewayConfiguration(previousGatewayConfiguration => {
      const existingPoint = previousGatewayConfiguration.findPointByUuid(pointForm.uuid!)
      if (existingPoint) {
        existingPoint.name = pointForm.name
        existingPoint.portNumber = pointForm.portNumber
      } else {
        chosenPointRoom!.addPoint(new Point(pointForm.name, pointForm.portNumber, []))
      }
      const newGatewayConfiguration = new GatewayConfiguration(previousGatewayConfiguration.configVersion, previousGatewayConfiguration.name,
        previousGatewayConfiguration.mqttHostname, previousGatewayConfiguration.rooms);
      newGatewayConfiguration.isModified = true

      return newGatewayConfiguration
    })
    handleClosePointDetailsDialog()
  }

  return (
    <Container maxWidth="xl" className={classes.container}>
      <Paper>
        <List>
          { gatewayConfiguration.rooms.map((room) => {
            return (
              <RoomComponent key={room.uuid} room={room} onClick={() => handleOpenRoomDetailsDialog(room)}
                             onPointClick={(point) => handleOpenPointDetailsDialog(point, room)} />
            )})
          }
        </List>
      </Paper>
      <div className={classes.fabs}>
        <Fab variant="extended" aria-label="add room" color="primary" size="large" onClick={() => handleOpenRoomDetailsDialog(null)}>
          <AddIcon />
          Add Room
        </Fab>
      </div>
      <RoomDetailsDialog
        room={chosenRoom}
        open={roomDetailsDialogOpen}
        onClose={handleCloseRoomDetailsDialog}
        onDelete={handleDeleteRoom}
        onSaveAndClose={handleSaveRoomDetails}
      />
      <PointDetailsDialog
        point={chosenPoint}
        open={pointDetailsDialogOpen}
        onClose={handleClosePointDetailsDialog}
        onDelete={handleDeletePoint}
        onSaveAndClose={handleSavePointDetails}
      />
    </Container>
  );
}