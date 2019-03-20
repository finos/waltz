/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017  Waltz open source project
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

import _ from "lodash";
import { initialiseData, invokeFunction } from "../../../common";
import { isDescendant } from "../../../common/browser-utils";
import { entity } from "../../../common/services/enums/entity";
import { FILTER_CHANGED_EVENT } from "../../../common/constants";
import { CORE_API } from "../../../common/services/core-api-utils";
import { hierarchyQueryScope } from "../../../common/services/enums/hierarchy-query-scope";
import { mkSelectionOptions } from "../../../common/selector-utils";
import { viewStateToKind } from "../../../common/link-utils";
import { mkRef } from "../../../common/entity-utils";
import { areFiltersVisible } from "../../../facet/facet-utils";

import template from "./nav-filters-overlay.html";


const ESCAPE_KEYCODE = 27;

const bindings = {
    onDismiss: "<",
    visible: "<"
};


const initialState = {
    facetCounts: {},
    filterSelections: {}, // { ENTITY_KIND: { filterKind: { selections}, ... }, ... }

    onDismiss: () => console.log("nav filter overlay - defailt dismiss handler")
};


function mkDefaultSelections(optionsWithCountsById) {
    return _.transform(optionsWithCountsById, (result, value, key) => {
        result[value.id] = true;
    }, {});
}


function mkFilterSelections(appKindFacetCountsById) {
    /*
        {
            APPLICATION: {
                applicationKind: { key1: true|false, key2: true|false },
                lifecyclePhase: { key1: true|false, key2: true|false },
                criticality: { key1: true|false, key2: true|false },

            },
            // other entity kinds for which filters are enabled
        }
    */
    const filterSelections = {
        [entity.APPLICATION.key]: { "applicationKind": mkDefaultSelections(appKindFacetCountsById) }
    };
    return filterSelections;
}


function controller($element,
                    $document,
                    $rootScope,
                    $timeout,
                    $transitions,
                    $state,
                    $stateParams,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const documentClick = (e) => {
        const element = $element[0];
        if(!isDescendant(element, e.target)) {
            vm.dismiss();
        }
    };


    const onOverlayKeypress = (evt) => {
        if(evt.keyCode === ESCAPE_KEYCODE) {
            vm.dismiss();
        }
    };


    const loadFacets = (stateName, id) => {
        if(areFiltersVisible(stateName)) {
            const kind = viewStateToKind(stateName);

            const ref = mkRef(kind, id);
            const selector = mkSelectionOptions(
                ref,
                hierarchyQueryScope.CHILDREN.key);

            return serviceBroker
                .loadAppData(CORE_API.FacetStore.countByApplicationKind, [selector])
                .then(r => vm.facetCounts = _.keyBy(r.data, "id"))
                .then(() => vm.filterSelections = mkFilterSelections(vm.facetCounts));
        } else {
            return Promise.resolve();
        }
    };


    const setupTransitionHandler = () => {
        $transitions.onSuccess({ }, (transition) => {
            const name = $state.current.name;
            const id = _.parseInt($stateParams.id);
            loadFacets(name, id)
                .then(() => vm.filterChanged());
        });
    };


    vm.$onInit = () => {
        // set up transition handler
        setupTransitionHandler();

        // initial load of facets
        const id = _.parseInt($stateParams.id);
        loadFacets($state.current.name, id);
    };


    vm.$onChanges = (changes) => {
        if(vm.visible) {
            $timeout(() => $document.on("click", documentClick), 200);
            $timeout(() => $element.on("keydown", onOverlayKeypress), 200);
        }  else {
            $document.off("click", documentClick);
            $element.off("keydown", onOverlayKeypress);
        }
    };


    vm.dismiss = () => {
        invokeFunction(vm.onDismiss);
    };


    vm.filterChanged = () => {
        $rootScope.$broadcast(FILTER_CHANGED_EVENT, vm.filterSelections);
    };


    vm.resetFilters = () => {
        vm.filterSelections = mkFilterSelections(vm.facetCounts);
        vm.filterChanged();
    };
}


controller.$inject = [
    "$element",
    "$document",
    "$rootScope",
    "$timeout",
    "$transitions",
    "$state",
    "$stateParams",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller
};


export default {
    component,
    id: "waltzNavFiltersOverlay"
};
