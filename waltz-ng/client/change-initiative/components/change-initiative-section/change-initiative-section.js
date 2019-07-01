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

import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import {mkSelectionOptions} from "../../../common/selector-utils";

import template from "./change-initiative-section.html";
import {changeInitiative} from "../../../common/services/enums/change-initiative";
import {getEnumName} from "../../../common/services/enums";
import indexByKeyForType from "../../../enum-value/enum-value-utilities";
import {mkRef} from "../../../common/entity-utils";
import {fakeInitiative, fakeProgramme} from "../../change-initiative-utils";


const bindings = {
    parentEntityRef: "<",
};


const externalIdCellTemplate = `
    <div class="ui-grid-cell-contents" 
         style="vertical-align: baseline; ">
        <waltz-entity-link entity-ref="COL_FIELD">
        </waltz-entity-link>
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
            { field: "initiative", name: "Initiative", cellTemplate: externalIdCellTemplate },
            { field: "programme", name: "Programme", cellTemplate: externalIdCellTemplate },
            { field: "project", name: "Project", cellTemplate: externalIdCellTemplate },
            { field: "name", name: "Name" },
            { field: "kind", name: "Kind" },
            { field: "lifecyclePhase", name: "Phase" }
        ],
        data: []
    }
};


const NONE = null;

function determineHierarchy(cisById = {}, cisByParent = {}, ci) {
    switch (ci.kind) {
        case "INITIATIVE":
            return {
                initiative: ci,
                programme: NONE,
                project: NONE,
            };
        default:
            return {
                initiative: NONE,
                programme: NONE,
                project: NONE,
            };
    };
}

function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const prepareTableData = (changeInitiatives) => {
        const cisByParentId = _.groupBy(changeInitiatives, d => d.parentId);
        const cisById = _.keyBy(changeInitiatives, d => d.id);


        vm.gridOptions.data = _
            .chain(changeInitiatives)
            .map(ci => {
                const hier = determineHierarchy(cisById, cisByParentId, ci);
                return {
                    initiative: hier.initiative,
                    programme: hier.programme,
                    project: hier.project,
                    name: ci.name,
                    description: ci.description,
                    lifecyclePhase: getEnumName(vm.changeInitiativeLifecyclePhaseByKey, ci.lifecyclePhase),
                    kind: getEnumName(changeInitiative, ci.changeInitiativeKind)
                }
            })
            .value();

        return;

        const roots = _
            .chain(changeInitiatives)
            .filter(ci => !ci.parentId)
            .map(ci => {
                const children = cisByParentId[ci.id] || [];
                const icon = children.length > 0 ? "sitemap" : "fw";
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


    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                vm.changeInitiativeLifecyclePhaseByKey = indexByKeyForType(
                    r.data,
                    "changeInitiativeLifecyclePhase");
            });
    };

    vm.$onChanges = (changes) => {
        if(vm.parentEntityRef && changes.parentEntityRef.previousValue.id !== changes.parentEntityRef.currentValue.id) {
            serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                    [ mkSelectionOptions(vm.parentEntityRef) ])
                .then(r => prepareTableData(r.data));
        }
    };


}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzChangeInitiativeSection"
};
