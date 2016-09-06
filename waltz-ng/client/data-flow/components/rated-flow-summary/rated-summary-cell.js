import _ from "lodash";


const BINDINGS = {
    totalCount: '<',
    directCount: '<',
    maxCount: '<',
    decorator: '<',
    rating: '<',
    onClick: '&'
};


const initialState = {
    popoverText: '...',
    onClick: () => console.log("No on-click handler passed to rating-flow-summary-cell")
};


function calcPopoverText(decorator, directCount = 0, totalCount = 0) {
    if (! decorator) return "?";
    return `${decorator.name}.  Direct: ${directCount}, indirect: ${totalCount - directCount} (total: ${totalCount})`;
}


function mkStackValues(direct = 0, total = 0) {
    return [ direct, (total - direct )];
}


function controller() {
    const vm = _.defaultsDeep(this, initialState)

    vm.cellClick = ($event) => {
        vm.onClick();
        $event.stopPropagation();
    };

    vm.$onChanges = () => {
        vm.popoverText = calcPopoverText(vm.decorator, vm.directCount, vm.totalCount);
        vm.stackValues = mkStackValues(vm.directCount, vm.totalCount)
    };
}


const component = {
    bindings: BINDINGS,
    controller,
    template: require('./rated-summary-cell.html')
};


export default component;