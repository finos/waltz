/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
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
import BulkLogicalFlowLoaderWizard from './bulk-logical-flow-loader-wizard/bulk-logical-flow-loader-wizard';
import BulkLogicalFlowParser from './bulk-logical-flow-parser/bulk-logical-flow-parser';
import BulkLogicalFlowUploader from './bulk-logical-flow-uploader/bulk-logical-flow-uploader';
import LogicalFlowTypeEditor from './edit/logical-flow-type-editor';
import LogicalFlowEditPanel from './logical-flow-edit-panel/logical-flow-edit-panel';
import SourceAndTargetGraph from './source-and-target-graph/source-and-target-graph';
import SourceAndTargetPanel from './source-and-target-panel/source-and-target-panel';
import RatedFlowSummaryPanel from './rated-flow-summary/rated-flow-summary-panel';
import LogicalFlowsDataTypeSummaryPane from './logical-flows-data-type-summary-pane/logical-flows-data-type-summary-pane';
import LogicalFlowsBoingyGraph from './logical-flows-boingy-graph/logical-flows-boingy-graph';
import RatedFlowSummaryInfoCell from './rated-flow-summary/rated-flow-summary-info-cell';
import RatedSummaryTable from './rated-flow-summary/rated-summary-table';
import RatedSummaryCell from './rated-flow-summary/rated-summary-cell';
import AppCentricFlowTable from './app-centric-flow-table/app-centric-flow-table';
import LogicalFlowDiagram from './boingy-graph/boingy-graph';
import LogicalFlowCounterpartSelector from './edit/logical-flow-counterpart-selector';
import LogicalFlowsTabgroup from './logical-flows-tabgroup/logical-flows-tabgroup';
import LogicalFlowsTabgroupSection from './logical-flows-tabgroup-section/logical-flows-tabgroup-section';
import LogicalFlowTable from './../components/logical-flow-table/logical-flow-table';
import FlowFilterOptions from './flow-filter-options/flow-filter-options';


function setup(module) {

    registerComponents(module, [
        BulkLogicalFlowLoaderWizard,
        BulkLogicalFlowParser,
        BulkLogicalFlowUploader,
        LogicalFlowEditPanel,
        LogicalFlowTypeEditor,
        LogicalFlowsDataTypeSummaryPane,
        LogicalFlowsBoingyGraph,
        RatedFlowSummaryPanel,
        SourceAndTargetGraph,
        SourceAndTargetPanel
    ]);

    module
        .component('waltzRatedFlowSummaryInfoCell', RatedFlowSummaryInfoCell)
        .component('waltzRatedSummaryTable', RatedSummaryTable)
        .component('waltzRatedSummaryCell', RatedSummaryCell)
        .component('waltzAppCentricFlowTable', AppCentricFlowTable)
        .component('waltzLogicalFlowDiagram', LogicalFlowDiagram)
        .component('waltzLogicalFlowCounterpartSelector', LogicalFlowCounterpartSelector)
        .component('waltzLogicalFlowsTabgroup', LogicalFlowsTabgroup)
        .component('waltzLogicalFlowsTabgroupSection', LogicalFlowsTabgroupSection)
        .component('waltzLogicalFlowTable', LogicalFlowTable)
        .component('waltzFlowFilterOptions', FlowFilterOptions);
}


export default setup;