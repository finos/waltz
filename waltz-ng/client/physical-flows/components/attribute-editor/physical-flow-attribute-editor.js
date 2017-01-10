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

import _ from "lodash";
import {initialiseData, invokeFunction} from "../../../common";
import {
    transportField,
    frequencyField,
    basisOffsetSelectField,
    basisOffsetInputField
} from "../../formly/physical-flow-fields";


const bindings = {
    current: '<',
    onDismiss: '<',
    onChange: '<'
};


const template = require('./physical-flow-attribute-editor.html');


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


function controller() {
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
        }
    ];

    vm.fields = fields;

    const basisOffsetOptions = _.map(basisOffsetSelectField.templateOptions.options, 'code');

    vm.$onChanges = (changes) => {
        if(vm.current) {
            const isOtherBasisOffset = !_.includes(basisOffsetOptions, vm.current.basisOffset);

            vm.model = {
                transport: vm.current.transport,
                frequency: vm.current.frequency,
                basisOffsetSelect: isOtherBasisOffset ? 'OTHER' : vm.current.basisOffset,
                basisOffsetInput: isOtherBasisOffset ? vm.current.basisOffset : undefined
            };

        }
    };

    vm.onSubmit = () => {
        // get the submitted fields
        const { frequency, transport } = vm.model;
        const basisOffset = getBasisOffset(vm.model.basisOffsetSelect, vm.model.basisOffsetInput);
        invokeFunction(vm.onChange, { frequency, transport, basisOffset });
    };


    vm.onCancel = () => {
        invokeFunction(vm.onDismiss);
    };

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;