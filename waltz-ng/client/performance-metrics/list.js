
const initialState = {
    definitions: []
};

function controller(definitionStore) {
    const vm = Object.assign(this, initialState);

    definitionStore
        .findAll()
        .then(ds => vm.definitions = ds);
    
}


controller.$inject = [
    'PerformanceMetricDefinitionStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;