/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
 */

export default (module) => {
    module
        .directive('waltzDataFlowSection', require('./data-flow-section'))
        .directive('waltzDataFlowTable', require('./data-flow-table'))
        .directive('waltzSummaryRatedFlowChart', require('./rated-flow-chart/summary-rated-flow-chart'))
        .directive('waltzRatedFlowDetail', require('./rated-flow-chart/rated-flow-detail'))
        .directive('waltzRatedFlowSummaryDetail', require('./rated-flow-chart/rated-flow-summary-detail'))
        .directive('waltzAppDataFlowDiagram', require('./slope-graph/slope-graph-directive'))
        .directive('waltzDataFlowDiagram', require('./boingy-graph/boingy-graph'))
        .directive('waltzFlowCloudDiagram', require('./flow-cloud-diagram'))
        .directive('waltzDataFlowsTabgroup', require('./data-flows-tabgroup'))
        .directive('waltzDataFlowsTabgroupSection', require('./data-flows-tabgroup-section'))
        .directive('waltzFlowFilterOptionsOverlay', require('./flow-filter-options-overlay'))
        .directive('waltzDataFlowTypeEditor', require('./edit/data-flow-type-editor'))
        .directive('waltzRatedFlowSummaryCell', require('./rated-flow-summary/rated-flow-summary-cell'))
        .directive('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .directive('waltzRatedFlowSummaryPanel', require('./rated-flow-summary/rated-flow-summary-panel'))
        .directive('waltzRatedFlowSummaryTable', require('./rated-flow-summary/rated-flow-summary-table'));
};
