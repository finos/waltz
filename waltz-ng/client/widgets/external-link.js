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

import {toDomain} from "../common/string-utils";
import template from './external-link.html';

const BINDINGS = {
    url: '@',
    title: '@',
    showUrl: '='
};


function toPrettyUrl(url = "") {
    return _.truncate( toDomain(url), { length: 60 });
}


function controller($scope) {

    const vm = this;

    $scope.$watchGroup(
        ['ctrl.url', 'ctrl.title', 'ctrl.showUrl'],
        ([url, title, showUrl = false]) => {
            vm.prettyTitle = title
                ? title
                : toPrettyUrl(url);
            vm.prettyUrl = toPrettyUrl(url);
            vm.showAside = (showUrl && title && url);
        });

}


controller.$inject = [
    '$scope'
];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template
});

