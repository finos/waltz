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
import LegalEntityRelationshipKindView from "./pages/view/legal-entity-relationship-kind-view";
import LegalEntityRelationshipsSection from "./components/relationships-section/legal-entity-relationships-section";
import LegalEntityRelationshipKindStore from "./services/legal-entity-relationship-kind-store";
import BulkUploadLegalEntityRelationshipsStore from "./services/bulk-upload-legal-entity-relationships-store";
import LegalEntityRelationshipBulkUpload from "./components/bulk-upload/legal-entity-relationship-bulk-upload";

export default () => {

    const module = angular.module("waltz.legal-entity-relationship-kind", []);

    module
        .config(Routes);

    registerComponents(module, [
        LegalEntityRelationshipKindView,
        LegalEntityRelationshipsSection,
        LegalEntityRelationshipBulkUpload
    ]);

    registerStores(module, [
        LegalEntityRelationshipKindStore,
        BulkUploadLegalEntityRelationshipsStore
    ])

    return module.name;
};
