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

const BINDINGS = {
    model: '='
};


function controller(ApplicationStore) {

    this.apps = [];
    this.refresh = function(query) {
        if (!query) return;
        return ApplicationStore.search(query)
            .then((apps) => {
                this.apps = apps;
            });
    };
}

controller.$inject = ['ApplicationStore'];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./app-selector.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
};


export default () => directive;


