
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
import angular from 'angular';


export default () => {
    const module = angular.module('waltz.capabilities', []);

    module
        .config(require('./routes'));

    module
        .service('CapabilityStore', require('./services/capability-store'));

    module
        .component('waltzCapabilityRatingsSection', require('./components/capability-ratings-section'))
        .component('waltzCapabilityTree', require('./components/capability-tree'));

    module
        .directive('waltzCapabilitySelector', require('./directives/capability-selector'))
        .directive('waltzCapabilityFlowGraph', require('./directives/capability-flow-graph'))
        .directive('waltzCapabilityScorecard', require('./directives/capability-scorecard'))
        .directive('waltzCapabilitySummary', require('./directives/capability-summary'));

    return module.name;
};
