import _ from "lodash";
import {buildHierarchies} from "../common";


const initialState = {
    allCapabilities: [],
    appCapabilities: {
        byCapability: {},
        byApplication: {}
    },
    assetCostData: null,
    applications: [],
    bookmarks: [],
    capabilityTree: {
        options: {
            nodeChildren: "children",
            // dirSelectable: true,
            equality: (a, b) => a && b && a.id === b.id
        },
        data: []
    },
    dataFlows: [],
    dataFlowOptions: {},
    process: null,
    selectedCapability: null,
    selectedApplication: null,
    supportingCapabilities: [],
    supportingCapabilityIds: [],
    visibility: {}
};


function indexAppCapabilities(appCaps = []) {

    const byCapability = _.chain(appCaps)
            .groupBy('capabilityId')
            .mapValues(xs => _.map(xs, 'applicationId'))
            .value();

    const byApplication = _.chain(appCaps)
        .groupBy('applicationId')
        .mapValues(xs => _.map(xs, 'capabilityId'))
        .value();

    return {
        byCapability,
        byApplication
    };
}


function buildCapabilityTree(all, supporting) {

    const involvedIds = _.chain(supporting)
        .flatMap(c => ([ c.level1, c.level2, c.level3, c.level4, c.level5 ]))
        .filter(id => id != null)
        .uniq()
        .value();

    const involved = _.filter(all, c => _.includes(involvedIds, c.id));

    return buildHierarchies(involved);
}


function controller($scope,
                    $stateParams,
                    appCapabilityStore,
                    applicationStore,
                    assetCostViewService,
                    bookmarkStore,
                    capabilityStore,
                    dataFlowUtilityService,
                    dataFlowViewService,
                    processStore,
                    sourceDataRatingStore) {

    // -- INITIALISE

    const processId = $stateParams.id;

    const selectorOptions = {
        entityReference : {
            id: processId,
            kind: 'PROCESS'
        },
        scope: 'EXACT'
    };

    const vm = Object.assign(this, initialState);


    // -- LOADING

    processStore
        .getById(processId)
        .then(p => vm.process = p);

    processStore
        .findSupportingCapabilities(processId)
        .then(cs => vm.supportingCapabilities = cs)
        .then(cs => vm.supportingCapabilityIds = _.map(cs, 'id'));

    appCapabilityStore
        .findApplicationCapabilitiesByAppIdSelector(selectorOptions)
        .then(indexAppCapabilities)
        .then(indexes => vm.appCapabilities = indexes);

    applicationStore
        .findBySelector(selectorOptions)
        .then(apps => vm.applications = apps)
        .then(apps => vm.dataFlowOptions.graphTweakers = dataFlowUtilityService.buildGraphTweakers(_.map(apps, 'id')));

    bookmarkStore
        .findByParent({ id: processId, kind: 'PROCESS'})
        .then(bookmarks => vm.bookmarks = bookmarks);

    assetCostViewService
        .initialise(processId, 'PROCESS', 'EXACT', 2015)
        .then(d => vm.assetCostData = d);

    capabilityStore
        .findAll()
        .then(cs => vm.allCapabilities = cs);

    sourceDataRatingStore
        .findAll()
        .then(rs => vm.sourceDataRatings = rs);

    dataFlowViewService
        .initialise(processId, 'PROCESS', 'EXACT')
        .then(data => vm.dataFlows = data);


    // --  WATCHERS

    $scope.$watchGroup(
        ['ctrl.allCapabilities', 'ctrl.supportingCapabilities'],
        ([allCaps = [], supportingCaps = []]) => {
            vm.capabilityTree.data = buildCapabilityTree(allCaps, supportingCaps);
        });


    // -- INTERACTIONS

    vm.onAssetBucketSelect = bucket => {
        $scope.$applyAsync(() => {
            assetCostViewService.selectBucket(bucket);
            assetCostViewService
                .loadDetail()
                .then(data => vm.assetCostData = data);
        });
    };

    vm.isInferredCapability = (capabilityId) => ! _.includes(
        vm.supportingCapabilityIds,
        capabilityId);

    vm.onCapabilityNodeSelect = (node) => {
        vm.selectedCapability = node;
        vm.selectedApplication = null;
    };

    vm.countAppsForCapability = (capabilityId) => {
        const appIds = vm.appCapabilities.byCapability[capabilityId];
        return (appIds || []).length;
    };

    vm.findAppsForCapability = (capabilityId) => {
        const appIds = vm.appCapabilities.byCapability[capabilityId];
        return _.chain(vm.applications)
            .filter(app => _.includes(appIds, app.id))
            .orderBy('name')
            .value();
    };

    vm.findCapabilitiesForApp = (appId) => {
        const capIds = vm.appCapabilities.byApplication[appId];
        return _.chain(vm.allCapabilities)
            .filter(cap => _.includes(capIds, cap.id))
            .orderBy('name')
            .value();
    };

    vm.isCapabilityPartOfProcess = (capId) =>
        _.includes(vm.supportingCapabilityIds, capId);

    vm.onApplicationSelect = (app) => {
        vm.selectedApplication = app;
    };

    vm.loadFlowDetail = () => {
        dataFlowViewService.loadDetail();
    };

}


controller.$inject = [
    '$scope',
    '$stateParams',
    'AppCapabilityStore',
    'ApplicationStore',
    'AssetCostViewService',
    'BookmarkStore',
    'CapabilityStore',
    'DataFlowUtilityService',
    'DataFlowViewService',
    'ProcessStore',
    'SourceDataRatingStore'
];


const view = {
    template: require('./view.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;

