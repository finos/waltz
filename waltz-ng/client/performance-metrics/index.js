import angular from 'angular';

export default () => {

    const module = angular.module('waltz.performance.metrics', []);

    module.config(require('./routes'));

    module
        .directive('waltzPerformanceMetricDefinitionList', require('./directives/performance-metric-definition-list'));

    module
        .service('PerformanceMetricDefinitionStore', require('./services/performance-metric-definition-store'))
        .service('PerformanceMetricPackStore', require('./services/performance-metric-pack-store'));

    return module.name;
}
