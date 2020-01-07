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

import angular from "angular";

import { registerComponents, registerStores } from "../common/module-utils";

import BulkPhysicalFlowLoaderWizard from "./components/bulk-physical-flow-loader-wizard/bulk-physical-flow-loader-wizard";
import BulkPhysicalFlowParser from "./components/bulk-physical-flow-parser/bulk-physical-flow-parser";
import BulkPhysicalFlowUploader from "./components/bulk-physical-flow-uploader/bulk-physical-flow-uploader";
import * as PhysicalFlowStore from "./services/physical-flow-store";
import * as PhysicalFlowParticipantStore from "./services/physical-flow-participant-store";
import FlowSpecDefinitionSection from "./components/flow-spec-definition-section/flow-spec-definition-section";
import PhysicalFlowEditor from "./components/flow-editor/physical-flow-editor";
import PhysicalFlowCloneSelector from "./components/register/clone/physical-flow-clone-selector";
import PhysicalFlowOverview from "./components/overview/physical-flow-overview";
import PhysicalFlowOverviewNode from "./components/overview/physical-flow-overview-node";
import PhysicalFlowEditOverview from "./components/register/overview/physical-flow-edit-overview";
import PhysicalFlowEditTargetLogicalFlow from "./components/register/edit-target-logical-flow/physical-flow-edit-target-logical-flow";
import PhysicalFlowEditSpecification from "./components/register/edit-specification/physical-flow-edit-specification";
import PhysicalFlowAttributeEditor from "./components/register/attribute-editor/physical-flow-attribute-editor";
import PhysicalFlowTable from "./components/flow-table/physical-flow-table";
import PhysicalFlowAndSpecificationDetail from "./components/physical-flow-and-specification-detail/physical-flow-and-specification-detail";
import PhysicalFlowSection from "./components/physical-flow-section/physical-flow-section";
import PhysicalFlowParticipantSection from "./components/participants-section/physical-flow-participants-section";
import PhysicalFlowParticipantSubSection from "./components/participants-sub-section/physical-flow-participants-sub-section";

import Routes from "./routes";

function setup() {
    const module = angular.module("waltz.physical.flows", []);

    module
        .config(Routes);

    registerStores(module, [
        PhysicalFlowStore,
        PhysicalFlowParticipantStore
    ]);

    module
        .component("waltzPhysicalFlowTable", PhysicalFlowTable);

    registerComponents(module, [
        BulkPhysicalFlowLoaderWizard,
        BulkPhysicalFlowParser,
        BulkPhysicalFlowUploader,
        FlowSpecDefinitionSection,
        PhysicalFlowCloneSelector,
        PhysicalFlowEditor,
        PhysicalFlowOverview,
        PhysicalFlowOverviewNode,
        PhysicalFlowEditOverview,
        PhysicalFlowEditTargetLogicalFlow,
        PhysicalFlowEditSpecification,
        PhysicalFlowAttributeEditor,
        PhysicalFlowSection,
        PhysicalFlowParticipantSection,
        PhysicalFlowParticipantSubSection,
        PhysicalFlowAndSpecificationDetail
    ]);
    return module.name;
}


export default setup;
