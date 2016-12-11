import angular from 'angular';

import nagMessageService from "./services/nag-message-service";
import settingsStore from "./services/settings-store";
import settingsService from "./services/settings-service";
import hierarchiesStore from "./services/hierarchies-store";
import hasSetting from "./directives/has-setting";

export default () => {

    const module = angular.module('waltz.system', []);

    module
        .service('NagMessageService', nagMessageService)
        .service('SettingsStore', settingsStore)
        .service('SettingsService', settingsService)
        .service('HierarchiesStore', hierarchiesStore);

    module
        .directive('waltzHasSetting', hasSetting);

    module
        .config(require('./routes'));


    return module.name;
};
