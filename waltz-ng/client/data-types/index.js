export default (module) => {

    module.config(require('./routes'));

    module
        .service('DataTypeStore', require('./services/data-type-store'))
        .service('DataTypeService', require('./services/data-type-service'))
        .service('DataTypeViewDataService', require('./services/data-type-view-data'));

    module
        .component('waltzDataTypeOverview', require('./components/data-type-overview'))
        .component('waltzDataTypeFlowsTabgroup', require('./components/data-type-flows-tabgroup'))
        .component('waltzDataTypeFlowsTabgroupSection', require('./components/data-type-flows-tabgroup-section'))
        .component('waltzDataTypeTree', require('./components/data-type-tree'));

};
