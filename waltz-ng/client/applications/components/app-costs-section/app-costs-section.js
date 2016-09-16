import _ from 'lodash';


const bindings = {
    costs: '<',
    sourceDataRatings: '<'
};


const template = require('./app-costs-section.html');


function getCurrentYear(costs = []) {
    const defaultYear = new Date().getFullYear();
    const foundYear = _.chain(costs)
            .map('cost.year')
            .max()
            .value();
    return foundYear || defaultYear;
}


function calcTotalCost(costs = []) {
    return _.sumBy(costs, 'cost.amount');
}


function filterCostsForYear(year, costs = []) {
    return _.filter(costs, c => c.cost.year === year);
}


function controller() {
    const vm = this;
    vm.$onChanges = () => {
        vm.currentYear = getCurrentYear(vm.costs);
        vm.currentCosts = filterCostsForYear(vm.currentYear, vm.costs);
        vm.currentTotal = calcTotalCost(vm.currentCosts);
    };
}


const component = {
    bindings,
    template,
    controller
};


export default component;
