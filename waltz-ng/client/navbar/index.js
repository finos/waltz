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
import {registerComponents} from "../common/module-utils";
import NavbarFilters from "./components/navbar-filters/navbar-filters"
import NavbarSearch from "./components/navbar-search/navbar-search";
import NavFiltersOverlay from "./components/nav-filters-overlay/nav-filters-overlay";
import NavSearchOverlay from "./components/nav-search-overlay/nav-search-overlay";
import SveltePage from "./svelte-page";
import NavbarViewpoints from "./components/nav-viewpoints/navbar-viewpoints";

import Navbar from "./directives/navbar";
import NavbarRecentlyViews from "./directives/navbar-recently-viewed";
import NavbarProfile from "./directives/navbar-profile";


export default () => {

    const module = angular.module("waltz.navbar", []);

    module
        .directive("waltzNavbar", Navbar)
        .directive("waltzNavbarRecentlyViewed", NavbarRecentlyViews)
        .directive("waltzNavbarProfile", NavbarProfile);

    registerComponents(module, [
        NavbarFilters,
        NavbarSearch,
        NavFiltersOverlay,
        NavSearchOverlay,
        SveltePage,
        NavbarViewpoints
    ]);

    return module.name;
};