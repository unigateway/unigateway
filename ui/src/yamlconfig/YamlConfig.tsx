import React, {useState} from 'react';
import {makeStyles} from '@material-ui/core/styles';
import Container from '@material-ui/core/Container';
import SaveIcon from "@material-ui/icons/Save";
import Editor from "@monaco-editor/react";
import {Fab} from "@material-ui/core";

const useStyles = makeStyles((theme) => ({
  container: {
    paddingTop: theme.spacing(4),
    paddingBottom: theme.spacing(4),
  },
  actionButton: {
    position: "absolute",
    bottom: "15%",
    right: "5%",
  }
}));

type YamlConfigProps = {
  config: string,
  onSave: (newConfiguration: string) => void
}

export default function YamlConfig(props: YamlConfigProps) {
  const classes = useStyles();

  const [value, setValue] = useState(props.config);

  const handleSave = () => {
    if (value !== props.config) {
      props.onSave(value)
    }
  }

  function handleEditorChange(value: any) {
    setValue(value);
  }

  return (
    <Container maxWidth="xl" className={classes.container}>
      <Editor
        height="80vh"
        language="yaml"
        defaultValue={value}
        onChange={handleEditorChange}
      />
      <Fab color="primary" aria-label="add" className={classes.actionButton} onClick={handleSave} disabled={props.config === value}>
        <SaveIcon />
      </Fab>
    </Container>
  );
}