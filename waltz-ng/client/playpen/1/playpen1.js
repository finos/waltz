
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


function controller(appStore, flowViewService, tourService) {

    const vm = Object.assign(this, initData);

    vm.history = data;
}


controller.$inject = [
    'ApplicationStore',
    'DataFlowViewService',
    'TourService'
];


const view = {
    template: require('./playpen1.html'),
    controller,
    controllerAs: 'ctrl',
    bindToController: true,
    scope: {}
};


export default view;