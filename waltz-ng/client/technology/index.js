export default (module) => {
    module
        .directive('waltzTechnologySection', require('./directives/technology-section'))
        .directive('waltzTechnologySummaryPies', require('./directives/technology-summary-pies'));

    module
        .component('waltzGroupTechnologySummary', require('./components/group-technology-summary'));

    module
        .service('TechnologyStatisticsService', require('./services/technology-statistics-service'));
};
