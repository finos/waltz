
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

import { baseState, listState, viewState } from './capability-states';


export default (module) => {
    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.capabilities', baseState)
                .state('main.capabilities.list', listState)
                .state('main.capabilities.view', viewState);
        }
    ]);

    module.directive('waltzCapabilitySelector', require('./directives/capability-selector'));
    module.directive('waltzCapabilityFlowGraph', require('./directives/capability-flow-graph'));
    module.directive('waltzCapabilitySummary', require('./directives/capability-summary'));

    module.service('CapabilityStore', require('./services/capability-store'));
};
