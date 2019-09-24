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

import {CORE_API} from "../../../common/services/core-api-utils";
import {entityLifecycleStatuses, initialiseData} from "../../../common/index";
import {entity} from "../../../common/services/enums/entity";
import {isDescendant} from "../../../common/browser-utils";

import template from "./nav-search-overlay.html";
import {kindToViewState} from "../../../common/link-utils";

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
        entity.ACTOR.key,
        entity.CHANGE_INITIATIVE.key,
        entity.DATA_TYPE.key,
        entity.APP_GROUP.key,
        entity.ORG_UNIT.key,
        entity.MEASURABLE.key,
        entity.PHYSICAL_SPECIFICATION.key,
        entity.LOGICAL_DATA_ELEMENT.key,
        entity.ROADMAP.key,
        entity.SERVER.key
    ],
    selectedCategory: null,
    showActiveOnly: true,
    results: {},
    filteredResults: []
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
            vm.dismiss();
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
        $document.off("click", documentClick);
    };

    vm.dismiss = () => {
        if (vm.onDismiss) {
            vm.onDismiss();
        } else {
            console.log("No dismiss handler registered");
        }
    };

    vm.toggleCategory = (c) => {
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
    const handleSearch = (query, entityKind) => {
        const statuses = vm.showActiveOnly
            ? [entityLifecycleStatuses.ACTIVE, entityLifecycleStatuses.PENDING]
            : [entityLifecycleStatuses.ACTIVE, entityLifecycleStatuses.PENDING, entityLifecycleStatuses.REMOVED];

        const searchOptions = {
            entityKinds: [entityKind],
            entityLifecycleStatuses: statuses
        };

        return serviceBroker
            .loadViewData(CORE_API.EntitySearchStore.search, [ query, searchOptions ])
            .then(r => vm.results[entityKind] = r.data);
    };


    const doSearch = (query) => {
        if(!query){
            vm.clearSearch();
            return;
        }

        handleSearch(query, entity.APPLICATION.key,);
        handleSearch(query, entity.CHANGE_INITIATIVE.key,);
        handleSearch(query, entity.DATA_TYPE.key,);
        handleSearch(query, entity.PERSON.key,);
        handleSearch(query, entity.MEASURABLE.key,);
        handleSearch(query, entity.ORG_UNIT.key,);
        handleSearch(query, entity.ACTOR.key,);
        handleSearch(query, entity.PHYSICAL_SPECIFICATION.key,);
        handleSearch(query, entity.APP_GROUP.key,);
        handleSearch(query, entity.LOGICAL_DATA_ELEMENT.key,);
        handleSearch(query, entity.ROADMAP.key,);
        handleSearch(query, entity.SERVER.key,);
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
                vm.dismiss();
            }
        }
        evt.stopPropagation();
        if(evt.keyCode === ENTER_KEYCODE) {
            navigateToEntityIfResultContainsOnlyOne(_.chain(vm.results).values().flatten().value());
        }
    };

    const onOverlayKeypress = (evt) => {
        if(evt.keyCode === ESCAPE_KEYCODE) {
            vm.dismiss();
        }
    };

    vm.toggleActiveOnly = () => {
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
