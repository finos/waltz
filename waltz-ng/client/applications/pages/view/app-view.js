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

import { CORE_API } from "../../../common/services/core-api-utils";

import template from "./app-view.html";


const initialState = {
    app: {},
    availableSections: [],
    parentEntityRef: {},
    sections: []
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
                    dynamicSectionManager,
                    serviceBroker,
                    historyStore) {
    const vm = Object.assign(this, initialState);


    function loadAll(id) {
        serviceBroker
            .loadViewData(CORE_API.ApplicationStore.getById, [id])
            .then(r => vm.app = r.data)
            .then(() => postLoadActions(vm.app));
    }


    function postLoadActions(app) {
        addToHistory(historyStore, app);
        vm.parentEntityRef = Object.assign({}, vm.parentEntityRef, {name: app.name});
    }

    // -- BOOT --
    vm.$onInit = () => {
        const id = $stateParams.id;
        const entityReference = { id, kind: 'APPLICATION' };
        vm.availableSections = dynamicSectionManager.findAvailableSectionsForKind('APPLICATION');
        vm.sections = dynamicSectionManager.findUserSectionsForKind('APPLICATION');
        vm.parentEntityRef = entityReference;
        loadAll(id);
    };

    // -- INTERACT --
    vm.addSection = (section) => vm.sections = dynamicSectionManager.openSection(section, 'APPLICATION');
    vm.removeSection = (section) => vm.sections = dynamicSectionManager.removeSection(section, 'APPLICATION');
}


controller.$inject = [
    '$stateParams',
    'DynamicSectionManager',
    'ServiceBroker',
    'HistoryStore'
];


export default  {
    template,
    controller,
    controllerAs: 'ctrl'
};

