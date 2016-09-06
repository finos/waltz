/*
 *  Waltz
 * Copyright (c) David Watkins. All rights reserved.
 * The use and distribution terms for this software are covered by the
 * Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
 * which can be found in the file epl-v10.html at the root of this distribution.
 * By using this software in any fashion, you are agreeing to be bound by
 * the terms of this license.
 * You must not remove this notice, or any other, from this software.
 *
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
    return flowStore.findByAppIdSelector(mkSelector(params));
}

flowResolver.$inject = ['DataFlowDataStore', '$stateParams'];


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


export function flowDecoratorsResolver(dataFlowDecoratorStore, params) {
    return dataFlowDecoratorStore
        .findBySelectorAndKind(mkSelector(params), 'DATA_TYPE');
}

flowDecoratorsResolver.$inject = ['DataFlowDecoratorStore', '$stateParams'];