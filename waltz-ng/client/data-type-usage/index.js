export default (module) => {
    module
        .directive('waltzAppDataTypeUsageList', require("./directives/app-data-type-usage-list"))
        .service('DataTypeUsageStore', require('./services/data-type-usage-store'));
};
