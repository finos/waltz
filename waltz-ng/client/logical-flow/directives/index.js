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
        .component('waltzAppDataFlowDiagram', require('./slope-graph/slope-graph-directive'));

    module
        .directive('waltzSummaryRatedFlowChart', require('./rated-flow-chart/summary-rated-flow-chart'))
        .directive('waltzRatedFlowDetail', require('./rated-flow-chart/rated-flow-detail'))
        .directive('waltzRatedFlowSummaryDetail', require('./rated-flow-chart/rated-flow-summary-detail'))
        .directive('waltzFlowCloudDiagram', require('./flow-cloud-diagram'))
        .directive('waltzLogicalFlowsTabgroupSection', require('./logical-flows-tabgroup-section'))
        .directive('waltzFlowFilterOptionsOverlay', require('./flow-filter-options-overlay'));


};
