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

import _ from 'lodash';


const initialState = {
    costs: []
};


function controller($scope, assetCostStore, appStore, assetCostViewService) {
    const vm = Object.assign(this, initialState);

    const selector = { entityReference: { id: 10, kind: 'ORG_UNIT'}, scope: 'CHILDREN' };
    assetCostStore
        .findTopAppCostsByAppIdSelector(selector)
        .then(cs => vm.costs = cs);

    assetCostViewService.initialise(selector, 2016)
        .then(costView => vm.costView = costView);

    vm.loadAll = () => {
        assetCostViewService
            .loadDetail()
            .then(costView => vm.costView = costView);
    };

    vm.onHover = (d) => vm.hovered = d;
    vm.onSelect = (d) => vm.selected = d;
}


controller.$inject = [
    '$scope',
    'AssetCostStore',
    'ApplicationStore',
    'AssetCostViewService'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;