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

export function authSourcesResolver(authSourcesStore, params) {
    const { kind, id } = params;
    return authSourcesStore.findByReference(kind, id);
}

authSourcesResolver.$inject = ['AuthSourcesStore', '$stateParams'];


export function flowResolver(appStore, flowStore, params) {
    const { id } = params;

    return appStore.findByOrgUnit(id)
        .then(apps => _.map(apps, 'id'))
        .then(appIds => flowStore.findByAppIds(appIds));
}

flowResolver.$inject = ['ApplicationStore', 'DataFlowDataStore', '$stateParams'];


export function idResolver(params) {
    return params.id;
}

idResolver.$inject = ['$stateParams'];


export function orgUnitsResolver(orgUnitStore) {
    return orgUnitStore.findAll();
}

orgUnitsResolver.$inject = ['OrgUnitStore'];

