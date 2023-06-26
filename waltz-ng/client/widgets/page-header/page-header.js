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
import angular from "angular";
import {initialiseData} from "../../common";
import {sidebarState} from "../../navbar/sidebar-store";

import template from "./page-header.html";

export const pageHeaderDefaultOffset = 60;

const bindings = {
    name: "@",
    icon: "@",
    small: "@"
};

const initialState = {
    stickyVisible: false
};


function controller($document,
                    $scope,
                    $window) {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (c) => {
        if (c.name && document.title.name != c.name.currentValue) {
            document.title = `Waltz: ${c.name.currentValue}`;
        }
    };

    const scrollListener = () => {
        $scope.$applyAsync(() => {
            vm.stickyVisible = $window.pageYOffset > pageHeaderDefaultOffset
        });
    };

    vm.$onInit = () => {
        angular
            .element($window)
            .on("scroll", _.throttle(scrollListener, 100));
    };

    vm.$onDestroy = () => {
        angular
            .element($window)
            .off("scroll", scrollListener);
    };

    vm.scrollToTop = () => {
        $window.scrollTo(0, 0);
    };

}


controller.$inject=[
    "$document",
    "$scope",
    "$window"
];


const component = {
    bindings,
    template,
    controller,
    transclude: {
        "breadcrumbs": "?breadcrumbs",
        "actions": "?actions"
    }
};

export default component;

