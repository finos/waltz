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

import {frequencyKind} from "../../common/services/enums/frequency-kind";
import {toOptions} from "../../common/services/enums";


export const transportField = {
    type: "select",
    key: "transport",
    templateOptions: {
        valueProp: "code",
        labelProp: "name",
        options: [],
        label: "Transport",
        placeholder: "",
        required: true
    }
};


export const frequencyField = {
    type: "select",
    key: "frequency",
    templateOptions: {
        valueProp: "code",
        labelProp: "name",
        options: [],
        label: "Frequency",
        placeholder: "",
        required: true
    }
};


export const basisOffsetSelectField = {
    type: "select",
    key: "basisOffsetSelect",
    templateOptions: {
        valueProp: "code",
        labelProp: "name",
        options: [
            {code: "-30", name: "T-30"},
            {code: "-10", name: "T-10"},
            {code: "-7", name: "T-7"},
            {code: "-5", name: "T-5"},
            {code: "-3", name: "T-3"},
            {code: "-1", name: "T-1"},
            {code: "0", name: "T"},
            {code: "1", name: "T+1"},
            {code: "3", name: "T+3"},
            {code: "5", name: "T+5"},
            {code: "7", name: "T+7"},
            {code: "10", name: "T+10"},
            {code: "30", name: "T+30"},
            {code: "OTHER", name: "Other (Please specify)"},
        ],
        label: "Basis Offset",
        placeholder: "",
        required: true
    }
};


export const basisOffsetInputField = {
    type: "input",
    key: "basisOffsetInput",
    templateOptions: {
        label: "Custom Offset",
        placeholder: "please specify basis offset in (+/-)n format",
        required: true
    },
    validators: {
        basisOffset: {
            expression: function (viewValue, modelValue) {
                var value = modelValue || viewValue;
                return /^(?:T|t)?((?:\+?|-)\d+)$/.test(value);
            },
            message: "$viewValue + \" is not a valid basis offset\""
        }
    },
    hideExpression: "model.basisOffsetSelect !== \"OTHER\""
};


export const criticalityField = {
    type: "select",
    key: "criticality",
    templateOptions: {
        valueProp: "code",
        labelProp: "name",
        options: [],
        label: "Criticality",
        placeholder: "",
        required: true
    }
};
