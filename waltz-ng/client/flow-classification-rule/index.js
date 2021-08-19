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
import * as flowClassificationRuleStore from "./services/flow-classification-rule-store";
import * as flowClassificationStore from "./services/flow-classification-store";
import * as FlowClassificationRulesTable from "./components/table/flow-classification-rules-table";
import FlowClassificationRulesSummaryList from "./components/summary-list/flow-classification-rule-summary-list"
import CompanionAppRulesSection from "./components/companion-app-rules/companion-app-rules-section"
import CompanionDataTypeRulesSection from "./components/companion-data-type-rules/companion-data-type-rules-section"
import FlowClassificationRuleView from "./pages/view/flow-classification-rule-view"
import * as NonAuthSourcesPanel from "./components/discouraged-sources-panel/discouraged-sources-panel";
import * as FlowClassificationRuleSection from "./components/section/flow-classification-rules-section";
import * as FlowClassificationRulesSummaryPanel from "./components/summary-panel/flow-classification-rules-summary-panel";
import * as TreePicker from "./components/tree-picker/tree-picker";
import * as TreeFilter from "./components/tree-filter/tree-filter";
import {registerComponents, registerStores} from "../common/module-utils";
import routes from "./routes";


export default () => {

    const module = angular.module("waltz.flow.classification.rule", []);

    module.config(routes);

    registerStores(
        module, [flowClassificationStore, flowClassificationRuleStore]);

    registerComponents(
        module,
        [
            FlowClassificationRulesSummaryList,
            FlowClassificationRulesTable,
            FlowClassificationRuleSection,
            FlowClassificationRulesSummaryPanel,
            FlowClassificationRuleView,
            TreePicker,
            TreeFilter,
            NonAuthSourcesPanel,
            CompanionAppRulesSection,
            CompanionDataTypeRulesSection
        ]);

    return module.name;
};
