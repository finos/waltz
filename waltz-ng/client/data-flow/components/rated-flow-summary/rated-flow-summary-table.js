import d3 from "d3";
import _ from "lodash";


const bindings = {
    ratedFlows: '<',
    apps: '<',
    orgUnitId: '<',
    onClick: '<'
};


const template = require('./rated-flow-summary-table.html');


const initialState = {
    apps: [],
    ratedFlows: [],
    maxBucketSizes: {},
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


function calculateMaxBucketSizes(flows = []) {
    return _.countBy(flows, f => f.dataFlow.dataType);
}


function prepareData(flows = [], apps = [], orgUnitId) {

    const summary = summariseByTypeThenRating(flows, apps, orgUnitId);
    const maxBucketSizes = calculateMaxBucketSizes(flows);
    const totals = d3
        .nest()
        .key(f => f.dataFlow.dataType)
        .rollup(fs => fs.length)
        .map(flows);

    return {
        summary,
        maxBucketSizes,
        totals
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


const component = {
    template,
    controller,
    bindings
};


export default component;