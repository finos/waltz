import _ from "lodash";

const bindings = {
    app: '<',
    flows: '<',
    decorators: '<',
    dataTypes: '<',
    onSelect: '<'
};


const template = require('./app-centric-flow-table.html');


function enrichAndGroupFlows(app, flows, decorators, dataTypes) {

    const dataTypesById = _.keyBy(dataTypes, 'id');

    const dataTypeDecoratorsByFlowId = _.chain(decorators)
        .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
        .map(d => ({
            dataFlowId: d.dataFlowId,
            id: d.decoratorEntity.id,
            name: dataTypesById[d.decoratorEntity.id].name,
            code: dataTypesById[d.decoratorEntity.id].code,
            rating: d.rating
        }))
        .keyBy('dataFlowId')
        .value();

    const groupedFlows = _.chain(flows)
        .map(f => ({ ...f, direction: f.target.id === app.id ? 'Incoming' : 'Outgoing'}))
        .map(f => ({ ...f, decorator: dataTypeDecoratorsByFlowId[f.id]}))
        .map(f => ({ ...f, app: f.direction === 'Incoming' ? f.source : f.target}))
        .sortBy('direction')
        .groupBy('direction')
        .value();

    return groupedFlows;
}


function controller() {
    const vm = this;

    vm.$onChanges = changes => {
        vm.groupedFlows = enrichAndGroupFlows(vm.app, vm.flows, vm.decorators, vm.dataTypes);
    };

}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;