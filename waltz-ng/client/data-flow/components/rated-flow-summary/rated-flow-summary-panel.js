import _ from "lodash";
import d3 from "d3";


const bindings = {
    ratedFlows: '<',
    apps: '<',
    orgUnitId: '<'
};


const template = require('./rated-flow-summary-panel.html');

const initialState = {
    apps: [],
    ratedFlows: [],
    shouldShowIndirectApps: false
};


function isDirectlyInvolvedFlow(appIds = [], flow) {
    return _.includes(appIds, flow.dataFlow.source.id)
        || _.includes(appIds, flow.dataFlow.target.id);
}


function findDirectlyInvolvedFlows(flows = [], apps = [], orgUnitId) {
    const directlyInvolvedAppIds = _.chain(apps)
        .filter(a => a.organisationalUnitId === orgUnitId)
        .map('id')
        .value();

    return _.filter(flows, f => isDirectlyInvolvedFlow(directlyInvolvedAppIds, f));
}


function groupByTypeThenRating(flows = []) {
    return d3
        .nest()
        .key(d => d.dataFlow.dataType)
        .sortKeys(d3.ascending)
        .key(d => d.rating)
        .rollup(xs => _.map(xs, 'dataFlow'))
        .map(flows);
}


function prepareData(flows = [], apps = [], orgUnitId) {

    const directlyInvolvedFlows = findDirectlyInvolvedFlows(flows, apps, orgUnitId);
    const direct = groupByTypeThenRating(directlyInvolvedFlows);
    const all = groupByTypeThenRating(flows);

    return {
        all,
        direct
    };
}


function controller() {
    const vm = _.defaultsDeep(this, initialState);

    const refresh = () => Object.assign(
            vm,
            prepareData(
                vm.ratedFlows,
                vm.apps,
                vm.orgUnitId));

    vm.$onChanges = refresh;

    vm.onTableClick = (d) => {
        const { dataType, rating, type } = d;


        if (type === 'CELL') {
            const flows = vm.all[dataType][rating];
            vm.selectedBucket =  {
                type,
                rating,
                dataType,
                flows,
                applications: vm.apps
            };
        }
        else {
            console.log('rated-flow-summary-panel: unsupported selection', d);
        }
    };

}


const component = {
    template,
    controller,
    bindings
};


export default component;