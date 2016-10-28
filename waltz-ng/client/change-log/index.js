
export default (module) => {
    module
        .config(require('./routes'))
        .service('ChangeLogDataService', require('./services/change-log-data'));

    module
        .component('waltzChangeLogSection', require('./components/change-log-section'))
        .component('waltzChangeLogTable', require('./components/change-log-table'));
};
