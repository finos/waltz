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
import * as DataTypeUsageStore from "./services/data-type-usage-store";
import {registerComponents, registerStore} from "../common/module-utils"
import AppDataTypeUsageEditor from "./components/editor/app-data-type-usage-editor";
import AppDataTypeUsageList from "./components/app-data-type-usage-list/app-data-type-usage-list";
import DataTypeUsageStatTable from "./components/stat-table/data-type-usage-stat-table";


export default () => {
    const module = angular.module("waltz.data.type.usage", []);

    const components = [
        AppDataTypeUsageEditor,
        AppDataTypeUsageList,
        DataTypeUsageStatTable];

    registerComponents(module, components);
    registerStore(module, DataTypeUsageStore);

    return module.name;
};
