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
import template from "./svg-diagrams.html";

function getKey(group) {
    return `lastViewedSvg.${group}`;
}

function setLastViewed($state, index, diagram, localStorageService) {
    const lastViewed = {
        group: diagram.group,
        index: index
    };
    localStorageService.set(getKey($state.current.name), lastViewed);
}

function controller($state, $timeout, localStorageService) {
    const vm = this;

    vm.$onInit = () => {
        const activeTab = localStorageService.get(getKey($state.current.name));
        vm.active = activeTab ? activeTab.index : 0;
    };

    vm.show = (index, diagram) => {
        // timeout needed to prevent IE from crashing
        $timeout(() => diagram.visible = true, 100);
        setLastViewed($state, index, diagram, localStorageService)
    };

    vm.hide = (diagram) => {
        diagram.visible = false;
    };
}


controller.$inject = [
    "$state",
    "$timeout",
    "localStorageService"
];


export default {
    template,
    bindings: {
        diagrams: "<",
        blockProcessor: "<"
    },
    controller
};
