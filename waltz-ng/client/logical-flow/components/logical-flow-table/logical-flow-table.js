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
import _ from "lodash";
import {mkEntityLinkGridCell} from "../../../common/grid-utils";
import {mkAuthoritativeRatingSchemeItems} from "../../../ratings/rating-utils";


const bindings = {
    decorators: '<',
    flows: '<',
    onInitialise: '<'
};


const template = `<div class="row">
                    <div class="col-md-12">
                        <waltz-grid-with-search column-defs="$ctrl.columnDefs"
                                                entries="$ctrl.gridData"
                                                on-initialise="$ctrl.onInitialise">
                        </waltz-grid-with-search>
                    </div>
                  </div>`;


const ratingColumn = {
    field: 'rating',
    displayName: 'Authoritativeness',
    cellTemplate: `<span>
                     <waltz-rating-indicator-cell rating="row.entity.rating" 
                                                  show-name="true">
                     </waltz-rating-indicator-cell>
                   </span>`,
    sortingAlgorithm: (a, b) => a.name.localeCompare(b.name),
    exportFormatter: (input) => input.name
};


const columnDefs = [
    mkEntityLinkGridCell('Source', 'source', 'none'),
    mkEntityLinkGridCell('Target', 'target', 'none'),
    mkEntityLinkGridCell('Data Type', 'dataType', 'none'),
    ratingColumn
];


function groupDecoratorsByFlowId(decorators = [], displayNameService) {
    const resolveName = id => displayNameService.lookup('dataType', id);

    return _.chain(decorators)
        .filter(dc => dc.decoratorEntity.kind === 'DATA_TYPE')
        .map(dc => Object.assign({}, {
            dataFlowId: dc.dataFlowId,
            dataType: {
                id: dc.decoratorEntity.id,
                name: resolveName(dc.decoratorEntity.id),
                kind: 'DATA_TYPE'
            },
            authSourceRating: dc.rating
        }))
        .groupBy('dataFlowId')
        .value();
}


function prepareGridData(flows = [], decorators = [], displayNameService, ratingSchemeItems) {
    const groupedDecorators = groupDecoratorsByFlowId(decorators, displayNameService);
    return _.flatMap(flows,
        flow => _.map(groupedDecorators[flow.id],
            dc => Object.assign({
                dataType: dc.dataType,
                rating: ratingSchemeItems[dc.authSourceRating]
            },
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
    'DisplayNameService'
];


const component = {
    bindings,
    template,
    controller
};


export default component;