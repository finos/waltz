import angular from 'angular';


export default () => {

    const module = angular.module('waltz.databases', []);

    module
        .service('DatabaseStore', require('./services/database-store'));

    module
        .component('waltzDatabasePies', require('./components/database-pies'));

    return module.name;
};
