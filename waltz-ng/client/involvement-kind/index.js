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
import * as InvolvementKindStore from "./services/involvement-kind-store";
import {registerComponents, registerStores} from "../common/module-utils";
import InvolvementKindService from "./services/involvement-kind-service";
import InvolvementKindView from "./pages/view/involvement-kind-view"
import InvolvementKindList from "./pages/list/involvement-kinds-view"
import Routes from "./routes";

export default () => {

    const module = angular.module("waltz.involvement.kind", []);

    module
        .service("InvolvementKindService", InvolvementKindService);


    module
        .config(Routes);

    registerStores(module, [InvolvementKindStore]);
    registerComponents(module, [InvolvementKindView, InvolvementKindList])

    return module.name;
};
