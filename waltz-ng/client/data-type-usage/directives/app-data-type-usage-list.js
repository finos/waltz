import _ from "lodash";
import allUsageKinds from "../usage-kinds";

const BINDINGS = {
    usages: "<"
};


const initialState = {
    consolidatedUsages:{},
    allUsageKinds,
    usages: []
};


function consolidateUsages(usages = []) {
    return _.chain(usages)
        .groupBy('dataTypeCode')
        .mapValues(xs => _.chain(xs)
            .map('usage')
            .filter(u => u.isSelected || u.description.length > 0)
            .value())
        .value();
}


function findUsage(usages = [], dataTypeCode, usageKind) {
    return _.find(usages, { dataTypeCode , usage : { kind: usageKind }});
}

function controller($scope) {
    const vm = _.defaultsDeep(this, initialState);

    $scope.$watch(
        'ctrl.usages',
        (usages = []) => vm.consolidatedUsages = consolidateUsages(usages)
    );

    vm.isSelected = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage && foundUsage.usage.isSelected;
    };

    vm.hasDescription = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage && foundUsage.usage.description;
    };

    vm.lookupDescription = (dataTypeCode, usageKind) => {
        const foundUsage = findUsage(vm.usages, dataTypeCode, usageKind);
        return foundUsage
            ? foundUsage.usage.description
            : "";
    };

}


controller.$inject = ['$scope'];


const directive = {
    restrict: 'E',
    replace: false,
    template: require('./app-data-type-usage-list.html'),
    controller,
    controllerAs: 'ctrl',
    scope: {},
    bindToController: BINDINGS
};


export default () => directive;

