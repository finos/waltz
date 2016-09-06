import _ from "lodash";
import d3 from "d3";


const bindings = {
    apps: '<',
    entityReference: '<',
    flowData: '<',
    onLoadDetail: '<'
};


const template = require('./rated-flow-summary-panel.html');


const initialState = {
    apps: [],
    infoCell : null
};


function findMatchingDecorators(selection, decorators = []) {
    const matcher = {
        decoratorEntity: {
            kind: 'DATA_TYPE',
            id: selection.dataType.id
        },
        rating: selection.rating
    };

    return _.filter(decorators, matcher);
}


function findMatchingFlows(flows = [], decorators = []) {
    const flowIds = _.chain(decorators)
        .map('dataFlowId')
        .uniq()
        .value();

    return _.filter(
        flows,
        f => _.includes(flowIds, f.id));
}


function findConsumingApps(flows = [], apps = []) {
    const appsById = _.keyBy(apps, 'id');
    return _.chain(flows)
        .filter(f => f.target.kind === 'APPLICATION')
        .map('target.id')
        .uniq()
        .map(id => appsById[id])
        .value();
}


/**
 * Only interested in flows coming into this group
 * @param flows
 * @param apps
 * @returns {Array}
 */
function findPotentialFlows(flows = [], apps =[]) {
    const appIds = _.map(apps, 'id');
    return _.filter(flows, f => _.includes(appIds, f.target.id));
}


function mkInfoCell(selection, flowData, apps = []) {
    if (! selection) return null;
    if (flowData.flows.length == 0) return null;

    const potentialFlows = findPotentialFlows(flowData.flows, apps);

    const matchingDecorators = findMatchingDecorators(selection, flowData.decorators);
    const matchingFlows = findMatchingFlows(potentialFlows, matchingDecorators )
    const consumingApps = findConsumingApps(matchingFlows, apps);

    return { dataType: selection.dataType, rating: selection.rating, applications: consumingApps };
}


function controller(dataTypeService, dataFlowDecoratorStore) {
    const vm = _.defaultsDeep(this, initialState);

    const childSelector = {
        entityReference: vm.entityReference,
        scope: 'CHILDREN'
    };

    const exactSelector = {
        entityReference: vm.entityReference,
        scope: 'EXACT'
    };

    dataFlowDecoratorStore
        .summarizeBySelector(childSelector)
        .then(r => vm.childSummaries = r);

    dataFlowDecoratorStore
        .summarizeBySelector(exactSelector)
        .then(r => vm.exactSummaries = r);

    dataTypeService
        .loadDataTypes()
        .then(dts => vm.dataTypes = dts);

    vm.onTableClick = (clickData) => {
        if (clickData.type === 'CELL') {
            vm.onLoadDetail()
                .then(() => vm.infoPanel = mkInfoCell(clickData, vm.flowData, vm.apps));
        } else {
            console.log('rated-flow-summary-panel: unsupported selection', clickData);
        }
    };

}


controller.$inject = [
    'DataTypeService',
    'DataFlowDecoratorStore',
    'DataFlowDataStore'
];


const component = {
    template,
    controller,
    bindings
};


export default component;