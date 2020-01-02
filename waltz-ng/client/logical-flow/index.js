
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
import {registerStore} from "../common/module-utils";

import directives from "./directives";
import components from "./components";

import Routes from "./routes";
import LogicalFlowStore from "./services/logical-flow-store";
import LogicalFlowUtility from "./services/logical-flow-utility";

export default () => {

    const module = angular.module("waltz.logical.flow", []);

    module
        .config(Routes);

    directives(module);
    components(module);

    registerStore(module, LogicalFlowStore);

    module
        .service("LogicalFlowUtilityService", LogicalFlowUtility);

    return module.name;
};
