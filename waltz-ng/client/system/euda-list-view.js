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
import template from './euda-list-view.html';
import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import * as _ from "lodash";


const bindings = {};

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