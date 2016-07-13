
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
    labelOn: '@',
    labelOff: '@',
    iconOn: '@',
    iconOff: '@',
    onToggle: '&',
    state: '<'
};


const initialState = {
    labelOn: '',
    labelOff: '',
    iconOn: 'toggle-on',
    iconOff: 'toggle-off',
    onToggle: () => console.log('no on-toggle handler supplied to waltz-toggle component'),
    state: false
};


function controller() {
    _.defaultsDeep(this, initialState);
}


export default [
    () => ({
        restrict: 'E',
        replace: false,
        template: require('./toggle.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    })
]
;
