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

function controller(viewOptions) {
    const vm = this;

    vm.options = viewOptions.options;

    vm.toggleSubUnits = () => {
        vm.options.includeSubUnits = !vm.options.includeSubUnits;
    };

    vm.toggleProductionOnly = () => {
        vm.options.productionOnly = !vm.options.productionOnly;
    };

    vm.togglePrimaryOnly = () => {
        vm.options.primaryOnly = !vm.options.primaryOnly;
    };


}

controller.$inject = ['OrgUnitViewOptionsService'];


export default {
    template: require('./unit-nav-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true
};
