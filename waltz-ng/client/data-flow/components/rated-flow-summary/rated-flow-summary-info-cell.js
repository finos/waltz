import _ from "lodash";


const BINDINGS = {
    applications: '<',
    type: '<',
    flows: '<',
    orgUnitId: '<',
    rating: '<'
};


const initialState = {
    shouldShowIndirectApps: false
};


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.toggleIndirectApps = () => {
        vm.shouldShowIndirectApps = ! vm.shouldShowIndirectApps;
    };

    const refresh = () => {

        const appsById = _.keyBy(vm.applications, 'id');

        const targetApps = _.chain(vm.flows)
            .map('target.id')
            .uniq()
            .map(id => appsById[id])
            .value();

        const [directApps, indirectApps] = _.partition(
            targetApps,
            app => app.organisationalUnitId === vm.orgUnitId);

        vm.sourceApps = vm.shouldShowIndirectApps
            ? targetApps
            : directApps

    };

    vm.$onChanges = refresh;

    vm.toggleIndirectApps = () => {
        vm.shouldShowIndirectApps = ! vm.shouldShowIndirectApps;
        refresh();
    };

}


const component = {
    template: require('./rated-flow-summary-info-cell.html'),
    controller,
    bindings: BINDINGS
};


export default component;