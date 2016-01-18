import _ from 'lodash';
import angular from 'angular';


function resize(elem) {
    const width = angular.element(elem)[0].clientWidth;
    elem.find('svg').attr('width', width);
    elem.find('svg').attr('height', width * 0.6);
}


function controller($scope, $window) {
    const blockParsers = {
        'draw.io': (diagram, svg) => {
            const anchors = svg.find('a');
            return _.chain(anchors)
                .map(anchor => {
                    const attr = anchor.attributes['xlink:href'];
                    if (! attr) return null;
                    const empId = attr.value;
                    anchor.removeAttribute('xlink:href');
                    return {
                        rawProperty: empId,
                        parent: anchor,
                        value: empId,
                        name: 'xlink:href'
                    };
                })
                .compact()
                .value();
        },
        'visio': (diagram, svg) => {
            $window.setTimeout(() => resize(svg), 100);

            const custProps = svg.find('v:cp');
            return _.chain(custProps)
                .map(cp => {
                    const valAttr = cp.attributes['v:val'];
                    const value = valAttr
                        ? valAttr.value.replace(/.*\((.*)\).*/, '$1')
                        : '';

                    return {
                        rawProperty: cp,
                        parent: cp.parentNode.parentNode,
                        value,
                        name: cp.attributes['v:lbl'].value
                    };
                })
                .filter(b => b.name === diagram.keyProperty)
                .value();
        }
    };

    const vm = this;

    angular.element($window)
        .on('resize', () => resize($scope.elem));


    $scope.$watch('ctrl.diagram', f => {
        if (!f) return;

        const svg = $scope.elem.append(vm.diagram.svg);
        const blocks = blockParsers[vm.diagram.product](vm.diagram, svg);

        _.each(blocks, vm.blockProcessor);
    });

}

controller.$inject = ['$scope', '$window'];


function link(scope, elem) {
    scope.elem = elem;
}


export default () => ({
    restrict: 'E',
    replace: true,
    template: '<div></div>',
    scope: {},
    bindToController: {
        blockProcessor: '=',
        diagram: '='
    },
    link,
    controllerAs: 'ctrl',
    controller
});
