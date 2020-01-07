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


import {registerStores} from "../common/module-utils";
import * as TagStore from "./services/tag-store";
import Routes from "./routes";
import TagList from "./components/tag-list";
import TagEdit from "./components/tag-edit";
import AllTags from "./components/all-tags";


export default () => {

    const module = angular.module("waltz.tags", []);

    module
        .config(Routes);

    module
        .component("waltzTagList",  TagList )
        .component("waltzTagEdit",  TagEdit )
        .component("waltzAllTags",  AllTags);

    registerStores(module, [TagStore]);

    return module.name;
};
