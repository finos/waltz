import personPortfolioActions from './actions/person-portfolio-actions';
import personActions from './actions/person-actions';
import orgServerStatsActions from './actions/org-server-stats-actions';
import angular from 'angular';

export default () => {
    const module = angular.module('waltz.reports', []);

    module
        .service('PersonPortfolioActions', personPortfolioActions)
        .service('PersonActions', personActions)
        .service('OrgServerStatsActions', orgServerStatsActions);


    module.config([
        '$stateProvider',
        ($stateProvider) => {
            $stateProvider
                .state('main.report', {
                    url: 'report',
                    abstract: true,
                    //views: {'content@': { template: '<div><h1>Hi</h1><div ui-view="report"/></div>' }}
                })
                .state('main.report.portfolio', {
                    url: '/PORTFOLIO/:id',
                    views: {'content@': { template: '<div><waltz-person-portfolio></waltz-person-portfolio></div>' }}
                });
        }
    ]);

    module
        .directive('waltzPersonPortfolio', require('./containers/person-portfolio'))
        .directive('waltzPortfolioSection', require('./components/portfolio-section'))
        .directive('waltzPortfolioSummarySection', require('./components/portfolio-summary-section'));

    return module.name;
};

