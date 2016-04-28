export default (module) => {
    module.directive('waltzTechnologySection', require('./directives/technology-section'));
    module.directive('waltzTechnologySummaryPies', require('./directives/technology-summary-pies'));
    module.directive('waltzGroupTechnologySummary', require('./directives/group-technology-summary'));

    module.service('TechnologyStatisticsService', require('./services/technology-statistics-service'));
};
