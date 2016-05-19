
const BINDINGS = {
    name: '@',
    icon: '@',
    small: '@',
    visible: '='
};

export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./overlay-panel.html'),
    scope: {},
    bindToController: BINDINGS,
    controller: () => {},
    controllerAs: 'ctrl',
    transclude: true
});

