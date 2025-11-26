/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */
import {initialiseData} from "../../../common/index";

import template from "./data-type-usage-panel.html";
import {loadUsageData} from "../../data-type-utils";
import toasts from "../../../svelte-stores/toast-store";
import _ from "lodash";
import {CORE_API} from "../../../common/services/core-api-utils";
import {mkRef, toEntityRef, toEntityRefWithKind} from "../../../common/entity-utils";
import {entity} from "../../../common/services/enums/entity";
import EditReasonSelection from "./EditReasonSelection.svelte"
import {editDataType} from "../../../data-flow/components/svelte/propose-data-flow/propose-data-flow-store"
import {getDataFlowProposalsRatingScheme, isDataFlowProposalsEnabled} from "../../../common/utils/settings-util";

const bindings = {
    parentEntityRef: "<",
    parentFlow: "<?",
    helpText: "@"
};


const initialState = {
    helpText: null,
    isDirty: false,
    parentFlow: null,
    visibility: {
        editor: false,
        controls: false
    },
    settings:null,
    EditReasonSelection,
    ratingsScheme:null,
    ratingSchemeExtId:null,
    dataFlowProposalsEnabled:null,
    selectedReason:null
};


function controller(serviceBroker, userService, $q) {
    const vm = initialiseData(this, initialState);


    const reload = (force = false) => {
        loadUsageData($q, serviceBroker, vm.parentEntityRef, force)
            .then(usageData => vm.used = usageData);
    };

    vm.$onInit = () => {
        let dataFlowProposalsRatingSchemeSetting="";
        const settingsPromise = serviceBroker
            .loadViewData(CORE_API.SettingsStore.findAll, [])
            .then(r => {
                vm.settings = r.data;
                vm.dataFlowProposalsEnabled= isDataFlowProposalsEnabled(vm.settings)

                vm.ratingSchemeExtId = getDataFlowProposalsRatingScheme(vm.settings)

            });


        const decoratedRef = vm.parentEntityRef
            ? vm.parentEntityRef
            : mkRef(entity.PHYSICAL_SPECIFICATION.key, vm.parentFlow.specificationId);

        serviceBroker
            .loadViewData(
                CORE_API.DataTypeDecoratorStore.findPermissions,
                [decoratedRef])
            .then(r => {
                vm.visibility.controls = _.some(r.data, d => _.includes(["ADD", "UPDATE", "REMOVE"], d));
            });
    };

    vm.$onChanges = () => {
        if (! vm.parentEntityRef) return;
        reload(true);
    };

    vm.onShowEdit = () => {
        vm.visibility.editor = true;
    };

    vm.onHideEdit = () => {
        vm.visibility.editor = false;
    };

    editDataType.subscribe(value => {
        vm.selectedReason = value;
    });

    vm.onSave = () => {
        if(!vm.isDirty)
            return;
        if(vm.parentEntityRef.kind === "PHYSICAL_SPECIFICATION" && !confirm("This will affect all associated physical flows. Do you want to continue?")){
            return;
        }
        if (vm.save) {
            vm.save()
                .then(() => {
                    toasts.success("Data types updated successfully");
                    reload(true);
                    vm.onHideEdit();
                });
        } else {
            console.log("onSave - no impl");
        }
    };

    vm.onSavePropose = ()=>{
        Promise.all([
            serviceBroker.loadViewData(
                CORE_API.PhysicalSpecificationStore.getById,
                [vm.parentFlow.specificationId]
            ),
            serviceBroker.loadViewData(
                CORE_API.LogicalFlowStore.getById,
                [vm.parentFlow.logicalFlowId]
            )
        ]).then(([specResponse, logicalFlowResponse]) => {
            const specificationData = specResponse.data;
            const logicalFlowData = logicalFlowResponse.data;

            const specification = {
                owningEntity: {id:vm.parentFlow.id,kind:vm.parentFlow.kind},
                name: specificationData.name,
                description: specificationData.description,
                format: specificationData.format,
                lastUpdatedBy: "waltz",
                externalId: !_.isEmpty(specificationData.externalId) ? specificationData.externalId : null,
                id: specificationData.id || null
            };

            const logicalFlow = {
                logicalFlowId: logicalFlowData.id || null,
                source: logicalFlowData.source || null,
                target: logicalFlowData.target || null
            };

            const flowAttributes = {
                name: vm.parentFlow.name,
                transport: vm.parentFlow.transport,
                frequency: vm.parentFlow.frequency,
                basisOffset: vm.parentFlow.basisOffset,
                criticality: vm.parentFlow.criticality,
                description: vm.parentFlow.description,
                externalId: !_.isEmpty(vm.parentFlow.externalId) ? vm.parentFlow.externalId : null
            };

            const command = {
                specification,
                flowAttributes,
                logicalFlowId: logicalFlow.logicalFlowId,
                source: logicalFlow.source,
                target: logicalFlow.target,
                physicalFlowId: vm.parentFlow.id,
                dataTypeIds: [],
                proposalType: "EDIT",
                reason:{
                    ratingId:vm.selectedReason.rating[0].id,
                    description:vm.selectedReason.rating[0].description
                }
            };

            if (!vm.isDirty) return;

            if (vm.savePropose) {
                vm.savePropose(command)
                    .then(()=> {
                        toasts.success("Data types updated successfully");
                        reload(true);
                        vm.onHideEdit();}
                    )
                    .catch(error => {
                        console.error("Error in savePropose:", error);
                    });

            }
        });
    }
    vm.onDirty = (dirtyFlag) => {
        vm.isDirty = dirtyFlag;
    };

    vm.registerSaveFn = (saveFn) => {
        vm.save = saveFn;
    };
    vm.registerSaveProposeFn = (saveFn) => {
        vm.savePropose = saveFn;
    };
}

controller.$inject = [
    "ServiceBroker",
    "UserService",
    "$q"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    id: "waltzDataTypeUsagePanel",
    component
}