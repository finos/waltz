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
import angular from "angular";
import * as measurableRelationshipStore from "./services/measurable-relationship-store";
import {registerComponents, registerStore} from "../common/module-utils";
import CreateEditor from "./components/editor/create-related-measurable-editor";
import UpdateEditor from "./components/editor/update-related-measurable-editor";
import RelatedMeasurableItemView from "./components/item-view/related-measurable-item-view";
import RelatedMeasurablesTable from "./components/table/related-measurables-table";
import RelatedMeasurablesTree from "./components/tree/related-measurables-tree";
import RelatedMeasurablesPanel from "./components/panel/related-measurables-panel";
import RelatedMeasurablesViz from "./components/viz/related-measurables-viz";


export default () => {
    const module = angular.module('waltz.measurable.relationship', []);

    registerStore(module, measurableRelationshipStore);

    module
        .component('waltzRelatedMeasurablesPanel', RelatedMeasurablesPanel)
        .component('waltzRelatedMeasurablesViz', RelatedMeasurablesViz);

    registerComponents(module, [
        CreateEditor,
        UpdateEditor,
        RelatedMeasurableItemView,
        RelatedMeasurablesTable,
        RelatedMeasurablesTree
    ]);


    return module.name;
};
