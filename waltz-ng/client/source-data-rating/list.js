const initData = {
    ratings: []
};

function controller($scope, sourceDataRatingStore) {
    const vm = Object.assign(this, initData);

    sourceDataRatingStore
        .findAll()
        .then(ratings => vm.ratings = ratings);
}

controller.$inject = [
    '$scope', 'SourceDataRatingStore'
];


export default {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};
