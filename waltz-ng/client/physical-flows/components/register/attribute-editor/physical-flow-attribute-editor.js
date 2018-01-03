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
import template from './physical-flow-attribute-editor.html';


const bindings = {
    current: '<',
    onDismiss: '<',
    onChange: '<'
};




const initialState = {
    onDismiss: () => console.log("No dismiss handler for physical flow attribute editor"),
    onChange: (atts) => console.log("No dismiss handler for physical flow attribute editor: ", atts)
};


function getBasisOffset(basisOffsetSelect, basisOffsetInput) {
    if(basisOffsetSelect && basisOffsetSelect !== 'OTHER') {
        return Number(basisOffsetSelect);
    } else if(basisOffsetInput) {
        const basisOffsetRegex = /^(?:T|t)?((?:\+?|-)\d+)$/g;
        const match = basisOffsetRegex.exec(basisOffsetInput);
        if(match !== null && match[1]) {
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
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [transportField] },
                { className: 'col-sm-6', fieldGroup: [frequencyField] }
            ]
        }, {
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [basisOffsetSelectField] },
                { className: 'col-sm-6', fieldGroup: [basisOffsetInputField] }
            ]
        }, {
            className: 'row',
            fieldGroup: [
                { className: 'col-sm-6', fieldGroup: [criticalityField] }
            ]
        }
    ];

    vm.fields = fields;

    const basisOffsetOptions = _.map(basisOffsetSelectField.templateOptions.options, 'code');

    vm.$onInit = () => {
        serviceBroker
            .loadAppData(CORE_API.EnumValueStore.findAll)
            .then(r => {
                const criticalityValuesByCode = _
                    .chain(r.data)
                    .filter({ type: 'physicalFlowCriticality'})
                    .map(c => ({ code: c.key, name: c.name }))
                    .keyBy('code')
                    .value();
                criticalityField.templateOptions.options = [
                    criticalityValuesByCode['VERY_HIGH'],
                    criticalityValuesByCode['HIGH'],
                    criticalityValuesByCode['MEDIUM'],
                    criticalityValuesByCode['LOW']
                ];
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
                basisOffsetSelect: isOtherBasisOffset ? 'OTHER' : vm.current.basisOffset,
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
    'ServiceBroker'
];


const component = {
    bindings,
    template,
    controller
};


export default {
    component,
    id: 'waltzPhysicalFlowAttributeEditor'
};