import _ from "lodash";


const initialState = {
    definitions: [],
    definitionsById: {},
    packs: [],
    selectedPack: null,
    loadingPack: false
};


function processPack(p) {
    const items = _.map(p.items, item => {
        const goalsByCheckpointId = _.keyBy(item.goals, 'checkpointId');
        return { ...item, goalsByCheckpointId }
    });
    p.sections = _.groupBy(items, 'sectionName');
    return p;
}


function toPrettyBaseline(item) {
    return `${item.baseLine}%`;
}


function toPrettyGoal(goal) {
    if (! goal) return '-';

    switch (goal.goalType) {
        case 'PERCENT_INCREASE':
            return `${goal.value}% increase`;
        case 'PERCENT_DECREASE':
            return `${goal.value}% decrease`;
        case 'BELOW_THRESHOLD':
            return `Below ${goal.value}%`;
        case 'ABOVE_THRESHOLD':
            return `Above ${goal.value}%`;
        default:
            console.log("No pretty print registered for goal type: ", goal.type);
            return `${goal.type} ${goal.value}`;
    }
}


function controller(performanceMetricDefinitionStore,
                    performanceMetricPackStore) {

    const vm = Object.assign(this, initialState);

    // -- LOAD

    performanceMetricDefinitionStore
        .findAll()
        .then(defns => vm.definitions = defns)
        .then(defns => vm.definitionsById = _.keyBy(defns, 'id'));

    performanceMetricPackStore
        .findAllReferences()
        .then(packs => vm.packs = packs);


    // -- INTERACT

    vm.packSelected = (packRef) => {
        vm.loadingPack = true;
        performanceMetricPackStore
            .getById(packRef.id)
            .then(p => vm.selectedPack = processPack(p))
            .then(() => vm.loadingPack = false);
    };


    vm.toPrettyGoal = toPrettyGoal;
    vm.toPrettyBaseline = toPrettyBaseline;
    vm.cellClass = (item, goal) => {
        return goal ? "success" : "warning";
    };

}


controller.$inject = [
    'PerformanceMetricDefinitionStore',
    'PerformanceMetricPackStore'
];


const view = {
    template: require('./list.html'),
    controller,
    controllerAs: 'ctrl'
};


export default view;