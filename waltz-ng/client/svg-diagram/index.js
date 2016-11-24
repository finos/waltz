
export default (module) => {
    module
        .service('SvgDiagramStore', require('./services/svg-diagram-store'))
        .component('waltzSvgDiagram', require('./component/svg-diagram'))
        .component('waltzSvgDiagrams', require('./component/svg-diagrams'))
        .component('waltzCommonSvgDefs', require('./component/common-svg-defs'));

};
