import _ from "lodash";
import {initialiseData} from "../../common";

const bindings = {
    dataTypes: '<',
    distributors: '<',
    logicalFlows: '<',
};


const initialState = {
    nonAuthSources: [],
    distributors: [],
};


const template = require('./non-auth-sources-list.html');


function calculate(dataTypes = [], distributorsByDataType = [], logicalFlows = []) {
    const logicalFlowsBySource = _.groupBy(logicalFlows, 'source.id');
    const dataTypesById = _.keyBy(dataTypes, 'id');

    const nonAuthSources = _.chain(distributorsByDataType)
        .flatMap((values, key) => {
            const dataType = dataTypesById[key] || {};
            return  _.map(values, v => Object.assign(v, {dataType} ));
        })
        .map(distributor => {
            const consumers = _.map(logicalFlowsBySource[distributor.id] || [], f => f.target);
            return Object.assign({}, distributor, {consumers});
        })
        .value();

    return {
        nonAuthSources
    };
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes => {
        const nonAuthSources = calculate(
            vm.dataTypes,
            vm.distributors,
            vm.logicalFlows);
        Object.assign(vm, nonAuthSources);
    };


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