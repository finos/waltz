function setup(module) {

    module
        .config(require('./routes'))
        .component(
            'waltzLineageCandidateEditor',
            require('./components/candidate-editor/lineage-candidate-editor'))
        .component(
            'waltzLineageFlowTabgroup',
            require('./components/lineage-flow-tabgroup/lineage-flow-tabgroup'))
        .service(
            'LineageReportStore',
            require('./services/lineage-report-store'))
        ;
}


export default setup;
