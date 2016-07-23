const bindings = {
    name: '@',
    size: '@',
    flip: '@',
    rotate: '@',
    stack: '@',
    fixedWidth: '@',
    inverse: '@',
    spin: '@'
};


const template = '<span style="font-size: smaller"><i ng-class="$ctrl.classNames"/></span>';


function controller() {
    const vm = this;
    vm.$onChanges = () => {
        vm.classNames = [
            'fa',
            `fa-${vm.name}`,
            vm.flip ? `fa-flip-${vm.flip}` : '',
            vm.rotate ? `fa-rotate-${vm.rotate}` : '',
            vm.size ? `fa-${vm.size}` : '',
            vm.stack ? `fa-stack-${vm.stack}` : '',
            vm.fixedWidth ? 'fa-fw' : '',
            vm.inverse ? 'fa-inverse' : '',
            vm.spin ? 'fa-spin' : ''
        ].join(' ');
    }
}


const component = {
    bindings,
    template,
    controller
};


export default component;


