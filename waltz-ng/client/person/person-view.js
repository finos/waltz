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
