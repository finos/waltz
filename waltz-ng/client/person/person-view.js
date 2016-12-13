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
import {notEmpty} from "../common";


function hasInvolvements(involvements) {
    return notEmpty(involvements.all);
}


function controller($scope,
                    $stateParams,
                    viewService,
                    tourService,
                    historyStore) {

    const vm = this;
    vm.state = viewService.state;

    const employeeId = $stateParams.empId;

    viewService
        .load(employeeId)
        .then(() => tourService.initialiseForKey('main.person.view', true))
        .then(tour => vm.tour = tour);

    $scope.$watch(() => viewService.state.model, () => {
        const model = viewService.state.model;
        Object.assign(vm, model);

        if (model.person) {
            historyStore.put(model.person.displayName, 'PERSON', 'main.person.view', { empId: model.person.employeeId });
            vm.entityRef = { kind: 'PERSON', id: model.person.id };
        }

        vm.hasInvolvements = hasInvolvements(viewService.state.model.combinedAppInvolvements);
    }, true);


    vm.loadAllCosts = () => {
        $scope.$applyAsync(() => {
            viewService.loadAllCosts();
        });
    };


    vm.lineageTableInitialised = (api) => {
        vm.exportLineageReports = api.export;
    };


    vm.loadFlowDetail = () => viewService.loadFlowDetail();

}

controller.$inject = [
    '$scope',
    '$stateParams',
    'PersonViewDataService',
    'TourService',
    'HistoryStore'
];


export default {
    template: require('./person-view.html'),
    controller,
    controllerAs: 'ctrl'
};
