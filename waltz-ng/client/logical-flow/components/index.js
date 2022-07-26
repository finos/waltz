/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {registerComponents} from "../../common/module-utils";
import BulkLogicalFlowLoaderWizard from "./bulk-logical-flow-loader-wizard/bulk-logical-flow-loader-wizard";
import BulkLogicalFlowParser from "./bulk-logical-flow-parser/bulk-logical-flow-parser";
import BulkLogicalFlowUploader from "./bulk-logical-flow-uploader/bulk-logical-flow-uploader";
import LogicalFlowTypeEditor from "./edit/logical-flow-type-editor";
import LogicalFlowEditPanel from "./logical-flow-edit-panel/logical-flow-edit-panel";
import SourceAndTargetGraph from "./source-and-target-graph/source-and-target-graph";
import SourceAndTargetPanel from "./source-and-target-panel/source-and-target-panel";
import RatedFlowSummaryPanel from "./rated-flow-summary/rated-flow-summary-panel";
import LogicalFlowsDataTypeSummaryPane from "./logical-flows-data-type-summary-pane/logical-flows-data-type-summary-pane";
import LogicalFlowsBoingyGraph from "./logical-flows-boingy-graph/logical-flows-boingy-graph";
import RatedFlowSummaryInfoCell from "./rated-flow-summary/rated-flow-summary-info-cell";
import RatedSummaryTable from "./rated-flow-summary/rated-summary-table";
import RatedSummaryCell from "./rated-flow-summary/rated-summary-cell";
import LogicalFlowDiagram from "./boingy-graph/boingy-graph";
import LogicalFlowCounterpartSelector from "./edit/logical-flow-counterpart-selector";
import LogicalFlowsTabgroup from "./logical-flows-tabgroup/logical-flows-tabgroup";
import LogicalFlowsTabgroupSection from "./logical-flows-tabgroup-section/logical-flows-tabgroup-section";
import LogicalFlowTable from "./../components/logical-flow-table/logical-flow-table";
import FlowFilterOptions from "./flow-filter-options/flow-filter-options";


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
        .component("waltzRatedFlowSummaryInfoCell", RatedFlowSummaryInfoCell)
        .component("waltzRatedSummaryTable", RatedSummaryTable)
        .component("waltzRatedSummaryCell", RatedSummaryCell)
        .component("waltzLogicalFlowDiagram", LogicalFlowDiagram)
        .component("waltzLogicalFlowCounterpartSelector", LogicalFlowCounterpartSelector)
        .component("waltzLogicalFlowsTabgroup", LogicalFlowsTabgroup)
        .component("waltzLogicalFlowsTabgroupSection", LogicalFlowsTabgroupSection)
        .component("waltzLogicalFlowTable", LogicalFlowTable)
        .component("waltzFlowFilterOptions", FlowFilterOptions);
}


export default setup;