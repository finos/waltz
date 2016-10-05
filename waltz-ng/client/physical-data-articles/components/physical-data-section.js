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
    return _.map(
        articles,
        a => {
            const logicalById = _.keyBy(logicalFlows, "id");

            const relevantPhysicalFlows = _.chain(physicalFlows)
                .filter({ articleId: a.id })
                .map(pf => Object.assign({}, pf, { logicalFlow: logicalById[pf.flowId] }))
                .value();

            return {
                article: a,
                physicalFlows: relevantPhysicalFlows
            };

        });
}

function enrichConsumes(articles = [],
                physicalFlows = [],
                logicalFlows = [])
{
    return _.map(
        articles,
        article => {
            const physicalFlow = _.find(physicalFlows, { articleId: article.id });
            const logicalFlow = _.find(logicalFlows, f => f.id === physicalFlow.flowId);
            return {
                article,
                logicalFlow,
                physicalFlow
            };
        });
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
        vm.enrichedArticles = mkData(vm.articles, vm.physicalFlows, vm.logicalFlows);
    };
}


controller.$inject = [];


const component = {
    template,
    bindings,
    controller
};


export default component;