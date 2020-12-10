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


import template from "./playpen1.html";
import {initialiseData} from "../../common";
import App from "./App.svelte";
import {mkRef} from "../../common/entity-utils";
import TestWidget from "./TestWidget.svelte";
import BookmarkList from "./bookmark/BookmarkList.svelte";
import {dynamicSections} from "../../dynamic-section/dynamic-section-definitions";

const initData = {
    ref: mkRef("APPLICATION", 913),
    ref1: mkRef("APPLICATION", 12),
    ref2: mkRef("APPLICATION", 22),
    ref3: mkRef("APPLICATION", 212),
    ref4: mkRef("APPLICATION", 913),
    testWidget: TestWidget,
    bookmarkList: BookmarkList,
    bookmarksSection: dynamicSections.bookmarksSection,
    app: App
};

function controller($element, $q, serviceBroker) {

    const vm = initialiseData(this, initData);

    vm.$onInit = () => {
        setTimeout(() => {
            const targ = document.querySelector("#svelte-app");
            console.log({targ});
            const app = new App({
                target: targ,
                props: {
                    name: "world",
                    serviceBroker
                }
            });
        }, 10);
    };

    vm.myVars = {};

    let x = 0;
    vm.changeVars = () => {
        vm.myVars = { msg: "Hello " + (x ++ ) };
    };
}

controller.$inject = ["$element", "$q", "ServiceBroker"];

const view = {
    template,
    controller,
    controllerAs: "$ctrl",
    bindToController: true,
    scope: {}
};

export default view;