/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {initialiseData} from "../common";
import {toGraphId} from "../flow-diagram/flow-diagram-utils";
import _ from "lodash";


import template from './physical-specification-view.html';


const initialState = {
    visibility: {
        createReportOverlay: false,
        createReportButton: true,
        createReportBusy: false
    },
    createReportForm: {
        name: ""
    },
    selectedSpecDefinition: {},
    specDefinitions: [],
    specDefinitionCreate: {
        creating: false
    }
};


const addToHistory = (historyStore, spec) => {
    if (! spec) { return; }
    historyStore.put(
        spec.name,
        'PHYSICAL_SPECIFICATION',
        'main.physical-specification.view',
        { id: spec.id });
};



function loadFlowDiagrams(specId, $q, flowDiagramStore, flowDiagramEntityStore) {
    const ref = {
        id: specId,
        kind: 'PHYSICAL_SPECIFICATION'
    };

    const selector = {
        entityReference: ref,
        scope: 'EXACT'
    };

    const promises = [
        flowDiagramStore.findForSelector(selector),
        flowDiagramEntityStore.findForSelector(selector)
    ];
    return $q
        .all(promises)
        .then(([flowDiagrams, flowDiagramEntities]) => ({ flowDiagrams, flowDiagramEntities }));
}


function mkReleaseLifecycleStatusChangeCommand(newStatus) {
    return { newStatus };
};


function controller($q,
                    $stateParams,
                    applicationStore,
                    flowDiagramStore,
                    flowDiagramEntityStore,
                    historyStore,
                    logicalFlowStore,
                    notification,
                    orgUnitStore,
                    physicalSpecDefinitionStore,
                    physicalSpecDefinitionFieldStore,
                    physicalSpecDefinitionSampleFileStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const specId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_SPECIFICATION',
        id: specId
    };

    vm.entityReference = ref;

    // -- LOAD ---

    physicalSpecificationStore
        .getById(specId)
        .then(spec => vm.specification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou)
        .then(() => vm.entityReference = Object.assign({}, vm.entityReference, { name: vm.specification.name }))
        .then(() => addToHistory(historyStore, vm.specification));

    physicalFlowStore
        .findBySpecificationId(specId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);

    logicalFlowStore
        .findBySelector({ entityReference: ref, scope: 'EXACT'})
        .then(logicalFlows => {
            vm.logicalFlows = logicalFlows;
            vm.logicalFlowsById = _.keyBy(logicalFlows, 'id')
        });

    const loadSpecDefinitions = () => physicalSpecDefinitionStore
        .findForSpecificationId(specId)
        .then(specDefs => vm.specDefinitions = specDefs)
        .then(specDefs => {
            const activeSpec = _.find(specDefs, { status: 'ACTIVE'});
            if (activeSpec) vm.selectSpecDefinition(activeSpec);
        });

    loadSpecDefinitions();


    vm.loadFlowDiagrams = () => {
        loadFlowDiagrams(specId, $q, flowDiagramStore, flowDiagramEntityStore)
            .then(r => Object.assign(vm, r));
    };

    vm.loadFlowDiagrams();


    vm.selectSpecDefinition = (def) => {
        const specDefFieldPromise = physicalSpecDefinitionFieldStore
            .findForSpecDefinitionId(def.id);

        const specDefSampleFilePromise = physicalSpecDefinitionSampleFileStore
            .findForSpecDefinitionId(def.id);

        $q.all([specDefFieldPromise, specDefSampleFilePromise])
            .then(([fields, file]) => {
                vm.selectedSpecDefinition.def = def;
                vm.selectedSpecDefinition.fields = fields;
                vm.selectedSpecDefinition.sampleFile = file;
            });
    };

    vm.showCreateSpecDefinition = () => {
        vm.specDefinitionCreate.creating = true;
    };

    vm.hideCreateSpecDefinition = () => {
        vm.specDefinitionCreate.creating = false;
    };

    vm.createSpecDefinition = (specDef) => {
        physicalSpecDefinitionStore
            .create(specId, specDef.def)
            .then(specDefId => {
                const fieldsPromise = physicalSpecDefinitionFieldStore
                    .createFields(specDefId, specDef.fields);

                if (specDef.sampleData) {
                    const sampleDataPromise = physicalSpecDefinitionSampleFileStore
                        .create(specDefId, {
                            name: vm.specification.name,
                            fileData: specDef.sampleData
                        });

                    return $q.all([fieldsPromise, sampleDataPromise]);
                } else {
                    return fieldsPromise;
                }
            })
            .then(r => {
                notification.success('Specification definition created successfully');
                loadSpecDefinitions();
                vm.hideCreateSpecDefinition();
            }, r => {
                notification.error("Failed to create specification definition. Ensure that 'version' is unique");
            });
    };

    vm.deleteSpec = (specDef) => {
        physicalSpecDefinitionStore
            .deleteSpecification(specDef.id)
            .then(result => {
                if (result) {
                    notification.success(`Deleted version ${specDef.version}`);
                    loadSpecDefinitions();
                } else {
                    notification.error(`Could not delete version ${specDef.version}`);
                }
            })
    };

    vm.activateSpec = (specDef) => {
        physicalSpecDefinitionStore
            .updateStatus(specDef.id, mkReleaseLifecycleStatusChangeCommand('ACTIVE'))
            .then(result => {
                if (result) {
                    notification.success(`Marked version ${specDef.version} as active`);
                    loadSpecDefinitions();
                } else {
                    notification.error(`Could not mark version ${specDef.version} as active`);
                }
            })
    };

    vm.markSpecObsolete = (specDef) => {
        physicalSpecDefinitionStore
            .updateStatus(specDef.id, mkReleaseLifecycleStatusChangeCommand('OBSOLETE'))
            .then(result => {
                if (result) {
                    notification.success(`Marked version ${specDef.version} as obsolete`);
                    loadSpecDefinitions();
                } else {
                    notification.error(`Could not mark version ${specDef.version} as obsolete`);
                }
            })
    };

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'FlowDiagramStore',
    'FlowDiagramEntityStore',
    'HistoryStore',
    'LogicalFlowStore',
    'Notification',
    'OrgUnitStore',
    'PhysicalSpecDefinitionStore',
    'PhysicalSpecDefinitionFieldStore',
    'PhysicalSpecDefinitionSampleFileStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};