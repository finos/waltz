import _ from "lodash";
import {kindToViewState, initialiseData} from "../common";

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
    parentStateRef: '.',
    visibility: {
        related: false
    }
};


function updateUrlWithoutReload($state, navItem) {
    $state.go('.', {id: navItem.id}, {notify: false});
}


function hasRelatedDefinitions(defs) {
    const relatedCount = defs.children.length
        + (defs.parent
            ? 1
            : 0)
        + defs.siblings.length;

    return relatedCount > 0;
}


function controller($q,
                    $state,
                    $stateParams,
                    bookmarkStore,
                    entityStatisticUtilities,
                    entityStatisticStore) {
    const vm = initialiseData(this, initData);

    const statId = $stateParams.statId;
    const entityKind = $stateParams.kind;
    const entityId = $stateParams.id;

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

    const navItemPromise = entityStatisticUtilities
        .findAllForKind(entityKind)
        .then(xs => vm.navItems = xs);

    $q.all([navItemPromise, definitionPromise])
        .then(() => /* boot */ vm.onSelectNavItem(_.find(vm.navItems, { id: entityId })));


    function resetValueData() {
        const clearData = initialiseData({}, initData);
        vm.statistic.summary = clearData.statistic.summary;
        vm.statistic.values = clearData.statistic.values;
        vm.summaries = clearData.summaries;
    }


    vm.onSelectNavItem = (navItem) => {
        resetValueData();

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
            .findStatTallies([statId], selector)
            .then(summaries => vm.statistic.summary = summaries[0])
            .then(() => {
                const related = [
                    vm.relatedDefinitions.parent,
                    ...vm.relatedDefinitions.siblings,
                    ...vm.relatedDefinitions.children ];

                return entityStatisticStore.findStatTallies(_.map(related, 'id'), selector);
            })
            .then(summaries => vm.summaries = summaries);

        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.statistic.values = stats);

        updateUrlWithoutReload($state, navItem);
    };

    vm.goToParent = () => {
        const stateName = kindToViewState(entityKind);
        const navId = vm.selectedNavItem.id;
        $state.go(stateName, { id: navId });
    };
}


controller.$inject = [
    '$q',
    '$state',
    '$stateParams',
    'BookmarkStore',
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