import _ from 'lodash';
import {variableScale} from '../../../common/colors';


const bindings = {
    history: '<'
};


const template = require('./entity-statistic-history-panel.html');


function prepareData(data = []) {
    return _.chain(data)
        .flatMap(
            d => _.map(d.tallies, t => {
                return {
                    series: t.id,
                    count: t.count,
                    date: new Date(d.lastUpdatedAt)
                };
            }))
        .orderBy(d => d.date)
        .value();
}

function prepareStyles(data = []) {
    return _.chain(data)
        .flatMap('tallies')
        .map('id')
        .uniq()
        .reduce((acc, outcome) => {
            acc[outcome] = {
                color: variableScale(outcome)
            };
            return acc;
        }, {})
        .value();
}

function findRelevantStats(history = [], d) {
    return _.find(
        history,
        t => new Date(d).getTime() === new Date(t.lastUpdatedAt).getTime());
}

function controller($scope) {

    const vm = this;

    const highlight = (d) => {
        vm.options = Object.assign({}, vm.options, { highlightedDate: d });
        const relevantStats = findRelevantStats(vm.history, d);
        if (relevantStats) vm.selected = relevantStats;
    };

    vm.options = {
        highlightedDate: null,
        onHover: (d) => $scope.$applyAsync(() => highlight(d))
    };


    vm.$onChanges = () => {
        vm.points = prepareData(vm.history);
        vm.styles = prepareStyles(vm.history);
    };
}


controller.$inject = [
    '$scope'
];


const component = {
    bindings,
    template,
    controller
};


export default component;