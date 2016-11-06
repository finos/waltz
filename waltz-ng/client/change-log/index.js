
export default (module) => {
    module
        .config(require('./routes'))
        .service('ChangeLogStore', require('./services/change-log-store'));

    module
        .component('waltzChangeLogSection', require('./components/change-log-section'))
        .component('waltzChangeLogTable', require('./components/change-log-table'));
};
