import _ from "lodash";


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
    checkedItemIds: [],
    expandedItemIds: [],
    originalSelectedItemIds: [],
    onSave: (x) => console.log('dfte: default onSave()', x),
    onDelete: (x) => console.log('dfte: default onDelete()', x),
    onCancel: (x) => console.log('dfte: default onCancel()', x),
    onDirty: (x) => console.log('dfte: default onDirty()', x)
};


function isDirty(selectedIds = [], originalSelectedIds = []) {
    return !_.isEqual(selectedIds.sort(), originalSelectedIds.sort());
}


function anySelected(selectedIds = []) {
    return selectedIds.length > 0;
}


function mkTitle(flow) {
    return flow
        ? `Datatypes sent from ${flow.source.name} to ${flow.target.name}`
        : '?';
}


function mkSelectedTypeIds(decorators = []) {
    return _.chain(decorators)
        .filter(d => d.decoratorEntity.kind === 'DATA_TYPE')
        .map('decoratorEntity.id')
        .value();
}


function mkUpdateCommand(flow, selectedIds = [], originalIds = []) {
    const addedIds = _.difference(selectedIds, originalIds);
    const removedIds = _.difference(originalIds, selectedIds);

    const command = {
        flowId: flow.id,
        addedDecorators: _.map(addedIds, id => ({ id, kind: 'DATA_TYPE' })),
        removedDecorators: _.map(removedIds, id => ({ id, kind: 'DATA_TYPE' }))
    };

    return command;
}


function controller() {

    const vm = _.defaultsDeep(this, initialState);

    vm.$onChanges = (changes) => {
        vm.title = mkTitle(vm.flow);
        vm.checkedItemIds = mkSelectedTypeIds(vm.decorators);
        vm.originalSelectedItemIds = mkSelectedTypeIds(vm.decorators);
        vm.expandedItemIds = mkSelectedTypeIds(vm.decorators);
    };

    vm.save = () => {
        const command = mkUpdateCommand(vm.flow, vm.checkedItemIds, vm.originalSelectedItemIds);
        vm.onSave(command);
    };

    vm.delete = () => vm.onDelete(vm.flow);
    vm.cancel = () => vm.onCancel();
    vm.onChange = () => vm.onDirty(isDirty(vm.checkedItemIds, vm.originalSelectedItemIds));
    vm.canSave = () => isDirty(vm.checkedItemIds, vm.originalSelectedItemIds) && anySelected(vm.checkedItemIds);
    vm.anySelected = () => anySelected(vm.checkedItemIds);

    vm.typeSelected = (id) => {};

    vm.typeChecked = (id) => {
        vm.checkedItemIds = _.union(vm.checkedItemIds, [id])
    };

    vm.typeUnchecked = (id) => {
        vm.checkedItemIds = _.without(vm.checkedItemIds, id);
    };
}


const component = {
    bindings,
    controller,
    template
};


export default component;