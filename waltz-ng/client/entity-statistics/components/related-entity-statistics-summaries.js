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

import _ from "lodash";
import {mkChunks} from "../../common/list-utils";
import template from './related-entity-statistics-summaries.html';
import {initialiseData} from "../../common";


const bindings = {
    parentRef: '<',
    definitions: '<',
    summaries: '<'
};


const initialState = {
    chunkedEntries: []
};


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = (changes => {
        if (vm.summaries) {
            const summariesByDefinitionId = _.keyBy(vm.summaries, 'entityReference.id');
            const entries = _.map(vm.definitions.children,
                c => ({
                    definition: c,
                    summary: summariesByDefinitionId[c.id]
                }));
            vm.chunkedEntries = mkChunks(entries, 2);
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
