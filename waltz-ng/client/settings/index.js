import settingsStore from "./services/settings-store";
import hasSetting from "./directives/has-setting";

export default (module) => {
    module.service('SettingsStore', settingsStore);
    module.directive('waltzHasSetting', hasSetting);

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.settings', {
                    url: 'settings',
                    views: { 'content@': require('./settings-view') }
                });
        }
    ]);
};
