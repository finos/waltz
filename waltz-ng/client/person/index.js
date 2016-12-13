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

    const module = angular.module('waltz.person', []);

    module
        .config(require('./routes'))

    module
        .directive('waltzPersonSelector', require('./directives/person-selector'))
        .directive('waltzPersonLink', require('./directives/person-link'))
        .directive('waltzManagerList', require('./directives/manager-list'))
        .directive('waltzPersonDirectsList', require('./directives/person-directs-list'))
        .directive('waltzPersonSummary', require('./directives/person-summary'));

    module
        .service('PersonViewDataService', require('./person-view-data'))
        .service('PersonStore', require('./services/person-store'));

    return module.name;
};
