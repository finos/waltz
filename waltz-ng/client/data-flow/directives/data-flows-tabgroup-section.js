const BINDINGS = {
    ratings: '<',
    flowData: '<',
    applications: '<',
    onLoadDetail: '<',
    flowDecorators: '<'
};


const initialState = {
    ratings: [],
    flowData: null,
    applications: [],
    onLoadDetail: () => console.log('onLoadDetail not provided to data flows tabgroup section'),
    visibility: {
        flowConfigOverlay: false,
        flowConfigButton: false,
        sourcesOverlay: false
    }
};


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.tabChanged = (name, index) => {
        vm.visibility.flowConfigButton = index > 0;
        if(index === 0) vm.visibility.flowConfigOverlay = false;
    }

}


controller.$inject = [
];


const directive = {
    restrict: 'E',
    replace: true,
    controller,
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    scope: {},
    template: require('./data-flows-tabgroup-section.html')
};


export default () => directive;