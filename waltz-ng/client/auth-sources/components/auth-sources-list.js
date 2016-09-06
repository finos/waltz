import _ from "lodash";
import {initialiseData} from "../../common";


const bindings = {
    authSources: '<',
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


function calculate(authSources = [], orgUnits = []) {
    const orgUnitsById = indexById(orgUnits);

    return _.chain(authSources)
        .groupBy('parentReference.id')
        .map((v,k) => ({ orgUnit: orgUnitsById[k], authSources: v }) )
        .value();
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes => {
        vm.authSourcesGroupedByOrgUnit = calculate(vm.authSources, vm.orgUnits);
    };
}


controller.$inject = [];


const component = {
    bindings,
    controller,
    template
};


export default component;