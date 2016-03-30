import settingsStore from "./service/settings-store";

export default (module) => {
    module.service('SettingsStore', settingsStore);
};
