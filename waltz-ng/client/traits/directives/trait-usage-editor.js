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

const BINDINGS = {
    usages: '=',
    traits: '=',
    add: '=',
    remove: '='

};


function controller($scope) {

    const vm = this;

    $scope.$watchGroup(['ctrl.usages', 'ctrl.traits'], ([usages, traits]) => {
        if (! usages || ! traits) { return; }

        const traitsById = _.keyBy(traits, 'id');
        const usedTraitIds = _.map(usages, usage => usage.traitId);

        vm.usedTraits = _.map(usedTraitIds, id => traitsById[id]);
        vm.availableTraits = _.reject(traits, t => _.includes(usedTraitIds, t.id));
    });

}


controller.$inject = ['$scope'];


function directive() {
    return {
        restrict: 'E',
        replace: 'true',
        bindToController: BINDINGS,
        scope: {},
        template: require('./trait-usage-editor.html'),
        controller,
        controllerAs: 'ctrl'
    };
}

export default directive;