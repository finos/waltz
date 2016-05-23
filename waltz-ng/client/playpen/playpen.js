const initData = {
    list: []
};


function controller($scope, changeInitiativeStore) {
    const vm = Object.assign(this, initData);

    changeInitiativeStore
        .findByRef("APP_GROUP", 2)
        .then(list => vm.list = list);
}

controller.$inject = [
    '$scope', 'ChangeInitiativeStore'
];


export default {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};