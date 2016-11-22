function controller() {
    const vm = this;

    vm.show = (diagram) => {
        diagram.visible = true;
    };

    vm.hide = (diagram) => {
        diagram.visible = false;
    };
}


export default {
    template: require('./svg-diagrams.html'),
    bindings: {
        diagrams: '<',
        blockProcessor: '<'
    },
    controller
};
