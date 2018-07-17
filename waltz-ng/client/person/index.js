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
import {registerComponents, registerStore} from '../common/module-utils';

import PersonStore from './services/person-store';
import PersonSummary from './components/summary/person-summary';
import PersonAppsSection from './components/person-apps-section/person-apps-section';
import PersonHierarchySection from './components/person-hierarchy-section/person-hierarchy-section';
import Routes from './routes';
import PersonLink from './directives/person-link';
import ManagersList from './directives/manager-list';
import PersonDirectsList from './directives/person-directs-list';


export default () => {

    const module = angular.module('waltz.person', []);

    module
        .config(Routes)

    module
        .directive('waltzPersonLink', PersonLink)
        .directive('waltzManagerList', ManagersList)
        .directive('waltzPersonDirectsList', PersonDirectsList);

    registerStore(module, PersonStore);
    registerComponents(module, [
        PersonAppsSection,
        PersonHierarchySection,
        PersonSummary
    ]);

    return module.name;
};
