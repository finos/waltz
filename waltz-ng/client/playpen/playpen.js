const initData = {
    list: [],
    model: null
};


function controller($scope,
                    appGroupStore,
                    changeInitiativeStore) {
    const vm = Object.assign(this, initData);

    vm.refresh = (query) => {
        if (!query) return;
        return changeInitiativeStore
            .search(query)
            .then((results) => this.results = results);
    };


    $scope.$watch(
        'ctrl.model',
        (model) => {
            if (!model) return;

            appGroupStore
                .addChangeInitiative(2, model.id)
                .then(cis => vm.list = cis);

        });

    vm.remove = (id) => appGroupStore
        .removeChangeInitiative(2, id)
        .then(cis => vm.list = cis);

}

controller.$inject = [
    '$scope',
    'AppGroupStore',
    'ChangeInitiativeStore'
];


export default {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};