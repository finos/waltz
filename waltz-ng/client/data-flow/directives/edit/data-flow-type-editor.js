import _ from "lodash";
import {toRef} from "../../registration-utils";


const BINDINGS = {
    primaryEntity: '<',
    counterpartEntity: '<',
    direction: '<',
    currentDataTypes: '<',
    allDataTypes: '<',
    onSave: '<',
    onCancel: '<'
};


function calculateWorkingTypes(all = [], current = []) {
    return _.map(all, d => _.extend(d, {
        original: _.includes(current, d.code),
        selected: _.includes(current, d.code)
    }));
}


function controller($scope) {

    const vm = this;

    vm.isSelected = (option) => true;

    $scope.$watchGroup(
        ['ctrl.allDataTypes', 'ctrl.currentDataTypes'],
        ([all = [], current = []]) => vm.workingTypes = calculateWorkingTypes(all, current));


    vm.save = () => {
        const source = toRef(vm.direction === 'target' ? vm.primaryEntity : vm.counterpartEntity);
        const target = toRef(vm.direction === 'source' ? vm.primaryEntity : vm.counterpartEntity);

        const [added, removed] = _.chain(vm.workingTypes)
            .filter(st => st.selected !== st.original)
            .partition('selected')
            .value();

        const command = {
            source,
            target,
            addedTypes: _.map(added, 'code'),
            removedTypes: _.map(removed, 'code')
        };

        vm.onSave(command);
    };

    vm.cancel = () => vm.onCancel();
}


controller.$inject = ['$scope'];


const directive = {
    restrict: 'E',
    replace: false,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    controller,
    template: require('./data-flow-type-editor.html')
};


export default () => directive;