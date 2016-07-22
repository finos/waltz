import _ from "lodash";


const initData = {
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    summaries: [],
    directs: [],
    managers: [],
    peers: [],
    person: null
};


const template = require('./person-entity-statistic-view.html');


function updateUrlWithoutReload($state, person) {
    $state.go('.', {id: person.id}, {notify: false});
}


function controller($q,
                    $state,
                    $stateParams,
                    entityStatisticStore,
                    personStore) {

    const vm = Object.assign(this, initData);
    const statId = $stateParams.statId;
    const personId = $stateParams.id;

    const personPromise = personStore
        .getById(personId);


    const definitionPromise = entityStatisticStore
        .findRelatedStatDefinitions(statId)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self);


    $q.all([personPromise, definitionPromise])
        .then(([person, definitions]) => vm.onSelectPerson(person));

    vm.onSelectPerson = (person) => {
        vm.person = person;

        const entityReference = {
            id: person.id,
            kind: 'PERSON'
        };
        vm.parentRef = entityReference;

        const selector = {
            scope: 'CHILDREN',
            entityReference
        };

        entityStatisticStore
            .findStatTallies(vm.relatedDefinitions, selector)
            .then(summaries => vm.summaries = summaries)
            .then(summaries => vm.statistic.summary = _.find(summaries, { entityReference: { id: statId }}));

        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.statistic.values = stats);

        personStore
            .findDirects(person.employeeId)
            .then(directs => vm.directs = directs);

        personStore
            .findManagers(person.employeeId)
            .then(managers => vm.managers = managers);

        // personStore
        //     .findPeers(person.employeeId)
        //     .then(peers => vm.peers = peers);

        updateUrlWithoutReload($state, person);
    };

}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'EntityStatisticStore',
    'PersonStore'
];


const page = {
    controller,
    template,
    controllerAs: 'ctrl'
};


export default page;


// http://localhost:8000/#/entity-statistic/PERSON/mwpHhjMzq/34