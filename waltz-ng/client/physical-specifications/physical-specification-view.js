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
import _ from 'lodash';


const template = require('./physical-specification-view.html');


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


function controller($q,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
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

    // -- LOAD ---

    physicalSpecificationStore
        .getById(specId)
        .then(spec => vm.specification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou)
        .then(() => addToHistory(historyStore, vm.specification));

    physicalFlowStore
        .findBySpecificationId(specId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);

    logicalFlowStore
        .findBySelector({ entityReference: ref, scope: 'EXACT'})
        .then(logicalFlows => vm.logicalFlowsById = _.keyBy(logicalFlows, 'id'));

    const loadSpecDefinitions = () => physicalSpecDefinitionStore
        .findForSpecificationId(specId)
        .then(specDefs => vm.specDefinitions = specDefs)
        .then(specDefs => {
            const activeSpec = _.find(specDefs, { status: 'ACTIVE'});
            if (activeSpec) vm.selectSpecDefinition(activeSpec);
        });

    loadSpecDefinitions();

    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);

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
            .then(specDefId => physicalSpecDefinitionFieldStore
                .createFields(specDefId, specDef.fields))
            .then(r => {
                notification.success('Specification definition created successfully');
                loadSpecDefinitions();
                vm.hideCreateSpecDefinition();
            }, r => {
                notification.error("Failed to create specification definition. Ensure that 'version' is unique");
            });
    };

}


controller.$inject = [
    '$q',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
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