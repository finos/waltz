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

import _ from 'lodash';

import { CORE_API } from '../../../common/services/core-api-utils';
import { initialiseData } from '../../../common';
import { mkSelectionOptions } from "../../../common/selector-utils";
import { buildHierarchies } from "../../../common/hierarchy-utils";

import {changeInitiative} from "../../../common/services/enums/change-initiative";
import {getEnumName} from "../../../common/services/enums";

import template from './change-initiative-navigator-section.html';

const bindings = {
    parentEntityRef: '<',
};


const externalIdCellTemplate = `
    <div class="ui-grid-cell-contents" style="vertical-align: baseline; ">
        <a ng-click="grid.appScope.$ctrl.onSelectViaGrid(row.entity)"
           class="clickable">
            <span ng-bind="row.entity.externalId"></span>
            &nbsp;
            <waltz-icon name="{{row.entity.children.length > 0 ? 'sitemap' : 'fw'}}" rotate="270"></waltz-icon>
        </a>
    </div>
`;

const nameCellTemplate = `
    <div class="ui-grid-cell-contents">
        <a ng-click="grid.appScope.$ctrl.onSelectViaGrid(row.entity)"
           class="clickable">
            <span ng-bind="row.entity.name"></span>
        </a>
    </div>
`;


const initialState = {
    selectedChange: null,
    visibility: {
        sourcesOverlay: false
    },
    changeInitiativeLifecyclePhaseByKey: {},
    children: [],
    childrenGridOptions: {
        columnDefs: [
            { field: 'externalId', name: 'id', cellTemplate: externalIdCellTemplate },
            { field: 'name', cellTemplate: nameCellTemplate },
            { field: 'kindName', name: 'Kind' },
            { field: 'lifecyclePhaseName', name: 'Phase' }
        ],
        data: [],
        enableGridMenu: false,
        enableColumnMenus: false,
    },
    parents: [],
    parentGridOptions: {
        columnDefs: [
            { field: 'externalId', name: 'id', cellTemplate: externalIdCellTemplate },
            { field: 'name', cellTemplate: nameCellTemplate },
            { field: 'kindName', name: 'Kind' },
            { field: 'lifecyclePhaseName', name: 'Phase' }
        ],
        data: [],
        enableGridMenu: false,
        enableColumnMenus: false,
    }
};


function enrichChangeInitiative(ci, changeInitiativeLifecyclePhaseByKey = {}) {
    const extensions = {
        kindName: getEnumName(changeInitiative, ci.changeInitiativeKind),
        lifecyclePhaseName: getEnumName(changeInitiativeLifecyclePhaseByKey, ci.lifecyclePhase)
    };

    return Object.assign({}, ci, extensions);
}


function buildChangeInitiativeHierarchy(changeInitiatives = [], changeInitiativeLifecyclePhaseByKey = {}) {
    const enriched = _.map(
        changeInitiatives,
        ci => enrichChangeInitiative(ci, changeInitiativeLifecyclePhaseByKey));
    return buildHierarchies(enriched);
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    vm.onSelectViaGrid = (ci) => {
        vm.selectedChange = ci;
    };

    vm.onClearSelection = () => vm.selectedChange = null;

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                vm.changeInitiativeLifecyclePhaseByKey = _
                    .chain(r.data)
                    .filter({ type: 'changeInitiativeLifecyclePhase'})
                    .map(c => ({ key: c.key, name: c.name }))
                    .keyBy('key')
                    .value();
            });
    };

    vm.$onChanges = () => {
        if(vm.parentEntityRef) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findBySelector,
                    [ mkSelectionOptions(vm.parentEntityRef, 'CHILDREN') ], { force: true })
                .then(r => {
                    vm.children = _.reject(r.data, { id: vm.parentEntityRef.id });
                    vm.childrenGridOptions.data = buildChangeInitiativeHierarchy(vm.children, vm.changeInitiativeLifecyclePhaseByKey);
                });

            serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findBySelector,
                    [ mkSelectionOptions(vm.parentEntityRef, 'PARENTS') ], { force: true })
                .then(r => {
                    vm.parents = _.reject(r.data, { id: vm.parentEntityRef.id });
                    vm.parentGridOptions.data = buildChangeInitiativeHierarchy(vm.parents, vm.changeInitiativeLifecyclePhaseByKey);
                });
        }
    };


}


controller.$inject = [
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzChangeInitiativeNavigatorSection'
};
