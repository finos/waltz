import _ from "lodash";
import {resetData} from "../common";
import {updateUrlWithoutReload, hasRelatedDefinitions} from "./utilities";


const initData = {
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    bookmarks: [],
    summaries: [],
    directs: [],
    managers: [],
    peers: [],
    person: null,
    history: [],
    visibility: {
        related: false
    }
};


const template = require('./person-entity-statistic-view.html');


function controller($q,
                    $state,
                    $stateParams,
                    applicationStore,
                    bookmarkStore,
                    entityStatisticStore,
                    personStore) {

    const vm = resetData(this, initData);
    const statId = $stateParams.statId;
    const personId = $stateParams.id;

    const personPromise = personStore
        .getById(personId);

    const statRef = {
        id: statId,
        kind: 'ENTITY_STATISTIC'
    };

    bookmarkStore
        .findByParent(statRef)
        .then(bs => vm.bookmarks = bs);

    const definitionPromise = entityStatisticStore
        .findRelatedStatDefinitions(statId)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self)
        .then(() => vm.visibility.related = hasRelatedDefinitions(vm.relatedDefinitions));


    $q.all([personPromise, definitionPromise])
        .then(([person, definitions]) => vm.onSelectPerson(person));


    function resetValueData() {
        const clearData = resetData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
        vm.history = [];
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
            .calculateStatTally(vm.statistic.definition, selector)
            .then(summary => vm.statistic.summary = summary)
            .then(() => {
                const related = vm.relatedDefinitions.children;

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

        entityStatisticStore
            .calculateHistoricStatTally(vm.statistic.definition, selector)
            .then(h => vm.history = h);

        applicationStore
            .findBySelector(selector)
            .then(apps => vm.applications = apps);

        updateUrlWithoutReload($state, person);
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'ApplicationStore',
    'BookmarkStore',
    'EntityStatisticStore',
    'PersonStore'
];


const page = {
    controller,
    template,
    controllerAs: 'ctrl'
};


export default page;