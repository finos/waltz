
/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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