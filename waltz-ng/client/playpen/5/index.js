import angular from "angular";
import playpenView5 from "./playpen5";

import dataService from './data-service';
import entNameNotesPanel2 from './components/entity-named-notes-panel';

export default () => {
    const module = angular.module('waltz.playpen5', []);

    module.config(['$stateProvider', ($stateProvider) => {
        $stateProvider.state('main.playpen.5', {url: '/5', views: {'content@': playpenView5}})
    }]);

    module.service('DataService', dataService);

    module.component('waltzEntityNamedNotesPanel2', entNameNotesPanel2);

    function configResetViewData($rootScope, dataService) {
        $rootScope.$on('$stateChangeSuccess', () => {
            dataService.resetViewData();
        });
    }

    configResetViewData.$inject = ['$rootScope', 'DataService'];

    module.run(configResetViewData);

    return module.name;
};
