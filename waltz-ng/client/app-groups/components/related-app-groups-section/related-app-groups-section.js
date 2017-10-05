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


import _ from 'lodash';

import {initialiseData} from "../../../common/index";
import {CORE_API} from "../../../common/services/core-api-utils";

import template from './related-app-groups-section.html';
import {sameRef} from "../../../common/entity-utils";


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    groups: []
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
        serviceBroker
            .loadViewData(
                CORE_API.ChangeInitiativeStore.findRelatedForId,
                [ vm.parentEntityRef.id ])
            .then(r => _
                .chain(r.data)
                .flatMap(rel => [rel.a, rel.b])
                .filter(ref => ref.kind === 'APP_GROUP')
                .reject(ref => sameRef(ref, vm.parentEntityRef))
                .map("id")
                .value())
            .then(groupIds => serviceBroker
                .loadViewData(
                    CORE_API.AppGroupStore.findByIds,
                    [ groupIds ]))
            .then(r => vm.groups = r.data);
    };
}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    controller,
    bindings,
    template
};


const id = 'waltzRelatedAppGroupsSection';


export default {
    id,
    component
}