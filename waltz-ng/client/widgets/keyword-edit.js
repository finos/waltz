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

import angular from "angular";
import _ from "lodash";
import {initialiseData} from "../common";
import template from "./keyword-edit.html";
import {CORE_API} from "../common/services/core-api-utils";


/**
 * @name waltz-keyword-edit
 *
 * @description
 * This component ...
 */


const bindings = {
    onCancel: "<",
    onSave: "<",
    keywords: "<",
    autoComplete: "<",
    entityKind: "<"
};


const initialState = {};


function controller(serviceBroker) {
    const vm = this;

    vm.$onInit = () => initialiseData(vm, initialState);

    vm.$onChanges = (c) => {
        vm.working = angular.copy(vm.keywords);
    };

    vm.save = () => {
        const values = _.map(vm.working, "text");
        vm.onSave(values);
    };

    vm.onKeyDown = (event) => {
        if (event.ctrlKey && event.keyCode === 13) {  // ctrl + enter
            vm.save();
            vm.onCancel();
        }
    };

    vm.loadExistingTags = (query) => {
        const filterTags = (availableTags) => {
            return _.filter(availableTags,
                tag => tag.toLowerCase().indexOf(query.toLowerCase()) !== -1);
        };

        return serviceBroker
            .loadViewData(
                CORE_API.EntityTagStore.findTagsByEntityKind,
                [vm.entityKind],
                {force: false})
            .then(r => filterTags(r.data));
    };
}


controller.$inject = [ "ServiceBroker" ];


const component = {
    template,
    bindings,
    controller
};


export default component;