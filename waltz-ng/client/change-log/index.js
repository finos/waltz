
export default (module) => {
    module
        .config(require('./routes'))
        .service('ChangeLogDataService', require('./services/change-log-data'))
        .directive('waltzChangeLogSection', require('./directives/change-log-section'))
        .directive('waltzChangeLogTable', require('./directives/change-log-table'));
};
