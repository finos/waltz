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

import {initialiseData, invokeFunction} from "../../../common";
import _ from "lodash";


const bindings = {
    status: '<',
    onCancel: '<',
    onSubmit: '<'
};


const initialState = {
    allowedTypesMap: {
        DATE: true,
        DECIMAL: true,
        INTEGER: true,
        STRING: true,
        BOOLEAN: true
    },
    status: null,
    specDefinition: {
        def: {
            delimiter: ',',
            type: 'DELIMITED'
        }
    },
    visibility: 'FORM',
    specDefFields: {},
    onCancel: () => console.log('psdcp::onCancel'),
    onSubmit: (specDef) => console.log('psdcp::onSubmit', specDef)
};


const template = require('./physical-spec-definition-create-panel.html');


function parseField(fieldSplit = [], position, allowedTypesMap = {}) {
    const fieldData = {
        field: null,
        errors: []
    };
    if (fieldSplit.length !== 3) {
        fieldData.errors.push('Make sure all three columns: Name, Type and Description are populated');
    } else {
        fieldData.field = {
            name: _.trim(fieldSplit[0]),
            type: _.trim(fieldSplit[1]),
            description: _.trim(fieldSplit[2]),
            position
        };

        if (_.isEmpty(fieldData.field.name)) {
            fieldData.errors.push('Name must be defined');
        }

        if (_.isEmpty(fieldData.field.type)) {
            fieldData.errors.push('Type must be defined');
        }

        if (_.isEmpty(fieldData.field.description)) {
            fieldData.errors.push('Description must be defined');
        }

        if (! allowedTypesMap[fieldData.field.type]) {
            fieldData.errors.push('Type is invalid');
        }
    }

    return fieldData;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.cancel = () => invokeFunction(vm.onCancel);

    vm.preview = () => {
        vm.specDefFields.hasErrors = false;
        vm.specDefFields.parsedData = [];

        const lines = _.split(vm.specDefFields.rawData, '\n');

        vm.specDefFields.parsedData = _.map(lines, (line, index) => {
            const fieldSplit = _.split(line, '\t');
            const fieldData = parseField(fieldSplit, index + 1, vm.allowedTypesMap);
            if (fieldData.errors.length > 0) {
                vm.specDefFields.hasErrors = true;
            }
            return fieldData;
        });
        vm.visibility = 'PREVIEW';
        vm.specDefinition.def.status = vm.status;
    };

    vm.showForm = () => {
        vm.visibility = 'FORM';
    };

    vm.submit = () => {
        vm.specDefinition.fields = _.map(vm.specDefFields.parsedData, 'field');
        invokeFunction(vm.onSubmit, vm.specDefinition);
    };
}


const component = {
    controller,
    template,
    bindings
};


export default component;