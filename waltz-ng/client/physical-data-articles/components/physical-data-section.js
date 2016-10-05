import _ from 'lodash';


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
    return _.chain(articles)
        .map(article => {
            const physicalFlow = _.find(physicalFlows, { articleId: article.id });
            const logicalFlow = _.find(logicalFlows, f => f.id === physicalFlow.flowId);
            return {
                article,
                logicalFlow,
                physicalFlow
            };
        })
        .groupBy("logicalFlow.source.id")
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


function controller() {

    const vm = this;

    vm.$onChanges = (changes) => {
        Object.assign(vm, mkData(vm.articles, vm.physicalFlows, vm.logicalFlows));
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;