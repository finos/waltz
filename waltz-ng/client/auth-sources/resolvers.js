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


function mkSelector(params) {
    const { id } = params;

    const options = {
        entityReference: {id, kind: "ORG_UNIT"},
        scope: "CHILDREN"
    };

    return options;
}


export function authSourcesResolver(authSourcesStore, params) {
    const { kind, id } = params;
    return authSourcesStore.findByReference(kind, id);
}

authSourcesResolver.$inject = ['AuthSourcesStore', '$stateParams'];


export function flowResolver(flowStore, params) {
    return flowStore.findBySelector(mkSelector(params));
}

flowResolver.$inject = ['LogicalFlowStore', '$stateParams'];


export function idResolver(params) {
    return params.id;
}

idResolver.$inject = ['$stateParams'];


export function orgUnitsResolver(orgUnitStore) {
    return orgUnitStore.findAll();
}

orgUnitsResolver.$inject = ['OrgUnitStore'];


export function dataTypesResolver(dataTypeService) {
    return dataTypeService.loadDataTypes();
}

dataTypesResolver.$inject = ['DataTypeService'];


export function flowDecoratorsResolver(logicalFlowDecoratorStore, $stateParams) {
    return logicalFlowDecoratorStore
        .findBySelectorAndKind(mkSelector($stateParams), 'DATA_TYPE');
}

flowDecoratorsResolver.$inject = ['LogicalFlowDecoratorStore', '$stateParams'];