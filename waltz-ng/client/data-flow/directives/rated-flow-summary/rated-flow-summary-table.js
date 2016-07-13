import d3 from "d3";
import _ from "lodash";


const BINDINGS = {
    ratedFlows: '<',
    apps: '<',
    orgUnitId: '<',
    onClick: '<'
};


const initialState = {
    apps: [],
    ratedFlows: [],
    largestBucket: 0,
    onClick: (data) => console.log('no on-click handler supplied to rated-flow-summary-table', data)
};


function isDirectlyInvolvedFlow(appIds = [], flow) {
    return _.includes(appIds, flow.dataFlow.source.id)
        || _.includes(appIds, flow.dataFlow.target.id);
}


function summariseByTypeThenRating(flows = [], apps = [], orgUnitId) {

    const directlyInvolvedAppIds = _.chain(apps)
        .filter(a => a.organisationalUnitId === orgUnitId)
        .map('id')
        .value();

    return d3
        .nest()
        .key(d => d.dataFlow.dataType)
        .sortKeys(d3.ascending)
        .key(d => d.rating)
        .rollup(xs => {
            const all = xs.length;
            const direct = _.filter(xs, f => isDirectlyInvolvedFlow(directlyInvolvedAppIds, f)).length;
            const indirect = all - direct;

            return {
                all,
                direct,
                indirect,
                stack: [direct, indirect]
            };
        })
        .map(flows);
}


function findLargestBucket(summary) {
    return _.chain(summary)
        .flatMap(x => _.map(x))    // get all the 'leaf' values of the nested obj structure
        .map('all')                // focus on 'all' attribute
        .max()                     // find the biggest
        .value();
}


function prepareData(flows = [], apps = [], orgUnitId) {

    const summary = summariseByTypeThenRating(flows, apps, orgUnitId);
    const largestBucket = findLargestBucket(summary);

    return {
        largestBucket,
        summary
    };
}


function controller() {
    const vm = _.defaultsDeep(this, initialState);
    const refresh = () => {
        const data = prepareData(vm.ratedFlows, vm.apps, vm.orgUnitId);
        Object.assign(vm, data);
    };

    vm.$onChanges = refresh;

    const invokeClick = ($event, d) => {
        vm.onClick(d);
        if ($event) $event.stopPropagation();
    };

    vm.columnClick = ($event, rating) => invokeClick($event, { type: 'COLUMN', rating });
    vm.rowClick = ($event, dataType) => invokeClick($event, { type: 'ROW', dataType });
    vm.cellClick = ($event, dataType, rating) => invokeClick($event, { type: 'CELL', dataType, rating });
}


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./rated-flow-summary-table.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: BINDINGS,
    scope: {}
};


export default () => directive;