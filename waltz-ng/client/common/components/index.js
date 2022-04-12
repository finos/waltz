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
import BooleanRenderer from "./boolean-renderer";
import FilterChangedWatcher from "./filter-change-watcher/filter-change-watcher";
import LastUpdated from "./last-updated/last-updated";
import MultiSelectTreeControl from "./multi-select-tree-control/multi-select-tree-control";
import SearchControl from "./search-control/search-control";
import EntityIconLabel from "./entity-link/entity-icon-label";
import EntityLink from "./entity-link/entity-link";
import Grid from "./grid/grid";
import GridSref from "./grid-sref/grid-sref";
import GridWithSearch from "./grid/grid-with-search";
import DatePickerFormInput from "./date-picker/date-picker-form-input";
import MiniActions from "./mini-actions/mini-actions";
import FavouritesButton from "./favourites-button/favourites-button"
import {registerComponents} from "../module-utils";


export default (module) => {

    module
        .component("waltzBooleanRenderer", BooleanRenderer)
        .component("waltzLastUpdated", LastUpdated)
        .component("waltzMultiSelectTreeControl", MultiSelectTreeControl)
        .component("waltzSearchControl", SearchControl)
        .component("waltzEntityIconLabel", EntityIconLabel)
        .component("waltzEntityLink", EntityLink)
        .component("waltzGrid", Grid)
        .component("waltzGridSref", GridSref)
        .component("waltzGridWithSearch", GridWithSearch)
        .component("waltzDatePickerFormInput", DatePickerFormInput);

    registerComponents(module, [
        FilterChangedWatcher,
        MiniActions,
        FavouritesButton,
    ]);

};
