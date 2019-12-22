/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
 * See README.md for more information
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific
 *
 */

import {CORE_API} from "../common/services/core-api-utils";
import {initialiseData} from "../common";
import {dynamicSections} from "../dynamic-section/dynamic-section-definitions";


import template from './logical-data-element-view.html';


const initialState = {
    logicalDataElement: null,
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
                    serviceBroker)
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
        .then(() => addToHistory(historyStore, vm.logicalDataElement));
}


controller.$inject = [
    '$stateParams',
    'HistoryStore',
    'ServiceBroker',
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
