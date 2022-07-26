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
import {registerComponents, registerStores} from "../common/module-utils";

import userAgentInfoStore from "./services/user-agent-info-store";
import userService from "./services/user-service";
import userStore from "./services/user-store";
import userPreferenceStore from "./services/user-preference-store";
import userPickList from "./components/user-pick-list/user-pick-list";

import hasRole from "./directives/has-role";
import hasRoleForEntityKind from "./directives/has-role-for-entity-kind";
import unlessRole from "./directives/unless-role";
import Routes from "./routes";
import * as _ from "lodash";
import {checkIsEntityRef} from "../common/checks";


export const lastViewedMeasurableCategoryKey = "main.measurable-category.list.lastCategory";
export const appLogicalFlowFilterExcludedTagIdsKey = "main.app-view.logical-flow.filter.excludedTagIds";
export const groupLogicalFlowFilterExcludedTagIdsKey = "main.group-views.logical-flow.filter.excludedTagIds";
export const favouriteAssessmentDefinitionIdsKey = "main.app-view.assessment-rating.favouriteAssessmentDefnIds";
export const lastViewedFlowTabKey = "main.app-view.data-flows.lastTab";

export function mkAssessmentDefinitionsIdsBaseKey(entityReference) {
    checkIsEntityRef(entityReference);
    return `${favouriteAssessmentDefinitionIdsKey}.${_.camelCase(entityReference.kind)}`;
}

export default () => {
    const module = angular.module("waltz.user", []);

    module
        .config(Routes);

    registerStores(module, [userStore, userPreferenceStore]);
    registerComponents(module, [userPickList]);

    module
        .service("UserAgentInfoStore", userAgentInfoStore)
        .service("UserService", userService);

    module
        .directive("waltzHasRole", hasRole)
        .directive("waltzHasRoleForEntityKind", hasRoleForEntityKind)
        .directive("waltzUnlessRole", unlessRole);

    return module.name;
};
