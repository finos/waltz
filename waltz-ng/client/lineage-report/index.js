function setup(module) {

    module
        .config(require('./routes'))
        .component(
            'waltzLineageCandidateEditor',
            require('./components/candidate-editor/lineage-candidate-editor'))
        .service(
            'LineageReportStore',
            require('./services/lineage-report-store'))
        ;
}


export default setup;
