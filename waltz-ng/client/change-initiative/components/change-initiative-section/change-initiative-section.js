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

import { CORE_API } from "../../../common/services/core-api-utils";
import { initialiseData } from "../../../common";
import { mkSelectionOptions } from "../../../common/selector-utils";

import template from "./change-initiative-section.html";
import { changeInitiative } from "../../../common/services/enums/change-initiative";
import { getEnumName } from "../../../common/services/enums";
import indexByKeyForType from "../../../enum-value/enum-value-utilities";
import { isSameParentEntityRef } from "../../../common/entity-utils";
import { fakeInitiative, fakeProgramme } from "../../change-initiative-utils";
import { filterByAssessmentRating, mkAssessmentSummaries } from "../../../assessments/assessment-utils";

const bindings = {
    parentEntityRef: "<",
};



const externalIdCellTemplate = `
    <div class="ui-grid-cell-contents" 
         style="vertical-align: baseline; ">
        <waltz-entity-link icon-placement="none" 
                           entity-ref="COL_FIELD">
        </waltz-entity-link>
    </div>
`;


function mkRefCol(propName) {
    return {
        width: "15%",
        field: propName,
        toSearchTerm: d => _.get(d, [propName, "name"], ""),
        cellTemplate: externalIdCellTemplate
    };
}


const initialState = {
    changeInitiatives: [],
    changeInitiativeLifecyclePhaseByKey: {},
    selectedChange: null,
    visibility: {
        sourcesOverlay: false
    },
    filterHelpText: "Select an assessment category to filter the change initiatives",
    gridOptions: {
        columnDefs: [
            { width: "15%", field: "kind", name: "Kind" },
            mkRefCol("initiative"),
            mkRefCol("programme"),
            mkRefCol("project"),
            { width: "25%", field: "name", name: "Name" },
            { width: "15%", field: "lifecyclePhase", name: "Phase" }
        ],
        data: []
    }
};


function determineHierarchy(cisById = {}, ci) {
    const none = null;

    switch (ci.changeInitiativeKind) {
        case "INITIATIVE":
            return {
                initiative: ci,
                programme: none,
                project: none,
            };
        case "PROGRAMME":
            const programmeParent = cisById[ci.parentId] || fakeInitiative;
            return {
                initiative: programmeParent,
                programme: ci,
                project: none,
            };
        case "PROJECT":
            const projectParent = cisById[ci.parentId] || fakeProgramme;
            const projectProgrammeParent = cisById[projectParent.parentId] || fakeInitiative;
            return {
                initiative: projectProgrammeParent,
                programme: projectParent || fakeInitiative,
                project: ci,
            };
        default:
            return {
                initiative: none,
                programme: none,
                project: none,
            };
    }
}


function toExtRef(d) {
    if (!d) {
        return null;
    } else {
        return {
            kind: d.kind,
            name: d.externalId,
            id: d.id
        };
    }
}


function prepareTableData(changeInitiatives = [], lifecycleNamesByKey = {}) {
    const cisById = _.keyBy(changeInitiatives, d => d.id);

    return _
        .chain(changeInitiatives)
        .map(ci => {
            const hierarchy = determineHierarchy(cisById, ci);
            const phaseName = getEnumName(lifecycleNamesByKey, ci.lifecyclePhase);
            const changeKind = getEnumName(changeInitiative, ci.changeInitiativeKind);
            return {
                initiative: toExtRef(hierarchy.initiative),
                programme: toExtRef(hierarchy.programme),
                project: toExtRef(hierarchy.project),
                name: ci.name,
                description: ci.description,
                lifecyclePhase: phaseName,
                kind: changeKind
            }
        })
        .orderBy(d => ["initiative.name", "programme.name", "project.name", "name"])
        .value();
}


function controller($q, serviceBroker) {
    const vm = initialiseData(this, initialState);

    function init() {
        const enumPromise = serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                vm.changeInitiativeLifecyclePhaseByKey = _
                    .chain(r.data)
                    .filter({ type: "changeInitiativeLifecyclePhase"})
                    .map(d => ({ key: d.key, name: d.name }))
                    .keyBy( d => d.key)
                    .value();
            });

        const schemePromise = serviceBroker
            .loadAppData(CORE_API.RatingSchemeStore.findAll)
            .then(r => vm.ratingSchemes = r.data);

        const assessmentDefinitionPromise = serviceBroker
            .loadAppData(
                CORE_API.AssessmentDefinitionStore.findByKind,
                [ "CHANGE_INITIATIVE" ])
            .then(r => vm.assessmentDefinitions = r.data);

        return $q.all([enumPromise, schemePromise, assessmentDefinitionPromise]);
    }

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                vm.changeInitiativeLifecyclePhaseByKey = indexByKeyForType(
                    r.data,
                    "changeInitiativeLifecyclePhase");
            });

        init();
    };

    vm.$onChanges = (changes) => {
        const sameParent = isSameParentEntityRef(changes);

        if (vm.parentEntityRef && !sameParent) {
            const selectionOptions = mkSelectionOptions(vm.parentEntityRef);
            const ciPromise = serviceBroker
                .loadViewData(
                    CORE_API.ChangeInitiativeStore.findHierarchyBySelector,
                    [ mkSelectionOptions(vm.parentEntityRef) ])
                .then(r => {
                    vm.changeInitiatives = r.data;
                    vm.gridOptions.data = prepareTableData(vm.changeInitiatives, vm.changeInitiativeLifecyclePhaseByKey);
                });

            const assessmentRatingsPromise = serviceBroker
                .loadViewData(
                    CORE_API.AssessmentRatingStore.findByTargetKindForRelatedSelector,
                    [ "CHANGE_INITIATIVE", selectionOptions ])
                .then(r => vm.assessmentRatings = r.data);

            $q.all([init(), ciPromise, assessmentRatingsPromise])
                .then(() => {
                    vm.assessmentSummaries = mkAssessmentSummaries(
                        vm.assessmentDefinitions,
                        vm.ratingSchemes,
                        vm.assessmentRatings,
                        vm.changeInitiatives.length);
                });
        }
    };

    vm.onFilterSelect = d => {
        const changeInitiatives = d === null
            ? vm.changeInitiatives
            : filterByAssessmentRating(vm.changeInitiatives, vm.assessmentRatings, d);

        vm.gridOptions.data = prepareTableData(changeInitiatives, vm.changeInitiativeLifecyclePhaseByKey);
    };
}


controller.$inject = [
    "$q",
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
