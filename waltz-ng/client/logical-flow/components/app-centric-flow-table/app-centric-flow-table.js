import _ from "lodash";
import {initialiseData} from "../../../common";

const bindings = {
    app: '<',
    flows: '<',
    decorators: '<',
    onSelect: '<'
};


const initialState = {
    onSelect: (app) => console.log("Default handler for appCentricFlowTable.onSelect(). ", app)
};


const template = require('./app-centric-flow-table.html');


function enrichAndGroupFlows(app, flows = [], decorators = []) {
    if(!app) return {};

    const dataTypeDecoratorsByFlowId = _.chain(decorators)
        .filter(d => d.decoratorEntity.kind === "DATA_TYPE")
        .map(d => ({
            dataFlowId: d.dataFlowId,
            id: d.decoratorEntity.id,
            rating: d.rating
        }))
        .keyBy('dataFlowId')
        .value();

    const groupedFlows = _.chain(flows)
        .filter(f => f.target.id === app.id || f.source.id === app.id)
        .map(f => ({ ...f, direction: f.target.id === app.id ? 'Incoming' : 'Outgoing'}))
        .map(f => ({ ...f, decorator: dataTypeDecoratorsByFlowId[f.id]}))
        .map(f => ({ ...f, app: f.direction === 'Incoming' ? f.source : f.target}))
        .sortBy('direction')
        .groupBy('direction')
        .value();

    return groupedFlows;
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = changes => {
        vm.groupedFlows = enrichAndGroupFlows(vm.app, vm.flows, vm.decorators);
    };
}


controller.$inject = [];


const component = {
    bindings,
    template,
    controller
};


export default component;