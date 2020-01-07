
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

import userAgentInfoStore from "./services/user-agent-info-store";
import userService from "./services/user-service";
import userStore from "./services/user-store";
import userPreferenceStore from "./services/user-preference-store";
import userPreferenceService from "./services/user-preference-service";

import userPickList from "./components/user-pick-list/user-pick-list";

import hasRole from "./directives/has-role";
import hasRoleForEntityKind from "./directives/has-role-for-entity-kind";
import unlessRole from "./directives/unless-role";
import ifAnonymous from "./directives/if-anonymous";
import Routes from "./routes";


export default () => {
    const module = angular.module("waltz.user", []);

    module
        .config(Routes);

    registerStore(module, userStore);
    registerComponents(module, [userPickList])

    module
        .service("UserAgentInfoStore", userAgentInfoStore)
        .service("UserService", userService)
        .service("UserPreferenceStore", userPreferenceStore)
        .service("UserPreferenceService", userPreferenceService);

    module
        .directive("waltzHasRole", hasRole)
        .directive("waltzHasRoleForEntityKind", hasRoleForEntityKind)
        .directive("waltzUnlessRole", unlessRole)
        .directive("waltzIfAnonymous", ifAnonymous);

    return module.name;
};
