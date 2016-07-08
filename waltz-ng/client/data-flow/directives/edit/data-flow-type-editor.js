import _ from "lodash";
import {toRef} from "../../registration-utils";


const BINDINGS = {
    primaryEntity: '<',
    counterpartEntity: '<',
    direction: '<',
    currentDataTypes: '<',
    allDataTypes: '<',
    onSave: '<',
    onCancel: '<',
    onDirty: '<'
};


const initialState = {
    title: '-',
    workingTypes: []
};


function calculateWorkingTypes(all = [], current = []) {
    return _.map(all, d => _.extend(d, {
        original: _.includes(current, d.code),
        selected: _.includes(current, d.code)
    }));
}


function controller($scope) {

    const vm = _.defaultsDeep(this, initialState);

    $scope.$watchGroup(
        ['ctrl.primaryEntity', 'ctrl.counterpartEntity', 'ctrl.direction'],
        ([ primary, counterpart, dir]) => {
            if (! primary || ! counterpart || ! dir) return;

            vm.title = dir === 'source'
                    ? `From ${counterpart.name} to ${primary.name}`
                    : `From ${primary.name} to ${counterpart.name}`;
        });

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

    vm.onChange = () => {
        const dirty = _.some(vm.workingTypes, wt => wt.original != wt.selected);
        vm.onDirty(dirty);
    };
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