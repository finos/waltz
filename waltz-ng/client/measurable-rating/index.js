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
import { registerComponents, registerStores } from '../common/module-utils';

import measurableRatingStore from './services/measurable-rating-store';

import MeasurableRatingsBrowserSection from './components/browser-section/measurable-ratings-browser-section';
import MeasurableRatingEditPanel from './components/edit-panel/measurable-rating-edit-panel';


export default () => {
    const module = angular.module('waltz.measurable.rating', []);

    module
        .config(require('./routes'))
        ;


    registerStores(module, [measurableRatingStore]);


    module
        .component('waltzAssociatedPerspectives', require('./components/associated-perspectives/associated-perspectives'))
        .component('waltzMeasurableRatingAppSection', require('./components/app-section/measurable-rating-app-section'))
        .component('waltzMeasurableRatingExplorerSection', require('./components/explorer-section/measurable-rating-explorer-section'))
        .component('waltzMeasurableRatingPanel', require('./components/panel/measurable-rating-panel'))
        .component('waltzMeasurableRatingTree', require('./components/tree/measurable-rating-tree'))
        .component('waltzMeasurableRatingsBrowser', require('./components/browser/measurable-ratings-browser'))
        .component('waltzRelatedMeasurablesSection', require('./components/related-measurables-section/related-measurables-section'))
        ;

    registerComponents(module, [
        MeasurableRatingsBrowserSection,
        MeasurableRatingEditPanel
    ]);


    return module.name;
};
