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

import numeral from "numeral";
import EventDispatcher from "../../common/EventDispatcher";


const BINDINGS = {
    data: '=',
    onBucketSelect: '=?',
    csvName: '@?'
};


const rangeBandToBucket = (band, idx) => {
    const name = '€ '
        + numeral(band.id.low).format('0a')
        + " - € "
        + numeral(band.id.high).format('0a');

    return {
        min: band.id.low,
        max: band.id.high,
        idx: idx,
        size: band.count,
        name: name
    };
};



function controller($scope) {

    const vm = this;

    vm.eventDispatcher = new EventDispatcher();

    $scope.$watch(
        'ctrl.data.stats',
        assetCostStats => {
            if (! assetCostStats) return;
            vm.assetCostBuckets = _.map(assetCostStats.costBandCounts, rangeBandToBucket);
        });
}

controller.$inject = [
    '$scope'
];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./asset-costs-section.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
