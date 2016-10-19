import {initialiseData} from '../common'
import {green, grey} from '../common/colors';


const template = require('./lineage-view.html');


const initialState = {
    report: null
};


function setupGraphTweakers(application) {
    return {
        node: {
            update: (selection) => {
                selection
                    .select('circle')
                    .attr({
                        'fill': d => d.id === application.id
                            ? green
                            : grey,
                        'stroke': d => d.id === application.id
                            ? green.darker()
                            : grey.darker(),
                        'r': d => d.id === application.id
                            ? 15
                            : 10
                    });
            },
            exit: () => {},
            enter: () => {}
        }
    };
}


function controller($stateParams,
                    appStore,
                    bookmarkStore,
                    lineageReportStore,
                    logicalFlowStore,
                    orgUnitStore,
                    physicalSpecificationStore,
                    physicalFlowStore) {

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
        .then(report => vm.report = report)
        .then(report => physicalSpecificationStore.getById(report.specificationId))
        .then(specification => vm.specification = specification)
        .then(specification => appStore.getById(specification.owningApplicationId))
        .then(app => vm.application = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou)
        .then(() => vm.graphTweakers = setupGraphTweakers(vm.application));

    appStore
        .findBySelector(selectorOptions)
        .then(apps => vm.apps = apps);

    physicalSpecificationStore
        .findBySelector(selectorOptions)
        .then(specifications => vm.specifications = specifications);

    physicalFlowStore
        .findBySelector(selectorOptions)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);

    logicalFlowStore
        .findBySelector(Object.assign({}, selectorOptions, { desiredKind: 'LOGICAL_DATA_FLOW' }))
        .then(logicalFlows => vm.logicalFlows = logicalFlows);

    bookmarkStore
        .findByParent(reportRef)
        .then(bs => vm.bookmarks = bs);

}


controller.$inject = [
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'LineageReportStore',
    'DataFlowDataStore', // LogicalFlowStore
    'OrgUnitStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};


export default view;