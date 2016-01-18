
export default (module) => {
    module.directive('waltzChangeLogSection', require('./directives/change-log-section'));
    module.service('ChangeLogDataService', require('./services/change-log-data'));
};
