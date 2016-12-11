import angular from 'angular';


export default () => {

    const module = angular.module('waltz.software.catalog', []);

    module
        .service('SoftwareCatalogStore', require('./services/software-catalog-store'));

    module
        .component('waltzSimpleSoftwareUsagePies', require('./components/simple-software-usage-pies'));

    module
        .directive('waltzSimpleSoftwareUsageList', require('./directives/simple-software-usage-list'))
        .directive('waltzMaturityStatus', require('./directives/maturity-status'))
        .directive('waltzSoftwareCatalogSection', require('./directives/software-catalog-section'));

    return module.name;
};
