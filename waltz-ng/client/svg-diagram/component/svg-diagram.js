/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017 Waltz open source project
 * See README.md for more information
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from 'lodash';
import angular from 'angular';


function resize(elem) {
    const width = elem.parent()[0].clientWidth || 1024;
    elem.find('svg').attr('width', width);
    elem.find('svg').attr('height', width * 0.6);
}


function controller($element, $window) {
    const vm = this;

    vm.$onInit = () => angular
        .element($window)
        .on('resize', () => resize($element));

    vm.$onDestroy = () => angular
        .element($window)
        .off('resize', () => resize($element));

    vm.$onChanges = () => {
        if (!vm.diagram) return;

        const svg = $element.html(vm.diagram.svg);

        $window.setTimeout(() => resize(svg), 100);

        const dataProp = 'data-' + vm.diagram.keyProperty;
        const dataBlocks = svg.querySelectorAll('[' + dataProp + ']');

        const blocks = _.map(dataBlocks, b => ({
            block: b,
            value: b.attributes[dataProp].value
        }));

        _.each(blocks, vm.blockProcessor);
    };

}

controller.$inject = ['$element', '$window'];


export default {
    bindings: {
        blockProcessor: '<',
        diagram: '<'
    },
    controller
};
