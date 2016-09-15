import _ from "lodash";
import allUsageKinds from "../../usage-kinds";
import {initialiseData} from '../../../common'

const bindings = {
    primaryEntity: '<',
    type: '<',
    usages: "<",
    onCancel: "<",
    onSave: "<"
};


const initialState = {
    rows: [],
    onCancel: () => 'no onCancel handler provided for app-data-type-usage-editor',
    onSave: () => 'no onSave handler provided for app-data-type-usage-editor'
};


function prepareSave(usageRows = []) {
    const usages = _.map(usageRows, row => {
        return {
            kind: row.kind,
            isSelected: row.selected,
            description: row.description
        }
    });
    return usages;
}


function mkUsageRows(usages = []) {
    const usagesByKind = _.keyBy(usages, 'kind');

    return _.chain(allUsageKinds)
        .map(usageKind => {
            const currentUsageForThisKind = usagesByKind[usageKind.kind];
            return {
                ...usageKind,
                description: currentUsageForThisKind
                    ? currentUsageForThisKind.description
                    : '',
                selected: currentUsageForThisKind != null
                && currentUsageForThisKind.isSelected
            };
        })
        .orderBy('kind')
        .value()
}


function controller() {
    const vm = initialiseData(this, initialState);

    vm.$onChanges = () => vm.usageRows = mkUsageRows(vm.usages);
    vm.save = () => vm.onSave(prepareSave(vm.usageRows, vm.type, vm.primaryEntity));
    vm.cancel = () => vm.onCancel();
}


controller.$inject = [];


const component = {
    template: require('./app-data-type-usage-editor.html'),
    controller,
    bindings
};


export default component;

