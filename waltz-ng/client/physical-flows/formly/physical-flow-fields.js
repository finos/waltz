/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import {toOptions, frequencyKindNames, transportKindNames} from "../../common/services/display_names";


export const transportField = {
    type: 'select',
    key: 'transport',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(transportKindNames, true),
        label: 'Transport',
        placeholder: '',
        required: true
    }
};


export const frequencyField = {
    type: 'select',
    key: 'frequency',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(frequencyKindNames, true),
        label: 'Frequency',
        placeholder: '',
        required: true
    }
};


export const basisOffsetSelectField = {
    type: 'select',
    key: 'basisOffsetSelect',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [
            { code: '-30',  name: 'T-30'},
            { code: '-10',  name: 'T-10'},
            { code: '-7',   name: 'T-7'},
            { code: '-5',   name: 'T-5'},
            { code: '-3',   name: 'T-3'},
            { code: '-1',   name: 'T-1'},
            { code: '0',    name: 'T' },
            { code: '1',    name: 'T+1' },
            { code: '3',    name: 'T+3' },
            { code: '5',    name: 'T+5' },
            { code: '7',    name: 'T+7' },
            { code: '10',   name: 'T+10' },
            { code: '30',   name: 'T+30' },
            { code: 'OTHER', name: 'Other (Please specify)' },
        ],
        label: 'Basis Offset',
        placeholder: '',
        required: true
    }
};


export const basisOffsetInputField = {
    type: 'input',
    key: 'basisOffsetInput',
    templateOptions: {
        label: 'Custom Offset',
        placeholder: 'please specify basis offset in (+/-)n format',
        required: true
    },
    validators: {
        basisOffset: {
            expression: function(viewValue, modelValue) {
                var value = modelValue || viewValue;
                return /^(?:T|t)?((?:\+?|-)\d+)$/.test(value);
            },
            message: '$viewValue + " is not a valid basis offset"'
        }
    },
    hideExpression: 'model.basisOffsetSelect !== "OTHER"'
};