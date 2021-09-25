export default class ConfigurationReplacementResult {
  constructor(readonly success: boolean, readonly jsonValidationSucceeded: boolean, readonly validationFailures: string[]) {}
}