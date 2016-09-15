export default (module) => {
    module
        .directive('waltzAppDataTypeUsageList', require("./directives/app-data-type-usage-list"))

    module
        .component('waltzDataTypeUsageStatTable', require('./components/stat-table/data-type-usage-stat-table'))
        .component('waltzAppDataTypeUsageEditor', require("./components/editor/app-data-type-usage-editor"));

    module
        .service('DataTypeUsageStore', require('./services/data-type-usage-store'));
};
