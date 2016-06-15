import moment from "moment";


const BINDINGS = {
    timestamp: '=',
    daysOnly: '=' // boolean
};


const formats = {
    daysAndMinutes: 'ddd Do MMM YYYY - HH:mm:ss',
    daysOnly: 'ddd Do MMM YYYY',
    parse: 'YYYY-MM-DDThh:mm:ss.SSS'
};


const template = '<span title="{{ ::ctrl.hoverValue }}" ng-bind="::ctrl.fromNow"></span>';


function controller($scope) {
    const vm = this;

    $scope.$watch('ctrl.timestamp', (nv) => {

        if (! nv) return;

        const m = moment.utc(nv, formats.parse );

        const hoverFormat = vm.daysOnly
            ? formats.daysOnly
            : formats.daysAndMinutes;

        vm.hoverValue = m.local().format(hoverFormat);
        vm.fromNow = m.fromNow();
    });
}

controller.$inject=['$scope'];


const directive = {
    restrict: 'E',
    replace: true,
    scope: {},
    bindToController: BINDINGS,
    controllerAs: 'ctrl',
    template,
    controller
};


export default () => directive;
