import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import React, {useEffect, useState} from "react";
import {Room} from "../MqGatewayMutableTypes";

export class RoomForm {
  name: string

  constructor(name?: string, readonly uuid?: string) {
    this.name = name || "";
  }

  static fromRoom(room: Room): RoomForm {
    return new RoomForm(room.name, room.uuid)
  }
}

interface RoomDetailsDialogProps {
  open: boolean,
  onClose: () => void,
  onDelete: (roomForm: RoomForm) => void,
  onSaveAndClose: (roomForm: RoomForm) => void,
  room: Room | null
}

export default function RoomDetailsDialog(props: RoomDetailsDialogProps) {

  const {room, open, onClose, onDelete, onSaveAndClose} = props
  const [roomForm, setRoomForm] = useState(new RoomForm())

  useEffect(() => {
    if (room) {
      setRoomForm(RoomForm.fromRoom(room))
    } else {
      setRoomForm(new RoomForm())
    }
  }, [room])

  const handleRoomChange = (event: React.ChangeEvent<{ value: string }>) => {
    setRoomForm({...roomForm, name: event.target.value})
  }

  return (<Dialog open={open} onClose={onClose} aria-labelledby="form-dialog-title">
    <DialogTitle id="form-dialog-title">Room</DialogTitle>
    <DialogContent>
      <TextField
        autoFocus
        margin="dense"
        id="name"
        label="Room name"
        fullWidth
        value={roomForm.name}
        onChange={handleRoomChange}
      />
    </DialogContent>
    <DialogActions>
      <Button onClick={() => onDelete(roomForm)} color="secondary">
        Delete
      </Button>
      <Button onClick={onClose} color="primary">
        Cancel
      </Button>
      <Button onClick={() => {onSaveAndClose(roomForm)}} color="primary">
        Save
      </Button>
    </DialogActions>
  </Dialog>)
}