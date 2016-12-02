import {initialiseData} from "../common";

const template = require('./actor-view.html');


const initialState = {
    logs: [],
    physicalFlows: [],
    physicalSpecifications: [],
    physicalFlowsUnusedSpecificationsCount: 0,
    physicalFlowsProducesCount: 0,
    physicalFlowsConsumesCount: 0
};


function mkHistoryObj(actor) {
    return {
        name: actor.name,
        kind: 'ACTOR',
        state: 'main.actor.view',
        stateParams: { id: actor.id }
    };
}


function addToHistory(historyStore, actor) {
    if (! actor) { return; }

    const historyObj = mkHistoryObj(actor);

    historyStore.put(
        historyObj.name,
        historyObj.kind,
        historyObj.state,
        historyObj.stateParams);
}


function controller($stateParams,
                    actorStore,
                    bookmarkStore,
                    changeLogStore,
                    historyStore,
                    physicalFlowStore,
                    physicalSpecificationStore,
                    sourceDataRatingStore) {

    const vm = initialiseData(this, initialState);

    const id = $stateParams.id;
    const entityRef = { kind: 'ACTOR', id };
    Object.assign(vm, { id, entityRef });

    actorStore
        .getById(id)
        .then(a => vm.actor = a)
        .then(() => addToHistory(historyStore, vm.actor));

    physicalFlowStore
        .findByEntityReference(entityRef)
        .then(flows => vm.physicalFlows = flows);

    physicalSpecificationStore
        .findByEntityReference(entityRef)
        .then(specs => vm.physicalSpecifications = specs);

    bookmarkStore.findByParent(entityRef)
        .then(bookmarks => vm.bookmarks = bookmarks);

    sourceDataRatingStore
        .findAll()
        .then(sdrs => vm.sourceDataRatings = sdrs);

    changeLogStore
        .findByEntityReference('ACTOR', id)
        .then(log => vm.log = log);

    vm.onPhysicalFlowsInitialise = (e) => {
        vm.physicalFlowProducesExportFn = e.exportProducesFn;
        vm.physicalFlowConsumesExportFn = e.exportConsumesFn;
        vm.physicalFlowUnusedExportFn = e.exportUnusedSpecificationsFn;
    };

    vm.onPhysicalFlowsChange = (e) => {
        vm.physicalFlowsProducesCount = e.producesCount;
        vm.physicalFlowsConsumesCount = e.consumesCount;
        vm.physicalFlowsUnusedCount = e.unusedSpecificationsCount;
    };

    vm.exportPhysicalFlowProduces = () => {
        vm.physicalFlowProducesExportFn();
    };

    vm.exportPhysicalFlowConsumes = () => {
        vm.physicalFlowConsumesExportFn();
    };

    vm.exportPhysicalFlowUnused = () => {
        vm.physicalFlowUnusedSpecificationsExportFn();
    };


}


controller.$inject = [
    '$stateParams',
    'ActorStore',
    'BookmarkStore',
    'ChangeLogStore',
    'HistoryStore',
    'PhysicalFlowStore',
    'PhysicalSpecificationStore',
    'SourceDataRatingStore'
];


const view = {
    template,
    controller,
    controllerAs: 'ctrl'
};

export default view;