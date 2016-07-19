


const initData = {

};



function controller(orgUnitStore, entityStatisticStore) {

    const vm = Object.assign(this, initData);
    const statId = 6;

    orgUnitStore
        .findAll()
        .then(xs => vm.orgUnits = xs);

    vm.onSelectOrgUnit = (ou) => {
        vm.selectedOrgUnit = ou;
        const selector = {
            scope: 'CHILDREN',
            entityReference: {
                id: ou.id,
                kind: 'ORG_UNIT'
            }
        };
        entityStatisticStore
            .findSummaryStatsByIdSelector(selector)
            .then(stats => vm.entityStatisticsSummary = stats);

        entityStatisticStore
            .findStatValuesByIdSelector(statId, selector)
            .then(stats => vm.entityStatisticValues = stats);
    };

    vm.jumpOrgUnit = () => vm.onSelectOrgUnit(_.find(vm.orgUnits, { id: 140 }));
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