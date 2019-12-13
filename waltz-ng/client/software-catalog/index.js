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

import angular from "angular";
import { registerComponents, registerStore } from "../common/module-utils";
import * as SoftwareCatalogStore from "./services/software-catalog-store";
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
