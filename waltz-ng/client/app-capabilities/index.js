
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

    const module = angular.module('waltz.app.capabilities', []);


    module
        .config(require('./routes.js'));

    module
        .service('AppCapabilityStore', require('./services/app-capability-store'));

    module
        .component('waltzAppRatingTabgroupSection', require('./components/app-rating-tabgroup-section/app-rating-tabgroup-section'))
        .component('waltzAppCapabilityEditor', require('./components/app-capability-editor/app-capability-editor'))
        .component('waltzAppCapabilityPicker', require('./components/app-capability-picker/app-capability-picker'))
        .component('waltzAppCapabilityTable', require('./components/app-capability-table/app-capability-table'))
        .component('waltzAppCapabilityUsageEditor', require('./components/app-capability-usage-editor/app-capability-usage-editor'));

    require('./directives')(module);


    return module.name;
};
