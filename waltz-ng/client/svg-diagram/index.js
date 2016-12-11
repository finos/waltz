import angular from 'angular';


export default () => {

    const module = angular.module('waltz.svg.diagram', []);

    module
        .service('SvgDiagramStore', require('./services/svg-diagram-store'));

    module
        .component('waltzSvgDiagram', require('./component/svg-diagram'))
        .component('waltzSvgDiagrams', require('./component/svg-diagrams'))
        .component('waltzCommonSvgDefs', require('./component/common-svg-defs'));

    return module.name;

};
