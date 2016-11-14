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
import {initialiseData} from "../common";


const bindings = {
    collapsed: '<',
    toggleExpansion: '<'
};


const template = require('./twistie.html');


const initialState = {
    collapsed: false,
    toggleExpansion: (collapsed) => console.log("Default handler defined for toggleExpansion - collapsed: ", collapsed)
};


function calcClassNames(collapsed = false) {
    const iconName = collapsed
        ? 'chevron-right'
        : 'chevron-down';

    return [
        'fa',
        'fa-fw',
        `fa-${ iconName }`
    ];
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.onClick = () => {
        vm.collapsed = ! vm.collapsed;
        if (vm.toggleExpansion) {
            vm.toggleExpansion(vm.collapsed);
        }
    };

    vm.$onChanges = () => {
        vm.clazz = calcClassNames(vm.collapsed);
    };

}


const component = {
    bindings,
    controller,
    template
};


export default component;


