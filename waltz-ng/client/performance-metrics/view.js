const initialState = {
    performanceMetric: null,
    visibility: {}
};




function controller($scope,
                    $stateParams,
                    performanceMetricDefinitionStore ) {

    // -- INITIALISE

    const definitionId = $stateParams.id;

    const selectorOptions = {
        entityReference : {
            id: definitionId,
            kind: 'PROCESS'
        },
        scope: 'EXACT'
    };

    const vm = Object.assign(this, initialState);

    // -- LOADING

    performanceMetricDefinitionStore
        .getById(definitionId)
        .then(d => vm.performanceMetric = d);


    // --  WATCHERS


    // -- INTERACTIONS

}


controller.$inject = [
    '$scope',
    '$stateParams',
    'PerformanceMetricDefinitionStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;

