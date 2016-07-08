import _ from "lodash";
import allUsageKinds from "../usage-kinds";


const BINDINGS = {
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


function controller($scope) {
    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        'ctrl.usages',
        (usages = []) => {
            const usagesByKind = _.keyBy(usages, 'kind');

            vm.usageRows = _.chain(allUsageKinds)
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
                .value();
        });

    vm.save = () => vm.onSave(prepareSave(vm.usageRows, vm.type, vm.primaryEntity));
    vm.cancel = () => vm.onCancel();
}


controller.$inject = ['$scope'];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./app-data-type-usage-editor.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;

