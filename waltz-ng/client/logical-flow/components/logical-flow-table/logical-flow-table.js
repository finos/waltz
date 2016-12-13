
/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import _ from 'lodash';
import {mkEntityLinkGridCell} from "../../../common";


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
    field: 'authSourceRating',
    displayName: 'Source Rating',
    cellTemplate: `<span>
                     <waltz-rating-indicator-cell rating="row.entity.ragRating"></waltz-rating-indicator-cell>
                     <span class="ui-grid-cell-contents" 
                           ng-bind="COL_FIELD | toDisplayName:'rating'"></span>
                   </span>`,
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
        .map(dc => _.assign({}, {
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


function toRag(authRating) {
    switch (authRating) {
        case "PRIMARY":
            return 'G';
        case "SECONDARY":
            return 'A';
        case "DISCOURAGED":
            return 'R';
        case "NO_OPINION":
            return 'Z';
        default:
            return ''
    }
}


function prepareGridData(flows = [], decorators = [], displayNameService) {
    const groupedDecorators = groupDecoratorsByFlowId(decorators, displayNameService);
    return _.flatMap(flows,
        flow => _.map(groupedDecorators[flow.id],
            dc => _.assign({
                dataType: dc.dataType,
                authSourceRating: dc.authSourceRating,
                ragRating: toRag(dc.authSourceRating)
            },
            flow)));
}


function controller(displayNameService) {
    const vm = this;

    vm.$onChanges = () => {
        const gridData = prepareGridData(vm.flows, vm.decorators, displayNameService);
        vm.columnDefs = columnDefs;
        vm.gridData = gridData;
    };
}


controller.$inject = [
    'DisplayNameService',
];


const component = {
    bindings,
    template,
    controller
};


export default component;