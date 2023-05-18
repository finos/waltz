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

import _ from "lodash";
import { initialiseData, invokeFunction } from "../../../common";
import { isDescendant } from "../../../common/browser-utils";
import { FILTER_CHANGED_EVENT } from "../../../common/constants";
import { CORE_API } from "../../../common/services/core-api-utils";
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
        $transitions.onSuccess({ }, () => {
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
        if (vm.visible) {
            $timeout(() => $document.on("click", documentClick), 200);
            $timeout(() => $element.on("keydown", onOverlayKeypress), 200);
        } else {
            $document.off("click", documentClick);
            $element.off("keydown", onOverlayKeypress);
        }
    };

    vm.$onDestroy = () => {
        $document.off("click", documentClick);
        $element.off("keydown", onOverlayKeypress);
    }

    vm.dismiss = () => {
        invokeFunction(vm.onDismiss);
    };


    vm.filterChanged = () => {
        const omitApplicationKinds = _
            .chain(vm.appKindFilterOptions)
            .reject(d => d.selected)
            .map(d => d.kind)
            .value();

        const filterOptions = _.isEmpty(omitApplicationKinds)
            ? {}
            : { omitApplicationKinds };

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
