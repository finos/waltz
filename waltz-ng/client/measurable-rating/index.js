/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import angular from 'angular';


export default () => {
    const module = angular.module('waltz.measurable.rating', []);

    module
        .config(require('./routes'))
        ;

    module
        .service('MeasurableRatingStore', require('./services/measurable-rating-store'))
        ;

    module
        .component('waltzMeasurableRatingAppSection', require('./components/app-section/measurable-rating-app-section'))
        .component('waltzMeasurableRatingExplorerSection', require('./components/explorer-section/measurable-rating-explorer-section'))
        .component('waltzMeasurableRatingPanel', require('./components/panel/measurable-rating-panel'))
        .component('waltzMeasurableRatingTree', require('./components/tree/measurable-rating-tree'))
        .component('waltzMeasurableRatingsBrowser', require('./components/browser/measurable-ratings-browser'))
        ;

    return module.name;
};
