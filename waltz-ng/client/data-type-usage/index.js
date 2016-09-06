export default (module) => {
    module
        .directive('waltzAppDataTypeUsageList', require("./directives/app-data-type-usage-list"))
        .directive('waltzAppDataTypeUsageEditor', require("./directives/app-data-type-usage-editor"));

    module
        .component('waltzDataTypeUsageStatTable', require('./components/data-type-usage-stat-table'));

    module
        .service('DataTypeUsageStore', require('./services/data-type-usage-store'));
};
