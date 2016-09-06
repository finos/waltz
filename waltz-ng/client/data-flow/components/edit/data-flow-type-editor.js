import _ from "lodash";
import {toRef} from "../../registration-utils";


const bindings = {
    flow: '<',
    decorators: '<',
    allDataTypes: '<',
    onSave: '<',
    onDelete: '<',
    onCancel: '<',
    onDirty: '<'
};


const template = require('./data-flow-type-editor.html');


const initialState = {
    title: '-',
    flow: null,
    decorators: [],
    allDataTypes: [],
    onSave: (x) => console.log('dfte: default onSave()', x),
    onDelete: (x) => console.log('dfte: default onDelete()', x),
    onCancel: (x) => console.log('dfte: default onCancel()', x),
    onDirty: (x) => console.log('dfte: default onDirty()', x)
};


function isDirty(types = []) {
    return _.some(
        types,
        wt => wt.original != wt.selected);
}


function anySelected(types = []) {
    return _.some(
        types,
        wt => wt.selected);
}


function mkTitle(flow) {
    return flow
        ? `Datatypes sent from ${flow.source.name} to ${flow.target.name}`
        : '?';
}


function mkWorkingTypes(allTypes = [], decorators = []) {
    const currentTypeIds = _.chain(decorators)
        .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
        .map('decoratorEntity.id')
        .value();

    return _.map(
        allTypes,
        t => {
            const selected = _.includes(currentTypeIds, t.id);
            return Object.assign({}, t, { selected, original: selected });
        });
}


function mkUpdateCommand(flow, workingTypes = []) {
    const [added, removed] = _.chain(workingTypes)
        .filter(st => st.selected !== st.original)
        .partition('selected')
        .value();

    const command = {
        flowId: flow.id,
        addedDecorators: _.map(added, t => ({ id: t.id, kind: 'DATA_TYPE', name: t.name })),
        removedDecorators: _.map(removed, t => ({ id: t.id, kind: 'DATA_TYPE', name: t.name }))
    };

    return command;
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        vm.title = mkTitle(vm.flow);
        vm.workingTypes = mkWorkingTypes(vm.allDataTypes, vm.decorators);
    };

    vm.save = () => {
        const command = mkUpdateCommand(vm.flow, vm.workingTypes);
        vm.onSave(command);
    };

    vm.delete = () => vm.onDelete(vm.flow);
    vm.cancel = () => vm.onCancel();
    vm.onChange = () => vm.onDirty(isDirty());
    vm.canSave = () => isDirty(vm.workingTypes) && anySelected(vm.workingTypes);
    vm.anySelected = () => anySelected(vm.workingTypes);
}


const component = {
    bindings,
    controller,
    template
};


export default component;