import personPortfolioActions from './actions/person-portfolio-actions';
import personActions from './actions/person-actions';
import orgServerStatsActions from './actions/org-server-stats-actions';


export default (module) => {
    module.service('PersonPortfolioActions', personPortfolioActions);
    module.service('PersonActions', personActions);
    module.service('OrgServerStatsActions', orgServerStatsActions);


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

    module.directive('waltzPersonPortfolio', require('./containers/person-portfolio'));
    module.directive('waltzPortfolioSection', require('./components/portfolio-section'));
    module.directive('waltzPortfolioSummarySection', require('./components/portfolio-summary-section'));
};

