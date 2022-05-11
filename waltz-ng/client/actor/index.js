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
import ActorOverview from "./components/actor-overview";
import ActorStore from "./services/actor-store";

import Routes from "./routes";
import ActorService from "./services/actor-service";
import ActorSelector from "./components/actor-selector";
import BasicActorSelector from "./components/basic-actor-selector";
import ActorListView from "./pages/list-view/actor-list-view";

export default () => {

    const module = angular.module("waltz.actor", []);

    module
        .config(Routes)
        .service("ActorService", ActorService);

    registerComponents(
        module,
        [
            ActorOverview,
            ActorListView
        ]);

    registerStores(
        module,
        [ ActorStore ]);

    module
        .component("waltzActorSelector", ActorSelector)
        .component("waltzBasicActorSelector", BasicActorSelector);

    return module.name;
};
