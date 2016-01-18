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


function controller(appStore) {

    const vm = this;
    vm.status = 'CLOSED';

    vm.loadRelated = () => {
        vm.status = 'LOADING';
        appStore.findRelatedById(vm.application.id).then(related => {
            vm.related = related;
            vm.status = 'LOADED';
        });
    };
}

controller.$inject = ['ApplicationStore'];

export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./asset-code-explorer.html'),
        scope: {},
        controller,
        controllerAs: 'ctrl',
        bindToController: {
            application: '='
        }
    };
};
