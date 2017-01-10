/*
 * Waltz - Enterprise Architecture
 * Copyright (C) 2016  Khartec Ltd.
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

import {resetData} from "../../common";


const bindings = {
    name: '@',
    definitions: '<',
    parentRef: '<'
};


const initData = {
    activeDefinition: null,
    activeSummary: null,
    definitions: [],
    loading: false,
    summaries: {}
};


const template = require('./entity-statistic-summary-section.html');


function controller(entityStatisticStore) {
    const vm = resetData(this, initData);

    vm.onDefinitionSelection = (d) => {
        vm.activeDefinition = d;
        if (vm.summaries[d.id]) {
            vm.loading = false;
            vm.activeSummary = vm.summaries[d.id];
        } else {
            vm.loading = true;
            entityStatisticStore
                .findStatTallies([d.id], { entityReference: vm.parentRef, scope: 'CHILDREN' })
                .then(summaries => {
                    vm.loading = false;
                    vm.summaries[d.id] = summaries[0];
                    vm.activeSummary = summaries[0];
                });
        }
    };
}


controller.$inject = [
    'EntityStatisticStore'
];


const component = {
    template,
    controller,
    bindings
};


export default component;
