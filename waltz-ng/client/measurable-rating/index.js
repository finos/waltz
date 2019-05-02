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
import {registerComponents, registerStores} from "../common/module-utils";

import MeasurableRatingStore from "./services/measurable-rating-store";
import MeasurableRatingsBrowserSection from "./components/browser-section/measurable-ratings-browser-section";
import MeasurableRatingsBrowserTreePanel from "./components/browser-tree-panel/measurable-ratings-browser-tree-panel";
import MeasurableRatingEditPanel from "./components/edit-panel/measurable-rating-edit-panel";
import MeasurableRatingAppSection from "./components/app-section/measurable-rating-app-section";
import MeasurableRatingExplorerSection from "./components/explorer-section/measurable-rating-explorer-section";
import MeasurableRatingPanel from "./components/panel/measurable-rating-panel";
import MeasurableRatingTree from "./components/tree/measurable-rating-tree";
import MeasurableRatingsBrowser from "./components/browser/measurable-ratings-browser";
import RelatedMeasurablesSection from "./components/related-measurables-section/related-measurables-section";
import Routes from "./routes";


export default () => {
    const module = angular.module("waltz.measurable.rating", []);

    module
        .config(Routes);

    registerStores(module, [MeasurableRatingStore]);

    registerComponents(module, [
        MeasurableRatingAppSection,
        MeasurableRatingEditPanel,
        MeasurableRatingExplorerSection,
        MeasurableRatingPanel,
        MeasurableRatingTree,
        MeasurableRatingsBrowser,
        MeasurableRatingsBrowserSection,
        MeasurableRatingsBrowserTreePanel,
        RelatedMeasurablesSection
    ]);

    return module.name;
};
