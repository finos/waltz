
export default (module) => {
    module.directive('waltzSvgDiagram', require('./directives/svg-diagram'));
    module.directive('waltzSvgDiagrams', require('./directives/svg-diagrams'));
    module.service('SvgDiagramStore', require('./services/svg-diagram-store'));

};
