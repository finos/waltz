export default (module) => {
    module
        .service('SoftwareCatalogStore', require('./services/software-catalog-store'));

    module
        .component('waltzSimpleSoftwareUsagePies', require('./components/simple-software-usage-pies'));

    module
        .directive('waltzSimpleSoftwareUsageList', require('./directives/simple-software-usage-list'))
        .directive('waltzMaturityStatus', require('./directives/maturity-status'))
        .directive('waltzSoftwareCatalogSection', require('./directives/software-catalog-section'));
};
