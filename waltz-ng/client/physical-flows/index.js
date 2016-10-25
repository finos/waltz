function setup(module) {
    module
        .config(require('./routes'))
        .service(
            'PhysicalFlowStore',
            require('./service/physical-flow-store'));
}


export default setup;
