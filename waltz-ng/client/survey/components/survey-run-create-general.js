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
import {initialiseData} from "../../common/index";
import template from './survey-run-create-general.html';


const bindings = {
    surveyTemplate: '<',
    surveyRun: '<',
    onSave: '<'
};


const exactScope = {
    value: 'EXACT',
    name: 'Exact'
};


const childrenScope = {
    value: 'CHILDREN',
    name: 'Children'
};


const initialState = {
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


function mkAllowedEntityKinds(entityKind) {
    const selectors = [{
        value: 'APP_GROUP',
        name: 'Application Group'
    },{
        value: 'ORG_UNIT',
        name: 'Org Unit'
    },{
        value: 'MEASURABLE',
        name: 'Measurable'
    }];

    if (entityKind === 'CHANGE_INITIATIVE') {
        selectors.push({
            value: 'CHANGE_INITIATIVE',
            name: 'Change Initiative'
        });
    }
    return selectors;
}


function controller(appGroupStore, involvementKindStore) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.surveyTemplate) {
            vm.allowedEntityKinds = mkAllowedEntityKinds(vm.surveyTemplate.targetEntityKind);
        }
    };

    Promise
        .all([appGroupStore.findPublicGroups(), appGroupStore.findPrivateGroups()])
        .then(([publicGroups = [], privateGroups = []]) => {
            vm.availableAppGroups = [].concat(publicGroups, privateGroups);
        }
    );

    involvementKindStore.findAll().then(
        involvementKinds => {
            vm.availableInvolvementKinds = involvementKinds;
        }
    );

    vm.onSelectorEntityKindChange = () => {
        vm.surveyRun.selectorEntity = null;
    };

    vm.onSelectorEntitySelect = (entity) => {
        vm.surveyRun.selectorEntity = entity;
    };

    vm.onSubmit = () => {
        vm.onSave(this.surveyRun);
    };
}


controller.$inject = [
    'AppGroupStore',
    'InvolvementKindStore'
];


export default {
    bindings,
    template,
    controller
};
