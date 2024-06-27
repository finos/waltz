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

import MeasurableRatingStore from "./services/measurable-rating-store";
import MeasurableRatingReplacementAppsStore from "./services/measurable-rating-replacement-store"
import MeasurableRatingPlannedDecommissionStore from "./services/measurable-rating-planned-decommission-store"
import MeasurableRatingsBrowserSection from "./components/browser-section/measurable-ratings-browser-section";
import MeasurableRatingsBrowserTreePanel from "./components/browser-tree-panel/measurable-ratings-browser-tree-panel";
import MeasurableRatingEditPanel from "./components/edit-panel/measurable-rating-edit-panel";
import MeasurableRatingEntitySection from "./components/entity-section/measurable-rating-entity-section";
import MeasurableRatingExplorerSection from "./components/explorer-section/measurable-rating-explorer-section";
import MeasurableRatingPanel from "./components/panel/measurable-rating-panel";
import MeasurableRatingTree from "./components/tree/measurable-rating-tree";
import MeasurableRatingsBrowser from "./components/browser/measurable-ratings-browser";
import RelatedMeasurablesSection from "./components/related-measurables-section/related-measurables-section";
import PlannedDecommissionInfo from "./components/planned-decommission-info/planned-decommission-info"
import PlannedDecommissionEditor from "./components/planned-decommission-editor/planned-decommission-editor"
import Routes from "./routes";


export default () => {
    const module = angular.module("waltz.measurable.rating", []);

    module
        .config(Routes);

    registerStores(module, [
        MeasurableRatingStore,
        MeasurableRatingReplacementAppsStore,
        MeasurableRatingPlannedDecommissionStore]);

    registerComponents(module, [
        MeasurableRatingEntitySection,
        MeasurableRatingEditPanel,
        MeasurableRatingExplorerSection,
        MeasurableRatingPanel,
        MeasurableRatingTree,
        MeasurableRatingsBrowser,
        MeasurableRatingsBrowserSection,
        MeasurableRatingsBrowserTreePanel,
        RelatedMeasurablesSection,
        PlannedDecommissionInfo,
        PlannedDecommissionEditor
    ]);

    return module.name;
};
