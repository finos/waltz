import _ from "lodash";
import {initialiseData} from "../../common";

const bindings = {
    dataTypes: '<',
    distributors: '<',
    consumers: '<',
};


const initialState = {
    nonAuthSources: [],
    distributors: [],
};


const template = require('./non-auth-sources-list.html');


function calculate(dataTypes = [], distributorsByDataType = [], allConsumers = []) {
    const consumersBySource = _.groupBy(allConsumers, 'source.id');
    const dataTypesById = _.keyBy(dataTypes, 'id');

    const distributersFlattened = _.flatMap(distributorsByDataType, (values, key) => {
        const dataType = dataTypesById[key] || {};
        return  _.map(values, v => Object.assign(v, {dataType} ));
    } );

    const nonAuthSources = _.chain(distributersFlattened)
        .map(distributor => {
            const consumers = consumersBySource[distributor.id] || [];
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
            vm.consumers);
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