import settingsStore from "./services/settings-store";
import hasSetting from "./directives/has-setting";

export default (module) => {
    module.service('SettingsStore', settingsStore);
    module.directive('waltzHasSetting', hasSetting);

    module.config(require('./routes'));
};
