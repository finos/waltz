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
import { categorizeCostsIntoBuckets } from '../../asset-cost/services/asset-cost-utilities';
import EventDispatcher from '../../common/EventDispatcher';


function controller($scope) {

    const vm = this;

    vm.eventDispatcher = new EventDispatcher();

    $scope.$watch('ctrl.assetCosts', assetCosts => {
        if (! assetCosts) return;
        vm.assetCostBuckets = categorizeCostsIntoBuckets(assetCosts);
    });

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./asset-costs-section.html'),
        scope: {},
        bindToController: {
            assetCosts: '='
        },
        controllerAs: 'ctrl',
        controller
    };
};
