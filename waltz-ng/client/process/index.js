import angular from 'angular';


export default () => {
    const module = angular.module('waltz.process', []);

    module
        .config(require('./routes'));

    module
        .directive('waltzProcessList', require('./directives/process-list'));

    module
        .service('ProcessStore', require('./services/process-store'));

    return module.name;
}
