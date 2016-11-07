
export default (module) => {
    module
        .service('SvgDiagramStore', require('./services/svg-diagram-store'))
        .directive('waltzSvgDiagram', require('./directives/svg-diagram'))
        .directive('waltzSvgDiagrams', require('./directives/svg-diagrams'))
        .component('waltzCommonSvgDefs', require('./component/common-svg-defs'));

};
