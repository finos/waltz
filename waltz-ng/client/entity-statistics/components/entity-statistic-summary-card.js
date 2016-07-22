import _ from "lodash";
import {variableScale} from "../../common/colors";


const bindings = {
    definition: '<',
    parentRef: '<',
    subTitle: '@',
    summary: '<'
};

const template = require('./entity-statistic-summary-card.html');


const PIE_SIZE = 100;


function mkStatChartData(counts) {
    return {
        config: {
            colorProvider: (d) => variableScale(d.data.key),
            labelProvider: d => d.key,
            size: PIE_SIZE
        },
        data: _.chain(counts)
            .map(c => ({ key: c.id, count: c.count }))
            .value()
    };
}


function controller($state) {

    const vm = this;

    vm.$onChanges = () => {
        if (vm.summary) {
            vm.pie = mkStatChartData(vm.summary.tallies);
        }
    };

    vm.goToStatistic = (definition) => {
        const params = {
            id: vm.parentRef.id,
            kind: vm.parentRef.kind,
            statId: definition.id
        };
        $state.go("main.entity-statistic.view", (params))
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