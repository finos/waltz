export default (module) => {
    module.service('SoftwareCatalogStore', require('./services/software-catalog-store'));
    module.directive('waltzSimpleSoftwareUsageList', require('./directives/simple-software-usage-list'));
    module.directive('waltzSimpleSoftwareUsagePies', require('./directives/simple-software-usage-pies'));
    module.directive('waltzMaturityStatus', require('./directives/maturity-status'));
};
