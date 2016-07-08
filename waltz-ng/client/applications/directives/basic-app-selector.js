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


const BINDINGS = {
    addLabel: '@',
    cancelLabel: '@',
    onCancel: '<',
    onAdd: '<'
};


const initialState = {
    addLabel: 'Add',
    cancelLabel: 'Cancel',
    onCancel: () => console.log('No onCancel provided to basic app selector'),
    onAdd: (a) => console.log('No onAdd provided to basic app selector', a)
};


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.add = (app) => {
        if (! app) return ;
        vm.onAdd(app);
    };

    vm.cancel = () => vm.onCancel();
}


controller.$inject = [];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./basic-app-selector.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;


