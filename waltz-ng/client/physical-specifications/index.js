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


function setup() {

    const module = angular.module('waltz.physical.specification', []);

    module
        .config(require('./routes'));

    module
        .component('waltzPhysicalDataSection', require('./components/physical-data-section/physical-data-section'))
        .component('waltzPhysicalSpecificationOverview', require('./components/overview/physical-specification-overview'))
        .component('waltzPhysicalSpecificationConsumers', require('./components/specification-consumers/physical-specification-consumers'))
        .component('waltzPhysicalSpecificationMentions', require('./components/mentions/physical-specification-mentions'));

    module
        .service('PhysicalSpecificationStore', require('./services/physical-specification-store'));

    return module.name;
}


export default setup;
