/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2017  Khartec Ltd.
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

import _ from  "lodash";
import { CORE_API } from "../../../common/services/core-api-utils";

import {
    bookmarkWidget,
    changesWidget,
    changeInitiativesWidget,
    costsWidget,
    entityNamedNoteWidget,
    flowDiagramsWidget,
    flowWidget,
    indicatorsWidget,
    measurableRatingsWidget,
    peopleWidget,
    surveysWidget,
    technologyWidget
} from "../../../section-definitions";

import template from "./app-view.html";


const initialState = {
    app: {},
    availableWidgets: [
        entityNamedNoteWidget,
        measurableRatingsWidget,
        bookmarkWidget,
        peopleWidget,
        changeInitiativesWidget,
        flowDiagramsWidget,
        flowWidget,
        technologyWidget,
        costsWidget,
        indicatorsWidget,
        surveysWidget,
        changesWidget
    ],
    parentEntityRef: {},
    widgets: []
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
        vm.parentEntityRef = entityReference;

        loadAll(id);
    };

    // -- INTERACT --
    vm.addWidget = w => {
        vm.widgets =  _.reject(vm.widgets, x => x.id === w.id)
        vm.widgets.unshift(w);
    };
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

