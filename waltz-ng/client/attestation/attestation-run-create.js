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
import _ from "lodash";
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common/index";
import {timeFormat} from "d3-time-format";

const exactScope = {
    value: 'EXACT',
    name: 'Exact'
};


const childrenScope = {
    value: 'CHILDREN',
    name: 'Children'
};

const initialState = {
    attestationRun: {
        targetEntityKind: 'APPLICATION',
        selectorEntityKind: 'APP_GROUP',
        selectorScope: 'EXACT'
    },
    targetEntityKinds: [{
        name: 'Application',
        value: 'APPLICATION'
    }],
    allowedEntityKinds: [{
        value: 'APP_GROUP',
        name: 'Application Group'
    },{
        value: 'ORG_UNIT',
        name: 'Org Unit'
    },{
        value: 'MEASURABLE',
        name: 'Measurable'
    }],
    allowedScopes: {
        'APP_GROUP': [exactScope],
        'CHANGE_INITIATIVE': [exactScope],
        'ORG_UNIT': [exactScope, childrenScope],
        'MEASURABLE': [exactScope, childrenScope]
    }

};


const template = require('./attestation-run-create.html');


function controller(notification, serviceBroker, involvementKindStore) {

    const vm = initialiseData(this, initialState);

    involvementKindStore.findAll().then(
        involvementKinds => {
            vm.availableInvolvementKinds = involvementKinds;
        }
    );

    vm.onSelectorEntityKindChange = () => {
        vm.attestationRun.selectorEntity = null;
    };

    vm.onSelectorEntitySelect = (itemId, entity) => {
        vm.attestationRun.selectorEntity = entity;
    };

    vm.onSubmit = () => {
        const involvementKindIds = _.map(vm.attestationRun.involvementKinds, ik => ik.id);
        const command = {
            name: vm.attestationRun.name,
            description: vm.attestationRun.description,
            selectionOptions: {
                entityReference: {
                    kind: vm.attestationRun.selectorEntityKind,
                    id: vm.attestationRun.selectorEntity.id
                },
                scope: vm.attestationRun.selectorScope
            },
            targetEntityKind: vm.attestationRun.targetEntityKind,
            involvementKindIds: involvementKindIds,
            dueDate: vm.attestationRun.dueDate
        };
        serviceBroker
            .execute(CORE_API.AttestationRunStore.create, [command])
            .then(res => {
                // todo redirect to attestation run page
                notification.success('Attestation run created successfully');                
            }, () => notification.error('Failed to create attestation run'))
    };

}


controller.$inject = [
    'Notification',
    'ServiceBroker',
    'InvolvementKindStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};

