function controller() {

    const vm = this;

    vm.show = (diagram) => {
        diagram.visible = true;

    };

    vm.hide = (diagram) => {
        diagram.visible = false;
    };
}


export default () => ({
    restrict: 'E',
    replace: true,
    template: require('./svg-diagrams.html'),
    scope: {},
    bindToController: {
        diagrams: '=',
        blockProcessor: '='
    },
    controllerAs: 'ctrl',
    controller
});
