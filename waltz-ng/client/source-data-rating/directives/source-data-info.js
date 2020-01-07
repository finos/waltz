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
import template from './source-data-info.html';


const BINDINGS = {
    ratings: '='
};


function controller() {

    const vm = this;

    vm.accuracyRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Trusted";
            case 'A':
                return "Indeterminate";
            case 'R':
                return "Untrusted";
            default:
                return "Unknown";
        }
    };

    vm.authoritativenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Golden";
            case 'A':
                return "Indirect";
            case 'R':
                return "'Found'";
            default:
                return "Unknown";
        }
    };

    vm.completenessRatingToName = (rag) => {
        switch (rag) {
            case 'G':
                return "Complete";
            case 'A':
                return "Partial";
            case 'R':
                return "Sparse";
            default:
                return "Unknown";
        }
    };
}


controller.$inject = [];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template
});

