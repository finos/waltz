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
    module.directive('waltzDataFlowSection', require('./data-flow-section'));
    module.directive('waltzDataFlowTable', require('./data-flow-table'));
    module.directive('waltzExpandedRatedFlowChart', require('./rated-flow-chart/expanded-rated-flow-chart'));
    module.directive('waltzSummaryRatedFlowChart', require('./rated-flow-chart/summary-rated-flow-chart'));
    module.directive('waltzRatedFlowDetail', require('./rated-flow-chart/rated-flow-detail'));
    module.directive('waltzRatedFlowSummaryDetail', require('./rated-flow-chart/rated-flow-summary-detail'));
    module.directive('waltzAppDataFlowDiagram', require('./slope-graph/slope-graph-directive'));
    module.directive('waltzDataFlowDiagram', require('./boingy-graph/boingy-graph'));
    module.directive('waltzFlowCloudDiagram', require('./flow-cloud-diagram'));
    module.directive('waltzDataFlowsTabgroup', require('./data-flows-tabgroup'));
    module.directive('waltzDataFlowsTabgroupSection', require('./data-flows-tabgroup-section'));
    module.directive('waltzFlowFilterOptionsOverlay', require('./flow-filter-options-overlay'));
    module.directive('waltzDataFlowTypeEditor', require('./edit/data-flow-type-editor'));
};
