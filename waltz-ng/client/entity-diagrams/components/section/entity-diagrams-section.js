/*
 * Waltz - Enterprise Architecture
 *  Copyright (C) 2016, 2017 Waltz open source project
 *  See README.md for more information
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
import {CORE_API} from "../../../common/services/core-api-utils";
import {initialiseData} from "../../../common";
import _ from "lodash";
import template from "./entity-diagrams-section.html";
import {refToString, toEntityRef} from "../../../common/entity-utils";
import {determineIfCreateAllowed} from "../../../flow-diagram/flow-diagram-utils";


const bindings = {
    parentEntityRef: "<"
};


const initialState = {
    selectedDiagram: null,
    visibility: {
        flowDiagramMode: null, // null | VIEW | EDIT
        makeNew: true
    },
};


function combineDiagrams(flowDiagrams = [], svgDiagrams = [], flowActions = []) {

    const convertFlowDiagramFn = d => {
        return {
            id: refToString(d),
            ref: toEntityRef(d),
            type: "Flow",
            name: d.name,
            icon: "random",
            description: d.description,
            actions: flowActions,
            lastUpdatedAt: d.lastUpdatedAt,
            lastUpdatedBy: d.lastUpdatedBy
        };
    };

    const convertSvgDiagramFn = d => {
        return {
            id: `ENTITY_SVG_DIAGRAM/${d.id}`,
            ref: toEntityRef(d, "ENTITY_SVG_DIAGRAM"),
            type: "Generic",
            name: d.name,
            icon: "picture-o",
            description: d.description,
            svg: d.svg
        }
    };

    const normalize = (normalizeFn, diagrams = []) => _.map(diagrams, normalizeFn);

    return _.chain([])
        .concat(normalize(convertFlowDiagramFn, flowDiagrams))
        .concat(normalize(convertSvgDiagramFn, svgDiagrams))
        .orderBy(d => d.name.toLowerCase())
        .value();
}


function selectInitialDiagram(diagrams = [], selectedDiagram) {
    if (selectedDiagram) return selectedDiagram;
    return _.find(diagrams, d => d.type === "Generic");
}


function controller($q,
                    serviceBroker,
                    notification) {
    const vm = initialiseData(this, initialState);

    const loadFlowDiagrams = (force = true) => serviceBroker
            .loadViewData(
                CORE_API.FlowDiagramStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force })
            .then(r => r.data);

    const loadEntitySvgDiagrams = (force = false) => serviceBroker
            .loadViewData(
                CORE_API.EntitySvgDiagramStore.findByEntityReference,
                [ vm.parentEntityRef ],
                { force  })
            .then(r => r.data);

    const flowActions = [
        {
            name: "Clone",
            icon: "clone",
            execute: (diagram) => {
                const newName = prompt("What shall the cloned copy be called ?", `Copy of ${diagram.name}`);
                if (newName == null) {
                    notification.warning("Clone cancelled");
                    return;
                }
                if (_.isEmpty(newName.trim())) {
                    notification.warning("Clone cancelled, no name given");
                    return;
                }
                serviceBroker
                    .execute(CORE_API.FlowDiagramStore.clone, [diagram.ref.id, newName])
                    .then(newId => {
                        notification.success("Diagram cloned");
                        reload();
                    })
                    .catch(e => notification.error(`Failed to clone diagram: ${e.data.message}`));

            }}
    ];

    function reload() {
        const promises = [
            loadFlowDiagrams(true),
            loadEntitySvgDiagrams(false)
        ];
        return $q.all(promises)
            .then(([flowDiagrams = [], svgDiagrams = []]) => {
                vm.diagrams = combineDiagrams(flowDiagrams, svgDiagrams, flowActions);
                vm.selectedDiagram = selectInitialDiagram(vm.diagrams, vm.selectedDiagram);
                return vm.diagrams;
            });
    }

    vm.$onInit = () => {
        reload();
        vm.visibility.makeNew = determineIfCreateAllowed(vm.parentEntityRef.kind);
    };

    vm.$onChanges = (changes) => {
    };

    vm.onDiagramSelect = (diagram) => {
        vm.selectedDiagram = diagram;
        vm.visibility.flowDiagramMode = "VIEW";
    };

    vm.onDiagramDismiss = () => {
        vm.selectedDiagram = null;
    };

    vm.onEditorClose = () => {
        vm.selectedDiagram = null;
        reload();
    };

    vm.onDiagramEdit = (diagram) => {
        vm.visibility.flowDiagramMode = "EDIT";
    };

    vm.onMakeNewFlowDiagram = () => {
        const name = prompt("Please enter a name for the new diagram ?");
        if (name == null) {
            notification.warning("Create cancelled");
            return;
        }
        if (_.isEmpty(name.trim())) {
            notification.warning("Create cancelled, no name given");
            return;
        }

        let newDiagramId = null;
        serviceBroker
            .execute(CORE_API.FlowDiagramStore.makeNewForEntityReference, [vm.parentEntityRef, name])
            .then(r => {
                newDiagramId = `FLOW_DIAGRAM/${r.data}`;
                return reload();
            })
            .then(diagrams => {
                notification.success("Diagram created, click edit if you wish to make changes");
                const newDiagram = _.find(diagrams, { id:  newDiagramId });
                vm.onDiagramSelect(newDiagram);
            })
            .catch(e => notification.error(`Failed to create new diagram: ${e.data.message}`));
    };
}


controller.$inject = [
    "$q",
    "ServiceBroker",
    "Notification"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzEntityDiagramsSection"
};
