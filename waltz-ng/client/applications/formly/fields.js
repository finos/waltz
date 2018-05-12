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
        required: true
    }
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
    }
};