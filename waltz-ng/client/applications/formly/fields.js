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

import {applicationKind} from "../../common/services/enums/application-kind";
import {criticality} from "../../common/services/enums/criticality";
import {lifecyclePhase} from "../../common/services/enums/lifecycle-phase";
import {toOptions} from "../../common/services/enums";


export const nameField = {
    type: 'input',
    key: 'name',
    templateOptions: {
        label: 'Name',
        placeholder: 'Name of application or service',
        required: true
    }
};


export const assetCodeField = {
    type: 'input',
    key: 'assetCode',
    templateOptions: {
        label: 'Asset Code',
        placeholder: 'Asset code associated with application'
    }
};


export const parentAssetCodeField = {
    type: 'input',
    key: 'parentAssetCode',
    templateOptions: {
        label: 'Parent Asset Code',
        placeholder: 'Optional parent code'
    }
};


export const descriptionField = {
    type: 'textarea',
    key: 'description',
    templateOptions: {
        label: 'Description',
        rows: 13,
        placeholder: 'Name of application or service'
    }
};


export const orgUnitField = {
    key: 'organisationalUnitId',
    type: 'org-unit-input',
    formControl: { $dirty: false },
    templateOptions: {
        label: 'Owning Organisational Unit',
        onSelect: (item) => {
            orgUnitField.model[orgUnitField.key] = item.id;
            orgUnitField.formControl =  { $dirty: true };
        }
    }
};


export const typeField = {
    type: 'select',
    key: 'applicationKind',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(applicationKind),
        label: 'Type',
        placeholder: 'Type of application',
        required: true
    }
};


export const lifecyclePhaseField = {
    type: 'select',
    key: 'lifecyclePhase',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(lifecyclePhase),
        label: 'Current Lifecycle Phase',
        placeholder: '',
        required: true
    }
};


export const overallRatingField = {
    type: 'select',
    key: 'overallRating',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [
            { code: 'A', name: 'Invest'},
            { code: 'G', name: 'Hold' },
            { code: 'R', name: 'Disinvest' },
            { code: 'Z', name: 'Unknown' }
        ],
        label: 'Overall Rating',
        placeholder: '',
        required: true,
    },
    className: "ux-app-reg-overall-rating"
};


export const businessCriticalityField = {
    type: 'select',
    key: 'businessCriticality',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(criticality),
        label: 'Business Criticality',
        placeholder: '',
        required: true
    },
    className: "ux-app-reg-business-criticality"
};