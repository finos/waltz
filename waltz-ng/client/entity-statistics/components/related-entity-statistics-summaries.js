/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import _ from "lodash";


const bindings = {
    parentRef: '<',
    definitions: '<',
    summaries: '<'
};


const template = require('./related-entity-statistics-summaries.html');


function controller() {
    const vm = this;

    vm.$onChanges = (changes => {
        if (vm.summaries) {
            vm.summariesByDefinitionId = _.keyBy(vm.summaries, 'entityReference.id');
        }
    });

}

controller.$inject = [];


const component = {
    template,
    controller,
    bindings
};


export default component;
