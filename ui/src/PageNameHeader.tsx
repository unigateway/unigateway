import React from "react";
import { withRouter } from "react-router-dom";

class PageNameHeader extends React.Component<any> {

  render() {
    const path = this.props.location.pathname.slice(1);
    let title;
    switch (path) {
      case "": title = "Dashboard"; break;
      case "topology": title = "Topology"; break;
      case "devices": title = "Devices"; break;
      case "rules": title = "Rules"; break;
      case "logs": title = "Logs"; break;
      case "yamlconfig": title = "YAML Configuration"; break;
    }
    return (<span>{title}</span>)
  }
}

export default withRouter<any, any>(PageNameHeader);