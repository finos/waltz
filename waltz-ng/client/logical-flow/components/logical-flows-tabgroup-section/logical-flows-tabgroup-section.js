import _ from 'lodash';


const bindings = {
    ratings: '<',
    flowData: '<',
    applications: '<',
    onLoadDetail: '<'
};


const initialState = {
    export: () => console.log('lfts: default do-nothing export function'),
    ratings: [],
    flowData: null,
    applications: [],
    onLoadDetail: () => console.log('onLoadDetail not provided to logical flows tabgroup section'),
    visibility: {
        exportButton: false,
        sourcesOverlay: false
    }
};


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    vm.tabChanged = (name, index) => {
        vm.visibility.flowConfigButton = index > 0;
        vm.visibility.exportButton = index == 2;
        if(index === 0) vm.visibility.flowConfigOverlay = false;
    };

    vm.tableInitialised = (cfg) =>
        vm.export = () => cfg.exportFn('logical-flows.csv');

}


controller.$inject = [
];


const component = {
    controller,
    bindings,
    template: require('./logical-flows-tabgroup-section.html')
};


export default component;