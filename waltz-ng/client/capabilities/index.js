
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


export default (module) => {
    module.config(require('./routes'));

    module.directive('waltzCapabilitySelector', require('./directives/capability-selector'));
    module.directive('waltzCapabilitySelectorAdvanced', require('./directives/capability-selector-advanced'));
    module.directive('waltzCapabilitySelectorModal', require('./directives/capability-selector-modal'));
    module.directive('waltzCapabilityPicker', require('./directives/capability-picker'));
    module.directive('waltzCapabilityFlowGraph', require('./directives/capability-flow-graph'));
    module.directive('waltzCapabilityScorecard', require('./directives/capability-scorecard'));
    module.directive('waltzCapabilitySummary', require('./directives/capability-summary'));
    module.directive('waltzCapabilityTree', require('./directives/capability-tree'));

    module.service('CapabilityStore', require('./services/capability-store'));

    module.component('waltzCapabilityRatingsSection', require('./components/capability-ratings-section'));
};
