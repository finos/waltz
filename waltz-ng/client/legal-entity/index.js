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
import {registerComponents, registerStores} from "../common/module-utils";

import Routes from "./routes";
import LegalEntityView from "./pages/view/legal-entity-view";
import LegalEntitySection from "./components/legal-entity-section/legal-entity-section";
import LegalEntityRelationshipSection
    from "./components/legal-entity-relationship-section/legal-entity-relationship-section";
import LegalEntityStore from "./services/legal-entity-store";
import LegalEntityRelationshipStore from "./services/legal-entity-relationship-store";
import LegalEntityRelationshipKindStore from "./services/legal-entity-relationship-kind-store";


export default () => {

    const module = angular.module("waltz.legal-entity", []);

    module
        .config(Routes);

    registerComponents(module, [
        LegalEntityView,
        LegalEntitySection,
        LegalEntityRelationshipSection
    ]);

    registerStores(module, [
        LegalEntityStore,
        LegalEntityRelationshipStore,
        LegalEntityRelationshipKindStore
    ])

    return module.name;
};
