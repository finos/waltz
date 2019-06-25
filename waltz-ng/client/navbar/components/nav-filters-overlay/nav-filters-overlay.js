/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016, 2017, 2018, 2019 Waltz open source project
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
    appKindFilterOptions: [], // [ { kind, selected, displayName, count } ]
    onDismiss: () => console.log("nav filter overlay - default dismiss handler")
};


function mkAppKindFilterOptions(countsByKind, displayNameService, descriptionService) {
    return _.chain(countsByKind)
        .map((val, kind) => ({
            kind,
            selected: true,
            count: val.count,
            displayName: displayNameService.lookup("applicationKind", kind, kind),
            description: descriptionService.lookup("applicationKind", kind, null)
        }))
        .sortBy(d => d.displayName)
        .value();
}


function controller($element,
                    $document,
                    $rootScope,
                    $timeout,
                    $transitions,
                    $state,
                    $stateParams,
                    displayNameService,
                    descriptionService,
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
            if(viewStateToKind(stateName).toString() === "ENTITY_STATISTIC"){
                const ref = mkRef($stateParams.kind.toString(), id);
                const selector = mkSelectionOptions(ref);

                return serviceBroker
                    .loadAppData(CORE_API.FacetStore.countByApplicationKind, [selector])
                    .then(r => vm.facetCounts = _.keyBy(r.data, "id"))
                    .then(() => vm.appKindFilterOptions = mkAppKindFilterOptions(vm.facetCounts, displayNameService, descriptionService));
            }else {
                const kind = viewStateToKind(stateName);
                const ref = mkRef(kind, id);
                const selector = mkSelectionOptions(ref);

                return serviceBroker
                    .loadAppData(CORE_API.FacetStore.countByApplicationKind, [selector])
                    .then(r => vm.facetCounts = _.keyBy(r.data, "id"))
                    .then(() => vm.appKindFilterOptions = mkAppKindFilterOptions(vm.facetCounts, displayNameService, descriptionService));
            }
        } else {
            return Promise.resolve();
        }
    };


    const setupTransitionHandler = () => {
        $transitions.onSuccess({ }, (transition) => {
            const name = $state.current.name;
            const id = _.parseInt($stateParams.id);
            loadFacets(name, id)
                .then(() => {
                    vm.filterChanged();
                });
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
        // we need to convert the simple internal structures to the more complex filterOptions object structure

        const appKindFilterOptions = _.reduce(
            vm.appKindFilterOptions,
            (acc, opt) => {
                acc[opt.kind] = opt.selected;
                return acc;
            },
            {});
        const filterOptions = {
            APPLICATION: {
                applicationKind: appKindFilterOptions
            }
        };
        $rootScope.$broadcast(FILTER_CHANGED_EVENT, filterOptions);
    };


    vm.selectAllAppKindFilters = () => {
        vm.appKindFilterOptions = setSelectedOnAllTo(vm.appKindFilterOptions, true);
        vm.filterChanged();
    };

    vm.deselectAllAppKindFilters = () => {
        vm.appKindFilterOptions = setSelectedOnAllTo(vm.appKindFilterOptions, false);
        vm.filterChanged();
    };
}


function setSelectedOnAllTo(options = [], selected) {
    return _.map(options, option => Object.assign({}, option, { selected }))
}

controller.$inject = [
    "$element",
    "$document",
    "$rootScope",
    "$timeout",
    "$transitions",
    "$state",
    "$stateParams",
    "DisplayNameService",
    "DescriptionService",
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
