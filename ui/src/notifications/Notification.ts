
export default class Notification {

  public static readonly CONFIGURATION_CHANGED_ID = "CONFIGURATION_CHANGED"
  public static readonly UPDATE_AVAILABLE_ID = "UPDATE_AVAILABLE"

  constructor(readonly id: string, readonly primaryText: string, readonly secondaryText: string, readonly action: () => void, readonly icon: JSX.Element) {
  }
}