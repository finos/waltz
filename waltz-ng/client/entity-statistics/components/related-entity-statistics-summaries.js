import _ from "lodash";
import {variableScale} from "../../common/colors";


const bindings = {
    parentRef: '<',
    definitions: '<',
    summaries: '<'
};


const template = require('./related-entity-statistics-summaries.html');
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


function convert(definition, summaries) {
    if (definition == null) return null;
    const summary = _.find(summaries, {entityReference: { id: definition.id}});

    const chartData = {
        pie: mkStatChartData(summary ? summary.tallies : [])
    };
    return Object.assign({} , definition, chartData);
}


function processSummaries(summaries = [], definitions) {
    if (!definitions) return null;

    const parent = convert(definitions.parent, summaries);
    const siblings = _.map(definitions.siblings, s => convert(s, summaries));
    const children = _.map(definitions.children, c => convert(c, summaries));

    return {
        parent,
        siblings,
        children
    };
}


function controller($state) {
    const vm = this;

    vm.$onChanges = (changes => {
        if (vm.summaries && vm.definitions) {
            vm.chartSummaries = processSummaries(vm.summaries, vm.definitions)
        }
    });

    vm.goToStatistic = (stat) => {
        const params = {
            id: vm.parentRef.id,
            kind: vm.parentRef.kind,
            statId: stat.id
        };
        $state.go("main.entity-statistic.view", (params))
    };
}

controller.$inject = [
    '$state'
];


const component = {
    template,
    controller,
    bindings
};


export default component;
