function setup(module) {

    module
        .config(require('./routes'))
        .component(
            'waltzLineageCandidateEditor',
            require('./components/candidate-editor/lineage-candidate-editor'))
        .component(
            'waltzPhysicalFlowLineagePanel',
            require('./components/lineage-panel/lineage-panel'))
        .service(
            'PhysicalFlowLineageStore',
            require('./services/physical-flow-lineage-store'))
        ;
}


export default setup;
