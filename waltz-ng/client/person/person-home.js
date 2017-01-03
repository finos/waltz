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

import angular from "angular";


const initialState = {
    person: null
};


function controller($scope,
                    $state,
                    staticPanelStore,
                    svgStore) {

    const vm = Object.assign(this, initialState);

    $scope.$watch('ctrl.person', (person) => {
        if (person) {
            const navKey = { empId: person.employeeId };
            $state.go('main.person.view', navKey);
        }
    });

    svgStore
        .findByGroup('ORG_TREE')
        .then(xs => vm.diagrams = xs);

    staticPanelStore
        .findByGroup("HOME.PERSON")
        .then(panels => vm.panels = panels);

    vm.blockProcessor = b => {
        b.block.onclick = () => $state.go('main.person.view', { empId: b.value });
        angular.element(b.block).addClass('clickable');
    };

}


controller.$inject = [
    '$scope',
    '$state',
    'StaticPanelStore',
    'SvgDiagramStore'
];


const view = {
    template: require('./person-home.html'),
    controllerAs: 'ctrl',
    controller
};


export default view;
