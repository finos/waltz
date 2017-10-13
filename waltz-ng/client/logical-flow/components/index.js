/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {registerComponents} from "../../common/module-utils";
import * as LogicalFlowTypeEditor from './edit/logical-flow-type-editor';
import * as LogicalFlowEditPanel from './logical-flow-edit-panel/logical-flow-edit-panel';
import * as SourceAndTargetGraph from './source-and-target-graph/source-and-target-graph';
import * as SourceAndTargetPanel from './source-and-target-panel/source-and-target-panel';
import * as RatedFlowSummaryPanel from './rated-flow-summary/rated-flow-summary-panel';
import LogicalFlowsDataTypeSummaryPane from './logical-flows-data-type-summary-pane/logical-flows-data-type-summary-pane';


function setup(module) {

    registerComponents(module, [
        LogicalFlowEditPanel,
        LogicalFlowTypeEditor,
        LogicalFlowsDataTypeSummaryPane,
        RatedFlowSummaryPanel,
        SourceAndTargetGraph,
        SourceAndTargetPanel
    ]);

    module
        .component('waltzRatedFlowSummaryInfoCell', require('./rated-flow-summary/rated-flow-summary-info-cell'))
        .component('waltzRatedSummaryTable', require('./rated-flow-summary/rated-summary-table'))
        .component('waltzRatedSummaryCell', require('./rated-flow-summary/rated-summary-cell'))
        .component('waltzAppCentricFlowTable', require('./app-centric-flow-table/app-centric-flow-table'))
        .component('waltzLogicalFlowDiagram', require('./boingy-graph/boingy-graph'))
        .component('waltzLogicalFlowCounterpartSelector', require('./edit/logical-flow-counterpart-selector'))
        .component('waltzLogicalFlowsTabgroup', require('./logical-flows-tabgroup/logical-flows-tabgroup'))
        .component('waltzLogicalFlowsTabgroupSection', require('./logical-flows-tabgroup-section/logical-flows-tabgroup-section'))
        .component('waltzLogicalFlowTable', require('./../components/logical-flow-table/logical-flow-table'))
        .component('waltzFlowFilterOptions', require('./flow-filter-options/flow-filter-options'));
}


export default setup;