function setup(module) {
    module
        .config(require('./routes'))
        .service(
            'PhysicalFlowStore',
            require('./service/physical-flow-store'));

    module
        .component('waltzPhysicalFlowTable', require('./components/flow-table/flow-table'));
}


export default setup;
