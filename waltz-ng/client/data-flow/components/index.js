function setup(module) {
    module
        .component('waltzRatedFlowSummaryPanel', require('./rated-flow-summary/rated-flow-summary-panel'))
        .component('waltzRatedFlowSummaryCell', require('./rated-flow-summary/rated-flow-summary-cell'))
        .component('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .component('waltzRatedFlowSummaryTable', require('./rated-flow-summary/rated-flow-summary-table'));
}


export default setup;