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

import angular from "angular";
import _ from "lodash";
import {initialiseData} from "../../common";
import template from "./tag-edit.html";
import {CORE_API} from "../../common/services/core-api-utils";


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
        const values = _.map(vm.working, "name");
        serviceBroker
            .loadViewData(
                CORE_API.TagStore.findTagsByEntityKind,
                [vm.entityKind], {force : true})
            .then(r => {
                const diff = _.difference(values, _.map(r.data, d => d.name));
                const message = _.isEmpty(diff)
                    ? "Updated Tags" : `New Tag [${diff}] created`;
                vm.onSave(values, message);
            });

    };

    vm.onKeyDown = (event) => {
        if (event.ctrlKey && event.keyCode === 13) {
            // ctrl + enter
            vm.save();
            vm.onCancel();
        }
    };

    vm.loadExistingTags = (query) => {
        const filterTags = (availableTags) => {
            return _.filter(availableTags,
                tag => tag.name.toLowerCase().indexOf(query.toLowerCase()) !== -1);
        };

        return serviceBroker
            .loadViewData(
                CORE_API.TagStore.findTagsByEntityKind,
                [vm.entityKind])
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