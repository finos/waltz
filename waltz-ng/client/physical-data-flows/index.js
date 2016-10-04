function setup(module) {
    module
        .service(
            'PhysicalDataFlowStore',
            require('./service/physical-data-flow-store'));
}


export default setup;
