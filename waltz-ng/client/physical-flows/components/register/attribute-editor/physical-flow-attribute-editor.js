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

import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../../common";
import {
    transportField,
    frequencyField,
    basisOffsetSelectField,
    basisOffsetInputField,
    criticalityField
} from "../../../formly/physical-flow-fields";
import {CORE_API} from "../../../../common/services/core-api-utils";
import template from "./physical-flow-attribute-editor.html";
import {toOptions} from "../../../../common/services/enums";


const bindings = {
    current: "<",
    onDismiss: "<",
    onChange: "<"
};




const initialState = {
    onDismiss: () => console.log("No dismiss handler for physical flow attribute editor"),
    onChange: (atts) => console.log("No dismiss handler for physical flow attribute editor: ", atts)
};


function getBasisOffset(basisOffsetSelect, basisOffsetInput) {
    if (basisOffsetSelect && basisOffsetSelect !== "OTHER") {
        return Number(basisOffsetSelect);
    } else if (basisOffsetInput) {
        const basisOffsetRegex = /^(?:T|t)?((?:\+?|-)\d+)$/g;
        const match = basisOffsetRegex.exec(basisOffsetInput);
        if (match !== null && match[1]) {
            return Number(match[1]);
        } else {
            throw "Could not parse basis offset: " + basisOffsetInput;
        }
    } else {
        throw "No valid Basis Offset value supplied"
    }
}


function controller(serviceBroker) {
    const vm = initialiseData(this, initialState);

    const fields = [
        {
            className: "row",
            fieldGroup: [
                {className: "col-sm-6", fieldGroup: [transportField]},
                {className: "col-sm-6", fieldGroup: [frequencyField]}
            ]
        }, {
            className: "row",
            fieldGroup: [
                {className: "col-sm-6", fieldGroup: [basisOffsetSelectField]},
                {className: "col-sm-6", fieldGroup: [basisOffsetInputField]}
            ]
        }, {
            className: "row",
            fieldGroup: [
                {className: "col-sm-6", fieldGroup: [criticalityField]}
            ]
        }
    ];

    vm.fields = fields;

    const basisOffsetOptions = _.map(basisOffsetSelectField.templateOptions.options, "code");

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                const enumsByType = _.groupBy(r.data, "type");

                transportField.templateOptions.options = toOptions(enumsByType["TransportKind"]);
                frequencyField.templateOptions.options = toOptions(enumsByType["Frequency"]);

                criticalityField.templateOptions.options = _.map(
                    enumsByType["physicalFlowCriticality"],
                    c => ({code: c.key, name: c.name}));
            });
    };

    vm.$onChanges = (c) => {
        if(vm.current) {
            const isOtherBasisOffset = !_.includes(
                basisOffsetOptions,
                vm.current.basisOffset);

            vm.model = {
                transport: vm.current.transport,
                frequency: vm.current.frequency,
                criticality: vm.current.criticality,
                basisOffsetSelect: isOtherBasisOffset ? "OTHER" : vm.current.basisOffset,
                basisOffsetInput: isOtherBasisOffset ? vm.current.basisOffset : undefined
            };

        }
    };

    vm.onSubmit = () => {
        // get the submitted fields
        const { frequency, transport, criticality } = vm.model;
        const basisOffset = getBasisOffset(vm.model.basisOffsetSelect, vm.model.basisOffsetInput);
        invokeFunction(vm.onChange, { frequency, transport, basisOffset, criticality });
    };


    vm.onCancel = () => {
        invokeFunction(vm.onDismiss);
    };

}


controller.$inject = [
    "ServiceBroker"
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: "waltzPhysicalFlowAttributeEditor"
};