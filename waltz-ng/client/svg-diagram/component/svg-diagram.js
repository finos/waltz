import _ from 'lodash';
import angular from 'angular';


function resize(elem) {
    const width = elem.parent()[0].clientWidth || 1024;
    elem.find('svg').attr('width', width);
    elem.find('svg').attr('height', width * 0.6);
}


function controller($element, $window) {
    const vm = this;

    angular
        .element($window)
        .on('resize', () => resize($element));


    vm.$onChanges = () => {
        if (!vm.diagram) return;

        const svg = $element.html(vm.diagram.svg);

        $window.setTimeout(() => resize(svg), 100);

        const dataProp = 'data-' + vm.diagram.keyProperty;
        const dataBlocks = svg.querySelectorAll('[' + dataProp + ']');

        const blocks = _.map(dataBlocks, b => ({
            block: b,
            value: b.attributes[dataProp].value
        }));

        _.each(blocks, vm.blockProcessor);
    };

}

controller.$inject = ['$element', '$window'];


export default {
    bindings: {
        blockProcessor: '<',
        diagram: '<'
    },
    controller
};
