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

import { initialiseData } from "../../../common/index";

import template from "./unit-view.html";


const initialState = {
    availableSections: [],
    parentEntityRef: {},
    sections: []
};


const addToHistory = (historyStore, orgUnit) => {
    if (! orgUnit) { return; }
    historyStore.put(
        orgUnit.name,
        'ORG_UNIT',
        'main.org-unit.view',
        { id: orgUnit.id });
};


function initTour(tourService, holder = {}) {
    return tourService.initialiseForKey('main.org-unit.view', true)
        .then(tour => holder.tour = tour);
}


function controller($stateParams,
                    dynamicSectionManager,
                    viewDataService,
                    historyStore,
                    tourService) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;

    vm.parentEntityRef = { kind: 'ORG_UNIT', id };
    vm.viewData = viewDataService.data;
    vm.selector =  {
        entityReference: vm.parentEntityRef,
        scope: 'CHILDREN'
    };

    vm.$onInit = () => {
        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind('ORG_UNIT');
        vm.sections = dynamicSectionManager.findUserSectionsForKind('ORG_UNIT');
        viewDataService
            .loadAll(id)
            .then(() => addToHistory(historyStore, vm.viewData.orgUnit))
            .then(() => initTour(tourService, vm))
            .then(() => vm.parentEntityRef = Object.assign({}, vm.parentEntityRef, {name: vm.viewData.orgUnit.name}));
    };



    // -- INTERACTIONS ---
    vm.loadFlowDetail = () => viewDataService
        .loadFlowDetail()
        .then(flowData => vm.viewData.dataFlows = flowData);

    vm.loadOrgUnitDescendants = (id) => viewDataService
        .loadOrgUnitDescendants(id)
        .then(descendants => vm.viewData.orgUnitDescendants = descendants);


    // -- DYNAMIC SECTIONS

    vm.addSection = s => vm.sections = dynamicSectionManager.openSection(s, 'ORG_UNIT');
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, 'ORG_UNIT');
}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'OrgUnitViewDataService',
    'HistoryStore',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
