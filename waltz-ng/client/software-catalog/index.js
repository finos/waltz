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
import {registerComponents, registerStore} from "../common/module-utils";
import * as SoftwareCatalogStore from "./services/software-catalog-store";
import * as VulnerabilityStore from "./services/vulnerability-store";
// components
import SimpleSoftwareUsagePies from "./components/usage-pies/simple-software-usage-pies";
import SoftwarePackageOverview from "./components/overview/software-package-overview";
import SoftwarePackagesSection from "./components/packages/software-packages-section";
import SoftwarePackageVersions from "./components/versions/software-package-versions";
// pages
import SoftwarePackageView from "./pages/view/software-package-view";
// directives
import SimpleSoftwareUsageList from "./directives/simple-software-usage-list";
import MaturityStatus from "./directives/maturity-status";
import SoftwareCatalogSection from "./directives/software-catalog-section";


import Routes from "./routes";


export default () => {

    const module = angular.module("waltz.software.catalog", []);

    module
        .config(Routes);

    registerStore(module, SoftwareCatalogStore);
    registerStore(module, VulnerabilityStore);


    registerComponents(module, [
        SimpleSoftwareUsagePies,
        SoftwarePackageOverview,
        SoftwarePackagesSection,
        SoftwarePackageVersions,
        SoftwarePackageView
    ]);

    module
        .directive("waltzSimpleSoftwareUsageList", SimpleSoftwareUsageList)
        .directive("waltzMaturityStatus", MaturityStatus)
        .directive("waltzSoftwareCatalogSection", SoftwareCatalogSection);

    return module.name;
};
