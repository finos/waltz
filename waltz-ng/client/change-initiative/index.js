
function setup(module) {
    module.service('ChangeInitiativeStore', require('./services/change-initiative-store'));

    module.directive('waltzChangeInitiativeTable', require('./directives/change-initiative-table'));

    module.config(require('./routes'));
}


export default setup;
