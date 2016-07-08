

export default (module) => {
    module
        .directive('waltzAppDataTypeUsageList', require("./directives/app-data-type-usage-list"))
        .directive('waltzAppDataTypeUsageEditor', require("./directives/app-data-type-usage-editor"))
        .service('DataTypeUsageStore', require('./services/data-type-usage-store'));
};
