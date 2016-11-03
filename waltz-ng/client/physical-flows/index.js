function setup(module) {
    module
        .config(require('./routes'))
        .service(
            'PhysicalFlowStore',
            require('./service/physical-flow-store'))
        .component(
            'waltzPhysicalFlowOverview',
            require('./components/overview/physical-flow-overview'))
        .component(
            'waltzPhysicalFlowEditOverview',
            require('./components/register/physical-flow-edit-overview'))
        .component(
            "waltzPhysicalFlowEditSpecification",
            require('./components/register/physical-flow-edit-specification'))
        .component(
            'waltzPhysicalFlowTable',
            require('./components/flow-table/flow-table'))
        .component(
            'waltzPhysicalFlowEditTargetEntity',
            require('./components/edit-target-entity/physical-flow-edit-target-entity'))
        .component(
            'waltzPhysicalFlowAttributeEditor',
            require('./components/attribute-editor/physical-flow-attribute-editor'));
}


export default setup;
