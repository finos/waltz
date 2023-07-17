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
import CategoryStore from "./services/measurable-category-store";
import TaxonomyChangesSection from "./components/taxonomy-changes/taxonomy-changes-section"
import TaxonomyChangeSummaryCell from "./components/taxonomy-change-summary-cell/taxonomy-change-summary-cell"
import Routes from "./routes";
import {registerComponents, registerStores} from "../common/module-utils";

export default () => {
    const module = angular.module("waltz.measurable-category", []);

    registerStores(module, [ CategoryStore ]);

    registerComponents(
        module,
        [
            TaxonomyChangesSection,
            TaxonomyChangeSummaryCell
        ]);

    module.config(Routes);

    return module.name;
};
