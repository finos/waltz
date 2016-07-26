import _ from "lodash";


const initData = {
    statistic: {
        definition: null,
        summary: null,
        values: []
    },
};

function perpareBars(stats = []) {
    const extent = d3.extent(stats, s => Number(s.value));
    console.log(extent, _.map(stats, s=> Number(s.value)));
}

function controller(entityStatisticStore) {

    const vm = Object.assign(this, initData);
    const statId = 20000;


    const selector = {
        scope: 'CHILDREN',
        entityReference: {
            id: 30,
            kind: 'ORG_UNIT'
        }
    };



    entityStatisticStore
        .findStatValuesByIdSelector(statId, selector)
        .then(stats => vm.statistic.values = stats)
        .then(stats => perpareBars(stats));

}


controller.$inject = [
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