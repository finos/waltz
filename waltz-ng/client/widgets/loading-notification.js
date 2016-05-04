const BINDINGS = {
    show: '=',
    name: '@'
}

function controller() {

}

export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./loading-notification.html'),
    scope: {},
    bindToController: BINDINGS,
    controller,
    controllerAs: 'ctrl'
});