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
