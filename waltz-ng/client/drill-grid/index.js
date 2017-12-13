
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
import DrillGridChart from './components/drill-grid-chart/drill-grid-chart';
import DrillGridPanel from './components/drill-grid-panel/drill-grid-panel';
import DrillGridDefinitionSelector from './components/definition-selector/drill-grid-definition-selector';
import * as DrillGridDefinitionStore from './services/drill-grid-definition-store';
import {registerComponents, registerStores} from '../common/module-utils';


export default () => {

    const module = angular.module('waltz.drill-grid', []);

    registerStores(
        module,
        [ DrillGridDefinitionStore ]);

    registerComponents(
        module,
        [ DrillGridChart,
          DrillGridPanel,
          DrillGridDefinitionSelector ]);

    return module.name;
};
