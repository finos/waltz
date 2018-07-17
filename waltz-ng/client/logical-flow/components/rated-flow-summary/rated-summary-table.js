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
