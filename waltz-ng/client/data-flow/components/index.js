function setup(module) {
    module
        .component('waltzRatedFlowSummaryPanel', require('./rated-flow-summary/rated-flow-summary-panel'))
        .component('waltzRatedFlowSummaryCell', require('./rated-flow-summary/rated-flow-summary-cell'))
        .component('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .component('waltzRatedFlowSummaryTable', require('./rated-flow-summary/rated-flow-summary-table'))
        .component('waltzSourceAndTargetGraph', require('./source-and-target-graph/source-and-target'))
        .component('waltzDataFlowTypeEditor', require('./edit/data-flow-type-editor'))
}


export default setup;