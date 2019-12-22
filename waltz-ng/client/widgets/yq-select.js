
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

import _ from 'lodash';
import template from './yq-select.html';


function controller() {
    const vm = this;

    const currentYear = new Date().getFullYear();
    const { start = { year: currentYear, quarter: 1 } } = vm;
    const { end = { year: start.year + 5, quarter: 4 } } = vm;

    vm.years = _.range(start.year, end.year + 1);
    vm.select = (yq) => {
        if (vm.onSelect()) {
            vm.onSelect()(yq);
        }
    };

    vm.isSelected = (y, q) => vm.selected && vm.selected.year === y && vm.selected.quarter === q;

    vm.isDisabled = (y, q) =>
            (y * 10 + q) < (start.year * 10 + start.quarter) ||
            (y * 10 + q) > (end.year * 10 + end.quarter);
}

export default function() {
    return {
        restrict: 'E',
        replace: true,
        template,
        scope: {
            start: '=',
            end: '=',
            selected: '=',
            onSelect: '&'
        },
        bindToController: true,
        controllerAs: 'ctrl',
        controller
    };
}
