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
import {nest} from 'd3-collection';
import {notEmpty} from "../../../common";
import template from './rated-summary-table.html';


const bindings = {
    exactSummaries: '<',
    childSummaries: '<',
    decoratorEntities: '<',
    onClick: '<'
};


function nestByDecoratorThenRating(summaries = []) {
    return nest()
        .key(d => d.decoratorEntityReference.id)
        .key(d => d.rating)
        .rollup(ds => notEmpty(ds)
            ? ds[0].count
            : 0)
        .map(summaries);
}


function getRelevantDecorators(allDecorators = [], summaries = []) {
    const decoratorEntitiesById = _.keyBy(allDecorators, 'id');
    return _.chain(summaries)
        .map('decoratorEntityReference.id')
        .uniq()
        .map(id => decoratorEntitiesById[id])
        .filter(dec => dec != null)
        .sortBy('name')
        .value();
}


function setup(decoratorEntities = [], exactSummaries = [], childSummaries = []) {
    const maxCounts = nest()
        .key(d => d.decoratorEntityReference.id)
        .rollup(ds => _.sumBy(ds, "count"))
        .map(childSummaries);

    const totalCounts = nestByDecoratorThenRating(childSummaries);
    const directCounts = nestByDecoratorThenRating(exactSummaries);

    const result = {
        maxCounts,
        directCounts,
        totalCounts,
        decorators: getRelevantDecorators(decoratorEntities, childSummaries)
    };

    return result;
}


function controller() {
    const vm = this;

    const invokeClick = ($event, d) => {
        vm.onClick(d);
        if ($event) $event.stopPropagation();
    };

    vm.$onChanges = () => Object.assign(
        vm,
        setup(vm.decoratorEntities, vm.exactSummaries, vm.childSummaries));

    vm.columnClick = ($event, rating) => invokeClick($event, { type: 'COLUMN', rating });
    vm.rowClick = ($event, dataType) => invokeClick($event, { type: 'ROW', dataType });
    vm.cellClick = ($event, dataType, rating) => invokeClick($event, { type: 'CELL', dataType, rating });
}


const component = {
    bindings,
    template,
    controller
};


export default component;
