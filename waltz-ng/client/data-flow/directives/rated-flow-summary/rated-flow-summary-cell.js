import _ from "lodash";


const BINDINGS = {
    scores: '<',
    type: '<',
    largestBucket: '<',
    onClick: '&'
};


const initialState = {
    onClick: () => console.log("No on-click handler passed to rating-flow-summary-cell")
};


function controller() {

    const vm = _.defaultsDeep(this, initialState)

    vm.cellClick = ($event) => {
        vm.onClick();
        $event.stopPropagation();
    };
}


const directive = {
    restrict: 'E',
    replace: false,
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl',
    scope: {},
    template: require('./rated-flow-summary-cell.html')
};


export default () => directive;