import _ from 'lodash';
import angular from 'angular';
import d3 from 'd3';


function resize(elem) {
    const width = angular.element(elem)[0].clientWidth;
    elem.find('svg').attr('width', width);
    elem.find('svg').attr('height', width * 0.6);
}


function controller($scope, $window) {
    const vm = this;

    angular.element($window)
        .on('resize', () => resize($scope.elem));


    $scope.$watch('ctrl.diagram', diagram => {
        if (!diagram) return;

        const svg = $scope.elem.append(diagram.svg);

        if (diagram.product === 'visio') {
            $window.setTimeout(() => resize(svg), 100);
        }

        const dataProp = 'data-' + diagram.keyProperty;
        const dataBlocks = svg.querySelectorAll('[' + dataProp + ']');

        const blocks = _.map(dataBlocks, b => ({
            block: b,
            value: b.attributes[dataProp].value
        }));

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
