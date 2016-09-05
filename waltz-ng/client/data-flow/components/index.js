function setup(module) {
    module
        .component('waltzRatedFlowSummaryPanel', require('./rated-flow-summary/rated-flow-summary-panel'))
        .component('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .component('waltzRatedSummaryTable', require('./rated-flow-summary/rated-summary-table'))
        .component('waltzRatedSummaryCell', require('./rated-flow-summary/rated-summary-cell'))
        .component('waltzSourceAndTargetGraph', require('./source-and-target-graph/source-and-target'))
        .component('waltzDataFlowTypeEditor', require('./edit/data-flow-type-editor'))
        .component('waltzDataFlowDiagram', require('./boingy-graph/boingy-graph'))

}


export default setup;