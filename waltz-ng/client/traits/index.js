
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

/*
 *  This file is part of Waltz.
 *
 *  Waltz is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Waltz is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with Waltz.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
import angular from 'angular';

import traitListView from './trait-list';
import traitView from './trait-view';


const traitListState = {
    url: 'traits',
    views: {'content@': traitListView }
};

const traitViewState = {
    url: '/{id:int}',
    views: {'content@': traitView }
};


function configureStates(stateProvider) {
    stateProvider
        .state('main.traits', traitListState)
        .state('main.traits.view', traitViewState);
}

configureStates.$inject = ['$stateProvider'];


export default () => {

    const module = angular.module('waltz.traits', []);

    module
        .config(configureStates);

    module
        .service('TraitStore', require('./services/trait-store'))
        .service('TraitUsageStore', require('./services/trait-usage-store'));

    module
        .directive('waltzTraitsIndicator', require('./directives/traits-indicator'))
        .directive('waltzTraitsTable', require('./directives/traits-table'))
        .directive('waltzTraitUsageEditor', require('./directives/trait-usage-editor'));

    return module.name;
};
