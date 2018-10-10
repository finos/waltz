/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import BooleanRenderer from "./boolean-renderer";
import BucketChart from "./bucket-chart";
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
import {registerComponents} from "../module-utils";


export default (module) => {

    module
        .component("waltzBooleanRenderer", BooleanRenderer)
        .component("waltzBucketChart", BucketChart)
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
        MiniActions
    ]);

};
