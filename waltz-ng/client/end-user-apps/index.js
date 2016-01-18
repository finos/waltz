
export default (module) => {
    module.service('EndUserAppStore', require('./services/end-user-app-store'));
    module.directive('waltzEndUserAppTable', require('./directives/end-user-app-table'));
};
