import _ from "lodash";
import {isEmpty} from "../../common";


const BINDINGS = {
    ratings: '=',
    entities: '=',
    visible: '='
};

function filterRatings(ratings,
                       entities = []) {
    return isEmpty(entities)
        ? ratings
        : _.filter(ratings, r => _.includes(entities, r.entityKind));
}


function controller($scope) {

    const vm = this;
    vm.filteredRatings = [];

    $scope.$watchGroup(
        ['ctrl.ratings', 'ctrl.entities'],
        ([ratings, entities = []]) => {
            if(!ratings) return;
            vm.filteredRatings = filterRatings(ratings, entities);
        }
    );

}

controller.$inject = ['$scope'];


export default () => ({
    replace: true,
    restrict: 'E',
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./source-data-overlay.html')
});