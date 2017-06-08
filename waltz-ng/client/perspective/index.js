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
import angular from "angular";

import {registerComponents, registerStores} from '../common/module-utils';

import perspectiveDefinitionStore from './services/perspective-definition-store';
import perspectiveRatingStore from './services/perspective-rating-store';


import perspectiveEditor from './components/editor/perspective-editor';
import perspectiveGrid from './components/grid/perspective-grid';
import perspectiveOverrides from './components/overrides/perspective-overrides';
import perspectRatingPanel from './components/panel/perspective-rating-panel';


function setup() {
    const module = angular.module('waltz.perspective', []);

    module
        .config(require('./routes'));

    registerStores(module, [
        perspectiveDefinitionStore,
        perspectiveRatingStore
    ]);


    module
        .component('waltzPerspectiveEditor', perspectiveEditor)
        .component('waltzPerspectiveGrid', perspectiveGrid)
        .component('waltzPerspectiveOverrides', perspectiveOverrides);

    registerComponents(module, [perspectRatingPanel]);

    return module.name;
}


export default setup;