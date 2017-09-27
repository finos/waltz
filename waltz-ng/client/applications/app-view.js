/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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
import { CORE_API } from "../common/services/core-api-utils";
import { CORE_API } from "../common/services/core-api-utils";

import template from "./app-view.html";


const initialState = {
    app: {}
};


const addToHistory = (historyStore, app) => {
    if (! app) { return; }
    historyStore.put(
        app.name,
        'APPLICATION',
        'main.app.view',
        { id: app.id });
};


function controller($stateParams,
                    serviceBroker,
                    historyStore) {

    const id = $stateParams.id;
    const entityReference = { id, kind: 'APPLICATION' };
    const vm = Object.assign(this, initialState);

    vm.entityRef = entityReference;


    function loadAll() {
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [id])
            .then(r => vm.app = r.data)
            .then(() => postLoadActions());
    }


    function postLoadActions() {
        addToHistory(historyStore, vm.app);
        vm.entityRef = Object.assign({}, vm.entityRef, {name: vm.app.name});
    }

    // load everything in priority order
    loadAll();
}


controller.$inject = [
    '$stateParams',
    'ServiceBroker',
    'HistoryStore'
];


export default  {
    template,
    controller,
    controllerAs: 'ctrl'
};

