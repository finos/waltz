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

import bookmarkStore from "./services/bookmark-store";
import bookmarkKindSelect from "./components/bookmark-kind-select/bookmark-kind-select";
import bookmarksSection from "./components/bookmarks-section/bookmarks-section";
import bookmarkKinds from "./components/bookmark-kinds/bookmark-kinds";
import bookmarksViewPanel from "./components/bookmarks-view-panel/bookmarks-view-panel";
import bookmarksEditPanel from "./components/bookmarks-edit-panel/bookmarks-edit-panel";
import bookmarkForm from "./components/form/bookmark-form";


export default () => {

    const module = angular.module("waltz.bookmarks", []);

    registerStores(module, [bookmarkStore]);

    registerComponents(module, [
        bookmarkForm,
        bookmarkKinds,
        bookmarkKindSelect,
        bookmarksSection,
        bookmarksEditPanel,
        bookmarksViewPanel]);

    return module.name;
};
