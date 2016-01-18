
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

function controller(viewData) {
    const vm = this;
    vm.viewData = viewData;

    vm.filter = {
        includeSubUnits: true,
        productionOnly: false
    };

    vm.toggleSubUnits = () => {
        vm.filter.includeSubUnits = !vm.filter.includeSubUnits;
        viewData.filter(vm.filter);
    };

    vm.toggleProductionOnly = () => {
        vm.filter.productionOnly = !vm.filter.productionOnly;
        viewData.filter(vm.filter);
    };

    vm.togglePrimaryOnly = () => {
        vm.filter.primaryOnly = !vm.filter.primaryOnly;
        viewData.filter(vm.filter);
    };
}

controller.$inject = ['viewData'];

export default {
    template: require('./unit-nav-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true
};
