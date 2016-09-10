import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    authSources: '<',
    consumers: '<',
    orgUnits: '<'
};


const initialState = {
    authSources: [],
    orgUnits: [],
    orgUnitsById: {},
    authSourcesGroupedByOrgUnit: []
};


const template = require('./auth-sources-list.html');


function indexById(entities = []) {
    return _.keyBy(entities, 'id');
}


function calculate(authSources = [], orgUnits = [], allConsumers = []) {
    const orgUnitsById = indexById(orgUnits);
    const consumersByAuthSource = _.keyBy(allConsumers, 'key.id');

    const authSourcesGroupedByOrgUnit = _.chain(authSources)
        .map(authSource => {
            const declaringOrgUnit = orgUnitsById[authSource.parentReference.id];
            const consumers = consumersByAuthSource[authSource.id] || { value : [] };
            return Object.assign({}, authSource, {consumers : consumers.value, declaringOrgUnit});
        })
        .groupBy('parentReference.id')
        .map((v,k) => ({ orgUnit: orgUnitsById[k], authSources: v }) )
        .value();

    return {
        authSourcesGroupedByOrgUnit
    };
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes =>
        Object.assign(
            vm,
            calculate(
                vm.authSources,
                vm.orgUnits,
                vm.consumers));

    vm.showDetail = selected =>
        vm.selected = selected;
}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default component;