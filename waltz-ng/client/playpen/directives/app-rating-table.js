import _ from 'lodash';
import d3 from 'd3';

import { perhaps } from '../../common';


const BINDINGS = {
    applications: '=',
    ratings: '=',
    measurable: '=',
    capabilities: '='
};


function controller($scope) {

    const vm = this;

    $scope.$watch('ctrl.ratings', (ratings => {
        if (!ratings) return;
        vm.ratingMap = d3.nest()
                .key(d => d.measurable.code)
                .key(d => d.parent.id)
                .key(d => d.capability.id)
                .map(ratings);
    }));

    vm.lookupCell = (appId, capId) => {
        if (! vm.ratingMap) { return ''; }
        return perhaps(() => vm.ratingMap['EQ'][appId][capId][0].ragRating, '');
    };

}

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./app-rating-table.html')
});
