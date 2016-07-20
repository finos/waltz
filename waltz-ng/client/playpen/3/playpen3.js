import _ from "lodash";
import {variableScale} from "../../common/colors";


const initData = {
};


const PIE_SIZE = 120;


function mkStatChartData(counts, onSelect) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: d => d.key,
            onSelect,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({ key: c.id, count: c.count }))
            .value()
    };
}


function setupPie(stats, statId, onSelect) {
    const stat = _.find(stats, { definition: {id : statId } });
    return mkStatChartData(stat.counts, onSelect);
}


function controller(orgUnitStore, entityStatisticStore) {

    const vm = Object.assign(this, initData);


    const statId = 31;

    const pieClickHandler = d => {
        vm.selectedPieSegment = d;
    };

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
            .then(stats => vm.entityStatisticsSummary = stats)
            .then(stats => vm.pie = setupPie(stats, statId, pieClickHandler));

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