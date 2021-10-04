import Button from '@material-ui/core/Button';
import TextField from '@material-ui/core/TextField';
import Dialog from '@material-ui/core/Dialog';
import DialogActions from '@material-ui/core/DialogActions';
import DialogContent from '@material-ui/core/DialogContent';
import DialogTitle from '@material-ui/core/DialogTitle';
import React, {useEffect, useState} from "react";
import {Point} from "../MqGatewayMutableTypes";
import {FormControl, MenuItem, Select} from "@material-ui/core";

export class PointForm {
  name: string
  portNumber: number

  constructor(name?: string, portNumber?: number, readonly uuid?: string) {
    this.name = name || "";
    this.portNumber = portNumber || 1
  }

  static fromPoint(point: Point): PointForm {
    return new PointForm(point.name, point.portNumber, point.uuid)
  }
}

interface PointDetailsDialogProps {
  open: boolean,
  onClose: () => void,
  onDelete: (pointForm: PointForm) => void,
  onSaveAndClose: (pointForm: PointForm) => void,
  point: Point | null
}

export default function PointDetailsDialog(props: PointDetailsDialogProps) {

  const {point, open, onClose, onDelete, onSaveAndClose} = props
  const [pointForm, setPointForm] = useState(new PointForm())
  const possiblePortNumbers = [1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,25,26,27,28,29,30,31,32]

  useEffect(() => {
    if (point) {
      setPointForm(PointForm.fromPoint(point))
    } else {
      setPointForm(new PointForm())
    }
  }, [point])

  const handlePointChange = (event: React.ChangeEvent<{ value: string }>) => {
    setPointForm({...pointForm, name: event.target.value})
  }

  const handlePortNumberChange = (event: React.ChangeEvent<{ value: unknown }>) => {
    setPointForm({...pointForm, portNumber: parseInt(event.target.value as string)})
  }

  return (<Dialog open={open} onClose={onClose} aria-labelledby="form-dialog-title">
    <DialogTitle id="form-dialog-title">Point</DialogTitle>
    <DialogContent>
      <FormControl>
        <TextField
          autoFocus
          margin="dense"
          id="name"
          label="Point name"
          fullWidth
          value={pointForm.name}
          onChange={handlePointChange}
        />
        <Select
          label="Port number"
          labelId="port-number-label"
          id="port-number"
          value={pointForm.portNumber}
          onChange={handlePortNumberChange}
        >
          {possiblePortNumbers.map((portNumber) =>
            <MenuItem key={portNumber} value={portNumber}>{portNumber}</MenuItem>
          )}
        </Select>
      </FormControl>
    </DialogContent>
    <DialogActions>
      <Button onClick={() => onDelete(pointForm)} color="secondary">
        Delete
      </Button>
      <Button onClick={onClose} color="primary">
        Cancel
      </Button>
      <Button onClick={() => {onSaveAndClose(pointForm)}} color="primary">
        Save
      </Button>
    </DialogActions>
  </Dialog>)
}