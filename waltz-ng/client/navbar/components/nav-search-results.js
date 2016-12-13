/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

const bindings = {
    query: '@',
    results: '<',
    onDismiss: '<'
};

const template = require('./nav-search-results.html');


function isDescendant(parent, child) {
    let node = child.parentNode;
    while (node != null) {
        if (node == parent) {
            return true;
        }
        node = node.parentNode;
    }
    return false;
}


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
