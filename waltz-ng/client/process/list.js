
const initialState = {
    processes: []
};

function controller(processStore) {
    const vm = Object.assign(this, initialState);

    processStore
        .findAll()
        .then(ps => vm.processes = ps);

}


controller.$inject = [
    'ProcessStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;