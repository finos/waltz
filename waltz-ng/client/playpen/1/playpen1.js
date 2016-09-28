
const initData = {
    apps: [],
    flowData: null
};


const data = [
    {
        tallies: [
            { id:"A", count: 10},
            { id:"B", count: 15},
            { id:"C", count: 20},
        ],
        entityRef: { id: 10, kind: 'ENTITY_STATISTIC' },
        lastUpdatedAt: new Date(2016, 9, 21)
    }, {
        tallies: [
            { id:"A", count: 5},
            { id:"B", count: 10},
            { id:"C", count: 5},
        ],
        entityRef: { id: 10, kind: 'ENTITY_STATISTIC' },
        lastUpdatedAt: new Date(2016, 9, 20)
    }, {
        tallies: [
            { id:"A", count: 15},
            { id:"B", count: 20},
            { id:"C", count: 25},
        ],
        entityRef: { id: 10, kind: 'ENTITY_STATISTIC' },
        lastUpdatedAt: new Date(2016, 9, 19)
    }
];


function controller($scope, orgUnitStore) {
    const vm = Object.assign(this, initData);

    const statDefn = {
        id: 10100,
        rollupKind: 'COUNT_BY_ENTITY'
    };

    const selector = {
        entityReference: { kind: 'ORG_UNIT', id: 170 },
        scope: 'EXACT'
    };


    orgUnitStore.findImmediateHierarchy(50)
        .then(h => vm.hierarchy = h);
}


controller.$inject = [
    '$scope',
    'OrgUnitStore'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;