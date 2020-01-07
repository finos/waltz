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
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {mkAuthoritativeRatingSchemeItems} from "../../../ratings/rating-utils";


const bindings = {
    decorators: "<",
    flows: "<"
};


const template = `<div class="row">
                    <div class="col-md-12">
                        <waltz-grid-with-search column-defs="$ctrl.columnDefs"
                                                entries="$ctrl.gridData">
                        </waltz-grid-with-search>
                    </div>
                  </div>`;


const ratingColumn = {
    field: "rating",
    displayName: "Authoritativeness",
    cellTemplate: `<span>
                     <waltz-rating-indicator-cell rating="row.entity.rating"
                                                  show-name="true">
                     </waltz-rating-indicator-cell>
                   </span>`,
    sortingAlgorithm: (a, b) => a.name.localeCompare(b.name),
    exportFormatter: (input) => input.name
};


const columnDefs = [
    mkEntityLinkGridCell("Source", "source", "none"),
    mkEntityLinkGridCell("Target", "target", "none"),
    mkEntityLinkGridCell("Data Type", "dataType", "none"),
    ratingColumn
];


function groupDecoratorsByFlowId(decorators = [], displayNameService) {
    const resolveName = id => displayNameService.lookup("dataType", id);

    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === "DATA_TYPE")
        .map(dc => Object.assign({}, {
            dataFlowId: dc.dataFlowId,
            dataType: {
                id: dc.decoratorEntity.id,
                name: resolveName(dc.decoratorEntity.id),
                kind: "DATA_TYPE"
            },
            authSourceRating: dc.rating
        }))
        .groupBy("dataFlowId")
        .value();
}


function prepareGridData(flows = [], decorators = [], displayNameService, ratingSchemeItems) {
    const groupedDecorators = groupDecoratorsByFlowId(decorators, displayNameService);
    return _.flatMap(
        flows,
        flow => _.map(
            groupedDecorators[flow.id],
            dc => Object.assign(
                {dataType: dc.dataType, rating: ratingSchemeItems[dc.authSourceRating] },
                flow)));
}


function controller(displayNameService) {
    const vm = this;

    vm.$onChanges = () => {
        const ratingScheme = mkAuthoritativeRatingSchemeItems(displayNameService);
        const gridData = prepareGridData(vm.flows, vm.decorators, displayNameService, ratingScheme);
        vm.columnDefs = columnDefs;
        vm.gridData = gridData;
    };
}


controller.$inject = [
    "DisplayNameService"
];


const component = {
    bindings,
    template,
    controller
};


export default component;