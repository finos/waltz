
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
import { registerComponents, registerStores } from "../common/module-utils";

import ServerInfoStore from "./services/server-info-store";
import ServerUsageStore from "./services/server-usage-store";
import Routes from './routes';

import ServerPies from "./components/server-pies/server-pies";
import ServerOverview from "./components/overview/server-overview";
import ServerUsagesSection from "./components/usages-section/server-usages-section";
import ServerBasicInfoTile from "./components/basic-info-tile/server-basic-info-tile";

import ServerView from "./pages/view/server-view";


export default () => {

    const module = angular.module("waltz.server.info", []);

    module.config(Routes);

    registerComponents(module, [
        ServerPies,
        ServerOverview,
        ServerUsagesSection,
        ServerView,
        ServerBasicInfoTile
    ]);

    registerStores(module, [
        ServerInfoStore,
        ServerUsageStore
    ]);

    return module.name;
};
