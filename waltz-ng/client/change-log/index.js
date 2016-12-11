import angular from 'angular';

export default () => {
    const module = angular.module('waltz.change.log', []);

    module
        .config(require('./routes'));

    module
        .service('ChangeLogStore', require('./services/change-log-store'));

    module
        .component('waltzChangeLogSection', require('./components/change-log-section'))
        .component('waltzChangeLogTable', require('./components/change-log-table'));

    return module.name;
};
