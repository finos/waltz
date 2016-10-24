import {initialiseData} from '../common';
import {green, grey} from '../common/colors';


const template = require('./physical-specification-view.html');


const initialState = {
    visibility: {
        createReportOverlay: false,
        createReportButton: true,
        createReportBusy: false
    },
    createReportForm: {
        name: ""
    },
    selectedFlow: null
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


function controller($state,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    orgUnitStore,
                    physicalFlowLineageStore,
                    physicalSpecificationStore,
                    physicalFlowStore)
{
    const vm = initialiseData(this, initialState);

    const specId = $stateParams.id;
    const ref = {
        kind: 'PHYSICAL_SPECIFICATION',
        id: specId
    };

    // -- LOAD ---

    physicalSpecificationStore
        .getById(specId)
        .then(spec => vm.specification = spec)
        .then(spec => applicationStore.getById(spec.owningEntity.id))
        .then(app => vm.owningEntity = app)
        .then(app => orgUnitStore.getById(app.organisationalUnitId))
        .then(ou => vm.organisationalUnit = ou)
        .then(() => vm.graphTweakers = setupGraphTweakers(vm.owningEntity));

    physicalFlowStore
        .findBySpecificationId(specId)
        .then(physicalFlows => vm.physicalFlows = physicalFlows);


    bookmarkStore
        .findByParent(ref)
        .then(bs => vm.bookmarks = bs);



    vm.onFlowSelect = (flow) => {
        vm.selectedFlow = {
            flow,
            mentions: [],
            lineage: []
        };

        physicalFlowLineageStore
            .findByPhysicalFlowId(flow.id)
            .then(lineage => {
                const lineageFlows =_.map(
                    lineage,
                    (x) => {
                        return {
                            id: x.flow.id,
                            source: x.sourceEntity,
                            target: x.targetEntity
                        };
                    });

                const lineageEntities = _.chain(lineage)
                    .flatMap(
                        x => [
                            vm.owningEntity,
                            x.sourceEntity,
                            x.targetEntity])
                    .uniqBy('id')
                    .value();

                vm.selectedFlow = Object.assign(
                    {},
                    vm.selectedFlow,
                    {
                        lineage,
                        lineageFlows,
                        lineageEntities
                    });
            });

        physicalFlowLineageStore
            .findContributionsByPhysicalFlowId(flow.id)
            .then(mentions => vm.selectedFlow = Object.assign({}, vm.selectedFlow, { mentions }))
    };

}


controller.$inject = [
    '$state',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'OrgUnitStore',
    'PhysicalFlowLineageStore',
    'PhysicalSpecificationStore',
    'PhysicalFlowStore'
];


export default {
    template,
    controller,
    controllerAs: 'ctrl'
};