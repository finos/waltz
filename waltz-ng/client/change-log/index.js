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
import changeLogStore from "./services/change-log-store";
import changeLogSummariesStore from "./services/change-log-summaries-store";
import Routes from "./routes";
import ChangeLogSection from "./components/change-log-section";
import ChangeLogTable from "./components/change-log-table";
import ChangeBreakdownTable from "./components/breakdown-table/change-breakdown-table";
import ChangeSummariesPanel from "./components/summaries-panel/change-summaries-panel";

export default () => {
    const module = angular.module("waltz.change.log", []);

    module
        .config(Routes);

    registerStores(module, [
        changeLogStore,
        changeLogSummariesStore
    ]);

    registerComponents(module, [ChangeBreakdownTable, ChangeSummariesPanel]);

    module
        .component("waltzChangeLogSection", ChangeLogSection)
        .component("waltzChangeLogTable", ChangeLogTable);

    return module.name;
};
