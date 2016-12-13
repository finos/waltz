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

import {applicationKindDisplayNames, lifecyclePhaseDisplayNames, toOptions} from "../../common/services/display_names";

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
    type: 'ui-select',
    templateOptions: {
        optionsAttr: 'bs-options',
        ngOptions: 'option[to.valueProp] as option in to.options | filter: $select.search',
        label: 'Owning Organisational Unit',
        valueProp: 'code',
        labelProp: 'name',
        placeholder: 'Owning Area',
        //  description: 'Template includes the allow-clear option on the ui-select-match element',
        options:[]
    }
};


export const typeField = {
    type: 'select',
    key: 'kind',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: toOptions(applicationKindDisplayNames),
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
        options: toOptions(lifecyclePhaseDisplayNames),
        label: 'Current Lifecycle Phase',
        placeholder: '',
        required: true
    }
};


export const aliasesField = {
    type: 'tags-input',
    key: 'aliases',
    templateOptions: {
        label: 'Aliases',
        placeholder: 'Add an alias',
        required: false
    }
};


export const tagsField = {
    type: 'tags-input',
    key: 'tags',
    templateOptions: {
        label: 'Additional Tags',
        required: false
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
}


export const businessCriticalityField = {
    type: 'select',
    key: 'businessCriticality',
    templateOptions: {
        valueProp: 'code',
        labelProp: 'name',
        options: [
            { code: 'LOW', name: 'Low'},
            { code: 'MEDIUM', name: 'Medium' },
            { code: 'HIGH', name: 'High' },
            { code: 'VERY_HIGH', name: 'Very high' },
            { code: 'NONE', name: 'None' },
            { code: 'UNKNOWN', name: 'Unknown' }
        ],
        label: 'Business Criticality',
        placeholder: '',
        required: true
    }
}