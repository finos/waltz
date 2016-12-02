const bindings = {
    ratings: '<',
    entities: '<',
    visible: '<',
    onDismiss: '<'
};


const template = require('./source-data-overlay.html');


function filterRatings(ratings,
                       entities = []) {
    return entities.length == 0
        ? ratings
        : _.filter(ratings, r => _.includes(entities, r.entityKind));
}


function controller($scope) {

    const vm = this;
    vm.filteredRatings = [];

    vm.$onChanges = () => {
        if(!vm.ratings || !vm.entities) return;
        vm.filteredRatings = filterRatings(vm.ratings, vm.entities);
    };

}


controller.$inject = ['$scope'];


const component = {
    bindings,
    template,
    controller
};


export default component;