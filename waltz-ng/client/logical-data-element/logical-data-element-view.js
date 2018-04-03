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

import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";


import template from './logical-data-element-view.html';


const initialState = {
    logicalDataElement: null,
    tour: [],
    bookmarksSection: dynamicSections.bookmarksSection,
    entityNamedNotesSection: dynamicSections.entityNamedNotesSection,
    relatedPhysicalFieldSection: dynamicSections.relatedPhysicalFieldSection,
};



function mkHistoryObj(logicalDataElement) {
    return {
        name: logicalDataElement.name,
        kind: 'LOGICAL_DATA_ELEMENT',
        state: 'main.logical-data-element.view',
        stateParams: { id: logicalDataElement.id }
    };
}


function addToHistory(historyStore, logicalDataElement) {
    if (!logicalDataElement) { return; }

    const historyObj = mkHistoryObj(logicalDataElement);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function controller($stateParams,
                    historyStore,
                    serviceBroker,
                    tourService)
{
    const vm = initialiseData(this, initialState);

    const logicalElementId = $stateParams.id;
    vm.entityReference = {
        id: logicalElementId,
        kind: 'LOGICAL_DATA_ELEMENT'
    };


    // -- LOAD ---
    const logicalElementPromise = serviceBroker
        .loadViewData(
            CORE_API.LogicalDataElementStore.getById,
            [ logicalElementId ])
        .then(r => vm.logicalDataElement = r.data);

    logicalElementPromise
        .then(() => tourService.initialiseForKey('main.logical-data-element.view', true))
        .then(tour => vm.tour = tour)
        .then(() => addToHistory(historyStore, vm.logicalDataElement));
}


controller.$inject = [
    '$stateParams',
    'HistoryStore',
    'ServiceBroker',
    'TourService'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
