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

import services from "./services";
import Routes from "./routes";
import { registerComponents } from "../common/module-utils";

import ChangeSetView from "./pages/view/change-set-view";

import ChangeSetOverview from "./components/overview/change-set-overview";
import ChangeSetPanel from "./components/panel/change-set-panel";
import ChangeSetSection from "./components/section/change-set-section";


export default () => {

    const module = angular.module("waltz.change.set", []);

    services(module);

    module.config(Routes);

    registerComponents(module, [
        ChangeSetView,
        ChangeSetOverview,
        ChangeSetPanel,
        ChangeSetSection
    ]);

    return module.name;
};
