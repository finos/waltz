const initData = {
    results: []
};


const BINDINGS = {
    model: '='
};


function controller(changeInitiativeStore) {
    const vm = Object.assign(this, initData);

    vm.refresh = (query) => {
        if (!query) return;
        return changeInitiativeStore
            .search(query)
            .then((results) => vm.results = results);
    };

}


controller.$inject = ['ChangeInitiativeStore'];


const directive = {
    restrict: 'E',
    replace: true,
    template: require('./change-initiative-selector.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    scope: {}
};


export default () => directive;