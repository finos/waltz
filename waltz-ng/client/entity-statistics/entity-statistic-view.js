import _ from "lodash";
import {kindToViewState} from "../common";

const initData = {
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    summaries: [],
    navItems: [],
    selectedNavItem: null,
    parentStateRef: '.'
};


function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


function controller($q,
                    $state,
                    $stateParams,
                    entityStatisticUtilities,
                    entityStatisticStore) {

    const vm = Object.assign(this, initData);
    const statId = $stateParams.statId;
    const entityKind = $stateParams.kind;
    const entityId = $stateParams.id;


    const definitionPromise = entityStatisticStore
        .findRelatedStatDefinitions(statId)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self);

    const navItemPromise = entityStatisticUtilities
        .findAllForKind(entityKind)
        .then(x => {
            console.log(x);
            return x;
        })
        .then(xs => vm.navItems = xs);

    $q.all([navItemPromise, definitionPromise])
        .then(() => /* boot */ vm.onSelectNavItem(_.find(vm.navItems, { id: entityId })));

    vm.onSelectNavItem = (navItem) => {
        vm.selectedNavItem = navItem;

        const entityReference = {
            id: navItem.id,
            kind: entityKind
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

        updateUrlWithoutReload($state, navItem);
    };


    vm.goToParent = () => {
        const stateName = kindToViewState(entityKind);
        const navId = vm.selectedNavItem.id;
        $state.go(stateName, { id: navId });
    }
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'EntityStatisticUtilities',
    'EntityStatisticStore'
];


const view = {
    template: require('./entity-statistic-view.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;