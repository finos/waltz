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

import {initialiseData} from "../common";

const template = require('./data-type-view.html');


const initialState = {
    dataFlow: null,
    entityRef: null
};


function controller(dataType, viewDataService, historyStore) {

    const vm = initialiseData(this, initialState);

    const appIdSelector = {
        entityReference: {
            kind: 'DATA_TYPE',
            id: dataType.id
        },
        scope: 'CHILDREN'
    };

    vm.entityRef = appIdSelector.entityReference;

    vm.dataType = dataType;

    const refresh = () => {
        if (!vm.rawViewData) return;
        const dataType = vm.rawViewData.dataType;

        historyStore.put(dataType.name, 'DATA_TYPE', 'main.data-type.view', { id: dataType.id });

        vm.viewData = vm.rawViewData;
    };

    viewDataService
        .loadAll(dataType.id)
        .then(data => vm.rawViewData = data)
        .then(d => refresh());
}


controller.$inject = [
    'dataType',
    'DataTypeViewDataService',
    'HistoryStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};
