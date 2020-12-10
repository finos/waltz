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
import {termSearch} from "../../common";
import template from './software-catalog-section.html';

const BINDINGS = {
    catalog: '='
};


const FIELDS_TO_SEARCH = ['vendor', 'name', 'version', 'maturityStatus'];



function controller($scope) {

    const vm = this;



    $scope.$watchGroup(
        ['ctrl.qry', 'ctrl.catalog.packages'],
        ([qry, packages = []]) => {
            vm.filteredPackages = qry
                ? termSearch(packages, qry, FIELDS_TO_SEARCH)
                : packages;
        }
    );


    $scope.$watch(
        'ctrl.catalog.usages',
        (usages = []) => {
            vm.usages = _.chain(usages)
                .groupBy('provenance')
                .value();
        }
    );

}

controller.$inject = [ '$scope' ];


export default () => ({
    restrict: 'E',
    replace: true,
    scope: {
        panel: '='
    },
    template,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller
});
