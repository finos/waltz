import _ from "lodash";


const BINDINGS = {
    scores: '<',
    type: '<',
    largestBucket: '<',
    onClick: '&'
};


const initialState = {
    popoverText: '...',
    onClick: () => console.log("No on-click handler passed to rating-flow-summary-cell")
};


function controller() {

    const vm = _.defaultsDeep(this, initialState)

    vm.cellClick = ($event) => {
        vm.onClick();
        $event.stopPropagation();
    };

    vm.$onChanges = () => {
        if (vm.scores && vm.type) {
            vm.popoverText = `${vm.type}:: direct: ${vm.scores.direct}, indirect: ${vm.scores.indirect}`;
        }
    };
}


const component = {
    bindings: BINDINGS,
    controller,
    template: require('./rated-flow-summary-cell.html')
};


export default component;