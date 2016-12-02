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
import {isDescendant} from "../../common";


const bindings = {
    query: '@',
    results: '<',
    onDismiss: '<'
};

const template = require('./nav-search-results.html');





function controller($element,
                    $document) {
    const vm = this;

    vm.dismiss = () => {
        if (vm.onDismiss) {
            vm.onDismiss();
        } else {
            console.log('No dismiss handler registered');
        }
    }

    const onClick = (e) => {
        const element = $element[0];
        if(!isDescendant(element, e.target)) vm.dismiss();
    }

    vm.$onInit = () => {
        $document.on('click', onClick);
    };

    vm.$onDestroy = () => {
        $document.off('click', onClick);
    };

}


controller.$inject = [
    '$element',
    '$document'
];


const component = {
    template,
    bindings,
    controller,
};

export default component;
