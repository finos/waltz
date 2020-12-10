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
import template from './euda-list-view.html';
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import * as _ from "lodash";


const initialState = {
    selectedEuda: null,
    recentlyPromoted: []
};


function controller(serviceBroker, notification) {

    const vm = initialiseData(this, initialState);

    function loadData() {
        return serviceBroker
            .loadViewData(CORE_API.EndUserAppStore.findAll, [], {force: true})
            .then(r => vm.eudas = r.data);
    }

    vm.$onInit = () => {
        vm.columnDefs = mkColumnDefs();

        serviceBroker
            .loadViewData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnitsById = _.keyBy(r.data, d => d.id));

        loadData();
    };

    vm.selectEuda = (euda) => {
        vm.selectedEuda = euda;
    };

    vm.promoteToApplication = (id) => {
        if (confirm("Are you sure you want to promote this End User Application to a Application in Waltz?")){
            return serviceBroker
                .loadViewData(CORE_API.EndUserAppStore.promoteToApplication,
                    [id] )
                .then(r => {
                    vm.recentlyPromoted = _.concat(vm.recentlyPromoted, Object.assign({}, vm.selectedEuda, {appId: r.data.id}));
                    vm.selectedEuda = null;
                })
                .then(() => notification.success('EUDA successfully promoted'))
                .catch(e => notification.error(`Could not promote EUDA: ${e.data.message}`))
                .then(() => loadData());
        }
    }
}


function mkColumnDefs() {
    return [
        {
            field: "id",
            name: "ID",
            width: "10%"
        },
        {
            field: "name",
            name: "Name",
            width: "30%"
        },
        {
            field: "description",
            name: "Description",
            width: "30%"
        },
        {
            field: "applicationKind",
            name: "Kind",
            width: "15%"
        },
        {
            field: "lifecyclePhase",
            name: "Lifecycle Phase",
            cellFilter: "toDisplayName:'lifecyclePhase'",
            width: "15%"
        }
    ];
}


controller.$inject = [
    "ServiceBroker",
    "Notification"
];


export default {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
};