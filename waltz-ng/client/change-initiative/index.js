import angular from 'angular';


function setup() {
    const module = angular.module('waltz.change.initiative', []);
    module
        .config(require('./routes'));

    module
        .service('ChangeInitiativeStore', require('./services/change-initiative-store'));

    module
        .directive('waltzChangeInitiativeTable', require('./directives/change-initiative-table'))
        .directive("waltzChangeInitiativeSelector", require("./directives/change-initiative-selector"));

    return module.name;
}


export default setup;
