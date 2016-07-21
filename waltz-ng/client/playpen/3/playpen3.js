import _ from "lodash";


const initData = {
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
    relatedDefinitions: null,
    summaries: [],
    navItems: [],
    selectedNavItem: null
};


function controller(orgUnitStore, entityStatisticStore) {

    const vm = Object.assign(this, initData);
    const statId = 34;


    entityStatisticStore
        .findRelatedStatDefinitions(statId)
        .then(ds => vm.relatedDefinitions = ds)
        .then(ds => vm.statistic.definition = ds.self);

    orgUnitStore
        .findAll()
        .then(xs => vm.navItems = xs)
        .then(() => /* boot */ vm.onSelectNavItem(_.find(vm.navItems, { id: 140 })));

    vm.onSelectNavItem = (navItem) => {
        vm.selectedNavItem = navItem;
        const selector = {
            scope: 'CHILDREN',
            entityReference: {
                id: navItem.id,
                kind: 'ORG_UNIT'
            }
        };

        entityStatisticStore
            .findStatTallies(vm.relatedDefinitions, selector)
            .then(summaries => vm.summaries = summaries)
            .then(summaries => vm.statistic.summary = _.find(summaries, { entityReference: { id: statId }}))

        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.statistic.values = stats);
    };

}


controller.$inject = [
    'OrgUnitStore',
    'EntityStatisticStore'
];


const view = {
    template: require('./playpen3.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;