/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import {initialiseData} from "../../common/index";
import template from "./survey-run-create-general.html";
import {CORE_API} from "../../common/services/core-api-utils";
import _ from "lodash";


const bindings = {
    surveyTemplate: "<",
    surveyRun: "<",
    onSave: "<"
};


const exactScope = {
    value: "EXACT",
    name: "Exact"
};


const childrenScope = {
    value: "CHILDREN",
    name: "Children"
};


const initialState = {
    allowedEntityKinds: [{
        value: "APP_GROUP",
        name: "Application Group"
    },{
        value: "ORG_UNIT",
        name: "Org Unit"
    },{
        value: "MEASURABLE",
        name: "Measurable"
    }],
    allowedScopes: {
        "APP_GROUP": [exactScope],
        "CHANGE_INITIATIVE": [exactScope],
        "ORG_UNIT": [exactScope, childrenScope],
        "MEASURABLE": [exactScope, childrenScope]
    },
    surveyRun: {
        selectorEntity: null,
        dueDate: null,
        approvalDueDate: null
    },
    surveyInstance: {
        dueDate: null,
        approvalDueDate: null,
        owningRole: null
    }
};


function mkAllowedEntityKinds(entityKind) {
    const selectors = [{
        value: "APP_GROUP",
        name: "Application Group"
    },{
        value: "ORG_UNIT",
        name: "Org Unit"
    },{
        value: "MEASURABLE",
        name: "Measurable"
    }];

    if (entityKind === "CHANGE_INITIATIVE") {
        selectors.push({
            value: "CHANGE_INITIATIVE",
            name: "Change Initiative"
        });
    }
    return selectors;
}


function controller(appGroupStore, involvementKindStore, serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => {
        if (vm.surveyTemplate) {
            vm.allowedEntityKinds = mkAllowedEntityKinds(vm.surveyTemplate.targetEntityKind);

            involvementKindStore.findAll()
                .then(involvementKinds => {
                    vm.availableInvolvementKinds = _.filter(
                        involvementKinds,
                        d => d.subjectKind === vm.surveyTemplate.targetEntityKind);
                });
        }
    };

    Promise
        .all([appGroupStore.findPublicGroups(), appGroupStore.findPrivateGroups()])
        .then(([publicGroups = [], privateGroups = []]) => {
            vm.availableAppGroups = [].concat(publicGroups, privateGroups);
        });


    vm.$onInit = () => {
        serviceBroker.loadViewData(CORE_API.RoleStore.findAllRoles)
            .then(r => vm.customRoles = _.filter(r.data, d => d.isCustom));
    };

    vm.onSelectorEntityKindChange = () => {
        vm.surveyRun.selectorEntity = null;
    };

    vm.onSelectorEntitySelect = (entity) => {
        vm.surveyRun.selectorEntity = entity;
    };

    vm.onSubmit = () => {
        vm.onSave(this.surveyRun, vm.surveyInstance);
    };

    vm.isLastInList = (item, list) => {
        return _.indexOf(list, item) + 1 !== list.length;
    }
}


controller.$inject = [
    "AppGroupStore",
    "InvolvementKindStore",
    "ServiceBroker"
];


export default {
    bindings,
    template,
    controller
};
