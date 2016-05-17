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

import "angular-ui-router";
import ListView from "./list-view";
import UnitView from "./unit-view";
import UnitNavView from "./unit-nav-view";
import {orgUnitsResolver, appTalliesResolver, endUserAppTalliesResolver} from "./resolvers.js";


const baseState = {
    resolve: {
        appTallies: appTalliesResolver,
        endUserAppTallies: endUserAppTalliesResolver,
        orgUnits: orgUnitsResolver
    }
};

const listState = {
    url: 'org-units',
    views: {'content@': ListView}
};


const viewState = {
    url: 'org-units/{id:int}',
    views: {
        'content@': UnitView,
        'sidenav@main': UnitNavView
    }
};


export default (module) => {

    require('./directives')(module);
    require('./templates')(module);

    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.org-units', baseState)
                .state('main.org-units.list', listState)
                .state('main.org-units.unit', viewState);
        }
    ]);

    module.service('OrgUnitStore', require('./services/org-unit-store'));
    module.service('OrgUnitUtilityService', require('./services/org-unit-utility'));
    module.service('OrgUnitViewDataService', require('./services/org-unit-view-data'));
    module.service('OrgUnitViewOptionsService', require('./services/org-unit-view-options'));
};
