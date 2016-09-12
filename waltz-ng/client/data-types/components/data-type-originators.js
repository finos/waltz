import _ from 'lodash';
import {initialiseData} from '../../common'


const bindings = {
    flowOriginators: '<'
};


const template = require('./data-type-originators.html');


const initialState = {
    dataTypes: [],
    flowOriginators: {}
};


function prepareData(dataTypes = [], flowOriginators = {}) {
    const typesById = _.keyBy(dataTypes, 'id');

    const typeToAppIdsForFlows = _.mapValues(flowOriginators, xs => _.map(xs, 'id'));


    // TODO: enrich with dtu flow info
    return _.map(flowOriginators, (apps, typeId) => {
        return {
            dataType: typesById[typeId],
            appReferences: _.sortBy(apps, 'name')
        };
    });


}


function controller(dataTypeService) {

    const vm = initialiseData(this, initialState);

    dataTypeService.loadDataTypes()
        .then(dataTypes => vm.dataTypes = dataTypes);


    vm.$onChanges = () => {
        vm.originators = prepareData(vm.dataTypes, vm.flowOriginators);
    };

}


controller.$inject = [
    'DataTypeService'
];


const component = {
    bindings,
    template,
    controller
};




export default component;
