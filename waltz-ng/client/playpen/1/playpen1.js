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


import template from "./playpen1.html";
import {initialiseData} from "../../common";
import {mkRef} from "../../common/entity-utils";
import {CORE_API} from "../../common/services/core-api-utils";
import {mkSelectionOptions} from "../../common/selector-utils";
import {mkLinkGridCell} from "../../common/grid-utils";
import _ from "lodash";

const initData = {
    categoryExtId: "CLOUD_READINESS",
    parentEntityRef: mkRef("APP_GROUP", 11785)
};

const nameCol = mkLinkGridCell("Name", "application.name", "application.id", "main.app.view", { pinnedLeft:true, width: 200});
const extIdCol = { field: "application.externalId", displayName: "Ext. Id", width: 100, pinnedLeft:true};


function prepareColumnDefs(gridData) {
    const measurableCols = _
        .chain(gridData.measurableReferences)
        .orderBy(d => d.name)
        .map(m => ({
            field: `_${m.id}`,
            displayName: m.name,
            width: 100,
            cellTemplate: `
            <div class="waltz-grid-color-cell"
                 ng-bind="COL_FIELD.name"
                 ng-style="{'background-color': COL_FIELD.color}">
            </div>`,
            sortingAlgorithm: (a, b) => {
                if (a == null) return 1;
                if (b == null) return -1;
                return a.position - b.position;
            }
        }))
        .value();

    return _.concat([nameCol, extIdCol], measurableCols);
}


function prepareTableData(gridData) {
    const appsById = _.keyBy(gridData.applications, d => d.id);
    const ratingSchemeItemsByCode = _.keyBy(gridData.ratingSchemeItems, d => d.rating);

    return _
        .chain(gridData.ratings)
        .groupBy(d => d.applicationId)
        .map((xs, k) => _.reduce(
            xs,
            (acc, x) => {
                acc[`_${x.measurableId}`] = ratingSchemeItemsByCode[x.rating];
                return acc;
            },
            { application: appsById[k]}))
        .orderBy(d => d.application.name)
        .value();
}


function controller(serviceBroker) {

    const vm = initialiseData(this, initData);

    serviceBroker
        .loadViewData(
            CORE_API.MeasurableRatingStore.getGridViewByCategoryExtId,
            [vm.categoryExtId, mkSelectionOptions(vm.parentEntityRef)])
        .then(r => {
            const gridData = r.data;
            vm.columnDefs = prepareColumnDefs(gridData);
            vm.tableData = prepareTableData(gridData);
            console.log(vm);
        });

}

controller.$inject = ["ServiceBroker"];

const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

export default view;