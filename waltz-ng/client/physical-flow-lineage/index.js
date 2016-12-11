import angular from 'angular';


function setup() {
    const module = angular.module('waltz.physical.flow.lineage', []);

    module
        .config(require('./routes'));

    module
        .component('waltzLineageCandidateEditor', require('./components/candidate-editor/lineage-candidate-editor'))
        .component('waltzPhysicalFlowLineagePanel', require('./components/lineage-panel/physical-flow-lineage-panel'));

    module
        .service('PhysicalFlowLineageStore', require('./services/physical-flow-lineage-store'));

    return module.name;
}


export default setup;
