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
import _ from "lodash";


export function orgUnitResolver(orgUnits, params) {
    return _.find(orgUnits, { id: params.id });
}

orgUnitResolver.$inject = ['orgUnits', '$stateParams'];


export function appTalliesResolver(appStore) {
    return appStore.countByOrganisationalUnit();
}

appTalliesResolver.$inject = ['ApplicationStore'];


export function endUserAppTalliesResolver(endUserAppStore) {
    return endUserAppStore.countByOrganisationalUnit();
}

endUserAppTalliesResolver.$inject = ['EndUserAppStore'];


export function orgUnitsResolver(orgUnitStore) {
    return orgUnitStore.findAll();
}

orgUnitsResolver.$inject = ['OrgUnitStore'];


export function orgUnitViewDataResolver(viewSvc, params) {
    return viewSvc.loadAll(params.id);
}

orgUnitViewDataResolver.$inject = ['OrgUnitViewDataService', '$stateParams'];
