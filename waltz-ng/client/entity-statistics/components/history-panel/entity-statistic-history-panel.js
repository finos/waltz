import _ from "lodash";
import {timeFormat} from "d3-time-format";
import {variableScale} from "../../../common/colors";
import {initialiseData} from "../../../common";


const bindings = {
    history: '<',
    definition: '<',
    duration: '<',
    onChangeDuration: '<'
};


const template = require('./entity-statistic-history-panel.html');


const initialState = {
    duration: 'MONTH'
};


const dateFormatter = timeFormat('%a %d %b %Y');


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


function getOutcomeIds(data = []) {
    return _.chain(data)
        .flatMap('tallies')
        .map('id')
        .uniq()
        .value();
}


function prepareStyles(data = []) {
    const reducer = (acc, outcomeId) => {
        acc[outcomeId] = { color: variableScale(outcomeId).toString() };
        return acc;
    };
    return _.reduce(
        getOutcomeIds(data),
        reducer,
        {});
}


function findRelevantStats(history = [], d) {
    if (! d) return null;

    const soughtTime = d.getTime();
    return _.find(
        history,
        t => soughtTime === new Date(t.lastUpdatedAt).getTime());
}


function lookupStatColumnName(displayNameService, definition) {
    return definition
        ? displayNameService.lookup('rollupKind', definition.rollupKind)
        : 'Value';
}


function calcTotal(stats = { tallies: [] }) {
    return _.sumBy(stats.tallies, 'count');
}


function controller($scope, displayNameService) {
    const vm = initialiseData(this, initialState);

    const highlight = (d) => {
        vm.options = Object.assign(
            {},
            vm.options,
            { highlightedDate: d });
        const relevantStats = findRelevantStats(vm.history, d);
        if (relevantStats) {
            vm.selected = relevantStats;
            vm.selected.dateString = dateFormatter(d);
            vm.total = calcTotal(relevantStats);
        }
    };

    vm.options = {
        highlightedDate: null,
        onHover: (d) => $scope.$applyAsync(() => highlight(d))
    };

    vm.$onChanges = () => {
        vm.selected  = null;
        vm.points = prepareData(vm.history);
        vm.styles = prepareStyles(vm.history);
        vm.statColumnName = lookupStatColumnName(displayNameService, vm.definition);
    };

}


controller.$inject = [
    '$scope',
    'DisplayNameService'
];


const component = {
    bindings,
    template,
    controller
};


export default component;