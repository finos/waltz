/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import angular from 'angular';


function setup() {
    const module = angular.module('waltz.perspective', []);

    module
        .config(require('./routes'));

    module
        .service('PerspectiveDefinitionStore', require('./serivces/perspective-definition-store'))
        .service('PerspectiveRatingStore', require('./serivces/perspective-rating-store'));
    module
        .component('waltzPerspectiveEditor', require('./components/editor/perspective-editor'))
        .component('waltzPerspectiveGrid', require('./components/grid/perspective-grid'))
        .component('waltzPerspectiveOverrides', require('./components/overrides/perspective-overrides'));

    return module.name;
}


export default setup;