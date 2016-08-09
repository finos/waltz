import {initialiseData} from "../common";

const initialState = {
    tallies: [],
    kinds: [
        'CAPABILITY',
        'CHANGE_INITIATIVE',
        'DATA_TYPE',
        'ENTITY_STATISTIC',
        'ORG_UNIT',
        'PROCESS'
    ]
};

function controller(hierarchiesStore, notification) {

    const vm = initialiseData(this, initialState);


    const loadTallies = () => {
        hierarchiesStore
            .findTallies()
            .then(ts => vm.tallies = ts);
    };

    vm.build = (kind) => {
        console.log('rebuild')
        hierarchiesStore
            .buildForKind(kind)
            .then((count) => notification.success(`Hierarchy rebuilt for ${kind} with ${count} records`))
            .then(loadTallies);
    };

    loadTallies();
}


controller.$inject = [
    'HierarchiesStore',
    'Notification'
];


export default {
    template: require('./hierarchies-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


