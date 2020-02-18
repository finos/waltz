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

import {registerComponents} from "../common/module-utils";

import routes from "./routes";
import services from "./services";

import AppCostsSection from "./components/app-costs-section/app-costs-section";
import AppOverview from "./components/overview/app-overview";
import AppsSection from "./components/apps-section/apps-section";
import BulkApplicationSelector from "./components/bulk-application-selector/bulk-application-selector";
import RelatedAppsSection from "./components/related-apps-section/related-apps-section";
import AppSelector from "./directives/app-selector";
import AssetCodeExplorer from "./directives/asset-code-explorer";
import BasicAppSelector from "./components/basic-app-selector/basic-app-selector";
import AppsByInvestmentPie from "./components/apps-by-investment-pie";
import AppsByLifecyclePhasePie from "./components/apps-by-lifecycle-phase-pie";
import AppSummary from "./components/app-summary";
import AppTable from "./components/app-table";
import SimpleAppTable from "./components/apps-view/simple-app-table";

export default () => {

    const module = angular.module("waltz.applications", []);

    services(module);

    module
        .config(routes);

    module
        .directive("waltzAppSelector", AppSelector)
        .directive("waltzAssetCodeExplorer", AssetCodeExplorer);

    module
        .component("waltzAppsByInvestmentPie", AppsByInvestmentPie)
        .component("waltzAppsByLifecyclePhasePie", AppsByLifecyclePhasePie)
        .component("waltzAppSummary", AppSummary)
        .component("waltzAppTable", AppTable)
        .component("waltzSimpleAppTable", SimpleAppTable);

    registerComponents(module, [
        AppCostsSection,
        AppOverview,
        AppsSection,
        BasicAppSelector,
        BulkApplicationSelector,
        RelatedAppsSection
    ]);

    return module.name;
};
