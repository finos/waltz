import angular from 'angular';


export default () => {
    const module = angular.module('waltz.technology', []);

    module
        .directive('waltzTechnologySection', require('./directives/technology-section'));

    module
        .component('waltzGroupTechnologySummary', require('./components/group-technology-summary'))
        .component('waltzTechnologySummaryPies', require('./components/technology-summary-pies'));

    module
        .service('TechnologyStatisticsService', require('./services/technology-statistics-service'));

    return module.name;
};
