/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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

import _ from "lodash";
import {initialiseData} from "../../../common";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import template from "./auth-sources-table.html";


const bindings = {
    parentEntityRef: "<",
    authSources: "<"
};


const initialState = {
    consumersByAuthSourceId: {},
    columnDefs: null
};




function shouldShowConsumers(parentRef) {
    const kind = _.get(parentRef, "kind", "");
    return kind === "DATA_TYPE";
}


function mkColumnDefs(parentRef) {
    const consumerCell = shouldShowConsumers(parentRef)
        ? {
            field: "consumers",
            displayName: "Consumers",
            cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span class="label"
                      style="cursor: pointer"
                      ng-class="{ 'label-warning': COL_FIELD.length, 'label-default': COL_FIELD.length == 0 }"
                      uib-popover-template="'wast/consumers-popup.html'"
                      popover-class="waltz-popover-wide"
                      popover-append-to-body="true"
                      popover-placement="top-right"
                      popover-trigger="outsideClick"
                      ng-bind="COL_FIELD.length > 0
                            ? COL_FIELD.length
                            : '-'">
                </span>
            </div>`}
        : null;

    const notesCell = {
        field: "description",
        displayName: "Notes",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span ng-if="COL_FIELD.length > 0">
                    <waltz-icon name="sticky-note"
                                style="color: #337ab7; cursor: pointer"
                                uib-popover-template="'wast/desc-popup.html'"
                                popover-class="waltz-popover-wide"
                                popover-append-to-body="true"
                                popover-placement="top-right"
                                popover-trigger="outsideClick">
                    </waltz-icon>
                </span>
            </div>`
    };

    const ratingCell = {
        field: "ratingValue",
        displayName: "Rating",
        cellTemplate: `
            <div class="ui-grid-cell-contents">
                <span ng-bind="COL_FIELD.name"
                      title="{{COL_FIELD.description}}">
                </span>
            </div>`
    };

    return _.compact([
        mkEntityLinkGridCell("Data Type", "dataType", "none", "right"),
        mkEntityLinkGridCell("Declaring Org Unit", "declaringOrgUnit", "none"),
        mkEntityLinkGridCell("Application", "app", "none", "right"),
        consumerCell,
        ratingCell,
        notesCell
    ]);
}


function controller($q, serviceBroker, enumValueService) {

    const vm = initialiseData(this, initialState);


    function loadConsumers() {
        const selector = {
            entityReference: vm.parentEntityRef,
            scope: "CHILDREN"
        };

        return serviceBroker
            .loadViewData(
                CORE_API.AuthSourcesStore.calculateConsumersForDataTypeIdSelector,
                [ selector ])
            .then(r => {
                vm.consumersByAuthSourceId = _
                    .chain(r.data)
                    .keyBy(d => d.key.id)
                    .mapValues(v => _.sortBy(v.value, "name"))
                    .value();
            });
    }


    function mkGridData() {
        const dataTypesByCode= _.keyBy(vm.dataTypes, "code");
        const orgUnitsById = _.keyBy(vm.orgUnits, "id");

        vm.columnDefs = mkColumnDefs(vm.parentEntityRef);
        vm.gridData = _.map(vm.authSources, d => {
            const authoritativenessRatingEnum = vm.enums.AuthoritativenessRating[d.rating];
            return {
                app: d.applicationReference,
                dataType: dataTypesByCode[d.dataType],
                appOrgUnit: d.appOrgUnitReference,
                declaringOrgUnit: orgUnitsById[d.parentReference.id],
                description: d.description,
                rating: d.rating,
                ratingValue: authoritativenessRatingEnum,
                consumers: vm.consumersByAuthSourceId[d.id] || []
            };
        });
    }


    function loadAll() {
        const enumPromise = enumValueService
            .loadEnums()
            .then(r => vm.enums = r);

        const dataTypePromise = serviceBroker
            .loadAppData(CORE_API.DataTypeStore.findAll)
            .then(r => vm.dataTypes = r.data);

        const orgUnitPromise = serviceBroker
            .loadAppData(CORE_API.OrgUnitStore.findAll)
            .then(r => vm.orgUnits = r.data);

        const consumerPromise = shouldShowConsumers(vm.parentEntityRef)
            ? loadConsumers()
            : null;

        return $q
            .all(_.compact([enumPromise, dataTypePromise, orgUnitPromise, consumerPromise]))
            .then(mkGridData);
    }

    vm.$onInit = () => {
        loadAll();
    };

    vm.$onChanges = (changes) => {
        if(vm.authSources) {
            loadAll();
        }
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "EnumValueService"
];


export const component = {
    bindings,
    controller,
    template
};

export const id = "waltzAuthSourcesTable";

