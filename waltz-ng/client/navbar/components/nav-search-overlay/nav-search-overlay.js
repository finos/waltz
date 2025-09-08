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

import {CORE_API} from "../../../common/services/core-api-utils";
import {entityLifecycleStatuses, initialiseData} from "../../../common/index";
import {entity} from "../../../common/services/enums/entity";
import {isDescendant} from "../../../common/browser-utils";

import template from "./nav-search-overlay.html";
import {kindToViewState} from "../../../common/link-utils";
import _ from "lodash";
import {displayError} from "../../../common/error-utils";

const ESCAPE_KEYCODE = 27;
const ENTER_KEYCODE = 13;

const bindings = {
    query: "@",
    onDismiss: "<",
    visible: "<"
};


const initialState = {
    categories: [
        entity.APPLICATION.key,
        entity.PERSON.key,
        entity.CHANGE_INITIATIVE.key,
        entity.APP_GROUP.key,
        entity.ORG_UNIT.key,
        entity.ACTOR.key,
        entity.MEASURABLE.key,
        entity.DATA_TYPE.key,
        entity.PHYSICAL_SPECIFICATION.key,
        entity.PHYSICAL_FLOW.key,
        entity.END_USER_APPLICATION.key,
        entity.LEGAL_ENTITY.key,
        entity.SERVER.key,
        entity.DATABASE.key,
        entity.SOFTWARE.key,
        entity.ROADMAP.key,
        entity.LOGICAL_DATA_ELEMENT.key,
        entity.LICENCE.key
    ],
    selectedCategory: null,
    showActiveOnly: true,
    results: {},
    filteredResults: [],
    searching: false
};


function controller($element,
                    $document,
                    $timeout,
                    $state,
                    serviceBroker) {
    const vm = initialiseData(this, initialState);

    const documentClick = (e) => {
        const element = $element[0];
        if(!isDescendant(element, e.target)) {
            vm.onDismiss();
        }
    };

    vm.$onChanges = (c) => {
        if (c.visible) {
            vm.selectedCategory = null;
        }

        if(vm.visible) {
            const input = $element.find("input")[0];
            input.focus();
            $timeout(() => $document.on("mousedown", documentClick), 200);
            $timeout(() => $element.on("keydown", onOverlayKeypress), 200);
        }  else {
            $document.off("mousedown", documentClick);
            $element.off("keydown", onOverlayKeypress);
        }
    };

    vm.$onDestroy = () => {
        $document.off("mousedown", documentClick);
        $document.off("keydown", documentClick);
    };

    vm.onToggleCategory = (c) => {
        if ((vm.results[c] || []).length === 0) {
            return;
        }
        if (vm.selectedCategory === c) {
            vm.selectedCategory = null;
        } else {
            vm.selectedCategory = c;
        }
    };


    // helper fn, to reduce boilerplate
    const handleSearch = (query,
                          entityKinds) => {
        const statuses = vm.showActiveOnly
            ? [entityLifecycleStatuses.ACTIVE, entityLifecycleStatuses.PENDING]
            : [entityLifecycleStatuses.ACTIVE, entityLifecycleStatuses.PENDING, entityLifecycleStatuses.REMOVED];

        const searchOptions = {
            entityKinds,
            entityLifecycleStatuses: statuses,
            searchQuery: query
        };

        return serviceBroker
            .loadViewData(
                CORE_API.EntitySearchStore.search,
                [searchOptions])
            .then(r => Object.assign(
                vm.results,
                _.reduce(entityKinds, (acc, k) => { acc[k] = []; return acc;}, {}),
                _.groupBy(r.data, d => d.kind)));
    };


    const doSearch = (query) => {
        if(!query){
            vm.clearSearch();
            return;
        }

        if(query.length < 3) {
            vm.results = {};
            return;
        }

        vm.searching = true;
        handleSearch(query, [entity.APPLICATION.key, entity.PERSON.key])
            .then(() => handleSearch(query, [entity.APP_GROUP.key, entity.CHANGE_INITIATIVE.key, entity.ORG_UNIT.key]))
            .then(() => handleSearch(query, [entity.ACTOR.key, entity.MEASURABLE.key, entity.LEGAL_ENTITY.key, entity.END_USER_APPLICATION.key]))
            .then(() => handleSearch(query, [entity.PHYSICAL_SPECIFICATION.key, entity.PHYSICAL_FLOW.key, entity.DATA_TYPE.key, entity.SERVER.key, entity.DATABASE.key]))
            .then(() => handleSearch(query, [entity.SOFTWARE.key, entity.ROADMAP.key, entity.LOGICAL_DATA_ELEMENT.key, entity.LICENCE.key]))
            .catch(e => displayError("Failed to search"))
            .finally(() => vm.searching = false);
    };

    vm.doSearch = () => doSearch(vm.query);

    vm.clearSearch = () => {
        vm.results = {};
        vm.query = "";
        vm.selectedCategory = null;
    };

    function navigateToEntityIfResultContainsOnlyOne(results = []) {
        if (results.length === 1) {
            const result = results[0];
            vm.visible = false;
            $state.go(
                kindToViewState(result.kind),
                { id: result.id });
        }
    }

    vm.onKeypress = (evt) => {
        if(evt.keyCode === ESCAPE_KEYCODE) {
            if(vm.query) {
                vm.clearSearch();
            } else {
                vm.onDismiss();
            }
        }
        evt.stopPropagation();
        if(evt.keyCode === ENTER_KEYCODE) {
            navigateToEntityIfResultContainsOnlyOne(_.chain(vm.results).values().flatten().value());
        }
    };

    const onOverlayKeypress = (evt) => {
        if(evt.keyCode === ESCAPE_KEYCODE) {
            vm.onDismiss();
        }
    };

    vm.onToggleActiveOnly = () => {
        vm.showActiveOnly = ! vm.showActiveOnly;
        vm.doSearch();
    };
}


controller.$inject = [
    "$element",
    "$document",
    "$timeout",
    "$state",
    "ServiceBroker"
];


const component = {
    template,
    bindings,
    controller,
};


export default {
    component,
    id: "waltzNavSearchOverlay"
};
