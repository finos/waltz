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
import TaxonomyManagementStore from "./services/taxonomy-management-store";
import PendingTaxonomyChangesList from "./components/pending-taxonomy-changes-list/pending-taxonomy-changes-list";
import PendingTaxonomyChangesSubSection from "./components/pending-taxonomy-changes-sub-section/pending-taxonomy-changes-sub-section";
import TaxonomyChangeInfo from "./components/taxonomy-change-command-info/taxonomy-change-command-info";
import TaxonomyChangePreview from "./components/taxonomy-change-command-preview/taxonomy-change-command-preview";
import {registerStores, registerComponents} from "../common/module-utils";


export default () => {
    const module = angular.module("waltz.taxonomy-management", []);

    registerStores(module, [
        TaxonomyManagementStore ]);

    registerComponents(module, [
        PendingTaxonomyChangesList,
        PendingTaxonomyChangesSubSection,
        TaxonomyChangeInfo,
        TaxonomyChangePreview ]);

    return module.name;
};
