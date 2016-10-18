function setup(module) {
    module
        .service(
            'PhysicalFlowStore',
            require('./service/physical-flow-store'));
}


export default setup;
