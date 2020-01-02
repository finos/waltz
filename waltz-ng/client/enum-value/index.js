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
import * as EnumValueStore from "./services/enum-value-store";
import EnumValueService from "./services/enum-value-service";
import {registerComponents, registerStores} from "../common/module-utils";
import EnumValue from "./components/enum-value/enum-value";

export default () => {

    const module = angular.module("waltz.enum-value", []);

    module
        .service("EnumValueService", EnumValueService);

    registerStores(module, [ EnumValueStore ]);
    registerComponents(module, [ EnumValue ]);

    return module.name;
};
