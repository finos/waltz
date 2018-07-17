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

import angular from 'angular';
import {registerStores} from "../common/module-utils";
import * as SvgDiagramStore from './services/svg-diagram-store';
import SvgDiagram from './component/svg-diagram';
import SvgDiagrams from './component/svg-diagrams';
import CommonSvgDefs from './component/common-svg-defs';


export default () => {

    const module = angular.module('waltz.svg.diagram', []);


    registerStores(module, [SvgDiagramStore]);


    module
        .component('waltzSvgDiagram', SvgDiagram)
        .component('waltzSvgDiagrams', SvgDiagrams)
        .component('waltzCommonSvgDefs', CommonSvgDefs);

    return module.name;

};
