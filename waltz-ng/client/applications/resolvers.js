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

// -- APP --
export function appResolver(appStore, $stateParams) {
    return appStore.getById($stateParams.id);
}

appResolver.$inject = ['ApplicationStore', '$stateParams'];


export function appByAssetCodeResolver(appStore, $stateParams) {
    return appStore.findByAssetCode($stateParams.assetCode);
}

appByAssetCodeResolver.$inject = ['ApplicationStore', '$stateParams'];


// -- ALIASES --
export function aliasesResolver(aliasStore, $stateParams) {

    const ref = {
        id: $stateParams.id,
        kind: 'APPLICATION'
    };

    return aliasStore.getForEntity(ref);
}

aliasesResolver.$inject = ['AliasStore', '$stateParams'];


// -- TAGS --
export function tagsResolver(appStore, $stateParams) {
    return appStore.getAppTagsById($stateParams.id);
}

tagsResolver.$inject = ['ApplicationStore', '$stateParams'];


// -- OUs --
export function orgUnitsResolver(orgUnitStore) {
    return orgUnitStore.findAll();
}

orgUnitsResolver.$inject = [ 'OrgUnitStore' ];


