
export default (module) => {

    module.config(require('./routes'));

    module
        .directive(
            'waltzPerformanceMetricDefinitionList',
            require('./directives/performance-metric-definition-list'));

    module
        .service(
            'PerformanceMetricDefinitionStore',
            require('./services/performance-metric-definition-store'))
        .service(
            'PerformanceMetricPackStore',
            require('./services/performance-metric-pack-store'));
}
