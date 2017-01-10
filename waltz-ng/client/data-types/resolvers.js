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

export function loadDataTypes(dataTypeService) {
    return dataTypeService.loadDataTypes();
}

loadDataTypes.$inject = ['DataTypeService'];


export function dataTypeByIdResolver($stateParams, dataTypeService) {
    if(!$stateParams.id)
        throw "'id' not found in stateParams";

    return dataTypeService
        .loadDataTypes()
        .then(dataTypes => {
            const dataType = _.find(dataTypes, { id: $stateParams.id});
            if (dataType) {
                return dataType;
            } else {
                throw `data type with id: ${$stateParams.id} not found`;
            }
        });
}

dataTypeByIdResolver.$inject = ['$stateParams', 'DataTypeService'];


export function dataTypeByCodeResolver($stateParams, dataTypeService) {
    if(!$stateParams.code)
        throw "'code' not found in stateParams";

    return dataTypeService
        .loadDataTypes()
        .then(dataTypes => {
            const dataType = _.find(dataTypes, { code: $stateParams.code});
            if (dataType) {
                return dataType;
            } else {
                throw `data type with code: ${$stateParams.code} not found`;
            }
        });
}

dataTypeByCodeResolver.$inject = ['$stateParams', 'DataTypeService'];