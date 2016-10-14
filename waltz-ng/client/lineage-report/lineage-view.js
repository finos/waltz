import _ from 'lodash';
import {initialiseData} from '../common'


const template = require('./lineage-view.html');


const initialState = {
    report: null
};


function process(xs = []) {
    return {
        all: xs,
        byId: _.keyBy(xs, 'id')
    };
}


function controller($stateParams,
                    appStore,
                    lineageReportStore,
                    logicalFlowStore,
                    physicalDataArticleStore,
                    physicalDataFlowStore) {

    const vm = initialiseData(this, initialState);
    const reportId = $stateParams.id;

    const reportRef = {
        kind: 'LINEAGE_REPORT',
        id: reportId
    };

    const selectorOptions = {
        entityReference: reportRef,
        scope: 'EXACT'
    };

    lineageReportStore
        .getById(reportId)
        .then(r => vm.report = r);

    appStore
        .findBySelector(selectorOptions)
        .then(apps => Object.assign(vm, { apps: process(apps) }));

    physicalDataArticleStore
        .findBySelector(selectorOptions)
        .then(articles => Object.assign(vm, { articles: process(articles) }));

    physicalDataFlowStore
        .findBySelector(selectorOptions)
        .then(physicalFlows => Object.assign(vm, { physicalFlows: process(physicalFlows) }));

    logicalFlowStore
        .findBySelector(Object.assign({}, selectorOptions, { desiredKind: 'LOGICAL_DATA_FLOW' }))
        .then(x => {
            console.log(x);
            return x;
        })
        .then(logicalFlows => Object.assign(vm, { logicalFlows: process(logicalFlows) }));

}



controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'LineageReportStore',
    'DataFlowDataStore', // LogicalFlowStore
    'PhysicalDataArticleStore',
    'PhysicalDataFlowStore'
];

const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;