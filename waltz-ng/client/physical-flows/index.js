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
            "waltzPhysicalFlowEditSpecification",
            require('./components/register/physical-flow-edit-specification'))
        .component(
            'waltzPhysicalFlowTable',
            require('./components/flow-table/flow-table'));
}


export default setup;
