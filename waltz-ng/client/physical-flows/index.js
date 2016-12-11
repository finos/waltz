import angular from 'angular';


function setup() {
    const module = angular.module('waltz.physical.flows', []);

    module
        .config(require('./routes'));

    module
        .service('PhysicalFlowStore', require('./service/physical-flow-store'));

    module
        .component('waltzPhysicalFlowOverview', require('./components/overview/physical-flow-overview'))
        .component('waltzPhysicalFlowEditOverview', require('./components/register/physical-flow-edit-overview'))
        .component("waltzPhysicalFlowEditSpecification", require('./components/register/physical-flow-edit-specification'))
        .component('waltzPhysicalFlowTable', require('./components/flow-table/physical-flow-table'))
        .component('waltzPhysicalFlowEditTargetEntity', require('./components/edit-target-entity/physical-flow-edit-target-entity'))
        .component('waltzPhysicalFlowExportButtons', require('./components/export-buttons/physical-flow-export-buttons'))
        .component('waltzPhysicalFlowAttributeEditor', require('./components/attribute-editor/physical-flow-attribute-editor'));

    return module.name;
}


export default setup;
