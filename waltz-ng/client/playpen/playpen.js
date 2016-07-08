
function controller($stateParams, appStore) {

    const vm = this;

    vm.message = 'Hello World';
    vm.appId = $stateParams.id;

    console.log('as', appStore)


    const promise = appStore
        .getById(vm.appId)
        .then(app => vm.app = app);


    console.log(promise)
    global.vm = vm;
}


controller.$inject = [
    '$stateParams',
    'ApplicationStore'
];


const view = {
    template: require('./playpen.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;