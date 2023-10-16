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

import _ from "lodash";

import template from "./related-measurables-table.html";
import {initialiseData} from "../../../common/index";
import {sameRef} from "../../../common/entity-utils";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {CORE_API} from "../../../common/services/core-api-utils";


const bindings = {
    parentEntityRef: "<",
    rows: "<",
    onRowSelect: "<",
    selectedRow: "<"
};


const columnDefs = [
    mkEntityLinkGridCell("From", "a"),
    {
        field: "a.type",
        name: "(From Type)"
    }, {
        field: "relationshipsString",
        name: "Relationships",
        cellTemplate:`
                <div class="ui-grid-cell-contents"
                     uib-popover-template="'wrmt/relationships-popup.html'"
                     popover-trigger="mouseenter"
                     popover-append-to-body="true">
                    <span ng-bind="COL_FIELD">
                    </span>
                </div>`
    },
    mkEntityLinkGridCell("To", "b"),
    {
        field: "b.type",
        name: "(To Type)"
    }
];

const initialState = {
    rows: [],
    columnDefs,
    onRowSelect: (row) => console.log("default on row select", { row })
};


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.loadData = () => {
        const collectedRels = _.map(vm.rows, r => {

            const relatedKinds = _.chain(vm.rows)
                .filter(d => d.a.id === r.a.id && d.b.id === r.b.id)
                .map(rel => _.get(vm.relationshipKindsByCode, rel.relationship.relationship).name)
                .value();

            const relatedKindsString =  _.join(relatedKinds, ", ");

            return Object.assign(
                {},
                {
                    a: r.a,
                    b: r.b,
                    relationships: relatedKinds,
                    relationshipsString: relatedKindsString
                });
        });

        vm.data = _.uniqBy(collectedRels, r => JSON.stringify([r.a, r.b, r.relationshipsString]));
    };

    vm.$onInit = () => {
        serviceBroker.loadAppData(CORE_API.RelationshipKindStore.findAll)
            .then(r => vm.relationshipKindsByCode = _.keyBy(r.data, d => d.code))
            .then(() => vm.loadData())
    };

    vm.isSelected = (row) => {
        if (vm.selectedRow) {
            const sameA = sameRef(row.a, vm.selectedRow.a, { skipChecks: true });
            const sameB = sameRef(row.b, vm.selectedRow.b, { skipChecks: true });
            return sameA && sameB;
        } else {
            return false;
        }
    };

    vm.$onChanges = (c) => {
        if (c.rows && vm.relationshipKindsByCode) {
            vm.loadData();
        }
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    controller,
    bindings
};


export default {
    component,
    id: "waltzRelatedMeasurablesTable"
};