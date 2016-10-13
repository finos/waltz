import _ from "lodash";
import {variableScale} from "../../common/colors";
import {mkSummaryTableHeadings} from "../entity-statistic-utilities";
import {navigateToStatistic} from "../utilities";

const bindings = {
    definition: '<',
    parentRef: '<',
    subTitle: '@',
    summary: '<'
};


const template = require('./entity-statistic-summary-card.html');


const PIE_SIZE = 100;


function mkStatChartData(counts = []) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: d => d.key,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({
                key: c.id,
                count: c.count
            }))
            .value()
    };
}


function controller($state) {

    const vm = this;

    vm.$onChanges = () => {
        const tallies = vm.summary
            ? vm.summary.tallies
            : [];
        vm.pie = mkStatChartData(tallies);
        vm.tableHeadings = mkSummaryTableHeadings(vm.definition);
    };

    vm.goToStatistic = (definition) => {
        navigateToStatistic($state, definition.id, vm.parentRef);
    };
}


controller.$inject = [
    '$state'
];


const component = {
    bindings,
    controller,
    template
};


export default component;