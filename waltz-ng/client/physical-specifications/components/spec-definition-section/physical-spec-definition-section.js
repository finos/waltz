import _ from 'lodash';
import {CORE_API} from '../../../common/services/core-api-utils';
import {initialiseData} from '../../../common';

import template from './physical-spec-definition-section.html';


const bindings = {
    parentEntityRef: '<'
};


const initialState = {
    logicalDataElements: [],
    selectedSpecDefinition: {},
    specDefinitions: [],
    specDefinitionCreate: {
        creating: false
    }
};


function mkReleaseLifecycleStatusChangeCommand(newStatus) {
    return { newStatus };
}


function controller($q,
                    notification,
                    serviceBroker) {

    const vm = initialiseData(this, initialState);

    vm.$onInit = () => {
    };

    vm.$onChanges = (changes) => {
        if(vm.parentEntityRef) {
            serviceBroker
                .loadViewData(CORE_API.PhysicalSpecificationStore.getById, [vm.parentEntityRef.id])
                .then(r => r.data)
                .then(spec => vm.specification = spec)
                .then(spec => serviceBroker.loadViewData(CORE_API.ApplicationStore.getById, [spec.owningEntity.id]))
                .then(r => r.data)
                .then(app => vm.owningEntity = app)
                .then(app =>  serviceBroker.loadViewData(CORE_API.OrgUnitStore.getById, [app.organisationalUnitId]))
                .then(r => r.data)
                .then(ou => vm.organisationalUnit = ou)
                .then(() => vm.entityReference = Object.assign({}, vm.entityReference, { name: vm.specification.name }));

            loadSpecDefinitions();
        }
    };


    const loadSpecDefinitions = (force = false) => serviceBroker
        .loadViewData(CORE_API.PhysicalSpecDefinitionStore.findForSpecificationId, [ vm.parentEntityRef.id ], { force })
        .then(r => r.data)
        .then(specDefs => vm.specDefinitions = specDefs)
        .then(specDefs => {
            const activeSpec = _.find(specDefs, { status: 'ACTIVE'});
            if (activeSpec) vm.selectSpecDefinition(activeSpec, force);

            const selectionOptions = {
                scope: 'EXACT',
                entityReference: { kind: 'PHYSICAL_SPECIFICATION', id: vm.parentEntityRef.id }
            };

            serviceBroker
                .loadViewData(CORE_API.LogicalDataElementStore.findBySelector, [ selectionOptions ], { force })
                .then(r => vm.logicalDataElements = r.data);
        });


    vm.selectSpecDefinition = (def, force = false) => {
        const specDefFieldPromise = serviceBroker
            .loadViewData(CORE_API.PhysicalSpecDefinitionFieldStore.findForSpecDefinitionId, [def.id], { force })
            .then(r => r.data);


        const specDefSampleFilePromise = serviceBroker
            .loadViewData(CORE_API.PhysicalSpecDefinitionSampleFileStore.findForSpecDefinitionId, [def.id], { force })
            .then(r => r.data);

        $q.all([specDefFieldPromise, specDefSampleFilePromise])
            .then(([fields, file]) => vm.selectedSpecDefinition = { def, fields, sampleFile: file });
    };

    vm.showCreateSpecDefinition = () => {
        vm.specDefinitionCreate.creating = true;
    };

    vm.hideCreateSpecDefinition = () => {
        vm.specDefinitionCreate.creating = false;
    };

    vm.createSpecDefinition = (specDef) => {
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionStore.create, [vm.parentEntityRef.id, specDef.def])
            .then(r => r.data)
            .then(specDefId => {
                const fieldsPromise = serviceBroker
                    .execute(CORE_API.PhysicalSpecDefinitionFieldStore.createFields, [specDefId, specDef.fields])
                    .then(r => r.data);

                if (specDef.sampleData) {
                    const sampleDataPromise = serviceBroker
                        .execute(CORE_API.PhysicalSpecDefinitionSampleFileStore.create, [specDefId, {
                            name: vm.specification.name,
                            fileData: specDef.sampleData
                        }])
                        .then(r => r.data);

                    return $q.all([fieldsPromise, sampleDataPromise]);
                } else {
                    return fieldsPromise;
                }
            })
            .then(r => {
                notification.success('Specification definition created successfully');
                loadSpecDefinitions(true);
                vm.hideCreateSpecDefinition();
            }, r => {
                notification.error("Failed to create specification definition. Ensure that 'version' is unique");
            });
    };

    vm.deleteSpec = (specDef) => {
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionStore.deleteSpecification, [specDef.id])
            .then(r => r.data)
            .then(result => {
                if (result) {
                    notification.success(`Deleted version ${specDef.version}`);
                    loadSpecDefinitions(true);
                } else {
                    notification.error(`Could not delete version ${specDef.version}`);
                }
            });
    };

    vm.activateSpec = (specDef) => {
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionStore.updateStatus, [specDef.id, mkReleaseLifecycleStatusChangeCommand('ACTIVE')])
            .then(r => r.data)
            .then(result => {
                if (result) {
                    notification.success(`Marked version ${specDef.version} as active`);
                    loadSpecDefinitions(true);
                } else {
                    notification.error(`Could not mark version ${specDef.version} as active`);
                }
            });
    };

    vm.markSpecObsolete = (specDef) => {
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionStore.updateStatus, [specDef.id, mkReleaseLifecycleStatusChangeCommand('OBSOLETE')])
            .then(r => r.data)
            .then(result => {
                if (result) {
                    notification.success(`Marked version ${specDef.version} as obsolete`);
                    loadSpecDefinitions(true);
                } else {
                    notification.error(`Could not mark version ${specDef.version} as obsolete`);
                }
            });
    };

    vm.updateFieldDescription = (fieldId, change) => {
        const cmd = { newDescription: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateDescription, [fieldId, cmd])
            .then(result => {
                if (result) {
                    notification.success(`Updated description for field`);
                    vm.selectSpecDefinition(vm.selectedSpecDefinition.def, true);
                } else {
                    notification.error(`Could not update field description`);
                }
            });
    };

    vm.updateLogicalDataElement = (fieldId, change) => {
        const cmd = { newLogicalDataElement: change.newVal };
        serviceBroker
            .execute(CORE_API.PhysicalSpecDefinitionFieldStore.updateLogicalElement, [fieldId, cmd])
            .then(result => {
                if (result) {
                    notification.success(`Updated logical data element for field`);

                    const selectionOptions = {
                        scope: 'EXACT',
                        entityReference: { kind: 'PHYSICAL_SPECIFICATION', id: vm.parentEntityRef.id }
                    };

                    serviceBroker
                        .loadViewData(CORE_API.LogicalDataElementStore.findBySelector, [ selectionOptions ], { force: true })
                        .then(r => vm.logicalDataElements = r.data);

                    vm.selectSpecDefinition(vm.selectedSpecDefinition.def, true);
                } else {
                    notification.error(`Could not update logical data element`);
                }
            });
    };
}


controller.$inject = [
    '$q',
    'Notification',
    'ServiceBroker'
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: 'waltzPhysicalSpecDefinitionSection'
};
