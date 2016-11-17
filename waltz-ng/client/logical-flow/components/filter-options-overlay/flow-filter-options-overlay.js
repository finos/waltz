import _ from "lodash";


const bindings = {
    visible: '<',
    onChange: '<',
    types: '<' // [ dataTypeId... ]
};


const initialState = {
    selectedType: 'ALL',
    selectedScope: 'ALL',
    visible: false,
    onChange: () => console.log('No change handler registered for flow-filter-options-overlay::onChange')
};


function controller() {
    const vm = _.defaults(this, initialState);

    vm.$onChanges = vm.notifyChanges;

    vm.notifyChanges = () => {
        const options = {
            type: vm.selectedType || 'ALL',
            scope: vm.selectedScope || 'ALL'
        };
        vm.onChange(options);
    };

    // -- BOOT ---

    vm.notifyChanges();

}


controller.$inject = [];


const component = {
    controller,
    bindings,
    template: require('./flow-filter-options-overlay.html')
};


export default component;