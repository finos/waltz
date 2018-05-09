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

import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from './change-initiative-section.html';
import {changeInitiative} from "../../../common/services/enums/change-initiative";
import {getEnumName} from "../../../common/services/enums";


const bindings = {
    parentEntityRef: '<',
};


const externalIdCellTemplate = `
    <div class="ui-grid-cell-contents" style="vertical-align: baseline; ">
        <a ng-click="grid.appScope.onSelectViaGrid(row.entity)"
           class="clickable">
            <span ng-bind="row.entity.externalId"></span>
            &nbsp;
            <waltz-icon name="{{row.entity.icon}}" rotate="270"></waltz-icon>
        </a>
    </div>
`;

const nameCellTemplate = `
    <div class="ui-grid-cell-contents">
        <a ng-click="grid.appScope.onSelectViaGrid(row.entity)"
           class="clickable">
            <span ng-bind="row.entity.name"></span>
        </a>
    </div>
`;


const initialState = {
    changeInitiatives: [],
    changeInitiativeLifecyclePhaseByKey: {},
    selectedChange: null,
    visibility: {
        sourcesOverlay: false
    },
    gridOptions: {
        columnDefs: [
            { field: 'externalId', name: 'id', cellTemplate: externalIdCellTemplate },
            { field: 'name', cellTemplate: nameCellTemplate },
            { field: 'kindName', name: 'Kind' },
            { field: 'lifecyclePhaseName', name: 'Phase' }
        ],
        data: []
    }
};



function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const processChangeInitiativeHierarchy = (changeInitiatives) => {
        const cisByParentId = _.groupBy(changeInitiatives, 'parentId');
        const roots = _
            .chain(changeInitiatives)
            .filter(ci => !ci.parentId)
            .map(ci => {
                const children = cisByParentId[ci.id] || [];
                const icon = children.length > 0 ? 'sitemap' : 'fw';
                const extensions = {
                    kindName: getEnumName(changeInitiative, ci.changeInitiativeKind),
                    lifecyclePhaseName: getEnumName(vm.changeInitiativeLifecyclePhaseByKey, ci.lifecyclePhase),
                    icon,
                    parent: null,
                    children
                };
                return Object.assign({}, ci, extensions);
            })
            .value();

        vm.changeInitiatives = roots;
        vm.gridOptions.data = roots;
    };


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

    vm.$onChanges = (changes) => {
        if(vm.parentEntityRef && changes.parentEntityRef.previousValue.id !== changes.parentEntityRef.currentValue.id) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                    [ mkSelectionOptions(vm.parentEntityRef) ])
                .then(r => processChangeInitiativeHierarchy(r.data));
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
    id: 'waltzChangeInitiativeSection'
};
