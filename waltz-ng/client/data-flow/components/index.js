function setup(module) {
    module
        .component('waltzRatedFlowSummaryPanel', require('./rated-flow-summary/rated-flow-summary-panel'))
        .component('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .component('waltzRatedSummaryTable', require('./rated-flow-summary/rated-summary-table'))
        .component('waltzRatedSummaryCell', require('./rated-flow-summary/rated-summary-cell'))
        .component('waltzSourceAndTargetGraph', require('./source-and-target-graph/source-and-target'))
        .component('waltzAppCentricFlowTable', require('./app-centric-flow-table/app-centric-flow-table'))
        .component('waltzDataFlowTypeEditor', require('./edit/data-flow-type-editor'))
        .component('waltzDataFlowDiagram', require('./boingy-graph/boingy-graph'))
        .component('waltzDataFlowsTabgroup', require('./data-flows-tabgroup/data-flows-tabgroup'))
        .component('waltzDataFlowTable', require('./../components/data-flow-table/data-flow-table'))


}


export default setup;