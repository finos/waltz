import _ from 'lodash';
import {termSearch} from "../../common"


const bindings = {
    logicalFlows: '<',
    physicalFlows: '<',
    articles: '<'
};


const template = require('./physical-data-section.html');


function enrichProduces(articles = [],
                physicalFlows = [],
                logicalFlows = [])
{
    return _.chain(articles)
        .flatMap((a, i) => {
            const logicalById = _.keyBy(logicalFlows, "id");
            const relevantPhysicalFlows = _.filter(physicalFlows, { articleId: a.id });
            if (relevantPhysicalFlows.length === 0) {
                return {
                    article: a,
                    firstArticle: true
                }
            } else {
                return _.flatMap(relevantPhysicalFlows, (pf, j) => {
                    return {
                        article: a,
                        firstArticle: j === 0,
                        physicalFlow: pf,
                        firstPhysical: j === 0,
                        logicalFlow: logicalById[pf.flowId]
                    };
                });
            }
        })
        .value();
}


function enrichConsumes(articles = [],
                physicalFlows = [],
                logicalFlows = [])
{
    const visitedApps = [];

    return _.chain(articles)
        .map((article, i) => {
            const physicalFlow = _.find(physicalFlows, { articleId: article.id });
            const logicalFlow = _.find(logicalFlows, f => f.id === physicalFlow.flowId);
            const firstSource = !_.includes(visitedApps, article.owningApplicationId);
            if(firstSource === true) {
                visitedApps.push(article.owningApplicationId);
            }

            return {
                article,
                logicalFlow,
                physicalFlow,
                firstSource
            };
        })
        .value();
}


function mkData(articles = { produces: [], consumes: [] },
                physicalFlows = [],
                logicalFlows = [])
{
    const produces = enrichProduces(
        articles.produces,
        physicalFlows,
        logicalFlows);
    const consumes = enrichConsumes(
        articles.consumes,
        physicalFlows,
        logicalFlows);
    return { produces, consumes };
}


function controller($scope) {

    const vm = this;

    const produceFields = [
        'article.name',
        'article.externalId',
        'article.format',
        'article.description',
        'physicalFlow.transport',
        'physicalFlow.frequency',
        'logicalFlow.target.name'
    ];


    const consumeFields = [
        'article.name',
        'article.externalId',
        'article.format',
        'article.description',
        'physicalFlow.transport',
        'physicalFlow.frequency',
        'logicalFlow.source.name'
    ];

    vm.$onChanges = (changes) => {
        Object.assign(vm, mkData(vm.articles, vm.physicalFlows, vm.logicalFlows));
        vm.filterProduces("");
        vm.filterConsumes("");
    };

    vm.filterProduces = query => {
        vm.filteredProduces = termSearch(vm.produces, query, produceFields)
    };

    vm.filterConsumes = query => {
        vm.filteredConsumes = termSearch(vm.consumes, query, consumeFields)
    };
}

controller.$inject = ['$scope'];


const component = {
    template,
    bindings,
    controller
};


export default component;