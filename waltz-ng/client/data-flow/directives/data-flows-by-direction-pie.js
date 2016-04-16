
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

import {flowDirectionColorScale} from "../../common/colors";


const BINDINGS = {
    applications: '=',
    flows: '=',
    size: '='
};


function calcAppConnectionPieStats(flows, apps) {
    const logicalFlows = flows;

    const orgMemberAppIds = _.map(apps, 'id');

    return _.chain(logicalFlows)
        .uniq(false, f => f.source.id + '.' + f.target.id)
        .map(f => {
            const sourceIsMember = _.includes(orgMemberAppIds, f.source.id);
            const targetIsMember = _.includes(orgMemberAppIds, f.target.id);
            if (sourceIsMember && targetIsMember) return 'INTRA';
            if (sourceIsMember) return 'INBOUND';
            if (targetIsMember) return 'OUTBOUND';
            return 'UNKNOWN';
        })
        .countBy()
        .map((v, k) => ({ key: k, count: v }))
        .value();
}

const config = {
    colorProvider: (d) => flowDirectionColorScale(d.data.key),
    size: 80
};


function controller($scope) {
    const vm = this;

    vm.config = config;
    vm.data = [];

    $scope.$watch('ctrl.size', sz => vm.config.size = sz ? sz : 80);

    $scope.$watchGroup(['ctrl.applications', 'ctrl.flows'], ([apps, flows]) => {
        if (!apps || !flows) return;
        vm.data = calcAppConnectionPieStats(flows, apps);
    });

}

controller.$inject = ['$scope'];


export default () => {
    return {
        restrict: 'E',
        replace: true,
        template: require('./data-flows-by-direction-pie.html'),
        scope: {},
        bindToController: BINDINGS,
        controllerAs: 'ctrl',
        controller
    };
};
