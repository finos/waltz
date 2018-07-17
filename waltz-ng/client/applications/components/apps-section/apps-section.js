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

import _ from 'lodash';
import {initialiseData} from '../../../common';
import {mkSelectionOptions} from '../../../common/selector-utils';
import {CORE_API} from '../../../common/services/core-api-utils';
import template from './apps-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    apps: [],
    endUserApps: [],
};




const DEFAULT_APP_SETTINGS = {
    management: 'End User',
    kind: 'EUC',
    overallRating: 'Z'
};


function combine(apps = [], endUserApps = []) {
    return _.concat(apps, endUserApps);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const refresh = () => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);
    };

    vm.$onInit = () => {
        const selectorOptions = mkSelectionOptions(vm.parentEntityRef);

        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.findBySelector, [selectorOptions])
            .then(r => r.data)
            .then(apps => vm.apps = _.map(
                apps,
                a => _.assign(a, { management: 'IT' })))
            .then(refresh);

        if (vm.parentEntityRef.kind === 'ORG_UNIT') {
            serviceBroker
                .loadViewData(CORE_API.EndUserAppStore.findBySelector, [selectorOptions])
                .then(r => r.data)
                .then(endUserApps => vm.endUserApps = _.map(
                    endUserApps,
                    a => _.assign(a, { platform: a.applicationKind }, DEFAULT_APP_SETTINGS)))
                .then(refresh);
        }
    };

    vm.$onChanges = () => {
        vm.combinedApps = combine(vm.apps, vm.endUserApps);
    };

    vm.onInitialise = (cfg) => {
        vm.export = () => cfg.exportFn(`apps.csv`);
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default component;