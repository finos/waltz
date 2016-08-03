import _ from "lodash";
import {resetData} from "../common";

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

    const vm = resetData(this, initData);
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


    function resetValueData() {
        const clearData = resetData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
    }

    vm.onSelectPerson = (person) => {
        resetValueData();
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
            .findStatTallies([statId], selector)
            .then(summaries => vm.statistic.summary = summaries[0])
            .then(() => {
                const related = [
                    vm.relatedDefinitions.parent,
                    ...vm.relatedDefinitions.siblings,
                    ...vm.relatedDefinitions.children ];

                const relatedIds = _.chain(related)
                    .filter(s => s != null)
                    .map('id')
                    .value();

                return entityStatisticStore.findStatTallies(relatedIds, selector);
            })
            .then(summaries => vm.summaries = summaries);


        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.statistic.values = stats);

        personStore
            .findDirects(person.employeeId)
            .then(directs => vm.directs = directs);

        personStore
            .findManagers(person.employeeId)
            .then(managers => vm.managers = managers);

        personStore
            .findDirects(person.managerEmployeeId)
            .then(peers => _.reject(peers, p => p.id === person.id))
            .then(peers => vm.peers = peers);

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